package pl.zpo.app.domain.algorithm;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.resource.ResourceEntity;

/**
 * Everything the algorithm needs — pure data, so it can be evaluated with no database and no web
 * layer (see the unit tests). {@code existingRequests} are the currently active requests for the
 * same resource (used for collision and capacity checks).
 *
 * @param resource         the resource being requested (may be {@code null} to represent "missing")
 * @param startAt          requested window start (nullable)
 * @param endAt            requested window end (nullable)
 * @param quantity         requested quantity (nullable)
 * @param requestMetadata  domain-specific request attributes (may hold e.g. "discountPercent")
 * @param existingRequests active requests already booked against the resource
 * @param profile          the active domain profile (mode, pricing unit, currency)
 */
public record DomainAlgorithmInput(
        ResourceEntity resource,
        Instant startAt,
        Instant endAt,
        Integer quantity,
        Map<String, Object> requestMetadata,
        List<RequestEntity> existingRequests,
        DomainProfile profile
) {

    public DomainAlgorithmInput {
        requestMetadata = requestMetadata == null ? Map.of() : Map.copyOf(requestMetadata);
        existingRequests = existingRequests == null ? List.of() : List.copyOf(existingRequests);
    }

    public boolean hasTimeWindow() {
        return startAt != null && endAt != null;
    }
}
