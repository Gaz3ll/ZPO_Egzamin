package pl.zpo.app.domain.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.availability.CapacityMatcher;
import pl.zpo.app.domain.availability.TimeCollisionDetector;
import pl.zpo.app.domain.config.DomainProfile;
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
        if (isLibraryInput(input)) return evaluateLibrary(input);
        return evaluateGeneric(input);
    }

    private boolean isLibraryInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "borrowerName")
                || (input.resource() != null && hasAny(input.resource().getMetadata(),
                "author", "isbn", "totalCopies", "availableCopies"));
    }

    private DomainAlgorithmResult evaluateLibrary(DomainAlgorithmInput input) {
        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();

        ResourceEntity resource = input.resource();
        if (resource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak ksiazki");
            return DomainAlgorithmResult.failure(List.of("Ksiazka jest wymagana"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: ksiazka niedostepna");
            return DomainAlgorithmResult.failure(List.of("Ksiazka nie jest dostepna"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        Integer available = readInteger(resource.getMetadata(), "availableCopies");
        if (available == null || available <= 0) {
            errors.add("Brak dostepnych egzemplarzy tej ksiazki");
            breakdown.addRule("STOCK_CHECK: brak egzemplarzy (availableCopies=" + available + ")");
        } else {
            breakdown.addRule("STOCK_CHECK: ok (dostepne: " + available + ")");
        }

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        breakdown.addRule("WYPOZYCZENIE_OK");
        return DomainAlgorithmResult.success(null, resource.getId(), breakdown.build());
    }

    // ----- generic fallback -----

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
            breakdown.addRule("RESOURCE_CHECK: zasób nieaktywny");
            return DomainAlgorithmResult.failure(List.of("Zasób nie jest aktywny"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        List<RequestEntity> activeExisting = activeExistingFor(resource, input.existingRequests());
        long durationUnits = 1;
        boolean hasStart = input.startAt() != null;
        boolean hasEnd = input.endAt() != null;

        if (profile.algorithmMode().checksTime() || hasStart || hasEnd) {
            if (hasStart ^ hasEnd) {
                errors.add("Zakres dat jest niekompletny");
                breakdown.addRule("TIME_RANGE_CHECK: niekompletny zakres");
            } else if (hasStart) {
                if (!input.startAt().isBefore(input.endAt())) {
                    errors.add("Nieprawidlowy zakres dat");
                    breakdown.addRule("TIME_RANGE_CHECK: nieprawidlowy zakres");
                } else {
                    if (profile.algorithmMode().checksTime()) {
                        List<RequestEntity> collisions =
                                collisionDetector.findCollisions(input.startAt(), input.endAt(), activeExisting);
                        if (!collisions.isEmpty()) {
                            errors.add("Termin koliduje z istniejacymi: " + ids(collisions));
                            breakdown.addRule("TIME_COLLISION_CHECK: kolizja");
                        } else {
                            breakdown.addRule("TIME_COLLISION_CHECK: brak kolizji");
                        }
                    }
                }
            }
        }

        if (!errors.isEmpty()) return DomainAlgorithmResult.failure(errors, breakdown.build());
        return DomainAlgorithmResult.success(null, resource.getId(), breakdown.build());
    }

    private List<RequestEntity> activeExistingFor(ResourceEntity resource, List<RequestEntity> existing) {
        List<RequestEntity> result = new ArrayList<>();
        for (RequestEntity r : existing) {
            boolean active = r.getStatus() != null && r.getStatus().isActive();
            boolean same = resource.getId() == null || r.getResourceId() == null
                    || resource.getId().equals(r.getResourceId());
            if (active && same) result.add(r);
        }
        return result;
    }

    private String ids(List<RequestEntity> requests) {
        return requests.stream().map(r -> String.valueOf(r.getId())).toList().toString();
    }

    private Integer readInteger(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) return null;
        Object v = metadata.get(key);
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (NumberFormatException e) { return null; }
    }

    private boolean hasAny(Map<String, Object> metadata, String... keys) {
        if (metadata == null) return false;
        for (String k : keys) if (metadata.containsKey(k)) return true;
        return false;
    }
}
