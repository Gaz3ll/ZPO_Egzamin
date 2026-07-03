package pl.zpo.app.domain.availability;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.zpo.app.support.TestFixtures.T10;
import static pl.zpo.app.support.TestFixtures.T11;
import static pl.zpo.app.support.TestFixtures.T12;
import static pl.zpo.app.support.TestFixtures.T13;
import static pl.zpo.app.support.TestFixtures.T14;
import static pl.zpo.app.support.TestFixtures.request;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.zpo.app.domain.request.RequestStatus;

class TimeCollisionDetectorTest {

    private final TimeCollisionDetector detector = new TimeCollisionDetector();

    @Test
    @DisplayName("Overlapping intervals collide")
    void overlapCollides() {
        assertThat(detector.collides(T10, T12, T11, T13)).isTrue();
    }

    @Test
    @DisplayName("Half-open intervals: touching endpoints do not collide")
    void touchingDoesNotCollide() {
        assertThat(detector.collides(T10, T12, T12, T14)).isFalse();
        assertThat(detector.collides(T12, T14, T10, T12)).isFalse();
    }

    @Test
    @DisplayName("Disjoint intervals do not collide")
    void disjointDoesNotCollide() {
        assertThat(detector.collides(T10, T11, T13, T14)).isFalse();
    }

    @Test
    @DisplayName("findCollisions returns only overlapping existing requests")
    void findCollisionsFilters() {
        var existing = List.of(
                request(1L, 1L, T13, T14, null, RequestStatus.CONFIRMED),  // disjoint (after window)
                request(2L, 1L, T11, T13, null, RequestStatus.CONFIRMED)); // overlaps T10-T12

        var collisions = detector.findCollisions(T10, T12, existing);

        assertThat(collisions).extracting("id").containsExactly(2L);
    }
}
