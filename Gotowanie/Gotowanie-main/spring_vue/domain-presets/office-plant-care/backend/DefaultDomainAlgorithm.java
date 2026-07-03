package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
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
 * Office plant care algorithm.
 *
 * <p>Resource = an office plant, Request = a care task or an adoption. For ADOPTION the algorithm
 * blocks plants that already have a caretaker. For care tasks it derives the recommended
 * frequency per care type (watering/fertilizing/repotting/health check), compares it with the
 * days since the last care, and computes {@code calculatedValue} = care priority score:
 * {@code baseValue × overdueFactor × healthMultiplier × difficultyMultiplier}. The breakdown also
 * carries the next recommended care date.</p>
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
        if (!isPlantCareInput(input)) {
            return evaluateGeneric(input);
        }

        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();

        if (resource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak rośliny");
            return DomainAlgorithmResult.failure(List.of("Roślina jest wymagana"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: roślina niedostępna (" + resource.getStatus() + ")");
            return DomainAlgorithmResult.failure(List.of("Roślina nie jest dostępna"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        String careType = readString(input.requestMetadata(), "careType");
        if (careType == null) {
            breakdown.addRule("CARE_TYPE: brak");
            return DomainAlgorithmResult.failure(List.of("Typ zadania (careType) jest wymagany"), breakdown.build());
        }
        careType = careType.toUpperCase(Locale.ROOT);
        breakdown.addRule("CARE_TYPE: " + careType);
        breakdown.addNote("careType=" + careType);

        boolean isAdopted = readBoolean(resource.getMetadata(), "isAdopted");
        if ("ADOPTION".equals(careType)) {
            if (isAdopted) {
                errors.add("Roślina '" + resource.getName() + "' jest już zaadoptowana");
                breakdown.addRule("ADOPTION_CHECK: roślina już zaadoptowana");
            } else {
                breakdown.addRule("ADOPTION_CHECK: dostępna do adopcji");
            }
        }

        BigDecimal base = resource.getBaseValue() != null ? resource.getBaseValue() : BigDecimal.TEN;
        breakdown.addLine("basePriority", base, "bazowy priorytet opieki");

        BigDecimal overdueFactor = BigDecimal.ONE;
        if (!"ADOPTION".equals(careType)) {
            Integer recommendedFrequency = recommendedFrequencyDays(careType, resource);
            breakdown.addRule("RECOMMENDED_FREQUENCY: co " + recommendedFrequency + " dni");
            breakdown.addNote("recommendedFrequency=" + recommendedFrequency);

            LocalDate lastCare = readDate(input.requestMetadata(), "lastCareAt");
            if (lastCare == null) {
                lastCare = readDate(input.requestMetadata(), "lastWateredAt");
            }
            if (lastCare == null) {
                lastCare = readDate(resource.getMetadata(), "lastCareAt");
            }
            long daysSinceLastCare = lastCare == null
                    ? recommendedFrequency
                    : Math.max(0, ChronoUnit.DAYS.between(lastCare, LocalDate.now()));
            breakdown.addRule("DAYS_SINCE_LAST_CARE: " + daysSinceLastCare
                    + (lastCare == null ? " (brak daty — przyjęto zalecaną częstotliwość)" : ""));
            breakdown.addNote("daysSinceLastCare=" + daysSinceLastCare);

            overdueFactor = BigDecimal.valueOf(daysSinceLastCare)
                    .divide(BigDecimal.valueOf(recommendedFrequency), 2, RoundingMode.HALF_UP)
                    .min(new BigDecimal("3.00"));
            breakdown.addRule("OVERDUE_FACTOR: × " + overdueFactor.toPlainString()
                    + (overdueFactor.compareTo(BigDecimal.ONE) > 0 ? " (po terminie)" : ""));

            LocalDate nextRecommended = (lastCare == null ? LocalDate.now() : lastCare)
                    .plusDays(recommendedFrequency);
            breakdown.addRule("NEXT_RECOMMENDED_DATE: " + nextRecommended);
            breakdown.addNote("nextRecommendedDate=" + nextRecommended);
        }

        BigDecimal healthMultiplier = healthMultiplier(readString(resource.getMetadata(), "healthStatus"));
        breakdown.addRule("HEALTH_MULTIPLIER: × " + healthMultiplier.toPlainString());
        breakdown.addNote("healthMultiplier=" + healthMultiplier.toPlainString());

        BigDecimal difficultyMultiplier = difficultyMultiplier(readString(resource.getMetadata(), "difficulty"));
        breakdown.addRule("DIFFICULTY_MULTIPLIER: × " + difficultyMultiplier.toPlainString());
        breakdown.addNote("difficultyMultiplier=" + difficultyMultiplier.toPlainString());

        BigDecimal score = base.multiply(overdueFactor).multiply(healthMultiplier)
                .multiply(difficultyMultiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal delta = score.subtract(base);
        if (delta.signum() != 0) {
            breakdown.addLine("priorityModifiers", delta,
                    "× " + overdueFactor.toPlainString() + " (termin) × " + healthMultiplier.toPlainString()
                            + " (zdrowie) × " + difficultyMultiplier.toPlainString() + " (trudność)");
        }
        breakdown.addRule("CARE_PRIORITY_SCORE: " + score.toPlainString());
        breakdown.addNote("carePriorityScore=" + score.toPlainString());

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        return DomainAlgorithmResult.success(breakdown.total().amount(), resource.getId(), breakdown.build());
    }

    private Integer recommendedFrequencyDays(String careType, ResourceEntity resource) {
        Integer waterDays = readInteger(resource.getMetadata(), "waterFrequencyDays");
        Integer fertilizeDays = readInteger(resource.getMetadata(), "fertilizeFrequencyDays");
        Integer repotMonths = readInteger(resource.getMetadata(), "repotFrequencyMonths");
        return switch (careType) {
            case "WATERING" -> orDefault(waterDays, 7);
            case "FERTILIZING" -> orDefault(fertilizeDays, 30);
            case "REPOTTING" -> orDefault(repotMonths, 12) * 30;
            default -> 30; // HEALTH_CHECK
        };
    }

    private BigDecimal healthMultiplier(String healthStatus) {
        if (healthStatus == null) {
            return BigDecimal.ONE;
        }
        return switch (healthStatus.toUpperCase(Locale.ROOT)) {
            case "OK" -> new BigDecimal("1.10");
            case "BAD" -> new BigDecimal("1.50");
            case "CRITICAL" -> new BigDecimal("2.00");
            default -> BigDecimal.ONE; // GOOD
        };
    }

    private BigDecimal difficultyMultiplier(String difficulty) {
        if (difficulty == null) {
            return BigDecimal.ONE;
        }
        return switch (difficulty.toUpperCase(Locale.ROOT)) {
            case "MEDIUM" -> new BigDecimal("1.20");
            case "HARD" -> new BigDecimal("1.40");
            default -> BigDecimal.ONE;
        };
    }

    private boolean isPlantCareInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "careType", "plantCondition", "lastCareAt", "lastWateredAt")
                || (input.resource() != null && hasAny(input.resource().getMetadata(),
                "species", "waterFrequencyDays", "lightRequirement", "isAdopted", "healthStatus"));
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

    private LocalDate readDate(Map<String, Object> metadata, String key) {
        String value = readString(metadata, key);
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value.length() > 10 ? value.substring(0, 10) : value);
        } catch (DateTimeParseException ex) {
            return null;
        }
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

    private int orDefault(Integer value, int fallback) {
        return value == null || value <= 0 ? fallback : value;
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
