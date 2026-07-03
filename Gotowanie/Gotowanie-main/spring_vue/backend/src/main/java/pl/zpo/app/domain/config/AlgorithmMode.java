package pl.zpo.app.domain.config;

/**
 * Declares which generic checks the {@code DefaultDomainAlgorithm} performs. Selecting a mode
 * (in {@code DomainProfileProvider}) is how you adapt the same algorithm to different subjects.
 *
 * <ul>
 *   <li>{@link #TIME_AVAILABILITY_AND_CALCULATION} — car rental / vet / room booking:
 *       time-collision + capacity + value.</li>
 *   <li>{@link #CAPACITY_MATCHING} — parcel lockers / cinema seats: capacity + value, no time.</li>
 *   <li>{@link #TIME_COLLISION_ONLY} — pure scheduling: only detect time clashes.</li>
 *   <li>{@link #VALUE_CALCULATION_ONLY} — pure pricing, no availability constraints.</li>
 * </ul>
 */
public enum AlgorithmMode {
    TIME_AVAILABILITY_AND_CALCULATION(true, true, true),
    CAPACITY_MATCHING(false, true, true),
    TIME_COLLISION_ONLY(true, false, false),
    VALUE_CALCULATION_ONLY(false, false, true);

    private final boolean checksTime;
    private final boolean checksCapacity;
    private final boolean calculatesValue;

    AlgorithmMode(boolean checksTime, boolean checksCapacity, boolean calculatesValue) {
        this.checksTime = checksTime;
        this.checksCapacity = checksCapacity;
        this.calculatesValue = calculatesValue;
    }

    public boolean checksTime() {
        return checksTime;
    }

    public boolean checksCapacity() {
        return checksCapacity;
    }

    public boolean calculatesValue() {
        return calculatesValue;
    }
}
