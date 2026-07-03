package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.util.List;

/**
 * Outcome of running the domain algorithm.
 *
 * @param success            true if all applicable checks passed
 * @param calculatedValue    the computed value (price/cost), or {@code null} if not applicable
 * @param assignedResourceId the resource that satisfied the request (equals the input resource in
 *                           the default algorithm; an extension point for assignment-style domains)
 * @param breakdown          itemized explanation (also carries the applied rules)
 * @param errors             reasons the request cannot be satisfied (empty when {@code success})
 */
public record DomainAlgorithmResult(
        boolean success,
        BigDecimal calculatedValue,
        Long assignedResourceId,
        AlgorithmBreakdown breakdown,
        List<String> errors
) {

    public DomainAlgorithmResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static DomainAlgorithmResult success(BigDecimal value, Long assignedResourceId, AlgorithmBreakdown breakdown) {
        return new DomainAlgorithmResult(true, value, assignedResourceId, breakdown, List.of());
    }

    public static DomainAlgorithmResult failure(List<String> errors, AlgorithmBreakdown breakdown) {
        return new DomainAlgorithmResult(false, null, null, breakdown, errors);
    }

    /** Convenience accessor required by the spec — the rules the algorithm applied. */
    public List<String> appliedRules() {
        return breakdown == null ? List.of() : breakdown.appliedRules();
    }
}
