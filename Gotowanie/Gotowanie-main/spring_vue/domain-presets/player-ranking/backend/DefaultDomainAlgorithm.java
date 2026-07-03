package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;

@Component
public class DefaultDomainAlgorithm implements DomainAlgorithm {

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder("PTS");
        List<String> errors = new ArrayList<>();

        ResourceEntity resource = input.resource();
        if (resource == null) {
            return DomainAlgorithmResult.failure(List.of("Turniej jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            return DomainAlgorithmResult.failure(List.of("Turniej nie jest aktywny"), breakdown.build());
        }
        breakdown.addRule("TOURNAMENT_CHECK: ok");

        Integer score = readInteger(input.requestMetadata(), "score");
        Integer rank = readInteger(input.requestMetadata(), "rank");

        if (score == null || score < 0) {
            errors.add("Wynik musi być >= 0");
            breakdown.addRule("SCORE_CHECK: nieprawidlowy");
        } else {
            breakdown.addRule("SCORE_CHECK: " + score);
        }

        if (rank == null || rank < 1) {
            errors.add("Pozycja musi być >= 1");
            breakdown.addRule("RANK_CHECK: nieprawidlowa");
        } else {
            breakdown.addRule("RANK_CHECK: " + rank);
        }

        if (!errors.isEmpty()) return DomainAlgorithmResult.failure(errors, breakdown.build());

        double rankBonus = 1.0 + (1.0 / rank);
        double totalPoints = score * rankBonus;

        breakdown.addLine("baseScore", BigDecimal.valueOf(score), "Wynik bazowy: " + score);
        breakdown.addLine("rankBonus", BigDecimal.valueOf(rankBonus).setScale(2, RoundingMode.HALF_UP),
                "Bonus za miejsce " + rank + ": ×" + String.format("%.2f", rankBonus));
        breakdown.addLine("totalPoints", BigDecimal.valueOf(totalPoints).setScale(0, RoundingMode.HALF_UP),
                "Łącznie: " + String.format("%.0f", totalPoints) + " PTS");
        breakdown.addRule("RANKING: " + String.format("%.0f", totalPoints) + " PTS (miejsce " + rank + ")");

        return DomainAlgorithmResult.success(
                BigDecimal.valueOf(totalPoints).setScale(0, RoundingMode.HALF_UP),
                resource.getId(), breakdown.build());
    }

    private Integer readInteger(Map<String, Object> m, String k) {
        if (m == null || m.get(k) == null) return null;
        Object v = m.get(k);
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (NumberFormatException e) { return null; }
    }
}
