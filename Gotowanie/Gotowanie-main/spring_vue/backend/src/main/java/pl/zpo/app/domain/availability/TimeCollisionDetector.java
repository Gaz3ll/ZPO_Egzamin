package pl.zpo.app.domain.availability;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.request.RequestEntity;

/**
 * Detects time-window collisions using <b>half-open</b> intervals {@code [start, end)}: two
 * bookings that merely touch at an endpoint (one ends exactly when the next begins) do
 * <em>not</em> collide. Pure and side-effect free, so it is trivially unit-testable.
 */
@Component
public class TimeCollisionDetector {

    /**
     * @return true if {@code [start1, end1)} and {@code [start2, end2)} overlap.
     */
    public boolean collides(Instant start1, Instant end1, Instant start2, Instant end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * @return the subset of {@code existing} whose time window overlaps {@code [start, end)}.
     */
    public List<RequestEntity> findCollisions(Instant start, Instant end, Collection<RequestEntity> existing) {
        List<RequestEntity> collisions = new ArrayList<>();
        for (RequestEntity request : existing) {
            if (collides(start, end, request.getStartAt(), request.getEndAt())) {
                collisions.add(request);
            }
        }
        return collisions;
    }
}
