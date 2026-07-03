package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.availability.CapacityMatcher;
import pl.zpo.app.domain.availability.TimeCollisionDetector;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;

@Component
public class DefaultDomainAlgorithm implements DomainAlgorithm {

    private final TimeCollisionDetector collisionDetector;
    private final CapacityMatcher capacityMatcher;

    public DefaultDomainAlgorithm(TimeCollisionDetector c, CapacityMatcher m) {
        this.collisionDetector = c; this.capacityMatcher = m;
    }

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        if (isQuizInput(input)) return evaluateQuiz(input);
        return evaluateGeneric(input);
    }

    private boolean isQuizInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "correctAnswers", "wrongAnswers", "studentName", "score", "percentage", "passed")
                || (input.resource() != null && hasAny(input.resource().getMetadata(), "questionCount", "passThreshold"));
    }

    private DomainAlgorithmResult evaluateQuiz(DomainAlgorithmInput input) {
        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();

        ResourceEntity resource = input.resource();
        if (resource == null) {
            return DomainAlgorithmResult.failure(List.of("Quiz jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            return DomainAlgorithmResult.failure(List.of("Quiz nie jest dostępny"), breakdown.build());
        }
        breakdown.addRule("QUIZ_CHECK: ok");

        Integer totalQuestions = readInteger(resource.getMetadata(), "questionCount");
        Integer correct = readInteger(input.requestMetadata(), "correctAnswers");
        Integer wrong = readInteger(input.requestMetadata(), "wrongAnswers");

        if (totalQuestions == null || totalQuestions <= 0) {
            errors.add("Quiz nie ma zdefiniowanej liczby pytań");
            breakdown.addRule("QUIZ_CONFIG: brak liczby pytan");
        }
        if (correct == null || correct < 0) {
            errors.add("Podaj liczbę poprawnych odpowiedzi");
            breakdown.addRule("INPUT: brak poprawnych");
        }

        if (!errors.isEmpty()) return DomainAlgorithmResult.failure(errors, breakdown.build());

        int w = wrong != null ? wrong : 0;
        int blank = Math.max(0, totalQuestions - correct - w);

        double score = correct * 1.0 + w * (-0.5) + blank * 0;
        double maxScore = totalQuestions * 1.0;
        double percentage = maxScore > 0 ? (score / maxScore) * 100.0 : 0;

        breakdown.addLine("correctPoints", BigDecimal.valueOf(correct), correct + " x +1");
        if (w > 0) breakdown.addLine("wrongPenalty", BigDecimal.valueOf(w * -0.5), w + " x -0.5");
        if (blank > 0) breakdown.addLine("blankPoints", BigDecimal.ZERO, blank + " x 0");

        Integer threshold = readInteger(resource.getMetadata(), "passThreshold");
        boolean passed = threshold != null && percentage >= threshold;

        breakdown.addRule("SCORE: " + String.format("%.1f", score) + "/" + maxScore);
        breakdown.addRule("PERCENTAGE: " + String.format("%.1f", percentage) + "%");
        breakdown.addRule("THRESHOLD: " + (threshold != null ? threshold + "%" : "brak"));
        breakdown.addRule("RESULT: " + (passed ? "ZALICZONY" : "NIEZALICZONY"));

        return DomainAlgorithmResult.success(
                BigDecimal.valueOf(percentage).setScale(1, RoundingMode.HALF_UP),
                resource.getId(), breakdown.build());
    }

    private DomainAlgorithmResult evaluateGeneric(DomainAlgorithmInput input) {
        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();
        if (resource == null) return DomainAlgorithmResult.failure(List.of("Zasób jest wymagany"), breakdown.build());
        if (resource.getStatus() != ResourceStatus.ACTIVE)
            return DomainAlgorithmResult.failure(List.of("Zasób nie jest aktywny"), breakdown.build());
        breakdown.addRule("RESOURCE_CHECK: ok");
        if (!errors.isEmpty()) return DomainAlgorithmResult.failure(errors, breakdown.build());
        return DomainAlgorithmResult.success(null, resource.getId(), breakdown.build());
    }

    private Integer readInteger(Map<String, Object> m, String k) {
        if (m == null || m.get(k) == null) return null;
        Object v = m.get(k);
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (NumberFormatException e) { return null; }
    }

    private boolean hasAny(Map<String, Object> m, String... keys) {
        if (m == null) return false;
        for (String k : keys) if (m.containsKey(k)) return true;
        return false;
    }
}
