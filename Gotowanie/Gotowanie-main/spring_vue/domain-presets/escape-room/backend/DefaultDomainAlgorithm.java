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

/**
 * Escape room booking algorithm.
 *
 * <p>Resource = an escape room. Request = a booking for a slot. The algorithm checks the time
 * collision on the room and the player count against {@code maxPlayers}; if the room is busy it
 * records an alternative suggestion (another room of the same difficulty in a free slot) as a
 * breakdown note, and prices the session flat with a difficulty modifier plus a deposit hold.</p>
 */
@Component
public class DefaultDomainAlgorithm implements DomainAlgorithm {

    private static final BigDecimal DEFAULT_DEPOSIT = new BigDecimal("100.00");

    private final TimeCollisionDetector collisionDetector;
    private final CapacityMatcher capacityMatcher;

    public DefaultDomainAlgorithm(TimeCollisionDetector collisionDetector, CapacityMatcher capacityMatcher) {
        this.collisionDetector = collisionDetector;
        this.capacityMatcher = capacityMatcher;
    }

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        if (!isEscapeInput(input)) {
            return evaluateGeneric(input);
        }

        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();

        if (resource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak pokoju");
            return DomainAlgorithmResult.failure(List.of("Pokój jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: pokój niedostępny (" + resource.getStatus() + ")");
            return DomainAlgorithmResult.failure(List.of("Pokój nie jest dostępny"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        String difficulty = readString(resource.getMetadata(), "difficulty");
        String theme = readString(resource.getMetadata(), "theme");
        breakdown.addNote("selectedRoom=" + resource.getName());
        breakdown.addNote("difficulty=" + difficulty);
        breakdown.addNote("theme=" + theme);
        breakdown.addRule("ROOM_DIFFICULTY: " + difficulty);
        breakdown.addRule("ROOM_THEME: " + theme);

        List<RequestEntity> activeExisting = activeExistingFor(resource, input.existingRequests());
        boolean hasStart = input.startAt() != null;
        boolean hasEnd = input.endAt() != null;

        if (hasStart ^ hasEnd) {
            errors.add("Zakres rezerwacji jest niekompletny");
            breakdown.addRule("TIME_RANGE_CHECK: niekompletny zakres");
        } else if (hasStart) {
            if (!input.startAt().isBefore(input.endAt())) {
                errors.add("Nieprawidłowy zakres czasu: początek musi być przed końcem");
                breakdown.addRule("TIME_RANGE_CHECK: nieprawidłowy zakres");
            } else {
                List<RequestEntity> collisions =
                        collisionDetector.findCollisions(input.startAt(), input.endAt(), activeExisting);
                if (!collisions.isEmpty()) {
                    errors.add("Pokój jest zajęty w tym terminie: " + ids(collisions));
                    breakdown.addRule("TIME_COLLISION_CHECK: kolizja (" + collisions.size() + ")");
                    breakdown.addNote("alternativeRoom=szukaj innego pokoju o difficulty=" + difficulty
                            + " w wolnym terminie");
                    breakdown.addRule("ALTERNATIVE_ROOM: sugestia pokoju o difficulty=" + difficulty);
                } else {
                    breakdown.addRule("TIME_COLLISION_CHECK: brak kolizji");
                    breakdown.addNote("alternativeRoom=nie wymagana");
                }
            }
        } else if (profile.requiresTimeWindow()) {
            errors.add("Wymagany jest termin rezerwacji");
            breakdown.addRule("TIME_RANGE_CHECK: brak wymaganego zakresu");
        }

        Integer players = readInteger(input.requestMetadata(), "playersCount");
        Integer maxPlayers = readInteger(resource.getMetadata(), "maxPlayers");
        if (players != null && maxPlayers != null && players > maxPlayers) {
            errors.add("Zbyt duża drużyna: " + players + " > limit " + maxPlayers);
            breakdown.addRule("PLAYERS_CHECK: przekroczono (" + players + ">" + maxPlayers + ")");
        } else {
            breakdown.addRule("PLAYERS_CHECK: ok");
        }

        BigDecimal totalPrice = computeEscapeValue(resource, difficulty, input.requestMetadata(), breakdown);

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        breakdown.addRule("TOTAL_PRICE: " + totalPrice.toPlainString());
        return DomainAlgorithmResult.success(totalPrice, resource.getId(), breakdown.build());
    }

    private BigDecimal computeEscapeValue(ResourceEntity resource, String difficulty,
                                          Map<String, Object> request, AlgorithmBreakdownBuilder breakdown) {
        BigDecimal basePrice = resource.getBaseValue() != null ? resource.getBaseValue() : BigDecimal.ZERO;
        breakdown.addLine("basePrice", basePrice, "cena sesji");
        breakdown.addRule("BASE_PRICE: " + basePrice.toPlainString());

        BigDecimal difficultyMultiplier = difficultyMultiplier(difficulty);
        BigDecimal delta = basePrice.multiply(difficultyMultiplier).subtract(basePrice).setScale(2, RoundingMode.HALF_UP);
        if (delta.signum() != 0) {
            breakdown.addLine("difficultyModifier", delta, "difficulty=" + difficulty + " × " + difficultyMultiplier.toPlainString());
        }
        breakdown.addRule("DIFFICULTY_MODIFIER: × " + difficultyMultiplier.toPlainString());

        boolean depositRequired = readBoolean(resource.getMetadata(), "depositRequired");
        boolean depositPaid = readBoolean(request, "depositPaid");
        if (depositRequired) {
            breakdown.addRule("DEPOSIT: wymagana " + DEFAULT_DEPOSIT.toPlainString() + (depositPaid ? " (opłacona)" : " (do zapłaty)"));
            breakdown.addNote("deposit=" + DEFAULT_DEPOSIT.toPlainString() + ";paid=" + depositPaid);
        } else {
            breakdown.addRule("DEPOSIT: nie wymagana");
            breakdown.addNote("deposit=0");
        }

        BigDecimal total = breakdown.total().amount();
        breakdown.addNote("totalPrice=" + total.toPlainString());
        return total;
    }

    private BigDecimal difficultyMultiplier(String difficulty) {
        if (difficulty == null) {
            return BigDecimal.ONE;
        }
        return switch (difficulty.toUpperCase()) {
            case "MEDIUM" -> new BigDecimal("1.10");
            case "HARD" -> new BigDecimal("1.25");
            case "EXPERT" -> new BigDecimal("1.40");
            default -> BigDecimal.ONE;
        };
    }

    private boolean isEscapeInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "teamName", "playersCount", "preferredDifficulty",
                "preferredTheme", "depositPaid")
                || (input.resource() != null && hasAny(input.resource().getMetadata(), "roomName", "difficulty",
                "theme", "durationMinutes", "maxPlayers", "depositRequired"));
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
