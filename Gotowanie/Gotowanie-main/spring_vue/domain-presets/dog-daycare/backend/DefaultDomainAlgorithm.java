package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
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
        if (!isDogDaycareInput(input)) {
            return evaluateGeneric(input);
        }
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(input.profile().currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();
        if (resource == null) return DomainAlgorithmResult.failure(List.of("Strefa opieki jest wymagana"), breakdown.build());
        if (resource.getStatus() != ResourceStatus.ACTIVE) return DomainAlgorithmResult.failure(List.of("Strefa opieki nie jest dostępna"), breakdown.build());

        String dogSize = readString(input.requestMetadata(), "dogSize");
        Set<String> accepted = parseCsv(readString(resource.getMetadata(), "acceptedDogSizes"));
        if (dogSize != null && !accepted.isEmpty() && !accepted.contains(dogSize.toUpperCase())) {
            errors.add("Strefa nie przyjmuje psów w rozmiarze " + dogSize);
            breakdown.addRule("DOG_SIZE_CHECK: rejected");
        } else {
            breakdown.addRule("DOG_SIZE_CHECK: ok");
        }

        int capacityPoints = Math.max(0, orDefault(readInteger(resource.getMetadata(), "dailyCapacityPoints"),
                resource.getCapacityValue() == null ? 0 : resource.getCapacityValue()));
        int alreadyUsedPoints = activeExistingFor(resource, input.existingRequests()).stream()
                .map(RequestEntity::getMetadata)
                .mapToInt(meta -> dogCapacityWeight(readString(meta, "dogSize")))
                .sum();
        int dogCapacityWeight = dogCapacityWeight(dogSize);
        int remainingPoints = capacityPoints - alreadyUsedPoints - dogCapacityWeight;

        breakdown.addNote("capacityPoints=" + capacityPoints);
        breakdown.addNote("alreadyUsedPoints=" + alreadyUsedPoints);
        breakdown.addNote("dogCapacityWeight=" + dogCapacityWeight);
        breakdown.addNote("remainingPoints=" + remainingPoints);
        breakdown.addRule("CAPACITY_POINTS: " + capacityPoints);
        breakdown.addRule("ALREADY_USED_POINTS: " + alreadyUsedPoints);
        breakdown.addRule("DOG_CAPACITY_WEIGHT: " + dogCapacityWeight);
        breakdown.addRule("REMAINING_POINTS: " + remainingPoints);
        if (remainingPoints < 0) {
            errors.add("Brak punktów pojemności w strefie");
            breakdown.addRule("WEIGHTED_CAPACITY_CHECK: exceeded");
        } else {
            breakdown.addRule("WEIGHTED_CAPACITY_CHECK: ok");
        }

        BigDecimal total = computeDogDaycareValue(resource, input.requestMetadata(), breakdown);
        if (!errors.isEmpty()) return DomainAlgorithmResult.failure(errors, breakdown.build());
        return DomainAlgorithmResult.success(total, resource.getId(), breakdown.build());
    }

    private int dogCapacityWeight(String dogSize) {
        return switch (dogSize == null ? "" : dogSize.toUpperCase()) {
            case "SMALL" -> 1;
            case "MEDIUM" -> 2;
            case "LARGE" -> 3;
            default -> 2;
        };
    }

    private BigDecimal computeDogDaycareValue(ResourceEntity resource, Map<String, Object> request,
                                              AlgorithmBreakdownBuilder breakdown) {
        BigDecimal hourlyRate = resource.getBaseValue() == null ? BigDecimal.ZERO : resource.getBaseValue();
        int stayHours = Math.max(1, orDefault(readInteger(request, "stayHours"), 1));
        BigDecimal base = hourlyRate.multiply(BigDecimal.valueOf(stayHours));
        breakdown.addLine("carePrice", base, hourlyRate + "/h × " + stayHours);
        BigDecimal medicationFee = readBoolean(request, "needsMedication") ? new BigDecimal("25.00") : BigDecimal.ZERO;
        breakdown.addLine("medicationFee", medicationFee, "podanie leków");
        BigDecimal extraWalkFee = readBoolean(request, "extraWalk") ? new BigDecimal("20.00") : BigDecimal.ZERO;
        breakdown.addLine("extraWalkFee", extraWalkFee, "dodatkowy spacer");
        BigDecimal total = breakdown.total().amount();
        breakdown.addNote("medicationFee=" + medicationFee.toPlainString());
        breakdown.addNote("extraWalkFee=" + extraWalkFee.toPlainString());
        breakdown.addNote("totalPrice=" + total.toPlainString());
        breakdown.addRule("TOTAL_PRICE: " + total.toPlainString());
        return total;
    }

    private boolean isDogDaycareInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "dogName", "dogSize", "dogWeight", "stayHours", "needsMedication",
                "extraWalk", "feedingNotes")
                || (input.resource() != null && hasAny(input.resource().getMetadata(), "zoneName",
                "dailyCapacityPoints", "acceptedDogSizes", "hasOutdoorRun", "careLevel", "staffCount"));
    }

    private DomainAlgorithmResult evaluateGeneric(DomainAlgorithmInput input) {
        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();
        if (resource == null) return DomainAlgorithmResult.failure(List.of("Zasób jest wymagany"), breakdown.build());
        if (resource.getStatus() != ResourceStatus.ACTIVE) return DomainAlgorithmResult.failure(List.of("Zasób nie jest aktywny"), breakdown.build());
        long units = 1;
        boolean hasStart = input.startAt() != null;
        boolean hasEnd = input.endAt() != null;
        List<RequestEntity> active = activeExistingFor(resource, input.existingRequests());
        if (profile.algorithmMode().checksTime() || hasStart || hasEnd) {
            if (hasStart ^ hasEnd) errors.add("Zakres dat jest niekompletny (wymagane początek i koniec)");
            else if (hasStart) {
                if (!input.startAt().isBefore(input.endAt())) errors.add("Nieprawidłowy zakres dat: początek musi być przed końcem");
                else {
                    units = computeDurationUnits(profile.pricingUnit(), input.startAt(), input.endAt());
                    if (profile.algorithmMode().checksTime() && !collisionDetector.findCollisions(input.startAt(), input.endAt(), active).isEmpty()) errors.add("Termin koliduje z istniejącymi zgłoszeniami");
                }
            } else if (profile.requiresTimeWindow()) errors.add("Wymagany jest zakres dat");
        }
        int qty = input.quantity() == null ? 1 : input.quantity();
        if (input.quantity() != null && input.quantity() <= 0) errors.add("Ilość musi być dodatnia");
        else if (profile.requiresQuantity() && input.quantity() == null) errors.add("Wymagana jest ilość");
        if (profile.algorithmMode().checksCapacity() && input.quantity() != null && resource.getCapacityValue() != null) {
            int used = capacityMatcher.usedCapacity(active);
            int capacity = resource.getCapacityValue();
            if (!capacityMatcher.fits(capacity, used, qty)) errors.add("Przekroczono pojemność: użyte %d/%d, żądane %d".formatted(used, capacity, qty));
        }
        BigDecimal value = null;
        if (profile.algorithmMode().calculatesValue()) value = computeGenericValue(resource, units, qty, profile, breakdown);
        if (!errors.isEmpty()) return DomainAlgorithmResult.failure(errors, breakdown.build());
        return DomainAlgorithmResult.success(value, resource.getId(), breakdown.build());
    }

    private BigDecimal computeGenericValue(ResourceEntity resource, long units, int qty, DomainProfile profile, AlgorithmBreakdownBuilder breakdown) {
        BigDecimal base = resource.getBaseValue() == null ? BigDecimal.ZERO : resource.getBaseValue();
        BigDecimal subtotal = base.multiply(BigDecimal.valueOf(units)).multiply(BigDecimal.valueOf(qty));
        breakdown.addLine("Wartość bazowa", subtotal, "%s × %d (%s) × %d".formatted(base.toPlainString(), units, profile.pricingUnit(), qty));
        BigDecimal multiplier = readDecimal(resource.getMetadata(), "priceMultiplier");
        if (multiplier != null && multiplier.compareTo(BigDecimal.ONE) != 0) breakdown.addLine("Mnożnik", subtotal.multiply(multiplier).subtract(subtotal), "× " + multiplier);
        return breakdown.total().amount();
    }

    private List<RequestEntity> activeExistingFor(ResourceEntity resource, List<RequestEntity> existing) {
        List<RequestEntity> result = new ArrayList<>();
        for (RequestEntity request : existing) {
            boolean active = request.getStatus() != null && request.getStatus().isActive();
            boolean same = resource.getId() == null || request.getResourceId() == null || resource.getId().equals(request.getResourceId());
            if (active && same) result.add(request);
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

    private long ceilDiv(long value, long divisor) { return (value + divisor - 1) / divisor; }
    private Set<String> parseCsv(String value) {
        Set<String> result = new LinkedHashSet<>();
        if (value == null) return result;
        Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty()).map(s -> s.toUpperCase()).forEach(result::add);
        return result;
    }
    private int orDefault(Integer value, int fallback) { return value == null ? fallback : value; }
    private boolean hasAny(Map<String, Object> metadata, String... keys) {
        if (metadata == null) return false;
        for (String key : keys) if (metadata.containsKey(key)) return true;
        return false;
    }
    private String readString(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) return null;
        String value = String.valueOf(metadata.get(key));
        return value.isBlank() ? null : value;
    }
    private Integer readInteger(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) return null;
        Object value = metadata.get(key);
        if (value instanceof Number number) return number.intValue();
        try { return Integer.parseInt(String.valueOf(value)); } catch (NumberFormatException ex) { return null; }
    }
    private BigDecimal readDecimal(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) return null;
        Object value = metadata.get(key);
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        try { return new BigDecimal(String.valueOf(value)); } catch (NumberFormatException ex) { return null; }
    }
    private boolean readBoolean(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) return false;
        Object value = metadata.get(key);
        return value instanceof Boolean bool ? bool : Boolean.parseBoolean(String.valueOf(value));
    }
}
