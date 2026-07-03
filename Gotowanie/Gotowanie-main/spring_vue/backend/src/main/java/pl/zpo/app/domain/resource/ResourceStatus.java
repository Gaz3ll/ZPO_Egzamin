package pl.zpo.app.domain.resource;

/**
 * Lifecycle status of a resource. Only {@link #ACTIVE} resources may be requested.
 */
public enum ResourceStatus {
    /** Available to be requested. */
    ACTIVE,
    /** Temporarily hidden / not offered, but not removed. */
    INACTIVE,
    /** Exists but currently cannot be used (e.g. under maintenance). */
    UNAVAILABLE
}
