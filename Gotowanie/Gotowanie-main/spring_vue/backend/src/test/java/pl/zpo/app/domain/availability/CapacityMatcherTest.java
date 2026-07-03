package pl.zpo.app.domain.availability;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.zpo.app.support.TestFixtures.T10;
import static pl.zpo.app.support.TestFixtures.T12;
import static pl.zpo.app.support.TestFixtures.request;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.zpo.app.domain.request.RequestStatus;

class CapacityMatcherTest {

    private final CapacityMatcher matcher = new CapacityMatcher();

    @Test
    @DisplayName("fits() respects capacity")
    void fits() {
        assertThat(matcher.fits(5, 0, 3)).isTrue();
        assertThat(matcher.fits(5, 3, 2)).isTrue();
        assertThat(matcher.fits(5, 3, 3)).isFalse();
        assertThat(matcher.fits(5, 0, 0)).isFalse(); // zero requested is invalid
    }

    @Test
    @DisplayName("remaining() never goes negative")
    void remaining() {
        assertThat(matcher.remaining(5, 2)).isEqualTo(3);
        assertThat(matcher.remaining(5, 8)).isEqualTo(0);
    }

    @Test
    @DisplayName("usedCapacity() sums quantities, counting null as 1")
    void usedCapacity() {
        var requests = List.of(
                request(1L, 1L, T10, T12, 2, RequestStatus.CONFIRMED),
                request(2L, 1L, T10, T12, null, RequestStatus.PENDING)); // counts as 1

        assertThat(matcher.usedCapacity(requests)).isEqualTo(3);
    }
}
