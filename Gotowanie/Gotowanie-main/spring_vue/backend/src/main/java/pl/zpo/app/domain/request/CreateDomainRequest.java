package pl.zpo.app.domain.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Payload to create a request against a resource.
 *
 * <p><b>Edit this file to change validation of a request's fixed fields.</b> Whether the time
 * window / quantity are actually required is driven by {@code DomainProfileProvider}
 * ({@code requiresTimeWindow} / {@code requiresQuantity}) and enforced in {@code RequestService}
 * + the algorithm, so the same DTO works for time-based, quantity-based or value-only domains.</p>
 */
public record CreateDomainRequest(
        @NotNull Long resourceId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startAt,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endAt,
        @Positive Integer quantity,
        Map<String, Object> metadata
) {
}
