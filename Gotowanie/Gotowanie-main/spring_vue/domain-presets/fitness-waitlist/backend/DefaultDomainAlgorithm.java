package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.availability.CapacityMatcher;
import pl.zpo.app.domain.availability.TimeCollisionDetector;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.request.RequestStatus;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;

@Component
public class DefaultDomainAlgorithm implements DomainAlgorithm {

    private final TimeCollisionDetector collisionDetector;
    private final CapacityMatcher capacityMatcher;

    public DefaultDomainAlgorithm(TimeCollisionDetector c, CapacityMatcher m) {
        this.collisionDetector = c; this.capacityMatcher = m;
    }

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        if (isFitnessInput(input)) return evaluateFitness(input);
        return evaluateGeneric(input);
    }

    private boolean isFitnessInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "userName", "waitlistPosition", "listType")
                || (input.resource() != null && hasAny(input.resource().getMetadata(), "type", "maxCapacity"));
    }

    private DomainAlgorithmResult evaluateFitness(DomainAlgorithmInput input) {
        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();

        ResourceEntity resource = input.resource();
        if (resource == null) {
            return DomainAlgorithmResult.failure(List.of("Zajęcia są wymagane"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            return DomainAlgorithmResult.failure(List.of("Zajęcia nie są dostępne"), breakdown.build());
        }
        breakdown.addRule("CLASS_CHECK: ok");

        Integer maxCapacity = readInteger(resource.getMetadata(), "maxCapacity");
        if (maxCapacity == null || maxCapacity <= 0) {
            maxCapacity = 20; // default
        }

        // Count existing registrations on main list (CONFIRMED = main list)
        List<RequestEntity> existing = input.existingRequests() != null ? input.existingRequests() : new ArrayList<>();
        long mainListCount = existing.stream()
                .filter(r -> r.getStatus() == RequestStatus.CONFIRMED)
                .count();
        long waitlistCount = existing.stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING)
                .count();

        breakdown.addRule("CAPACITY: " + mainListCount + "/" + maxCapacity + " na liscie glownej");
        breakdown.addRule("WAITLIST: " + waitlistCount + " na liscie rezerwowej");

        if (mainListCount >= maxCapacity) {
            int position = (int) waitlistCount + 1;
            breakdown.addRule("WAITLIST_ASSIGN: pozycja " + position + " na liscie rezerwowej");
            breakdown.addNote("waitlistPosition=" + position);
            // The request will be saved as PENDING with waitlist metadata
            // Admin can promote to CONFIRMED when spot opens
        } else {
            breakdown.addRule("MAINLIST_ASSIGN: miejsce " + (mainListCount + 1) + " na liscie glownej");
            breakdown.addNote("waitlistPosition=0");
        }

        return DomainAlgorithmResult.success(BigDecimal.ZERO, resource.getId(), breakdown.build());
    }

    private DomainAlgorithmResult evaluateGeneric(DomainAlgorithmInput input) {
        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();
        if (resource == null) return DomainAlgorithmResult.failure(List.of("Zasób jest wymagany"), breakdown.build());
        if (resource.getStatus() != ResourceStatus.ACTIVE)
            return DomainAlgorithmResult.failure(List.of("Zasób nie jest aktywny"), breakdown.build());
        breakdown.addRule("RESOURCE_CHECK: ok");
        if (!errors.isEmpty()) return DomainAlgorithmResult.failure(errors, breakdown.build());
        return DomainAlgorithmResult.success(null, resource.getId(), breakdown.build());
    }

    private Integer readInteger(Map<String, Object> m, String k) {
        if (m == null || m.get(k) == null) return null;
        Object v = m.get(k);
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (NumberFormatException e) { return null; }
    }

    private boolean hasAny(Map<String, Object> m, String... keys) {
        if (m == null) return false;
        for (String k : keys) if (m.containsKey(k)) return true;
        return false;
    }
}
