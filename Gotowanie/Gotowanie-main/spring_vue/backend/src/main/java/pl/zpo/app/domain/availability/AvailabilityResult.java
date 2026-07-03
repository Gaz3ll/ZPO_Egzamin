package pl.zpo.app.domain.availability;

import java.util.List;

/**
 * Result of an availability check for a single resource.
 *
 * @param available              whether the resource can satisfy the request
 * @param message                human-readable reason (especially when unavailable)
 * @param conflictingRequestIds  ids of requests that block availability (empty when available)
 */
public record AvailabilityResult(
        boolean available,
        String message,
        List<Long> conflictingRequestIds
) {

    public AvailabilityResult {
        conflictingRequestIds = conflictingRequestIds == null ? List.of() : List.copyOf(conflictingRequestIds);
    }

    public static AvailabilityResult ok() {
        return new AvailabilityResult(true, "Dostępny", List.of());
    }

    public static AvailabilityResult unavailable(String message, List<Long> conflictingRequestIds) {
        return new AvailabilityResult(false, message, conflictingRequestIds);
    }
}
