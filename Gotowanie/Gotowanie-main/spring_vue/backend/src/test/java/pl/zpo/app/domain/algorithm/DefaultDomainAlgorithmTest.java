package pl.zpo.app.domain.algorithm;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.zpo.app.support.TestFixtures.T10;
import static pl.zpo.app.support.TestFixtures.T11;
import static pl.zpo.app.support.TestFixtures.T12;
import static pl.zpo.app.support.TestFixtures.T13;
import static pl.zpo.app.support.TestFixtures.T14;
import static pl.zpo.app.support.TestFixtures.T16;
import static pl.zpo.app.support.TestFixtures.profile;
import static pl.zpo.app.support.TestFixtures.request;
import static pl.zpo.app.support.TestFixtures.resource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.zpo.app.domain.availability.CapacityMatcher;
import pl.zpo.app.domain.availability.TimeCollisionDetector;
import pl.zpo.app.domain.config.AlgorithmMode;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.config.PricingUnit;
import pl.zpo.app.domain.request.RequestStatus;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;

/**
 * Unit tests for the core domain algorithm — pure logic, no Spring, no database.
 * Covers time collision (1-4), capacity (5-6), value calculation (7-8) and input errors (9-10).
 */
class DefaultDomainAlgorithmTest {

    private final DefaultDomainAlgorithm algorithm =
            new DefaultDomainAlgorithm(new TimeCollisionDetector(), new CapacityMatcher());

    private final DomainProfile timeProfile =
            profile(AlgorithmMode.TIME_AVAILABILITY_AND_CALCULATION, PricingUnit.PER_HOUR, true, false);

    @Test
    @DisplayName("No time collision → success with hourly value")
    void noTimeCollision() {
        ResourceEntity resource = resource(1L, ResourceStatus.ACTIVE, new BigDecimal("100.00"), null, null);
        var existing = List.of(request(10L, 1L, T14, T16, null, RequestStatus.CONFIRMED));

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, T10, T12, null, Map.of(), existing, timeProfile));

        assertThat(result.success()).isTrue();
        assertThat(result.errors()).isEmpty();
        assertThat(result.calculatedValue()).isEqualByComparingTo("200.00"); // 100 × 2h
        assertThat(result.assignedResourceId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Overlapping window → collision detected → failure")
    void timeCollisionDetected() {
        ResourceEntity resource = resource(1L, ResourceStatus.ACTIVE, new BigDecimal("100.00"), null, null);
        var existing = List.of(request(10L, 1L, T11, T13, null, RequestStatus.CONFIRMED));

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, T10, T12, null, Map.of(), existing, timeProfile));

        assertThat(result.success()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }

    @Test
    @DisplayName("Touching end==start → no collision")
    void touchingEndEqualsStart() {
        ResourceEntity resource = resource(1L, ResourceStatus.ACTIVE, new BigDecimal("100.00"), null, null);
        var existing = List.of(request(10L, 1L, T12, T14, null, RequestStatus.CONFIRMED));

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, T10, T12, null, Map.of(), existing, timeProfile));

        assertThat(result.success()).isTrue();
    }

    @Test
    @DisplayName("Touching start==end → no collision")
    void touchingStartEqualsEnd() {
        ResourceEntity resource = resource(1L, ResourceStatus.ACTIVE, new BigDecimal("100.00"), null, null);
        var existing = List.of(request(10L, 1L, T10, T12, null, RequestStatus.CONFIRMED));

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, T12, T14, null, Map.of(), existing, timeProfile));

        assertThat(result.success()).isTrue();
    }

    @Test
    @DisplayName("Quantity fits capacity → success")
    void quantityFitsCapacity() {
        DomainProfile capacityProfile =
                profile(AlgorithmMode.CAPACITY_MATCHING, PricingUnit.PER_UNIT, false, true);
        ResourceEntity resource = resource(1L, ResourceStatus.ACTIVE, new BigDecimal("10.00"), 5, null);

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, null, null, 3, Map.of(), List.of(), capacityProfile));

        assertThat(result.success()).isTrue();
        assertThat(result.calculatedValue()).isEqualByComparingTo("30.00"); // 10 × 3
    }

    @Test
    @DisplayName("Quantity exceeds capacity → failure")
    void quantityExceedsCapacity() {
        DomainProfile capacityProfile =
                profile(AlgorithmMode.CAPACITY_MATCHING, PricingUnit.PER_UNIT, false, true);
        ResourceEntity resource = resource(1L, ResourceStatus.ACTIVE, new BigDecimal("10.00"), 4, null);

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, null, null, 5, Map.of(), List.of(), capacityProfile));

        assertThat(result.success()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }

    @Test
    @DisplayName("calculatedValue for a simple case")
    void simpleValue() {
        DomainProfile valueProfile =
                profile(AlgorithmMode.VALUE_CALCULATION_ONLY, PricingUnit.FLAT, false, false);
        ResourceEntity resource = resource(1L, ResourceStatus.ACTIVE, new BigDecimal("100.00"), null, null);

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, null, null, null, Map.of(), List.of(), valueProfile));

        assertThat(result.success()).isTrue();
        assertThat(result.calculatedValue()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("calculatedValue with a modifier")
    void valueWithModifier() {
        DomainProfile valueProfile =
                profile(AlgorithmMode.VALUE_CALCULATION_ONLY, PricingUnit.FLAT, false, false);
        ResourceEntity resource = resource(1L, ResourceStatus.ACTIVE, new BigDecimal("100.00"), null,
                Map.of("priceMultiplier", "1.5"));

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, null, null, null, Map.of(), List.of(), valueProfile));

        assertThat(result.success()).isTrue();
        assertThat(result.calculatedValue()).isEqualByComparingTo("150.00"); // 100 × 1.5
        assertThat(result.breakdown().lines()).hasSize(2); // base + multiplier
    }

    @Test
    @DisplayName("Invalid date range → failure")
    void invalidDateRange() {
        ResourceEntity resource = resource(1L, ResourceStatus.ACTIVE, new BigDecimal("100.00"), null, null);

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, T12, T10, null, Map.of(), List.of(), timeProfile));

        assertThat(result.success()).isFalse();
        assertThat(result.errors()).anyMatch(e -> e.toLowerCase().contains("zakres"));
    }

    @Test
    @DisplayName("Missing resource → failure")
    void missingResource() {
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                null, T10, T12, null, Map.of(), List.of(), timeProfile));

        assertThat(result.success()).isFalse();
        assertThat(result.errors()).contains("Zasób jest wymagany");
    }
}
