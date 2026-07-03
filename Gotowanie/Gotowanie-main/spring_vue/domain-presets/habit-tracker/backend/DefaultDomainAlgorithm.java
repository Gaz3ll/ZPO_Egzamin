package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.availability.CapacityMatcher;
import pl.zpo.app.domain.availability.TimeCollisionDetector;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.config.PricingUnit;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;

/**
 * Habit tracker algorithm.
 *
 * <p>Resource = a habit, Request = a daily entry. The algorithm rejects a second entry for the
 * same habit on the same day, awards points only for completed entries
 * ({@code basePoints × difficultyMultiplier × quantity}), grants a streak bonus when the run of
 * consecutive completed days reaches {@code streakGoal}, and reports weekly progress against
 * {@code targetFrequencyPerWeek} (fallback: {@code capacityValue}).</p>
 */
@Component
public class DefaultDomainAlgorithm implements DomainAlgorithm {

    private static final BigDecimal STREAK_BONUS_RATE = new BigDecimal("0.25");

    private final TimeCollisionDetector collisionDetector;
    private final CapacityMatcher capacityMatcher;

    public DefaultDomainAlgorithm(TimeCollisionDetector collisionDetector, CapacityMatcher capacityMatcher) {
        this.collisionDetector = collisionDetector;
        this.capacityMatcher = capacityMatcher;
    }

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        if (!isHabitInput(input)) {
            return evaluateGeneric(input);
        }

        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();

        if (resource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak nawyku");
            return DomainAlgorithmResult.failure(List.of("Nawyk jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: nawyk nieaktywny (" + resource.getStatus() + ")");
            return DomainAlgorithmResult.failure(List.of("Nawyk nie jest aktywny"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        if (input.startAt() == null) {
            breakdown.addRule("DAY_CHECK: brak dnia wpisu");
            return DomainAlgorithmResult.failure(List.of("Wpis wymaga dnia wykonania (data początku)"), breakdown.build());
        }
        LocalDate entryDay = LocalDate.ofInstant(input.startAt(), ZoneOffset.UTC);
        breakdown.addNote("entryDay=" + entryDay);

        List<RequestEntity> activeExisting = activeExistingFor(resource, input.existingRequests());
        boolean sameDayExists = activeExisting.stream()
                .anyMatch(r -> r.getStartAt() != null
                        && LocalDate.ofInstant(r.getStartAt(), ZoneOffset.UTC).equals(entryDay));
        if (sameDayExists) {
            errors.add("Wpis dla tego nawyku istnieje już w dniu " + entryDay);
            breakdown.addRule("DAY_COLLISION_CHECK: kolizja (" + entryDay + ")");
        } else {
            breakdown.addRule("DAY_COLLISION_CHECK: brak kolizji");
        }

        boolean completed = readBoolean(input.requestMetadata(), "completed");
        breakdown.addNote("completed=" + completed);

        int quantity = input.quantity() != null ? input.quantity() : 1;
        if (input.quantity() != null && input.quantity() <= 0) {
            errors.add("Wartość wykonania musi być dodatnia");
            breakdown.addRule("QUANTITY_CHECK: nieprawidłowa wartość");
        }

        BigDecimal totalPoints = BigDecimal.ZERO;
        if (!completed) {
            breakdown.addRule("COMPLETION_CHECK: niewykonane (0 pkt)");
            String reason = readString(input.requestMetadata(), "skippedReason");
            if (reason != null) {
                breakdown.addNote("skippedReason=" + reason);
            }
        } else {
            breakdown.addRule("COMPLETION_CHECK: wykonane");
            totalPoints = computePoints(resource, activeExisting, entryDay, quantity, breakdown);
        }

        int progressPercent = computeProgressPercent(resource, activeExisting, entryDay, completed, breakdown);
        breakdown.addRule("PROGRESS_PERCENT: " + progressPercent + "%");
        breakdown.addNote("progressPercent=" + progressPercent);
        breakdown.addNote("totalPoints=" + totalPoints.toPlainString());

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        return DomainAlgorithmResult.success(totalPoints, resource.getId(), breakdown.build());
    }

    private BigDecimal computePoints(ResourceEntity resource, List<RequestEntity> existing,
                                     LocalDate entryDay, int quantity, AlgorithmBreakdownBuilder breakdown) {
        BigDecimal base = resource.getBaseValue() != null ? resource.getBaseValue() : BigDecimal.TEN;
        BigDecimal quantityFactor = BigDecimal.valueOf(Math.max(1, quantity));
        BigDecimal basePoints = base.multiply(quantityFactor);
        breakdown.addLine("basePoints", basePoints,
                base.toPlainString() + " pkt × " + quantityFactor.toPlainString() + " (quantity)");
        breakdown.addRule("BASE_POINTS: " + base.toPlainString() + " × " + quantityFactor.toPlainString());
        breakdown.addNote("quantityFactor=" + quantityFactor.toPlainString());

        BigDecimal difficultyMultiplier = difficultyMultiplier(readString(resource.getMetadata(), "difficulty"));
        breakdown.addNote("difficultyMultiplier=" + difficultyMultiplier.toPlainString());
        BigDecimal subtotal = basePoints.multiply(difficultyMultiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal difficultyDelta = subtotal.subtract(basePoints);
        if (difficultyDelta.signum() != 0) {
            breakdown.addLine("difficultyBonus", difficultyDelta, "× " + difficultyMultiplier.toPlainString());
        }
        breakdown.addRule("DIFFICULTY_MULTIPLIER: × " + difficultyMultiplier.toPlainString());

        Integer streakGoal = readInteger(resource.getMetadata(), "streakGoal");
        int streak = consecutiveCompletedDays(existing, entryDay) + 1;
        breakdown.addNote("streak=" + streak);
        if (streakGoal != null && streakGoal > 0 && streak >= streakGoal) {
            BigDecimal streakBonus = subtotal.multiply(STREAK_BONUS_RATE).setScale(2, RoundingMode.HALF_UP);
            breakdown.addLine("streakBonus", streakBonus,
                    "seria " + streak + " dni ≥ cel " + streakGoal + " (+25%)");
            breakdown.addRule("STREAK_BONUS: +25% (seria " + streak + "/" + streakGoal + ")");
        } else {
            breakdown.addRule("STREAK_BONUS: brak (seria " + streak
                    + (streakGoal == null ? ", cel nieustawiony" : "/" + streakGoal) + ")");
        }

        return breakdown.total().amount();
    }

    /** Consecutive days directly before {@code entryDay} that have a completed entry. */
    private int consecutiveCompletedDays(List<RequestEntity> existing, LocalDate entryDay) {
        Set<LocalDate> completedDays = new HashSet<>();
        for (RequestEntity request : existing) {
            if (request.getStartAt() != null && readBoolean(request.getMetadata(), "completed")) {
                completedDays.add(LocalDate.ofInstant(request.getStartAt(), ZoneOffset.UTC));
            }
        }
        int streak = 0;
        LocalDate day = entryDay.minusDays(1);
        while (completedDays.contains(day)) {
            streak++;
            day = day.minusDays(1);
        }
        return streak;
    }

    private int computeProgressPercent(ResourceEntity resource, List<RequestEntity> existing,
                                       LocalDate entryDay, boolean completed,
                                       AlgorithmBreakdownBuilder breakdown) {
        Integer target = readInteger(resource.getMetadata(), "targetFrequencyPerWeek");
        if (target == null || target <= 0) {
            target = resource.getCapacityValue();
        }
        if (target == null || target <= 0) {
            breakdown.addNote("progressTarget=brak");
            return 0;
        }
        WeekFields weekFields = WeekFields.ISO;
        int week = entryDay.get(weekFields.weekOfWeekBasedYear());
        int year = entryDay.get(weekFields.weekBasedYear());
        long completedThisWeek = existing.stream()
                .filter(r -> r.getStartAt() != null && readBoolean(r.getMetadata(), "completed"))
                .map(r -> LocalDate.ofInstant(r.getStartAt(), ZoneOffset.UTC))
                .filter(d -> d.get(weekFields.weekOfWeekBasedYear()) == week
                        && d.get(weekFields.weekBasedYear()) == year)
                .count();
        if (completed) {
            completedThisWeek++;
        }
        breakdown.addNote("completedThisWeek=" + completedThisWeek + "/" + target);
        return BigDecimal.valueOf(completedThisWeek * 100L)
                .divide(BigDecimal.valueOf(target), 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private BigDecimal difficultyMultiplier(String difficulty) {
        if (difficulty == null) {
            return BigDecimal.ONE;
        }
        return switch (difficulty.toUpperCase(Locale.ROOT)) {
            case "MEDIUM" -> new BigDecimal("1.25");
            case "HARD" -> new BigDecimal("1.50");
            default -> BigDecimal.ONE;
        };
    }

    private boolean isHabitInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "completed", "mood", "effortLevel", "skippedReason")
                || (input.resource() != null && hasAny(input.resource().getMetadata(),
                "habitCategory", "targetFrequencyPerWeek", "streakGoal", "preferredTimeOfDay"));
    }

    // ----- neutral fallback engine (shared, keeps the core unit tests green) -----

    private DomainAlgorithmResult evaluateGeneric(DomainAlgorithmInput input) {
        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();

        ResourceEntity resource = input.resource();
        if (resource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak zasobu");
            return DomainAlgorithmResult.failure(List.of("Zasób jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: zasób nieaktywny (" + resource.getStatus() + ")");
            return DomainAlgorithmResult.failure(List.of("Zasób nie jest aktywny"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        List<RequestEntity> activeExisting = activeExistingFor(resource, input.existingRequests());
        long durationUnits = 1;
        boolean hasStart = input.startAt() != null;
        boolean hasEnd = input.endAt() != null;

        if (profile.algorithmMode().checksTime() || hasStart || hasEnd) {
            if (hasStart ^ hasEnd) {
                errors.add("Zakres dat jest niekompletny (wymagane początek i koniec)");
                breakdown.addRule("TIME_RANGE_CHECK: niekompletny zakres");
            } else if (hasStart) {
                if (!input.startAt().isBefore(input.endAt())) {
                    errors.add("Nieprawidłowy zakres dat: początek musi być przed końcem");
                    breakdown.addRule("TIME_RANGE_CHECK: nieprawidłowy zakres");
                } else {
                    durationUnits = computeDurationUnits(profile.pricingUnit(), input.startAt(), input.endAt());
                    breakdown.addRule("TIME_RANGE_CHECK: ok");
                    if (profile.algorithmMode().checksTime()) {
                        List<RequestEntity> collisions =
                                collisionDetector.findCollisions(input.startAt(), input.endAt(), activeExisting);
                        if (!collisions.isEmpty()) {
                            errors.add("Termin koliduje z istniejącymi zgłoszeniami: " + ids(collisions));
                            breakdown.addRule("TIME_COLLISION_CHECK: kolizja (" + collisions.size() + ")");
                            breakdown.addNote("Kolizja z zgłoszeniami: " + ids(collisions));
                        } else {
                            breakdown.addRule("TIME_COLLISION_CHECK: brak kolizji");
                        }
                    }
                }
            } else if (profile.requiresTimeWindow()) {
                errors.add("Wymagany jest zakres dat");
                breakdown.addRule("TIME_RANGE_CHECK: brak wymaganego zakresu");
            }
        }

        int qty = input.quantity() != null ? input.quantity() : 1;
        if (input.quantity() != null && input.quantity() <= 0) {
            errors.add("Ilość musi być dodatnia");
            breakdown.addRule("QUANTITY_CHECK: nieprawidłowa ilość");
        } else if (profile.requiresQuantity() && input.quantity() == null) {
            errors.add("Wymagana jest ilość");
            breakdown.addRule("QUANTITY_CHECK: brak wymaganej ilości");
        }

        if (profile.algorithmMode().checksCapacity()
                && input.quantity() != null && input.quantity() > 0
                && resource.getCapacityValue() != null) {
            List<RequestEntity> relevant = (hasStart && hasEnd)
                    ? collisionDetector.findCollisions(input.startAt(), input.endAt(), activeExisting)
                    : activeExisting;
            int used = capacityMatcher.usedCapacity(relevant);
            int capacity = resource.getCapacityValue();
            if (!capacityMatcher.fits(capacity, used, qty)) {
                errors.add("Przekroczono pojemność: użyte %d/%d, żądane %d".formatted(used, capacity, qty));
                breakdown.addRule("CAPACITY_CHECK: przekroczono (%d+%d>%d)".formatted(used, qty, capacity));
            } else {
                breakdown.addRule("CAPACITY_CHECK: ok (%d/%d)".formatted(used + qty, capacity));
            }
        }

        BigDecimal calculatedValue = null;
        if (profile.algorithmMode().calculatesValue()) {
            calculatedValue = computeGenericValue(resource, input.requestMetadata(), durationUnits, qty, profile, breakdown);
        }

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        return DomainAlgorithmResult.success(calculatedValue, resource.getId(), breakdown.build());
    }

    private BigDecimal computeGenericValue(ResourceEntity resource, Map<String, Object> requestMetadata,
                                           long durationUnits, int qty, DomainProfile profile,
                                           AlgorithmBreakdownBuilder breakdown) {
        BigDecimal base = resource.getBaseValue() != null ? resource.getBaseValue() : BigDecimal.ZERO;
        BigDecimal subtotal = base.multiply(BigDecimal.valueOf(durationUnits)).multiply(BigDecimal.valueOf(qty));
        breakdown.addLine("Wartość bazowa", subtotal,
                "%s × %d (%s) × %d".formatted(base.toPlainString(), durationUnits, profile.pricingUnit(), qty));

        BigDecimal multiplier = readDecimal(resource.getMetadata(), "priceMultiplier");
        if (multiplier != null && multiplier.compareTo(BigDecimal.ONE) != 0) {
            BigDecimal delta = subtotal.multiply(multiplier).subtract(subtotal);
            breakdown.addLine("Mnożnik", delta, "× " + multiplier.toPlainString());
        }
        return breakdown.total().amount();
    }

    private List<RequestEntity> activeExistingFor(ResourceEntity resource, List<RequestEntity> existing) {
        List<RequestEntity> result = new ArrayList<>();
        for (RequestEntity request : existing) {
            boolean active = request.getStatus() != null && request.getStatus().isActive();
            boolean sameResource = resource.getId() == null
                    || request.getResourceId() == null
                    || resource.getId().equals(request.getResourceId());
            if (active && sameResource) {
                result.add(request);
            }
        }
        return result;
    }

    private long computeDurationUnits(PricingUnit unit, Instant start, Instant end) {
        Duration duration = Duration.between(start, end);
        long minutes = Math.max(0, duration.toMinutes());
        return switch (unit) {
            case PER_HOUR -> Math.max(1, ceilDiv(minutes, 60));
            case PER_DAY -> Math.max(1, ceilDiv(minutes, 60L * 24));
            case FLAT, PER_UNIT -> 1;
        };
    }

    private long ceilDiv(long value, long divisor) {
        return (value + divisor - 1) / divisor;
    }

    private String ids(List<RequestEntity> requests) {
        return requests.stream().map(r -> String.valueOf(r.getId())).toList().toString();
    }

    private boolean hasAny(Map<String, Object> metadata, String... keys) {
        if (metadata == null) {
            return false;
        }
        for (String key : keys) {
            if (metadata.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    private boolean readBoolean(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) {
            return false;
        }
        Object value = metadata.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private String readString(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) {
            return null;
        }
        String value = String.valueOf(metadata.get(key));
        return value.isBlank() ? null : value;
    }

    private Integer readInteger(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) {
            return null;
        }
        Object value = metadata.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BigDecimal readDecimal(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) {
            return null;
        }
        Object value = metadata.get(key);
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
