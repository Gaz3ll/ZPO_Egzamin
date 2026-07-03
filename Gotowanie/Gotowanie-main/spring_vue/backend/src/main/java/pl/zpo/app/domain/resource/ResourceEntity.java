package pl.zpo.app.domain.resource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

/**
 * A generic <em>resource</em> that requests are made against (a car, a locker, a seat,
 * a doctor, a room, ...). Domain-specific attributes live in the {@link #metadata} JSONB
 * column so the schema never has to change per subject.
 */
@Entity
@Table(
        name = "resources",
        indexes = {
                @Index(name = "ix_resources_status", columnList = "status"),
                @Index(name = "ix_resources_type", columnList = "type")
        }
)
public class ResourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    /** Domain-defined category/kind (e.g. "SEDAN", "LARGE_LOCKER", "VIP"). */
    @Column(length = 100)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResourceStatus status = ResourceStatus.ACTIVE;

    /** Base unit value used by the algorithm (e.g. price per hour/day/item). */
    @Column(name = "base_value", precision = 15, scale = 2)
    private BigDecimal baseValue;

    /** Capacity of the resource (e.g. seats, locker size, max simultaneous bookings). */
    @Column(name = "capacity_value")
    private Integer capacityValue;

    /** Free-form domain attributes stored as PostgreSQL JSONB. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ResourceEntity() {
        // required by JPA; also used by mappers and the seed loader
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public BigDecimal getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(BigDecimal baseValue) {
        this.baseValue = baseValue;
    }

    public Integer getCapacityValue() {
        return capacityValue;
    }

    public void setCapacityValue(Integer capacityValue) {
        this.capacityValue = capacityValue;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata == null ? new HashMap<>() : metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
