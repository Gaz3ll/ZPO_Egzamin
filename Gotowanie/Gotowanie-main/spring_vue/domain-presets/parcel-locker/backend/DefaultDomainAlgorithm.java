package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.availability.CapacityMatcher;
import pl.zpo.app.domain.availability.TimeCollisionDetector;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.config.PricingUnit;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.request.RequestRepository;
import pl.zpo.app.domain.request.RequestStatus;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceRepository;
import pl.zpo.app.domain.resource.ResourceStatus;

@Component
public class DefaultDomainAlgorithm implements DomainAlgorithm {

    private final TimeCollisionDetector collisionDetector;
    private final CapacityMatcher capacityMatcher;
    private ResourceRepository resourceRepository;
    private RequestRepository requestRepository;

    public DefaultDomainAlgorithm(TimeCollisionDetector collisionDetector, CapacityMatcher capacityMatcher) {
        this.collisionDetector = collisionDetector;
        this.capacityMatcher = capacityMatcher;
    }

    @Autowired(required = false)
    public void setRepositories(ResourceRepository resourceRepository, RequestRepository requestRepository) {
        this.resourceRepository = resourceRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        if (!isParcelInput(input)) {
            return evaluateGeneric(input);
        }

        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();

        ResourceEntity requestedResource = input.resource();
        if (requestedResource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak skrytki startowej");
            return DomainAlgorithmResult.failure(List.of("Skrytka startowa jest wymagana"), breakdown.build());
        }
        if (requestedResource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: skrytka startowa nieaktywna");
            return DomainAlgorithmResult.failure(List.of("Wybrana skrytka nie jest aktywna"), breakdown.build());
        }

        String parcelSize = readString(input.requestMetadata(), "parcelSize");
        BigDecimal parcelWeight = readDecimal(input.requestMetadata(), "weight");
        if (parcelSize == null || sizeRank(parcelSize) == 0) {
            errors.add("Rozmiar paczki musi być jednym z: S, M, L, XL");
        }
        if (parcelWeight == null || parcelWeight.signum() <= 0) {
            errors.add("Waga paczki musi być dodatnia");
        }
        if (!errors.isEmpty()) {
            breakdown.addRule("PARCEL_INPUT_CHECK: invalid");
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        breakdown.addRule("PARCEL_INPUT_CHECK: ok");

        ResourceEntity selected = candidateLockers(requestedResource, input).stream()
                .filter(locker -> fits(locker, parcelSize, parcelWeight, input))
                .min(Comparator
                        .comparingInt((ResourceEntity locker) -> sizeRank(readString(locker.getMetadata(), "lockerSize")))
                        .thenComparing(locker -> readDecimal(locker.getMetadata(), "maxWeight"), Comparator.nullsLast(BigDecimal::compareTo))
                        .thenComparing(ResourceEntity::getName))
                .orElse(null);

        if (selected == null) {
            breakdown.addRule("LOCKER_MATCHING: conflict");
            breakdown.addNote("Brak aktywnej, wolnej skrytki mieszczącej paczkę " + parcelSize
                    + " / " + parcelWeight + " kg");
            return DomainAlgorithmResult.failure(List.of("Brak dostępnej skrytki dla tej paczki"), breakdown.build());
        }

        String selectedSize = readString(selected.getMetadata(), "lockerSize");
        String selectedCode = readString(selected.getMetadata(), "lockerCode");
        breakdown.addRule("LOCKER_MATCHING: selected " + selectedCode);
        breakdown.addNote("parcelSize=" + parcelSize);
        breakdown.addNote("parcelWeight=" + parcelWeight.setScale(2, RoundingMode.HALF_UP) + " kg");
        breakdown.addNote("selectedLockerSize=" + selectedSize);
        breakdown.addNote("selectedLockerCode=" + selectedCode);

        BigDecimal baseShippingCost = selected.getBaseValue() != null ? selected.getBaseValue() : new BigDecimal("12.00");
        BigDecimal sizeFee = sizeFee(parcelSize);
        BigDecimal weightFee = weightFee(parcelWeight);
        breakdown.addLine("baseShippingCost", baseShippingCost, "koszt bazowy nadania");
        breakdown.addLine("sizeFee", sizeFee, "rozmiar paczki: " + parcelSize);
        breakdown.addLine("weightFee", weightFee, "waga paczki: " + parcelWeight + " kg");
        BigDecimal totalCost = breakdown.total().amount();
        breakdown.addRule("TOTAL_COST: " + totalCost.toPlainString());

        return DomainAlgorithmResult.success(totalCost, selected.getId(), breakdown.build());
    }

    private boolean isParcelInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "receiverName", "receiverEmail", "parcelSize", "weight", "pickupCode")
                || (input.resource() != null && hasAny(input.resource().getMetadata(), "lockerCode", "lockerSize", "maxWeight", "isOccupied"));
    }

    private List<ResourceEntity> candidateLockers(ResourceEntity requestedResource, DomainAlgorithmInput input) {
        if (resourceRepository == null) {
            return List.of(requestedResource);
        }
        return resourceRepository.findAllByStatusOrderByNameAsc(ResourceStatus.ACTIVE);
    }

    private boolean fits(ResourceEntity locker, String parcelSize, BigDecimal parcelWeight, DomainAlgorithmInput input) {
        if (locker.getStatus() != ResourceStatus.ACTIVE) {
            return false;
        }
        if (isOccupied(locker, input)) {
            return false;
        }
        String lockerSize = readString(locker.getMetadata(), "lockerSize");
        if (sizeRank(lockerSize) < sizeRank(parcelSize)) {
            return false;
        }
        BigDecimal maxWeight = readDecimal(locker.getMetadata(), "maxWeight");
        return maxWeight == null || parcelWeight.compareTo(maxWeight) <= 0;
    }

    private boolean isOccupied(ResourceEntity locker, DomainAlgorithmInput input) {
        if (readBoolean(locker.getMetadata(), "isOccupied")) {
            return true;
        }
        List<RequestEntity> existing = requestRepository == null
                ? input.existingRequests()
                : requestRepository.findAllByResourceIdAndStatusIn(locker.getId(), RequestStatus.activeStatuses());
        return existing.stream()
                .anyMatch(request -> request.getStatus() != null
                        && request.getStatus().isActive()
                        && locker.getId().equals(request.getResourceId()));
    }

    private int sizeRank(String size) {
        if (size == null) {
            return 0;
        }
        return switch (size.toUpperCase()) {
            case "S" -> 1;
            case "M" -> 2;
            case "L" -> 3;
            case "XL" -> 4;
            default -> 0;
        };
    }

    private BigDecimal sizeFee(String parcelSize) {
        return switch (parcelSize.toUpperCase()) {
            case "M" -> new BigDecimal("4.00");
            case "L" -> new BigDecimal("8.00");
            case "XL" -> new BigDecimal("12.00");
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal weightFee(BigDecimal weight) {
        BigDecimal freeLimit = new BigDecimal("5.00");
        if (weight.compareTo(freeLimit) <= 0) {
            return BigDecimal.ZERO;
        }
        return weight.subtract(freeLimit).multiply(new BigDecimal("1.50")).setScale(2, RoundingMode.HALF_UP);
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
            int used = capacityMatcher.usedCapacity(activeExisting);
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
        BigDecimal subtotal = base.multiply(BigDecimal.valueOf(durationUnits)).multiply(BigDecimal.valueOf(qty));
        breakdown.addLine("Wartość bazowa", subtotal,
                "%s x %d (%s) x %d".formatted(base.toPlainString(), durationUnits, profile.pricingUnit(), qty));

        BigDecimal multiplier = readDecimal(resource.getMetadata(), "priceMultiplier");
        if (multiplier != null && multiplier.compareTo(BigDecimal.ONE) != 0) {
            BigDecimal delta = subtotal.multiply(multiplier).subtract(subtotal);
            breakdown.addLine("Mnożnik", delta, "x " + multiplier.toPlainString());
        }

        BigDecimal discountPercent = readDecimal(requestMetadata, "discountPercent");
        if (discountPercent != null && discountPercent.signum() > 0) {
            BigDecimal discount = breakdown.total().amount()
                    .multiply(discountPercent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            breakdown.addLine("Rabat", discount.negate(), "-" + discountPercent.toPlainString() + "%");
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
