package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
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
        if (!isFitnessInput(input)) {
            return evaluateGeneric(input);
        }

        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();

        if (resource == null) {
            breakdown.addRule("CLASS_CHECK: brak zajęć");
            return DomainAlgorithmResult.failure(List.of("Zajęcia są wymagane"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("CLASS_CHECK: zajęcia niedostępne");
            return DomainAlgorithmResult.failure(List.of("Zajęcia nie są aktywne"), breakdown.build());
        }
        breakdown.addRule("CLASS_CHECK: ok");

        String classLevel = readString(resource.getMetadata(), "difficultyLevel");
        String preferred = readString(input.requestMetadata(), "preferredDifficulty");
        int difficultyDelta = difficultyRank(classLevel) - difficultyRank(preferred);
        breakdown.addRule("DIFFICULTY_DELTA: " + difficultyDelta);
        breakdown.addNote("difficultyDelta=" + difficultyDelta);
        if (Math.abs(difficultyDelta) > 1) {
            errors.add("Poziom zajęć nie pasuje do preferencji uczestnika");
            breakdown.addRule("DIFFICULTY_MATCH: mismatch");
        } else {
            breakdown.addRule("DIFFICULTY_MATCH: ok");
        }

        int participants = Math.max(1, input.quantity() == null ? 1 : input.quantity());
        List<RequestEntity> activeExisting = activeExistingFor(resource, input.existingRequests());
        int used = capacityMatcher.usedCapacity(activeExisting);
        int capacity = resource.getCapacityValue() != null
                ? resource.getCapacityValue()
                : orDefault(readInteger(resource.getMetadata(), "capacity"), 0);
        int remaining = capacity - used;
        breakdown.addRule("CAPACITY_POINTS: " + capacity);
        breakdown.addRule("ALREADY_REGISTERED: " + used);
        breakdown.addRule("REMAINING_SLOTS: " + remaining);
        breakdown.addNote("remainingSlots=" + remaining);
        if (!capacityMatcher.fits(capacity, used, participants)) {
            errors.add("Brak miejsc: wolne " + Math.max(0, remaining));
            breakdown.addRule("CLASS_CAPACITY_CHECK: full");
        } else {
            breakdown.addRule("CLASS_CAPACITY_CHECK: ok");
        }

        BigDecimal total = computeFitnessValue(resource, input.requestMetadata(), participants, breakdown);
        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        breakdown.addRule("TOTAL_PRICE: " + total.toPlainString());
        return DomainAlgorithmResult.success(total, resource.getId(), breakdown.build());
    }

    private BigDecimal computeFitnessValue(ResourceEntity resource, Map<String, Object> request,
                                           int participants, AlgorithmBreakdownBuilder breakdown) {
        BigDecimal dropIn = firstNonNull(readDecimal(resource.getMetadata(), "dropInPrice"), resource.getBaseValue());
        dropIn = dropIn == null ? BigDecimal.ZERO : dropIn;
        String passType = readString(request, "passType");
        BigDecimal passModifier = switch (passType == null ? "DROP_IN" : passType.toUpperCase()) {
            case "MULTISPORT" -> new BigDecimal("0.20");
            case "MONTHLY" -> BigDecimal.ZERO;
            default -> BigDecimal.ONE;
        };
        BigDecimal classPrice = dropIn.multiply(passModifier).multiply(BigDecimal.valueOf(participants));
        breakdown.addLine("classPrice", classPrice, dropIn + " × pass " + passModifier + " × " + participants);
        breakdown.addRule("PASS_MODIFIER: " + passModifier.toPlainString());
        boolean needsEquipment = readBoolean(request, "needsEquipment");
        BigDecimal equipmentFee = needsEquipment ? BigDecimal.valueOf(10L * participants) : BigDecimal.ZERO;
        breakdown.addLine("equipmentFee", equipmentFee, needsEquipment ? "sprzęt treningowy" : "bez wypożyczenia");
        breakdown.addRule("EQUIPMENT_FEE: " + equipmentFee.toPlainString());
        BigDecimal total = breakdown.total().amount();
        breakdown.addNote("totalPrice=" + total.toPlainString());
        return total;
    }

    private int difficultyRank(String level) {
        return switch (level == null ? "" : level.toUpperCase()) {
            case "BEGINNER" -> 1;
            case "INTERMEDIATE" -> 2;
            case "ADVANCED" -> 3;
            default -> 2;
        };
    }

    private int orDefault(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private boolean isFitnessInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "memberName", "preferredDifficulty", "passType", "needsEquipment", "healthNotes")
                || (input.resource() != null && hasAny(input.resource().getMetadata(), "className", "trainerName",
                "difficultyLevel", "capacity", "equipmentRequired", "dropInPrice"));
    }

    private DomainAlgorithmResult evaluateGeneric(DomainAlgorithmInput input) {
        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();
        if (resource == null) {
            return DomainAlgorithmResult.failure(List.of("Zasób jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            return DomainAlgorithmResult.failure(List.of("Zasób nie jest aktywny"), breakdown.build());
        }
        long units = 1;
        boolean hasStart = input.startAt() != null;
        boolean hasEnd = input.endAt() != null;
        List<RequestEntity> active = activeExistingFor(resource, input.existingRequests());
        if (profile.algorithmMode().checksTime() || hasStart || hasEnd) {
            if (hasStart ^ hasEnd) {
                errors.add("Zakres dat jest niekompletny (wymagane początek i koniec)");
            } else if (hasStart) {
                if (!input.startAt().isBefore(input.endAt())) {
                    errors.add("Nieprawidłowy zakres dat: początek musi być przed końcem");
                } else {
                    units = computeDurationUnits(profile.pricingUnit(), input.startAt(), input.endAt());
                    if (profile.algorithmMode().checksTime()
                            && !collisionDetector.findCollisions(input.startAt(), input.endAt(), active).isEmpty()) {
                        errors.add("Termin koliduje z istniejącymi zgłoszeniami");
                    }
                }
            } else if (profile.requiresTimeWindow()) {
                errors.add("Wymagany jest zakres dat");
            }
        }
        int qty = input.quantity() == null ? 1 : input.quantity();
        if (input.quantity() != null && input.quantity() <= 0) {
            errors.add("Ilość musi być dodatnia");
        } else if (profile.requiresQuantity() && input.quantity() == null) {
            errors.add("Wymagana jest ilość");
        }
        if (profile.algorithmMode().checksCapacity() && input.quantity() != null && resource.getCapacityValue() != null) {
            int used = capacityMatcher.usedCapacity(active);
            int capacity = resource.getCapacityValue();
            if (!capacityMatcher.fits(capacity, used, qty)) {
                errors.add("Przekroczono pojemność: użyte %d/%d, żądane %d".formatted(used, capacity, qty));
            }
        }
        BigDecimal value = null;
        if (profile.algorithmMode().calculatesValue()) {
            value = computeGenericValue(resource, input.requestMetadata(), units, qty, profile, breakdown);
        }
        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        return DomainAlgorithmResult.success(value, resource.getId(), breakdown.build());
    }

    private BigDecimal computeGenericValue(ResourceEntity resource, Map<String, Object> requestMetadata,
                                           long units, int qty, DomainProfile profile,
                                           AlgorithmBreakdownBuilder breakdown) {
        BigDecimal base = resource.getBaseValue() == null ? BigDecimal.ZERO : resource.getBaseValue();
        BigDecimal subtotal = base.multiply(BigDecimal.valueOf(units)).multiply(BigDecimal.valueOf(qty));
        breakdown.addLine("Wartość bazowa", subtotal,
                "%s × %d (%s) × %d".formatted(base.toPlainString(), units, profile.pricingUnit(), qty));
        BigDecimal multiplier = readDecimal(resource.getMetadata(), "priceMultiplier");
        if (multiplier != null && multiplier.compareTo(BigDecimal.ONE) != 0) {
            breakdown.addLine("Mnożnik", subtotal.multiply(multiplier).subtract(subtotal), "× " + multiplier);
        }
        return breakdown.total().amount();
    }

    private List<RequestEntity> activeExistingFor(ResourceEntity resource, List<RequestEntity> existing) {
        List<RequestEntity> result = new ArrayList<>();
        for (RequestEntity request : existing) {
            boolean active = request.getStatus() != null && request.getStatus().isActive();
            boolean same = resource.getId() == null || request.getResourceId() == null || resource.getId().equals(request.getResourceId());
            if (active && same) {
                result.add(request);
            }
        }
        return result;
    }

    private long computeDurationUnits(PricingUnit unit, Instant start, Instant end) {
        long minutes = Math.max(0, Duration.between(start, end).toMinutes());
        return switch (unit) {
            case PER_HOUR -> Math.max(1, ceilDiv(minutes, 60));
            case PER_DAY -> Math.max(1, ceilDiv(minutes, 1440));
            case FLAT, PER_UNIT -> 1;
        };
    }

    private long ceilDiv(long value, long divisor) {
        return (value + divisor - 1) / divisor;
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
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private BigDecimal firstNonNull(BigDecimal a, BigDecimal b) {
        return a != null ? a : b;
    }
}
