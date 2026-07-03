package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;

@Component
public class DefaultDomainAlgorithm implements DomainAlgorithm {

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder("KG");
        List<String> errors = new ArrayList<>();

        ResourceEntity resource = input.resource();
        if (resource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak ćwiczenia");
            return DomainAlgorithmResult.failure(List.of("Ćwiczenie jest wymagane"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: ćwiczenie nieaktywne (" + resource.getStatus() + ")");
            return DomainAlgorithmResult.failure(List.of("Ćwiczenie nie jest aktywne"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        Map<String, Object> meta = input.requestMetadata();

        Integer sets = readInteger(meta, "sets");
        Integer reps = readInteger(meta, "reps");
        Integer weight = readInteger(meta, "weight");
        Integer durationMinutes = readInteger(meta, "durationMinutes");
        String workoutDate = readString(meta, "workoutDate");

        if (sets == null || sets <= 0) {
            errors.add("Liczba serii musi być liczbą dodatnią");
            breakdown.addRule("SETS_CHECK: nieprawidłowa (" + sets + ")");
        } else {
            breakdown.addRule("SETS_CHECK: " + sets);
        }

        if (reps == null || reps <= 0) {
            errors.add("Liczba powtórzeń musi być liczbą dodatnią");
            breakdown.addRule("REPS_CHECK: nieprawidłowa (" + reps + ")");
        } else {
            breakdown.addRule("REPS_CHECK: " + reps);
        }

        if (weight == null || weight < 0) {
            errors.add("Ciężar musi być liczbą nieujemną");
            breakdown.addRule("WEIGHT_CHECK: nieprawidłowy (" + weight + ")");
        } else {
            breakdown.addRule("WEIGHT_CHECK: " + weight);
        }

        if (durationMinutes == null || durationMinutes <= 0) {
            errors.add("Czas trwania musi być liczbą dodatnią");
            breakdown.addRule("DURATION_CHECK: nieprawidłowy (" + durationMinutes + ")");
        } else {
            breakdown.addRule("DURATION_CHECK: " + durationMinutes);
        }

        if (workoutDate == null || workoutDate.isBlank()) {
            errors.add("Data treningu jest wymagana");
            breakdown.addRule("DATE_CHECK: brak");
        } else {
            breakdown.addRule("DATE_CHECK: " + workoutDate);
        }

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }

        long totalVolume = (long) sets * reps * weight;
        breakdown.addLine("totalVolume", BigDecimal.valueOf(totalVolume),
                sets + " serii × " + reps + " powtórzeń × " + weight + " kg = " + totalVolume + " kg");
        breakdown.addLine("totalDuration", BigDecimal.valueOf(durationMinutes),
                "Czas treningu: " + durationMinutes + " min");

        breakdown.addRule("TOTAL_VOLUME: " + totalVolume + " kg");
        breakdown.addRule("TOTAL_DURATION: " + durationMinutes + " min");
        breakdown.addNote("sets=" + sets + " reps=" + reps + " weight=" + weight + " duration=" + durationMinutes + " date=" + workoutDate);

        int weeklyMinutes = 0;
        if (workoutDate != null) {
            weeklyMinutes = computeWeeklyMinutes(workoutDate, input.existingRequests());
            breakdown.addNote("weeklyMinutes=" + weeklyMinutes);
            breakdown.addRule("WEEKLY_TOTAL: " + weeklyMinutes + " min w tym tygodniu");
        }

        return DomainAlgorithmResult.success(BigDecimal.valueOf(totalVolume), resource.getId(), breakdown.build());
    }

    private int computeWeeklyMinutes(String workoutDateStr, List<pl.zpo.app.domain.request.RequestEntity> existing) {
        try {
            LocalDate workoutDate = LocalDate.parse(workoutDateStr);
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            int targetWeek = workoutDate.get(weekFields.weekOfWeekBasedYear());
            int targetYear = workoutDate.get(weekFields.weekBasedYear());

            int total = 0;
            for (pl.zpo.app.domain.request.RequestEntity req : existing) {
                if (req.getStartAt() == null) continue;
                LocalDate reqDate = LocalDate.ofInstant(req.getStartAt(), ZoneOffset.UTC);
                if (reqDate.get(weekFields.weekOfWeekBasedYear()) == targetWeek
                        && reqDate.get(weekFields.weekBasedYear()) == targetYear) {
                    Integer dur = readInteger(req.getMetadata(), "durationMinutes");
                    if (dur != null && dur > 0) {
                        total += dur;
                    }
                }
            }
            return total;
        } catch (Exception e) {
            return 0;
        }
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
