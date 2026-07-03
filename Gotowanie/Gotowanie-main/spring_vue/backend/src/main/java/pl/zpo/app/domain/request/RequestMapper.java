package pl.zpo.app.domain.request;

import org.springframework.stereotype.Component;

/**
 * Maps {@link RequestEntity} to {@link RequestDto}. The denormalized display fields
 * ({@code resourceName}, {@code ownerEmail}) and {@code currency} are supplied by the caller,
 * which resolves them in batch to avoid N+1 lookups.
 */
@Component
public class RequestMapper {

    public RequestDto toDto(RequestEntity entity, String resourceName, String ownerEmail, String currency) {
        return new RequestDto(
                entity.getId(),
                entity.getOwnerId(),
                ownerEmail,
                entity.getResourceId(),
                resourceName,
                entity.getStatus(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getQuantity(),
                entity.getCalculatedValue(),
                currency,
                entity.getMetadata(),
                entity.getAlgorithmBreakdown(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
