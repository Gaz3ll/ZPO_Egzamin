package pl.zpo.app.domain.request;

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
import pl.zpo.app.domain.algorithm.AlgorithmBreakdown;

/**
 * A generic <em>request</em> a user makes against a resource (a reservation, a booking,
 * an appointment, a parcel, ...).
 *
 * <p>Relations (foreign-key columns with indexes):</p>
 * <ul>
 *   <li>{@code owner_id} -&gt; users.id  (N requests -&gt; 1 user)</li>
 *   <li>{@code resource_id} -&gt; resources.id  (N requests -&gt; 1 resource)</li>
 * </ul>
 *
 * <p>Domain-specific attributes live in {@link #metadata}; the algorithm's explanation is
 * persisted in {@link #algorithmBreakdown}. Both are PostgreSQL JSONB columns.</p>
 */
@Entity
@Table(
        name = "requests",
        indexes = {
                @Index(name = "ix_requests_owner", columnList = "owner_id"),
                @Index(name = "ix_requests_resource", columnList = "resource_id"),
                @Index(name = "ix_requests_status", columnList = "status"),
                @Index(name = "ix_requests_time", columnList = "start_at, end_at")
        }
)
public class RequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    private Integer quantity;

    @Column(name = "calculated_value", precision = 15, scale = 2)
    private BigDecimal calculatedValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "algorithm_breakdown_json", columnDefinition = "jsonb")
    private AlgorithmBreakdown algorithmBreakdown;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public RequestEntity() {
        // required by JPA; also used by services and the seed loader
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public void setEndAt(Instant endAt) {
        this.endAt = endAt;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getCalculatedValue() {
        return calculatedValue;
    }

    public void setCalculatedValue(BigDecimal calculatedValue) {
        this.calculatedValue = calculatedValue;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata == null ? new HashMap<>() : metadata;
    }

    public AlgorithmBreakdown getAlgorithmBreakdown() {
        return algorithmBreakdown;
    }

    public void setAlgorithmBreakdown(AlgorithmBreakdown algorithmBreakdown) {
        this.algorithmBreakdown = algorithmBreakdown;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
