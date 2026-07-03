package pl.zpo.app.domain.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Payload to create a resource (ADMIN).
 *
 * <p><b>Edit this file to change validation of a resource's fixed fields</b> (name, type,
 * baseValue, capacity). Domain-specific attributes go in {@code metadata} and are validated
 * against {@code DomainProfileProvider} by {@code DomainFieldValidator}.</p>
 */
public record CreateResourceRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 2000) String description,
        @Size(max = 100) String type,
        ResourceStatus status,
        @PositiveOrZero BigDecimal baseValue,
        @PositiveOrZero Integer capacityValue,
        Map<String, Object> metadata
) {
}
