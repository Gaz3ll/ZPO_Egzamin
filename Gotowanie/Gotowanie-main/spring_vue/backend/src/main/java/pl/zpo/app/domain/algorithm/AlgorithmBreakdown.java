package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.util.List;

/**
 * Human-readable explanation of how the algorithm reached its result. Persisted as JSONB in the
 * request ({@code algorithm_breakdown_json}) and shown in the UI's "AlgorithmBreakdown" component,
 * so both the user and the reviewer can see <em>why</em> a value/decision was produced.
 *
 * @param lines        itemized value lines (base, duration, quantity, modifiers, ...)
 * @param appliedRules ordered list of rules the algorithm applied (e.g. "TIME_COLLISION_CHECK: ok")
 * @param notes        additional remarks (e.g. collision or capacity messages)
 * @param total        final calculated value
 * @param currency     ISO currency code for {@code total} and line amounts
 */
public record AlgorithmBreakdown(
        List<BreakdownLine> lines,
        List<String> appliedRules,
        List<String> notes,
        BigDecimal total,
        String currency
) {

    public AlgorithmBreakdown {
        lines = lines == null ? List.of() : List.copyOf(lines);
        appliedRules = appliedRules == null ? List.of() : List.copyOf(appliedRules);
        notes = notes == null ? List.of() : List.copyOf(notes);
    }

    /**
     * One itemized line of the value computation.
     *
     * @param label  short label, e.g. "Wartość bazowa"
     * @param amount contribution to the total (may be negative for discounts)
     * @param detail free-form explanation, e.g. "100.00 × 3 h"
     */
    public record BreakdownLine(String label, BigDecimal amount, String detail) {
    }
}
