package pl.zpo.app.domain.resource;

import java.util.HashMap;
import org.springframework.stereotype.Component;

/** Maps between {@link ResourceEntity} and its DTOs. Keeps mapping out of services/controllers. */
@Component
public class ResourceMapper {

    public ResourceDto toDto(ResourceEntity entity) {
        return new ResourceDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getType(),
                entity.getStatus(),
                entity.getBaseValue(),
                entity.getCapacityValue(),
                entity.getMetadata(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public ResourceEntity fromCreate(CreateResourceRequest request) {
        ResourceEntity entity = new ResourceEntity();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setType(request.type());
        entity.setStatus(request.status() != null ? request.status() : ResourceStatus.ACTIVE);
        entity.setBaseValue(request.baseValue());
        entity.setCapacityValue(request.capacityValue());
        entity.setMetadata(request.metadata() != null ? new HashMap<>(request.metadata()) : new HashMap<>());
        return entity;
    }

    public void applyUpdate(ResourceEntity entity, UpdateResourceRequest request) {
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setType(request.type());
        entity.setStatus(request.status());
        entity.setBaseValue(request.baseValue());
        entity.setCapacityValue(request.capacityValue());
        entity.setMetadata(request.metadata() != null ? new HashMap<>(request.metadata()) : new HashMap<>());
    }
}
