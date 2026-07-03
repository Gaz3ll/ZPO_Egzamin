package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
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
 * Meal planning catering algorithm.
 *
 * <p>Resource = a single meal in the menu, Request = a weekly diet plan built from it. The
 * algorithm filters the meal by diet type (VEGE also accepts vegan meals), rejects meals whose
 * allergens intersect the excluded set, applies the portion multiplier (0.5 / 1.0 / 2.0), checks
 * the required portions against {@code capacityValue}, compares the resulting daily calories with
 * {@code targetCalories} (±300 kcal tolerance) and prices the plan:
 * {@code baseValue × portionMultiplier × mealsPerDay × daysCount}.</p>
 */
@Component
public class DefaultDomainAlgorithm implements DomainAlgorithm {

    private static final int CALORIE_TOLERANCE = 300;

    private final TimeCollisionDetector collisionDetector;
    private final CapacityMatcher capacityMatcher;

    public DefaultDomainAlgorithm(TimeCollisionDetector collisionDetector, CapacityMatcher capacityMatcher) {
        this.collisionDetector = collisionDetector;
        this.capacityMatcher = capacityMatcher;
    }

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        if (!isCateringInput(input)) {
            return evaluateGeneric(input);
        }

        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();

        if (resource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak posiłku");
            return DomainAlgorithmResult.failure(List.of("Posiłek jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: posiłek niedostępny (" + resource.getStatus() + ")");
            return DomainAlgorithmResult.failure(List.of("Posiłek nie jest dostępny"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");
        breakdown.addNote("selectedMeals=" + resource.getName());

        String requestedDiet = readString(input.requestMetadata(), "dietType");
        breakdown.addNote("dietType=" + requestedDiet);
        if (!dietCompatible(requestedDiet, resource)) {
            errors.add("Posiłek '" + resource.getName() + "' nie pasuje do diety " + requestedDiet
                    + " — brak posiłków dla tej diety w wybranym menu");
            breakdown.addRule("DIET_FILTER: brak dopasowania (" + requestedDiet + ")");
        } else {
            breakdown.addRule("DIET_FILTER: ok" + (requestedDiet == null ? "" : " (" + requestedDiet + ")"));
        }

        Set<String> mealAllergens = parseCsv(readString(resource.getMetadata(), "allergens"));
        Set<String> excluded = parseCsv(readString(input.requestMetadata(), "excludedAllergens"));
        Set<String> conflicting = new LinkedHashSet<>(mealAllergens);
        conflicting.retainAll(excluded);
        breakdown.addNote("allergenRules=wykluczone " + excluded);
        if (!conflicting.isEmpty()) {
            errors.add("Posiłek zawiera wykluczone alergeny: " + conflicting);
            breakdown.addRule("ALLERGEN_FILTER: konflikt " + conflicting);
        } else {
            breakdown.addRule("ALLERGEN_FILTER: ok");
        }

        BigDecimal portionMultiplier = readPortionMultiplier(input.requestMetadata());
        if (portionMultiplier == null) {
            errors.add("Nieprawidłowa porcja — dozwolone 0.5, 1.0 albo 2.0");
            breakdown.addRule("PORTION_MULTIPLIER: nieprawidłowa");
            portionMultiplier = BigDecimal.ONE;
        } else {
            breakdown.addRule("PORTION_MULTIPLIER: × " + portionMultiplier.toPlainString());
        }
        breakdown.addNote("portionMultiplier=" + portionMultiplier.toPlainString());

        int mealsPerDay = Math.max(1, orDefault(readInteger(input.requestMetadata(), "mealsPerDay"), 3));
        int daysCount = Math.max(1, orDefault(readInteger(input.requestMetadata(), "daysCount"), 7));
        int totalPortions = mealsPerDay * daysCount;
        breakdown.addRule("MEALS_PER_DAY: " + mealsPerDay + " × " + daysCount + " dni = " + totalPortions + " porcji");

        Integer availablePortions = resource.getCapacityValue();
        if (availablePortions != null && totalPortions > availablePortions) {
            errors.add("Za mało dostępnych porcji: potrzeba " + totalPortions
                    + ", dostępne " + availablePortions);
            breakdown.addRule("CAPACITY_CHECK: przekroczono (" + totalPortions + ">" + availablePortions + ")");
        } else {
            breakdown.addRule("CAPACITY_CHECK: ok (" + totalPortions + "/"
                    + (availablePortions == null ? "∞" : availablePortions) + ")");
        }

        int mealCalories = Math.max(0, orDefault(readInteger(resource.getMetadata(), "calories"), 0));
        BigDecimal portionCalories = BigDecimal.valueOf(mealCalories).multiply(portionMultiplier);
        BigDecimal dailyCalories = portionCalories.multiply(BigDecimal.valueOf(mealsPerDay))
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal weeklyCalories = dailyCalories.multiply(BigDecimal.valueOf(daysCount));
        breakdown.addRule("DAILY_CALORIES: " + dailyCalories.toPlainString() + " kcal");
        breakdown.addRule("WEEKLY_CALORIES: " + weeklyCalories.toPlainString() + " kcal");
        breakdown.addNote("dailyCalories=" + dailyCalories.toPlainString());
        breakdown.addNote("weeklyCalories=" + weeklyCalories.toPlainString());

        Integer targetCalories = readInteger(input.requestMetadata(), "targetCalories");
        breakdown.addNote("targetCalories=" + targetCalories);
        if (targetCalories != null && targetCalories > 0) {
            BigDecimal difference = dailyCalories.subtract(BigDecimal.valueOf(targetCalories));
            breakdown.addNote("calorieDifference=" + difference.toPlainString());
            if (difference.abs().compareTo(BigDecimal.valueOf(CALORIE_TOLERANCE)) > 0) {
                breakdown.addRule("CALORIE_MATCH: poza tolerancją ±" + CALORIE_TOLERANCE
                        + " kcal (różnica " + difference.toPlainString() + ")");
                breakdown.addNote("Zmień porcję (0.5 / 2.0) albo liczbę posiłków, żeby zbliżyć się do celu");
            } else {
                breakdown.addRule("CALORIE_MATCH: ok (różnica " + difference.toPlainString() + " kcal)");
            }
        }

        BigDecimal basePrice = resource.getBaseValue() != null ? resource.getBaseValue() : BigDecimal.ZERO;
        BigDecimal planBase = basePrice.multiply(BigDecimal.valueOf(totalPortions))
                .setScale(2, RoundingMode.HALF_UP);
        breakdown.addLine("basePrice", planBase,
                basePrice.toPlainString() + " PLN × " + totalPortions + " porcji");
        BigDecimal portionDelta = planBase.multiply(portionMultiplier).subtract(planBase)
                .setScale(2, RoundingMode.HALF_UP);
        if (portionDelta.signum() != 0) {
            breakdown.addLine("portionAdjustment", portionDelta, "porcja × " + portionMultiplier.toPlainString());
        }
        BigDecimal totalPrice = breakdown.total().amount();
        breakdown.addRule("TOTAL_PRICE: " + totalPrice.toPlainString());

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        return DomainAlgorithmResult.success(totalPrice, resource.getId(), breakdown.build());
    }

    /** VEGE plans also accept vegan meals; VEGAN requires a vegan meal. */
    private boolean dietCompatible(String requestedDiet, ResourceEntity resource) {
        if (requestedDiet == null) {
            return true;
        }
        String mealDiet = readString(resource.getMetadata(), "dietType");
        String requested = requestedDiet.toUpperCase(Locale.ROOT);
        String meal = mealDiet == null ? "" : mealDiet.toUpperCase(Locale.ROOT);
        if (requested.equals(meal)) {
            return true;
        }
        boolean isVegan = readBoolean(resource.getMetadata(), "isVegan") || "VEGAN".equals(meal);
        boolean isVegetarian = readBoolean(resource.getMetadata(), "isVegetarian") || isVegan
                || "VEGE".equals(meal);
        return switch (requested) {
            case "VEGE" -> isVegetarian;
            case "VEGAN" -> isVegan;
            default -> false;
        };
    }

    private BigDecimal readPortionMultiplier(Map<String, Object> metadata) {
        Object raw = metadata == null ? null : metadata.get("portionMultiplier");
        if (raw == null) {
            return BigDecimal.ONE;
        }
        BigDecimal value;
        try {
            value = new BigDecimal(String.valueOf(raw));
        } catch (NumberFormatException ex) {
            return null;
        }
        for (String allowed : List.of("0.5", "1.0", "2.0")) {
            if (value.compareTo(new BigDecimal(allowed)) == 0) {
                return value;
            }
        }
        return null;
    }

    private boolean isCateringInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "targetCalories", "mealsPerDay", "portionMultiplier",
                "excludedAllergens", "daysCount", "preferredMealTypes")
                || (input.resource() != null && hasAny(input.resource().getMetadata(),
                "mealType", "dietType", "calories", "allergens", "portionOptions"));
    }

    private Set<String> parseCsv(String value) {
        Set<String> result = new LinkedHashSet<>();
        if (value == null) {
            return result;
        }
        Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .forEach(result::add);
        return result;
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

    private int orDefault(Integer value, int fallback) {
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
