package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import pl.zpo.app.common.Money;
import pl.zpo.app.domain.algorithm.AlgorithmBreakdown.BreakdownLine;

/**
 * Accumulates the value lines, applied rules and notes produced while the algorithm runs, then
 * assembles a consistent {@link AlgorithmBreakdown}. The total is the sum of all line amounts
 * (each line stores its <em>contribution</em> to the total), computed with {@link Money} rounding.
 */
public class AlgorithmBreakdownBuilder {

    private final String currency;
    private final List<BreakdownLine> lines = new ArrayList<>();
    private final List<String> appliedRules = new ArrayList<>();
    private final List<String> notes = new ArrayList<>();

    public AlgorithmBreakdownBuilder(String currency) {
        this.currency = currency;
    }

    public AlgorithmBreakdownBuilder addLine(String label, BigDecimal amount, String detail) {
        lines.add(new BreakdownLine(label, amount.setScale(2, java.math.RoundingMode.HALF_UP), detail));
        return this;
    }

    public AlgorithmBreakdownBuilder addRule(String rule) {
        appliedRules.add(rule);
        return this;
    }

    public AlgorithmBreakdownBuilder addNote(String note) {
        notes.add(note);
        return this;
    }

    /** Sum of all line contributions, floored at zero (a value is never negative). */
    public Money total() {
        Money sum = Money.zero(currency);
        for (BreakdownLine line : lines) {
            sum = sum.add(Money.of(line.amount(), currency));
        }
        return sum.isNegative() ? Money.zero(currency) : sum;
    }

    public AlgorithmBreakdown build() {
        return new AlgorithmBreakdown(
                List.copyOf(lines),
                List.copyOf(appliedRules),
                List.copyOf(notes),
                total().amount(),
                currency);
    }
}
