package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.availability.CapacityMatcher;
import pl.zpo.app.domain.availability.TimeCollisionDetector;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.config.PricingUnit;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;

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
        if (!isZooInput(input)) {
            return evaluateGeneric(input);
        }

        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();

        ResourceEntity resource = input.resource();
        if (resource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak wybiegu");
            return DomainAlgorithmResult.failure(List.of("Wybieg jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: wybieg nieaktywny (" + resource.getStatus() + ")");
            return DomainAlgorithmResult.failure(List.of("Wybieg nie jest aktywny"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        List<RequestEntity> activeExisting = activeExistingFor(resource, input.existingRequests());
        boolean hasStart = input.startAt() != null;
        boolean hasEnd = input.endAt() != null;

        if (hasStart ^ hasEnd) {
            errors.add("Zakres czasu zadania jest niekompletny");
            breakdown.addRule("TIME_RANGE_CHECK: niekompletny zakres");
        } else if (hasStart) {
            if (!input.startAt().isBefore(input.endAt())) {
                errors.add("Nieprawidłowy zakres czasu: początek musi być przed końcem");
                breakdown.addRule("TIME_RANGE_CHECK: nieprawidłowy zakres");
            } else {
                breakdown.addRule("TIME_RANGE_CHECK: ok");
                List<RequestEntity> collisions =
                        collisionDetector.findCollisions(input.startAt(), input.endAt(), activeExisting);
                if (!collisions.isEmpty()) {
                    errors.add("Termin koliduje z innymi zadaniami dla tego wybiegu: " + ids(collisions));
                    breakdown.addRule("TIME_COLLISION_CHECK: kolizja (" + collisions.size() + ")");
                    breakdown.addNote("Kolizja z zadaniami: " + ids(collisions));
                } else {
                    breakdown.addRule("TIME_COLLISION_CHECK: brak kolizji");
                }
            }
        } else if (profile.requiresTimeWindow()) {
            errors.add("Wymagany jest planowany czas zadania");
            breakdown.addRule("TIME_RANGE_CHECK: brak wymaganego zakresu");
        }

        int quantity = input.quantity() != null ? input.quantity() : 1;
        if (input.quantity() != null && input.quantity() <= 0) {
            errors.add("Ilość / obciążenie zadania musi być dodatnie");
            breakdown.addRule("QUANTITY_CHECK: nieprawidłowa ilość");
        } else if (input.quantity() != null && resource.getCapacityValue() != null
                && input.quantity() > resource.getCapacityValue()) {
            errors.add("Obciążenie zadania przekracza pojemność sektora");
            breakdown.addRule("CAPACITY_CHECK: przekroczono (%d>%d)"
                    .formatted(input.quantity(), resource.getCapacityValue()));
        } else {
            breakdown.addRule("CAPACITY_CHECK: ok");
        }

        Map<String, Object> resourceMetadata = resource.getMetadata();
        Map<String, Object> requestMetadata = input.requestMetadata();
        String dangerLevel = readString(resourceMetadata, "dangerLevel");
        String taskType = readString(requestMetadata, "taskType");
        String priority = readString(requestMetadata, "priority");
        String animalHealthRisk = readString(requestMetadata, "animalHealthRisk");
        boolean quarantine = readBoolean(resourceMetadata, "isQuarantine");
        boolean requiresVet = readBoolean(requestMetadata, "requiresVet");
        boolean requiresTwoKeepers = readBoolean(requestMetadata, "requiresTwoKeepers");

        if (isDangerous(dangerLevel)) {
            breakdown.addRule("DANGER_STAFFING_CHECK: dangerLevel=" + dangerLevel);
            if (!requiresTwoKeepers) {
                errors.add("Sektor o poziomie zagrożenia " + dangerLevel
                        + " wymaga dwóch opiekunów albo zatwierdzenia operatora/admina");
                breakdown.addRule("DANGER_STAFFING_CHECK: missing requiresTwoKeepers");
            } else {
                breakdown.addRule("DANGER_STAFFING_CHECK: two keepers confirmed");
            }
        }

        if (quarantine) {
            breakdown.addRule("QUARANTINE_CHECK: active");
            if (isBasicTask(taskType) && isLowOrNormal(priority)) {
                breakdown.addRule("QUARANTINE_RULE: podnieś priorytet lub stosuj procedurę bezpieczeństwa");
                breakdown.addNote("quarantineRule=zadanie w kwarantannie wymaga dodatkowej procedury bezpieczeństwa");
            }
            if ("TRANSFER".equalsIgnoreCase(taskType) && !requiresVet) {
                errors.add("Przeniesienie z kwarantanny wymaga udziału weterynarza");
                breakdown.addRule("QUARANTINE_TRANSFER_CHECK: missing vet");
            }
        } else {
            breakdown.addRule("QUARANTINE_CHECK: inactive");
        }

        if ("CRITICAL".equalsIgnoreCase(animalHealthRisk) && !requiresVet) {
            errors.add("Krytyczne ryzyko zdrowotne wymaga udziału weterynarza");
            breakdown.addRule("HEALTH_RISK_CHECK: missing vet for CRITICAL risk");
        } else if (animalHealthRisk != null) {
            breakdown.addRule("HEALTH_RISK_CHECK: " + animalHealthRisk);
        }

        BigDecimal calculatedValue = computeZooValue(resource, requestMetadata, quantity, breakdown);

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        breakdown.addRule("ESTIMATED_WORKLOAD: " + calculatedValue.toPlainString());
        return DomainAlgorithmResult.success(calculatedValue, resource.getId(), breakdown.build());
    }

    private BigDecimal computeZooValue(ResourceEntity resource,
                                       Map<String, Object> requestMetadata,
                                       int quantity,
                                       AlgorithmBreakdownBuilder breakdown) {
        Map<String, Object> resourceMetadata = resource.getMetadata();
        BigDecimal runningTotal = resource.getBaseValue() != null ? resource.getBaseValue() : BigDecimal.ZERO;
        breakdown.addLine("baseWorkload", runningTotal, "bazowy czas/koszt obsługi sektora");
        breakdown.addRule("BASE_WORKLOAD: " + runningTotal.toPlainString());

        Integer animalCount = readInteger(resourceMetadata, "animalCount");
        BigDecimal animalCountFactor = animalCountFactor(animalCount, quantity);
        runningTotal = applyMultiplier(breakdown, "animalCountFactor", runningTotal, animalCountFactor,
                "animalCount=" + (animalCount == null ? "brak" : animalCount) + ", quantity=" + quantity);

        String cleaningDifficulty = readString(resourceMetadata, "cleaningDifficulty");
        runningTotal = applyMultiplier(breakdown, "cleaningDifficultyMultiplier", runningTotal,
                cleaningDifficultyMultiplier(cleaningDifficulty), "cleaningDifficulty=" + cleaningDifficulty);

        String dangerLevel = readString(resourceMetadata, "dangerLevel");
        runningTotal = applyMultiplier(breakdown, "dangerLevelMultiplier", runningTotal,
                dangerMultiplier(dangerLevel), "dangerLevel=" + dangerLevel);

        String taskType = readString(requestMetadata, "taskType");
        runningTotal = applyMultiplier(breakdown, "taskTypeModifier", runningTotal,
                taskTypeMultiplier(taskType), "taskType=" + taskType);

        String priority = readString(requestMetadata, "priority");
        runningTotal = applyMultiplier(breakdown, "priorityModifier", runningTotal,
                priorityMultiplier(priority), "priority=" + priority);

        boolean quarantine = readBoolean(resourceMetadata, "isQuarantine");
        BigDecimal quarantineMultiplier = quarantine ? new BigDecimal("1.25") : BigDecimal.ONE;
        runningTotal = applyMultiplier(breakdown, "quarantineRule", runningTotal, quarantineMultiplier,
                quarantine ? "sektor w kwarantannie" : "brak kwarantanny");

        boolean requiresTwoKeepers = readBoolean(requestMetadata, "requiresTwoKeepers");
        BigDecimal staffingMultiplier = requiresTwoKeepers ? new BigDecimal("1.15") : BigDecimal.ONE;
        runningTotal = applyMultiplier(breakdown, "requiresTwoKeepers", runningTotal, staffingMultiplier,
                requiresTwoKeepers ? "podwójna obsada" : "standardowa obsada");

        BigDecimal estimatedWorkload = breakdown.total().amount();
        breakdown.addNote("estimatedWorkload=" + estimatedWorkload.toPlainString());
        return estimatedWorkload;
    }

    private BigDecimal applyMultiplier(AlgorithmBreakdownBuilder breakdown,
                                       String label,
                                       BigDecimal currentValue,
                                       BigDecimal multiplier,
                                       String detail) {
        BigDecimal delta = currentValue.multiply(multiplier).subtract(currentValue)
                .setScale(2, RoundingMode.HALF_UP);
        breakdown.addLine(label, delta, detail + ", x " + multiplier.toPlainString());
        breakdown.addRule(label + ": x " + multiplier.toPlainString());
        return currentValue.add(delta);
    }

    private BigDecimal animalCountFactor(Integer animalCount, int quantity) {
        int count = animalCount == null ? 0 : Math.max(0, animalCount);
        int load = Math.max(1, quantity);
        int percent = Math.min(150, count * 3 + load * 5);
        return BigDecimal.ONE.add(BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    }

    private BigDecimal cleaningDifficultyMultiplier(String cleaningDifficulty) {
        if (cleaningDifficulty == null) {
            return BigDecimal.ONE;
        }
        return switch (cleaningDifficulty.toUpperCase()) {
            case "MEDIUM" -> new BigDecimal("1.15");
            case "HIGH" -> new BigDecimal("1.40");
            case "EXTREME" -> new BigDecimal("1.80");
            default -> BigDecimal.ONE;
        };
    }

    private BigDecimal dangerMultiplier(String dangerLevel) {
        if (dangerLevel == null) {
            return BigDecimal.ONE;
        }
        return switch (dangerLevel.toUpperCase()) {
            case "MEDIUM" -> new BigDecimal("1.15");
            case "HIGH" -> new BigDecimal("1.35");
            case "CRITICAL" -> new BigDecimal("1.70");
            default -> BigDecimal.ONE;
        };
    }

    private BigDecimal taskTypeMultiplier(String taskType) {
        if (taskType == null) {
            return BigDecimal.ONE;
        }
        return switch (taskType.toUpperCase()) {
            case "FEEDING" -> new BigDecimal("1.10");
            case "CLEANING" -> new BigDecimal("1.20");
            case "VET_CHECK" -> new BigDecimal("1.45");
            case "TRANSFER" -> new BigDecimal("1.60");
            case "TECHNICAL_INSPECTION" -> new BigDecimal("1.30");
            default -> BigDecimal.ONE;
        };
    }

    private BigDecimal priorityMultiplier(String priority) {
        if (priority == null) {
            return BigDecimal.ONE;
        }
        return switch (priority.toUpperCase()) {
            case "LOW" -> new BigDecimal("0.95");
            case "HIGH" -> new BigDecimal("1.25");
            case "URGENT" -> new BigDecimal("1.60");
            default -> BigDecimal.ONE;
        };
    }

    private boolean isDangerous(String dangerLevel) {
        return "HIGH".equalsIgnoreCase(dangerLevel) || "CRITICAL".equalsIgnoreCase(dangerLevel);
    }

    private boolean isBasicTask(String taskType) {
        return "FEEDING".equalsIgnoreCase(taskType) || "CLEANING".equalsIgnoreCase(taskType);
    }

    private boolean isLowOrNormal(String priority) {
        return priority == null || "LOW".equalsIgnoreCase(priority) || "NORMAL".equalsIgnoreCase(priority);
    }

    private boolean isZooInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "taskType", "priority", "requiresVet", "requiresTwoKeepers",
                "animalHealthRisk", "notes")
                || (input.resource() != null && hasAny(input.resource().getMetadata(), "animalSpecies",
                "animalCount", "dangerLevel", "feedingType", "cleaningDifficulty", "keeperZone",
                "isQuarantine", "lastInspectionDate"));
    }

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

    private BigDecimal computeGenericValue(ResourceEntity resource,
                                           Map<String, Object> requestMetadata,
                                           long durationUnits,
                                           int qty,
                                           DomainProfile profile,
                                           AlgorithmBreakdownBuilder breakdown) {
        BigDecimal base = resource.getBaseValue() != null ? resource.getBaseValue() : BigDecimal.ZERO;
        BigDecimal subtotal = base
                .multiply(BigDecimal.valueOf(durationUnits))
                .multiply(BigDecimal.valueOf(qty));
        breakdown.addLine("Wartość bazowa", subtotal,
                "%s × %d (%s) × %d".formatted(base.toPlainString(), durationUnits, profile.pricingUnit(), qty));
        breakdown.addRule("VALUE_BASE: %s × %d × %d".formatted(base.toPlainString(), durationUnits, qty));

        BigDecimal runningTotal = subtotal;
        BigDecimal multiplier = readDecimal(resource.getMetadata(), "priceMultiplier");
        if (multiplier != null && multiplier.compareTo(BigDecimal.ONE) != 0) {
            BigDecimal delta = subtotal.multiply(multiplier).subtract(subtotal);
            breakdown.addLine("Mnożnik", delta, "× " + multiplier.toPlainString());
            breakdown.addRule("VALUE_MULTIPLIER: × " + multiplier.toPlainString());
            runningTotal = runningTotal.add(delta);
        }

        BigDecimal surcharge = readDecimal(resource.getMetadata(), "fixedSurcharge");
        if (surcharge != null && surcharge.signum() != 0) {
            breakdown.addLine("Dopłata stała", surcharge, "opłata stała");
            breakdown.addRule("VALUE_SURCHARGE: + " + surcharge.toPlainString());
            runningTotal = runningTotal.add(surcharge);
        }

        BigDecimal discountPercent = readDecimal(requestMetadata, "discountPercent");
        if (discountPercent != null && discountPercent.signum() > 0) {
            BigDecimal discount = runningTotal
                    .multiply(discountPercent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            breakdown.addLine("Rabat", discount.negate(), "-" + discountPercent.toPlainString() + "%");
            breakdown.addRule("VALUE_DISCOUNT: -" + discountPercent.toPlainString() + "%");
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

    private boolean readBoolean(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) {
            return false;
        }
        Object value = metadata.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return "true".equalsIgnoreCase(String.valueOf(value));
    }
}
