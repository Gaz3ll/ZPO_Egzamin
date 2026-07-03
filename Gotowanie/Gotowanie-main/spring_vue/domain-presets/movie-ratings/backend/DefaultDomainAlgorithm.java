package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;

@Component
public class DefaultDomainAlgorithm implements DomainAlgorithm {

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(input.profile().currency());
        List<String> errors = new ArrayList<>();

        ResourceEntity resource = input.resource();
        if (resource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak filmu");
            return DomainAlgorithmResult.failure(List.of("Film jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: film nieaktywny (" + resource.getStatus() + ")");
            return DomainAlgorithmResult.failure(List.of("Film nie jest aktywny"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        Map<String, Object> meta = input.requestMetadata();
        Integer rating = readInteger(meta, "rating");

        if (rating == null || rating < 1 || rating > 5) {
            errors.add("Ocena musi być w zakresie 1-5");
            breakdown.addRule("RATING_CHECK: nieprawidłowa (" + rating + ")");
        } else {
            breakdown.addRule("RATING_CHECK: " + rating);
        }

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }

        int existingSum = 0;
        int existingCount = 0;

        for (RequestEntity req : input.existingRequests()) {
            if (req.getStatus() != null && req.getStatus().isActive()) {
                Integer r = readInteger(req.getMetadata(), "rating");
                if (r != null && r >= 1 && r <= 5) {
                    existingSum += r;
                    existingCount++;
                }
            }
        }

        int newSum = existingSum + rating;
        int newCount = existingCount + 1;
        double average = (double) newSum / newCount;

        breakdown.addLine("yourRating", BigDecimal.valueOf(rating), "Twoja ocena: " + rating + " / 5");
        if (existingCount > 0) {
            double oldAvg = (double) existingSum / existingCount;
            breakdown.addLine("existingAverage", BigDecimal.valueOf(oldAvg).setScale(2, RoundingMode.HALF_UP),
                    "Średnia przed oceną: " + String.format("%.2f", oldAvg) + " (z " + existingCount + " ocen)");
        } else {
            breakdown.addNote("Brak wcześniejszych ocen dla tego filmu");
        }
        breakdown.addLine("newAverage", BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP),
                "Nowa średnia: " + String.format("%.2f", average) + " (z " + newCount + " ocen)");
        breakdown.addLine("totalRatings", BigDecimal.valueOf(newCount),
                "Łączna liczba ocen po dodaniu: " + newCount);
        breakdown.addLine("ratingSum", BigDecimal.valueOf(newSum),
                "Suma wszystkich ocen: " + newSum);

        breakdown.addRule("AVERAGE_RATING: " + String.format("%.2f", average));
        breakdown.addRule("TOTAL_RATINGS: " + newCount);
        breakdown.addNote("rating=" + rating + " existingAvg=" + (existingCount > 0 ? String.format("%.2f", (double) existingSum / existingCount) : "brak") + " newAvg=" + String.format("%.2f", average));

        return DomainAlgorithmResult.success(BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP),
                resource.getId(), breakdown.build());
    }

    private String readString(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) {
            return null;
        }
        String value = String.valueOf(metadata.get(key));
        return value.isBlank() ? null : value;
    }

    private Integer readInteger(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) {
            return null;
        }
        Object value = metadata.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
