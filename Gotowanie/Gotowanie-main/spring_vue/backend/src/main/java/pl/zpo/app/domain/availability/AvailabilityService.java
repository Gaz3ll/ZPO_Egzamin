package pl.zpo.app.domain.availability;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.request.RequestRepository;
import pl.zpo.app.domain.request.RequestStatus;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceRepository;
import pl.zpo.app.domain.resource.ResourceStatus;

/**
 * Read-side availability: which active resources can satisfy a (optionally time-bounded, optionally
 * quantity-bounded) request. Powers {@code GET /api/resources/available}. It reuses the same
 * {@link TimeCollisionDetector} and {@link CapacityMatcher} as the algorithm, so the answer shown
 * when browsing matches what the algorithm enforces when a request is actually created.
 */
@Service
@Transactional(readOnly = true)
public class AvailabilityService {

    private final ResourceRepository resourceRepository;
    private final RequestRepository requestRepository;
    private final TimeCollisionDetector collisionDetector;
    private final CapacityMatcher capacityMatcher;
    private final DomainProfile profile;

    public AvailabilityService(ResourceRepository resourceRepository,
                               RequestRepository requestRepository,
                               TimeCollisionDetector collisionDetector,
                               CapacityMatcher capacityMatcher,
                               DomainProfile profile) {
        this.resourceRepository = resourceRepository;
        this.requestRepository = requestRepository;
        this.collisionDetector = collisionDetector;
        this.capacityMatcher = capacityMatcher;
        this.profile = profile;
    }

    public List<ResourceEntity> listActiveResources() {
        return resourceRepository.findAllByStatusOrderByNameAsc(ResourceStatus.ACTIVE);
    }

    /** Active resources that can satisfy the given (optional) window and quantity. */
    public List<ResourceEntity> findAvailable(Instant start, Instant end, Integer quantity) {
        return listActiveResources().stream()
                .filter(resource -> check(resource, start, end, quantity).available())
                .toList();
    }

    /** Availability of a single resource for the given window/quantity. */
    public AvailabilityResult check(ResourceEntity resource, Instant start, Instant end, Integer quantity) {
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            return AvailabilityResult.unavailable("Zasób jest nieaktywny", List.of());
        }

        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(
                resource.getId(), RequestStatus.activeStatuses());

        boolean hasWindow = start != null && end != null;

        if (profile.algorithmMode().checksTime() && hasWindow) {
            List<RequestEntity> collisions = collisionDetector.findCollisions(start, end, existing);
            if (!collisions.isEmpty()) {
                return AvailabilityResult.unavailable(
                        "Termin koliduje z istniejącymi zgłoszeniami",
                        collisions.stream().map(RequestEntity::getId).toList());
            }
        }

        if (profile.algorithmMode().checksCapacity() && quantity != null && resource.getCapacityValue() != null) {
            List<RequestEntity> relevant = hasWindow
                    ? collisionDetector.findCollisions(start, end, existing)
                    : existing;
            int used = capacityMatcher.usedCapacity(relevant);
            if (!capacityMatcher.fits(resource.getCapacityValue(), used, quantity)) {
                return AvailabilityResult.unavailable(
                        "Brak wystarczającej pojemności (użyte %d/%d, żądane %d)"
                                .formatted(used, resource.getCapacityValue(), quantity),
                        relevant.stream().map(RequestEntity::getId).toList());
            }
        }

        return AvailabilityResult.ok();
    }
}
