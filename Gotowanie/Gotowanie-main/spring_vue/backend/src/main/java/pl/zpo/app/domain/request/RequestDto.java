package pl.zpo.app.domain.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import pl.zpo.app.domain.algorithm.AlgorithmBreakdown;

/**
 * API representation of a request. Includes denormalized {@code resourceName}/{@code ownerEmail}
 * for display and the persisted {@link AlgorithmBreakdown} so the UI can explain the result.
 */
public record RequestDto(
        Long id,
        Long ownerId,
        String ownerEmail,
        Long resourceId,
        String resourceName,
        RequestStatus status,
        Instant startAt,
        Instant endAt,
        Integer quantity,
        BigDecimal calculatedValue,
        String currency,
        Map<String, Object> metadata,
        AlgorithmBreakdown algorithmBreakdown,
        Instant createdAt,
        Instant updatedAt
) {
}
