package pl.zpo.app.domain.availability;

import java.util.Collection;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.request.RequestEntity;

/**
 * Capacity arithmetic for quantity-based domains (locker size, cinema seats, simultaneous
 * bookings). Pure and side-effect free.
 */
@Component
public class CapacityMatcher {

    /**
     * @return true if adding {@code requested} on top of {@code used} stays within {@code capacity}.
     */
    public boolean fits(int capacity, int used, int requested) {
        return requested > 0 && used + requested <= capacity;
    }

    /** Remaining free capacity (never negative). */
    public int remaining(int capacity, int used) {
        return Math.max(0, capacity - used);
    }

    /**
     * Sum of quantities already consumed by the given requests. A request without an explicit
     * quantity counts as 1 (it still occupies the resource once).
     */
    public int usedCapacity(Collection<RequestEntity> requests) {
        int used = 0;
        for (RequestEntity request : requests) {
            used += request.getQuantity() == null ? 1 : request.getQuantity();
        }
        return used;
    }
}
