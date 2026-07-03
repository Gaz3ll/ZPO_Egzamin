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

/**
 * VR arcade reservation algorithm.
 *
 * <p>Resource = a VR zone with a pool of headsets. Request = a reservation of headsets for a group.
 * The algorithm sums the headsets already reserved ({@code reduce}), checks whether the requested
 * count fits the remaining pool, verifies the game type is offered, carries the QR code, and prices
 * the reservation per headset.</p>
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
        if (!isVrInput(input)) {
            return evaluateGeneric(input);
        }

        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();

        if (resource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak strefy VR");
            return DomainAlgorithmResult.failure(List.of("Strefa VR jest wymagana"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: strefa niedostępna (" + resource.getStatus() + ")");
            return DomainAlgorithmResult.failure(List.of("Strefa VR nie jest dostępna"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        int totalHeadsets = Math.max(0, orDefault(readInteger(resource.getMetadata(), "totalHeadsets"),
                resource.getCapacityValue() != null ? resource.getCapacityValue() : 0));
        List<RequestEntity> activeExisting = activeExistingFor(resource, input.existingRequests());
        int currentlyReserved = activeExisting.stream()
                .mapToInt(r -> r.getQuantity() == null ? 1 : r.getQuantity())
                .reduce(0, Integer::sum);
        int requestedHeadsets = Math.max(1, orDefault(readInteger(input.requestMetadata(), "playersCount"),
                input.quantity() != null ? input.quantity() : 1));
        int remaining = totalHeadsets - currentlyReserved;

        breakdown.addRule("TOTAL_HEADSETS: " + totalHeadsets);
        breakdown.addRule("CURRENTLY_RESERVED: " + currentlyReserved);
        breakdown.addRule("REQUESTED_HEADSETS: " + requestedHeadsets);
        breakdown.addRule("REMAINING_HEADSETS: " + remaining);
        breakdown.addNote("totalHeadsets=" + totalHeadsets);
        breakdown.addNote("currentlyReserved=" + currentlyReserved);
        breakdown.addNote("requestedHeadsets=" + requestedHeadsets);
        breakdown.addNote("remainingHeadsets=" + remaining);

        if (requestedHeadsets > remaining) {
            errors.add("Brak wolnych gogli: żądane " + requestedHeadsets + ", wolne " + Math.max(0, remaining));
            breakdown.addRule("HEADSET_POOL_CHECK: przekroczono pulę");
        } else {
            breakdown.addRule("HEADSET_POOL_CHECK: ok");
        }

        String gameType = readString(input.requestMetadata(), "gameType");
        Set<String> gameTypes = parseCsvSet(readString(resource.getMetadata(), "gameTypes"));
        if (gameType != null && !gameTypes.isEmpty() && !gameTypes.contains(gameType.toUpperCase())) {
            errors.add("Strefa nie oferuje gry " + gameType);
            breakdown.addRule("GAME_TYPE_CHECK: mismatch");
        } else {
            breakdown.addRule("GAME_TYPE_CHECK: ok");
        }

        String qrCode = readString(input.requestMetadata(), "qrCode");
        if (qrCode == null) {
            qrCode = "VR-" + resource.getId() + "-" + Math.abs((gameType + requestedHeadsets).hashCode());
        }
        breakdown.addRule("QR_CODE: " + qrCode);
        breakdown.addNote("qrCode=" + qrCode);

        BigDecimal totalPrice = computeVrValue(resource, requestedHeadsets, breakdown);

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        breakdown.addRule("TOTAL_PRICE: " + totalPrice.toPlainString());
        return DomainAlgorithmResult.success(totalPrice, resource.getId(), breakdown.build());
    }

    private BigDecimal computeVrValue(ResourceEntity resource, int headsets, AlgorithmBreakdownBuilder breakdown) {
        BigDecimal perHeadset = resource.getBaseValue() != null ? resource.getBaseValue() : BigDecimal.ZERO;
        BigDecimal total = perHeadset.multiply(BigDecimal.valueOf(headsets));
        breakdown.addLine("headsetPrice", total, perHeadset.toPlainString() + " × " + headsets + " gogli");
        breakdown.addRule("HEADSET_PRICE: " + perHeadset.toPlainString() + " × " + headsets);
        BigDecimal result = breakdown.total().amount();
        breakdown.addNote("totalPrice=" + result.toPlainString());
        return result;
    }

    private int orDefault(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private Set<String> parseCsvSet(String value) {
        Set<String> result = new LinkedHashSet<>();
        if (value == null) {
            return result;
        }
        Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase())
                .forEach(result::add);
        return result;
    }

    private boolean isVrInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "playersCount", "gameType", "customerName", "qrCode")
                || (input.resource() != null && hasAny(input.resource().getMetadata(), "zoneName",
                "totalHeadsets", "gameTypes", "maxPlayers", "hasMultiplayer"));
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
