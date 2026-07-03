package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.availability.CapacityMatcher;
import pl.zpo.app.domain.availability.TimeCollisionDetector;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.config.PricingUnit;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;

/**
 * Employee scheduler algorithm.
 *
 * <p>Resource = an employee, Request = a shift entry. The algorithm detects shift collisions
 * for the same employee, checks the weekly hour total against {@code maxHoursPerWeek},
 * and prices the shift:
 * {@code hours × hourlyRate × shiftTypeMultiplier} (EVENING ×1.1).</p>
 *
 * <p>Shifts: MORNING 7:00-15:00, EVENING 15:00-23:00 (8h each)</p>
 */
@Component
public class DefaultDomainAlgorithm implements DomainAlgorithm {

    private final TimeCollisionDetector collisionDetector;
    private final CapacityMatcher capacityMatcher;

    public DefaultDomainAlgorithm(TimeCollisionDetector collisionDetector, CapacityMatcher capacityMatcher) {
        this.collisionDetector = collisionDetector;
        this.capacityMatcher = capacityMatcher;
    }

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        if (!isSchedulerInput(input)) {
            return evaluateGeneric(input);
        }

        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();

        if (resource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak pracownika");
            return DomainAlgorithmResult.failure(List.of("Pracownik jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: pracownik niedostępny (" + resource.getStatus() + ")");
            return DomainAlgorithmResult.failure(List.of("Pracownik nie jest dostępny"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        if (input.startAt() == null || input.endAt() == null || !input.startAt().isBefore(input.endAt())) {
            breakdown.addRule("TIME_RANGE_CHECK: nieprawidłowy zakres");
            return DomainAlgorithmResult.failure(
                    List.of("Wpis grafiku wymaga poprawnego zakresu od-do"), breakdown.build());
        }
        breakdown.addRule("TIME_RANGE_CHECK: ok");

        List<RequestEntity> activeExisting = activeExistingFor(resource, input.existingRequests());
        List<RequestEntity> collisions =
                collisionDetector.findCollisions(input.startAt(), input.endAt(), activeExisting);
        if (!collisions.isEmpty()) {
            errors.add("Zmiana koliduje z istniejącymi wpisami grafiku: " + ids(collisions));
            breakdown.addRule("SHIFT_COLLISION_CHECK: kolizja (" + collisions.size() + ")");
        } else {
            breakdown.addRule("SHIFT_COLLISION_CHECK: brak kolizji");
        }

        BigDecimal workedHours = BigDecimal.valueOf(Duration.between(input.startAt(), input.endAt()).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        breakdown.addNote("workedHours=" + workedHours.toPlainString());

        Integer maxHoursPerWeek = readInteger(resource.getMetadata(), "maxHoursPerWeek");
        BigDecimal weeklyHoursAfter = weeklyHours(activeExisting, input.startAt()).add(workedHours);
        breakdown.addNote("weeklyHoursAfterAssignment=" + weeklyHoursAfter.toPlainString());
        breakdown.addNote("maxHoursPerWeek=" + (maxHoursPerWeek == null ? "brak" : maxHoursPerWeek));
        if (maxHoursPerWeek != null && maxHoursPerWeek > 0
                && weeklyHoursAfter.compareTo(BigDecimal.valueOf(maxHoursPerWeek)) > 0) {
            errors.add("Przekroczony tygodniowy limit godzin: " + weeklyHoursAfter.toPlainString()
                    + "h > " + maxHoursPerWeek + "h");
            breakdown.addRule("WEEKLY_HOURS_CHECK: przekroczono ("
                    + weeklyHoursAfter.toPlainString() + "/" + maxHoursPerWeek + "h)");
        } else {
            breakdown.addRule("WEEKLY_HOURS_CHECK: ok (" + weeklyHoursAfter.toPlainString()
                    + (maxHoursPerWeek == null ? "h" : "/" + maxHoursPerWeek + "h") + ")");
        }

        BigDecimal hourlyRate = readDecimal(resource.getMetadata(), "hourlyRate");
        if (hourlyRate == null) {
            hourlyRate = resource.getBaseValue() != null ? resource.getBaseValue() : BigDecimal.ZERO;
        }
        breakdown.addNote("hourlyRate=" + hourlyRate.toPlainString());

        BigDecimal baseCost = workedHours.multiply(hourlyRate).setScale(2, RoundingMode.HALF_UP);
        breakdown.addLine("baseCost", baseCost,
                workedHours.toPlainString() + "h × " + hourlyRate.toPlainString() + " PLN/h");
        breakdown.addRule("BASE_COST: " + workedHours.toPlainString() + "h × " + hourlyRate.toPlainString());

        String shiftType = orDefault(readString(input.requestMetadata(), "shiftType"), "CUSTOM")
                .toUpperCase(Locale.ROOT);
        BigDecimal shiftMultiplier = shiftTypeMultiplier(shiftType);
        breakdown.addNote("shiftTypeMultiplier=" + shiftMultiplier.toPlainString());
        BigDecimal surcharge = baseCost.multiply(shiftMultiplier).subtract(baseCost)
                .setScale(2, RoundingMode.HALF_UP);
        if (surcharge.signum() != 0) {
            breakdown.addLine("shiftTypeSurcharge", surcharge,
                    "zmiana " + shiftType + " × " + shiftMultiplier.toPlainString());
        }
        breakdown.addRule("SHIFT_TYPE_MULTIPLIER: × " + shiftMultiplier.toPlainString() + " (" + shiftType + ")");

        BigDecimal totalCost = breakdown.total().amount();
        breakdown.addRule("TOTAL_COST: " + totalCost.toPlainString());
        breakdown.addNote("totalCost=" + totalCost.toPlainString());

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        return DomainAlgorithmResult.success(totalCost, resource.getId(), breakdown.build());
    }

    private BigDecimal weeklyHours(List<RequestEntity> existing, Instant reference) {
        WeekFields weekFields = WeekFields.ISO;
        LocalDate refDate = LocalDate.ofInstant(reference, ZoneOffset.UTC);
        int week = refDate.get(weekFields.weekOfWeekBasedYear());
        int year = refDate.get(weekFields.weekBasedYear());
        BigDecimal total = BigDecimal.ZERO;
        for (RequestEntity request : existing) {
            if (request.getStartAt() == null || request.getEndAt() == null) {
                continue;
            }
            LocalDate day = LocalDate.ofInstant(request.getStartAt(), ZoneOffset.UTC);
            if (day.get(weekFields.weekOfWeekBasedYear()) == week
                    && day.get(weekFields.weekBasedYear()) == year) {
                total = total.add(BigDecimal
                        .valueOf(Duration.between(request.getStartAt(), request.getEndAt()).toMinutes())
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
            }
        }
        return total;
    }

    private BigDecimal shiftTypeMultiplier(String shiftType) {
        return switch (shiftType) {
            case "EVENING" -> new BigDecimal("1.10");
            default -> BigDecimal.ONE; // MORNING
        };
    }

    private boolean isSchedulerInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "shiftType", "taskName")
                || (input.resource() != null && hasAny(input.resource().getMetadata(),
                "position", "department", "maxHoursPerWeek", "hourlyRate"));
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

    private String orDefault(String value, String fallback) {
        return value == null ? fallback : value;
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
