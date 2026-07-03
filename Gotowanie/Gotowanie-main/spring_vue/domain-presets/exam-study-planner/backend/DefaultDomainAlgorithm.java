package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.availability.CapacityMatcher;
import pl.zpo.app.domain.availability.TimeCollisionDetector;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.config.PricingUnit;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;

/**
 * Exam study planner algorithm.
 *
 * <p>Resource = an exam ({@code examDate}, {@code materialCount}), Request = a generated study
 * plan. The algorithm counts the days left until the exam, reserves revision days at the end,
 * spreads the material over the remaining study days, scales the estimated time by the exam
 * difficulty ({@code baseValue} as a multiplier, fallback: {@code difficulty} metadata) and flags
 * an overload when the daily estimate exceeds the daily study limit.
 * {@code calculatedValue} = total estimated study minutes.</p>
 */
@Component
public class DefaultDomainAlgorithm implements DomainAlgorithm {

    private final TimeCollisionDetector collisionDetector;
    private final CapacityMatcher capacityMatcher;

    public DefaultDomainAlgorithm(TimeCollisionDetector collisionDetector, CapacityMatcher capacityMatcher) {
        this.collisionDetector = collisionDetector;
        this.capacityMatcher = capacityMatcher;
    }

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        if (!isStudyPlannerInput(input)) {
            return evaluateGeneric(input);
        }

        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();

        if (resource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak egzaminu");
            return DomainAlgorithmResult.failure(List.of("Egzamin jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: egzamin nieaktywny (" + resource.getStatus() + ")");
            return DomainAlgorithmResult.failure(List.of("Egzamin nie jest aktywny"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        LocalDate examDate = readDate(resource.getMetadata(), "examDate");
        if (examDate == null) {
            breakdown.addRule("EXAM_DATE_CHECK: brak lub nieprawidłowa data");
            return DomainAlgorithmResult.failure(
                    List.of("Egzamin nie ma poprawnej daty (examDate)"), breakdown.build());
        }
        long daysUntilExam = ChronoUnit.DAYS.between(LocalDate.now(), examDate);
        breakdown.addRule("DAYS_UNTIL_EXAM: " + daysUntilExam);
        breakdown.addNote("daysUntilExam=" + daysUntilExam);
        if (daysUntilExam <= 0) {
            errors.add("Egzamin " + examDate + " już się odbył albo jest dzisiaj — nie można wygenerować planu");
            breakdown.addRule("EXAM_DATE_CHECK: egzamin w przeszłości");
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }

        int materialCount = Math.max(1, orDefault(readInteger(resource.getMetadata(), "materialCount"), 10));
        String materialUnit = orDefault(readString(resource.getMetadata(), "materialUnit"), "TOPICS");
        breakdown.addRule("MATERIAL_COUNT: " + materialCount + " (" + materialUnit + ")");
        breakdown.addNote("materialCount=" + materialCount);

        int revisionDays = daysUntilExam >= 7 ? 2 : (daysUntilExam >= 3 ? 1 : 0);
        long studyDays = Math.max(1, daysUntilExam - revisionDays);
        breakdown.addRule("REVISION_DAYS: " + revisionDays);
        breakdown.addNote("revisionDays=" + revisionDays);
        breakdown.addNote("studyDays=" + studyDays);

        long dailyMaterial = ceilDiv(materialCount, studyDays);
        breakdown.addRule("DAILY_MATERIAL: " + dailyMaterial + " " + materialUnit + "/dzień");
        breakdown.addNote("dailyMaterial=" + dailyMaterial);

        BigDecimal difficultyMultiplier = difficultyMultiplier(resource);
        breakdown.addRule("DIFFICULTY_MULTIPLIER: × " + difficultyMultiplier.toPlainString());
        breakdown.addNote("difficultyMultiplier=" + difficultyMultiplier.toPlainString());

        BigDecimal minutesPerUnit = minutesPerUnit(materialUnit);
        BigDecimal estimatedDailyMinutes = minutesPerUnit
                .multiply(BigDecimal.valueOf(dailyMaterial))
                .multiply(difficultyMultiplier)
                .setScale(0, RoundingMode.CEILING);
        breakdown.addRule("ESTIMATED_DAILY_MINUTES: " + estimatedDailyMinutes.toPlainString());
        breakdown.addNote("estimatedDailyMinutes=" + estimatedDailyMinutes.toPlainString());

        Integer dailyLimit = readInteger(input.requestMetadata(), "studyMinutes");
        if (dailyLimit == null) {
            dailyLimit = readInteger(resource.getMetadata(), "dailyStudyLimitMinutes");
        }
        boolean overloadWarning = dailyLimit != null && dailyLimit > 0
                && estimatedDailyMinutes.compareTo(BigDecimal.valueOf(dailyLimit)) > 0;
        breakdown.addNote("overloadWarning=" + overloadWarning);
        if (overloadWarning) {
            breakdown.addRule("OVERLOAD_CHECK: przeciążenie (" + estimatedDailyMinutes.toPlainString()
                    + " min > limit " + dailyLimit + " min/dzień)");
            breakdown.addNote("Za dużo materiału na dostępne dni — zacznij wcześniej albo zwiększ limit dzienny");
        } else {
            breakdown.addRule("OVERLOAD_CHECK: ok" + (dailyLimit == null ? " (limit nieustawiony)" : ""));
        }

        BigDecimal studyMinutesTotal = estimatedDailyMinutes.multiply(BigDecimal.valueOf(studyDays));
        breakdown.addLine("studyMinutes", studyMinutesTotal,
                estimatedDailyMinutes.toPlainString() + " min × " + studyDays + " dni nauki");

        if (revisionDays > 0) {
            BigDecimal revisionMinutes = estimatedDailyMinutes
                    .divide(BigDecimal.valueOf(2), 0, RoundingMode.CEILING)
                    .multiply(BigDecimal.valueOf(revisionDays));
            breakdown.addLine("revisionMinutes", revisionMinutes,
                    revisionDays + " dni powtórek × 50% dziennego czasu");
        }

        BigDecimal totalStudyMinutes = breakdown.total().amount();
        breakdown.addRule("TOTAL_STUDY_MINUTES: " + totalStudyMinutes.toPlainString());
        breakdown.addNote("totalStudyMinutes=" + totalStudyMinutes.toPlainString());

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        return DomainAlgorithmResult.success(totalStudyMinutes, resource.getId(), breakdown.build());
    }

    /** Difficulty comes from baseValue (estimated multiplier); fallback: difficulty metadata. */
    private BigDecimal difficultyMultiplier(ResourceEntity resource) {
        BigDecimal base = resource.getBaseValue();
        if (base != null && base.compareTo(new BigDecimal("0.5")) >= 0
                && base.compareTo(new BigDecimal("3")) <= 0) {
            return base;
        }
        String difficulty = readString(resource.getMetadata(), "difficulty");
        if (difficulty == null) {
            return BigDecimal.ONE;
        }
        return switch (difficulty.toUpperCase(Locale.ROOT)) {
            case "MEDIUM" -> new BigDecimal("1.25");
            case "HARD" -> new BigDecimal("1.50");
            default -> BigDecimal.ONE;
        };
    }

    private BigDecimal minutesPerUnit(String materialUnit) {
        return switch (materialUnit.toUpperCase(Locale.ROOT)) {
            case "CHAPTERS" -> new BigDecimal("60");
            case "PAGES" -> new BigDecimal("5");
            default -> new BigDecimal("45"); // TOPICS
        };
    }

    private boolean isStudyPlannerInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "selectedTopics", "studyMinutes", "isRevision")
                || (input.resource() != null && hasAny(input.resource().getMetadata(),
                "examDate", "materialCount", "materialUnit", "dailyStudyLimitMinutes"));
    }

    // ----- neutral fallback engine (shared, keeps the core unit tests green) -----

    private DomainAlgorithmResult evaluateGeneric(DomainAlgorithmInput input) {
        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();

        ResourceEntity resource = input.resource();
        if (resource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak zasobu");
            return DomainAlgorithmResult.failure(List.of("Zasób jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: zasób nieaktywny (" + resource.getStatus() + ")");
            return DomainAlgorithmResult.failure(List.of("Zasób nie jest aktywny"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        List<RequestEntity> activeExisting = activeExistingFor(resource, input.existingRequests());
        long durationUnits = 1;
        boolean hasStart = input.startAt() != null;
        boolean hasEnd = input.endAt() != null;

        if (profile.algorithmMode().checksTime() || hasStart || hasEnd) {
            if (hasStart ^ hasEnd) {
                errors.add("Zakres dat jest niekompletny (wymagane początek i koniec)");
                breakdown.addRule("TIME_RANGE_CHECK: niekompletny zakres");
            } else if (hasStart) {
                if (!input.startAt().isBefore(input.endAt())) {
                    errors.add("Nieprawidłowy zakres dat: początek musi być przed końcem");
                    breakdown.addRule("TIME_RANGE_CHECK: nieprawidłowy zakres");
                } else {
                    durationUnits = computeDurationUnits(profile.pricingUnit(), input.startAt(), input.endAt());
                    breakdown.addRule("TIME_RANGE_CHECK: ok");
                    if (profile.algorithmMode().checksTime()) {
                        List<RequestEntity> collisions =
                                collisionDetector.findCollisions(input.startAt(), input.endAt(), activeExisting);
                        if (!collisions.isEmpty()) {
                            errors.add("Termin koliduje z istniejącymi zgłoszeniami: " + ids(collisions));
                            breakdown.addRule("TIME_COLLISION_CHECK: kolizja (" + collisions.size() + ")");
                            breakdown.addNote("Kolizja z zgłoszeniami: " + ids(collisions));
                        } else {
                            breakdown.addRule("TIME_COLLISION_CHECK: brak kolizji");
                        }
                    }
                }
            } else if (profile.requiresTimeWindow()) {
                errors.add("Wymagany jest zakres dat");
                breakdown.addRule("TIME_RANGE_CHECK: brak wymaganego zakresu");
            }
        }

        int qty = input.quantity() != null ? input.quantity() : 1;
        if (input.quantity() != null && input.quantity() <= 0) {
            errors.add("Ilość musi być dodatnia");
            breakdown.addRule("QUANTITY_CHECK: nieprawidłowa ilość");
        } else if (profile.requiresQuantity() && input.quantity() == null) {
            errors.add("Wymagana jest ilość");
            breakdown.addRule("QUANTITY_CHECK: brak wymaganej ilości");
        }

        if (profile.algorithmMode().checksCapacity()
                && input.quantity() != null && input.quantity() > 0
                && resource.getCapacityValue() != null) {
            List<RequestEntity> relevant = (hasStart && hasEnd)
                    ? collisionDetector.findCollisions(input.startAt(), input.endAt(), activeExisting)
                    : activeExisting;
            int used = capacityMatcher.usedCapacity(relevant);
            int capacity = resource.getCapacityValue();
            if (!capacityMatcher.fits(capacity, used, qty)) {
                errors.add("Przekroczono pojemność: użyte %d/%d, żądane %d".formatted(used, capacity, qty));
                breakdown.addRule("CAPACITY_CHECK: przekroczono (%d+%d>%d)".formatted(used, qty, capacity));
            } else {
                breakdown.addRule("CAPACITY_CHECK: ok (%d/%d)".formatted(used + qty, capacity));
            }
        }

        BigDecimal calculatedValue = null;
        if (profile.algorithmMode().calculatesValue()) {
            calculatedValue = computeGenericValue(resource, input.requestMetadata(), durationUnits, qty, profile, breakdown);
        }

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        return DomainAlgorithmResult.success(calculatedValue, resource.getId(), breakdown.build());
    }

    private BigDecimal computeGenericValue(ResourceEntity resource, Map<String, Object> requestMetadata,
                                           long durationUnits, int qty, DomainProfile profile,
                                           AlgorithmBreakdownBuilder breakdown) {
        BigDecimal base = resource.getBaseValue() != null ? resource.getBaseValue() : BigDecimal.ZERO;
        BigDecimal subtotal = base.multiply(BigDecimal.valueOf(durationUnits)).multiply(BigDecimal.valueOf(qty));
        breakdown.addLine("Wartość bazowa", subtotal,
                "%s × %d (%s) × %d".formatted(base.toPlainString(), durationUnits, profile.pricingUnit(), qty));

        BigDecimal multiplier = readDecimal(resource.getMetadata(), "priceMultiplier");
        if (multiplier != null && multiplier.compareTo(BigDecimal.ONE) != 0) {
            BigDecimal delta = subtotal.multiply(multiplier).subtract(subtotal);
            breakdown.addLine("Mnożnik", delta, "× " + multiplier.toPlainString());
        }
        return breakdown.total().amount();
    }

    private List<RequestEntity> activeExistingFor(ResourceEntity resource, List<RequestEntity> existing) {
        List<RequestEntity> result = new ArrayList<>();
        for (RequestEntity request : existing) {
            boolean active = request.getStatus() != null && request.getStatus().isActive();
            boolean sameResource = resource.getId() == null
                    || request.getResourceId() == null
                    || resource.getId().equals(request.getResourceId());
            if (active && sameResource) {
                result.add(request);
            }
        }
        return result;
    }

    private long computeDurationUnits(PricingUnit unit, Instant start, Instant end) {
        Duration duration = Duration.between(start, end);
        long minutes = Math.max(0, duration.toMinutes());
        return switch (unit) {
            case PER_HOUR -> Math.max(1, ceilDiv(minutes, 60));
            case PER_DAY -> Math.max(1, ceilDiv(minutes, 60L * 24));
            case FLAT, PER_UNIT -> 1;
        };
    }

    private long ceilDiv(long value, long divisor) {
        return (value + divisor - 1) / divisor;
    }

    private String ids(List<RequestEntity> requests) {
        return requests.stream().map(r -> String.valueOf(r.getId())).toList().toString();
    }

    private boolean hasAny(Map<String, Object> metadata, String... keys) {
        if (metadata == null) {
            return false;
        }
        for (String key : keys) {
            if (metadata.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    private String readString(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) {
            return null;
        }
        String value = String.valueOf(metadata.get(key));
        return value.isBlank() ? null : value;
    }

    private LocalDate readDate(Map<String, Object> metadata, String key) {
        String value = readString(metadata, key);
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value.length() > 10 ? value.substring(0, 10) : value);
        } catch (DateTimeParseException ex) {
            return null;
        }
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

    private int orDefault(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private String orDefault(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private BigDecimal readDecimal(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) {
            return null;
        }
        Object value = metadata.get(key);
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
