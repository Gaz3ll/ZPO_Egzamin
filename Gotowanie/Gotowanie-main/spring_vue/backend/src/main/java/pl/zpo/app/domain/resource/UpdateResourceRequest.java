package pl.zpo.app.domain.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Map;

/** Payload to update a resource (ADMIN). All core fields are replaced; status is required. */
public record UpdateResourceRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 2000) String description,
        @Size(max = 100) String type,
        @NotNull ResourceStatus status,
        @PositiveOrZero BigDecimal baseValue,
        @PositiveOrZero Integer capacityValue,
        Map<String, Object> metadata
) {
}
