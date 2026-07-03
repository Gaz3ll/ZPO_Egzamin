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
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder("%");
        List<String> errors = new ArrayList<>();

        ResourceEntity resource = input.resource();
        if (resource == null) {
            return DomainAlgorithmResult.failure(List.of("Kurs jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            return DomainAlgorithmResult.failure(List.of("Kurs nie jest dostępny"), breakdown.build());
        }
        breakdown.addRule("COURSE_CHECK: ok");

        Integer totalLessons = readInteger(resource.getMetadata(), "totalLessons");
        Integer lessonsCompleted = readInteger(input.requestMetadata(), "lessonsCompleted");

        if (totalLessons == null || totalLessons <= 0) {
            errors.add("Kurs nie ma zdefiniowanej liczby lekcji");
            breakdown.addRule("CONFIG: brak totalLessons");
        }
        if (lessonsCompleted == null || lessonsCompleted < 0) {
            errors.add("Podaj liczbę ukończonych lekcji");
            breakdown.addRule("INPUT: brak lessonsCompleted");
        }
        if (totalLessons != null && lessonsCompleted != null && lessonsCompleted > totalLessons) {
            errors.add("Ukończone lekcje (" + lessonsCompleted + ") > łączna liczba (" + totalLessons + ")");
            breakdown.addRule("OVERFLOW: " + lessonsCompleted + " > " + totalLessons);
        }

        if (!errors.isEmpty()) return DomainAlgorithmResult.failure(errors, breakdown.build());

        double progress = ((double) lessonsCompleted / totalLessons) * 100.0;
        boolean completed = progress >= 100.0;

        breakdown.addLine("completed", BigDecimal.valueOf(lessonsCompleted), lessonsCompleted + " / " + totalLessons + " lekcji");
        breakdown.addLine("progress", BigDecimal.valueOf(progress).setScale(1, RoundingMode.HALF_UP),
                "Postęp: " + String.format("%.1f", progress) + "%");
        breakdown.addRule("PROGRESS: " + String.format("%.1f", progress) + "%");
        breakdown.addRule("STATUS: " + (completed ? "UKOŃCZONY" : "W TRAKCIE"));

        return DomainAlgorithmResult.success(
                BigDecimal.valueOf(progress).setScale(1, RoundingMode.HALF_UP),
                resource.getId(), breakdown.build());
    }

    private Integer readInteger(Map<String, Object> m, String k) {
        if (m == null || m.get(k) == null) return null;
        Object v = m.get(k);
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (NumberFormatException e) { return null; }
    }
}
