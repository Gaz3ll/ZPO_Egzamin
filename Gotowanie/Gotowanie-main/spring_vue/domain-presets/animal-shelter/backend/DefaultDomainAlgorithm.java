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
        if (!isShelterInput(input)) {
            return evaluateGeneric(input);
        }
        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();
        if (resource == null) {
            return DomainAlgorithmResult.failure(List.of("Zwierzę jest wymagane"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            return DomainAlgorithmResult.failure(List.of("Zwierzę nie jest dostępne do adopcji"), breakdown.build());
        }

        int matchingScore = 0;
        String species = readString(resource.getMetadata(), "species");
        String preferredSpecies = readString(input.requestMetadata(), "preferredSpecies");
        if (species != null && species.equalsIgnoreCase(preferredSpecies)) {
            matchingScore += 40;
            breakdown.addRule("SPECIES_MATCH: ok");
        } else {
            errors.add("Preferowany gatunek nie pasuje do zwierzęcia");
            breakdown.addRule("SPECIES_MATCH: mismatch");
        }

        boolean needsGarden = readBoolean(resource.getMetadata(), "needsGarden");
        boolean hasGarden = readBoolean(input.requestMetadata(), "hasGarden");
        if (needsGarden && !hasGarden) {
            errors.add("To zwierzę wymaga domu z ogrodem");
            breakdown.addRule("GARDEN_REQUIREMENT: failed");
        } else {
            matchingScore += needsGarden ? 20 : 10;
            breakdown.addRule("GARDEN_REQUIREMENT: ok");
        }

        String temperament = readString(resource.getMetadata(), "temperament");
        String experience = readString(input.requestMetadata(), "experienceLevel");
        int experienceScore = experienceScore(temperament, experience);
        matchingScore += experienceScore;
        breakdown.addRule("EXPERIENCE_SCORE: " + experienceScore);

        BigDecimal monthlyCost = readDecimal(resource.getMetadata(), "monthlyCost");
        monthlyCost = monthlyCost == null ? BigDecimal.ZERO : monthlyCost;
        BigDecimal budget = readDecimal(input.requestMetadata(), "budgetMonthly");
        budget = budget == null ? BigDecimal.ZERO : budget;
        if (budget.compareTo(monthlyCost) < 0) {
            errors.add("Budżet miesięczny jest niższy niż szacowany koszt opieki");
            breakdown.addRule("BUDGET_CHECK: too low");
        } else {
            matchingScore += 20;
            breakdown.addRule("BUDGET_CHECK: ok");
        }

        breakdown.addRule("MATCHING_SCORE: " + matchingScore);
        breakdown.addNote("matchingScore=" + matchingScore);
        breakdown.addNote("monthlyCost=" + monthlyCost.toPlainString());
        BigDecimal adoptionFee = resource.getBaseValue() == null ? BigDecimal.ZERO : resource.getBaseValue();
        breakdown.addLine("adoptionFee", adoptionFee, "opłata adopcyjna");
        BigDecimal starterPack = new BigDecimal("50.00");
        breakdown.addLine("starterPack", starterPack, "pakiet startowy");
        BigDecimal total = breakdown.total().amount();
        breakdown.addNote("totalPrice=" + total.toPlainString());

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        return DomainAlgorithmResult.success(total, resource.getId(), breakdown.build());
    }

    private int experienceScore(String temperament, String experience) {
        String t = temperament == null ? "" : temperament.toUpperCase();
        String e = experience == null ? "" : experience.toUpperCase();
        if ("ACTIVE".equals(t) && "LOW".equals(e)) {
            return 0;
        }
        if ("SHY".equals(t) && "HIGH".equals(e)) {
            return 30;
        }
        return "LOW".equals(e) ? 10 : 25;
    }

    private boolean isShelterInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "adopterName", "preferredSpecies", "homeType", "hasGarden",
                "experienceLevel", "budgetMonthly")
                || (input.resource() != null && hasAny(input.resource().getMetadata(), "animalName",
                "species", "size", "temperament", "needsGarden", "monthlyCost"));
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
            value = computeGenericValue(resource, units, qty, profile, breakdown);
        }
        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        return DomainAlgorithmResult.success(value, resource.getId(), breakdown.build());
    }

    private BigDecimal computeGenericValue(ResourceEntity resource, long units, int qty, DomainProfile profile,
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
}
