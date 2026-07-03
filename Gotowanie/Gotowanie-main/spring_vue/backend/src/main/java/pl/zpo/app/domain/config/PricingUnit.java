package pl.zpo.app.domain.config;

/**
 * How a resource's {@code baseValue} scales into a calculated value.
 *
 * <ul>
 *   <li>{@link #FLAT} — value = baseValue (× quantity if present).</li>
 *   <li>{@link #PER_HOUR} — value = baseValue × billed hours (× quantity).</li>
 *   <li>{@link #PER_DAY} — value = baseValue × billed days (× quantity).</li>
 *   <li>{@link #PER_UNIT} — value = baseValue × quantity (no time factor).</li>
 * </ul>
 */
public enum PricingUnit {
    FLAT,
    PER_HOUR,
    PER_DAY,
    PER_UNIT
}
