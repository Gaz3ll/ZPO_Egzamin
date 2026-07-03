package pl.zpo.app.domain.request;

import java.util.Set;

/**
 * Lifecycle status of a request.
 *
 * <pre>
 *   DRAFT ─▶ PENDING ─▶ CONFIRMED ─▶ COMPLETED
 *                 │           │
 *                 ├─▶ REJECTED│
 *                 └─▶ CANCELLED ◀─┘
 * </pre>
 */
public enum RequestStatus {
    DRAFT,
    PENDING,
    CONFIRMED,
    CANCELLED,
    REJECTED,
    COMPLETED;

    private static final Set<RequestStatus> CANCELLABLE = Set.of(DRAFT, PENDING, CONFIRMED);
    private static final Set<RequestStatus> ACTIVE = Set.of(PENDING, CONFIRMED);

    /** True when the owner (or an admin) may still cancel the request. */
    public boolean isCancellable() {
        return CANCELLABLE.contains(this);
    }

    /**
     * True when the request occupies capacity / a time slot and therefore participates
     * in collision and capacity checks.
     */
    public boolean isActive() {
        return ACTIVE.contains(this);
    }

    /** Statuses that occupy a resource — the ones collision/capacity checks must consider. */
    public static Set<RequestStatus> activeStatuses() {
        return ACTIVE;
    }
}
