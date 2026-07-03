package pl.zpo.app.support;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import pl.zpo.app.domain.config.AlgorithmMode;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.config.PricingUnit;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.request.RequestStatus;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;
import pl.zpo.app.security.CurrentUser;
import pl.zpo.app.users.Role;

/** Builders for pure unit tests (no Spring, no database). */
public final class TestFixtures {

    public static final Instant T10 = Instant.parse("2026-07-01T10:00:00Z");
    public static final Instant T11 = Instant.parse("2026-07-01T11:00:00Z");
    public static final Instant T12 = Instant.parse("2026-07-01T12:00:00Z");
    public static final Instant T13 = Instant.parse("2026-07-01T13:00:00Z");
    public static final Instant T14 = Instant.parse("2026-07-01T14:00:00Z");
    public static final Instant T16 = Instant.parse("2026-07-01T16:00:00Z");

    private TestFixtures() {
    }

    public static DomainProfile profile(AlgorithmMode mode, PricingUnit unit,
                                        boolean requiresTimeWindow, boolean requiresQuantity) {
        return new DomainProfile(
                "Test", "Zasób", "Zasoby", "Zgłoszenie", "Zgłoszenia", "PLN",
                mode, unit, requiresTimeWindow, requiresQuantity, List.of(), List.of());
    }

    public static ResourceEntity resource(Long id, ResourceStatus status,
                                          BigDecimal baseValue, Integer capacity,
                                          Map<String, Object> metadata) {
        ResourceEntity resource = new ResourceEntity();
        resource.setId(id);
        resource.setName("Resource " + id);
        resource.setStatus(status);
        resource.setBaseValue(baseValue);
        resource.setCapacityValue(capacity);
        if (metadata != null) {
            resource.setMetadata(metadata);
        }
        return resource;
    }

    public static RequestEntity request(Long id, Long resourceId, Instant start, Instant end,
                                        Integer quantity, RequestStatus status) {
        RequestEntity request = new RequestEntity();
        request.setId(id);
        request.setOwnerId(99L);
        request.setResourceId(resourceId);
        request.setStartAt(start);
        request.setEndAt(end);
        request.setQuantity(quantity);
        request.setStatus(status);
        return request;
    }

    public static CurrentUser currentUser(Long id, Role role) {
        return new CurrentUser(id, "user" + id + "@zpo.local", role);
    }
}
