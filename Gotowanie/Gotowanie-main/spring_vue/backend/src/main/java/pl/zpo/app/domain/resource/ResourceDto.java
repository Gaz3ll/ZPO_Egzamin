package pl.zpo.app.domain.resource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/** API representation of a resource, including its dynamic {@code metadata}. */
public record ResourceDto(
        Long id,
        String name,
        String description,
        String type,
        ResourceStatus status,
        BigDecimal baseValue,
        Integer capacityValue,
        Map<String, Object> metadata,
        Instant createdAt,
        Instant updatedAt
) {
}
