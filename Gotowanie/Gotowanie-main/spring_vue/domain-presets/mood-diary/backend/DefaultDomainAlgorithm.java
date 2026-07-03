package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.availability.CapacityMatcher;
import pl.zpo.app.domain.availability.TimeCollisionDetector;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.config.PricingUnit;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.request.RequestRepository;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceRepository;
import pl.zpo.app.domain.resource.ResourceStatus;

@Component
public class DefaultDomainAlgorithm implements DomainAlgorithm {

    private final TimeCollisionDetector collisionDetector;
    private final CapacityMatcher capacityMatcher;
    private final ResourceRepository resourceRepository;
    private final RequestRepository requestRepository;

    public DefaultDomainAlgorithm(TimeCollisionDetector collisionDetector,
                                  CapacityMatcher capacityMatcher,
                                  ResourceRepository resourceRepository,
                                  RequestRepository requestRepository) {
        this.collisionDetector = collisionDetector;
        this.capacityMatcher = capacityMatcher;
        this.resourceRepository = resourceRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        if (!isMoodDiaryInput(input)) {
            return evaluateGeneric(input);
        }

        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();
        ResourceEntity resource = input.resource();

        if (resource == null) {
            breakdown.addRule("RESOURCE_CHECK: brak dnia");
            return DomainAlgorithmResult.failure(List.of("Dzień jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("RESOURCE_CHECK: dzień nieaktywny (" + resource.getStatus() + ")");
            return DomainAlgorithmResult.failure(List.of("Dzień nie jest aktywny"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        String entryDateStr = readString(resource.getMetadata(), "entryDate");
        if (entryDateStr == null) {
            breakdown.addRule("ENTRY_DATE_CHECK: brak daty w zasobie");
            return DomainAlgorithmResult.failure(List.of("Dzień nie ma oznaczonej daty (entryDate)"), breakdown.build());
        }
        LocalDate entryDate;
        try {
            entryDate = LocalDate.parse(entryDateStr.length() > 10 ? entryDateStr.substring(0, 10) : entryDateStr);
        } catch (DateTimeParseException ex) {
            breakdown.addRule("ENTRY_DATE_CHECK: nieprawidłowy format daty");
            return DomainAlgorithmResult.failure(List.of("Nieprawidłowy format daty: " + entryDateStr), breakdown.build());
        }
        breakdown.addRule("ENTRY_DATE_CHECK: " + entryDate);
        breakdown.addNote("entryDate=" + entryDate);

        Map<String, Object> meta = input.requestMetadata();

        Integer moodScore = readInteger(meta, "moodScore");
        if (moodScore == null || moodScore < 1 || moodScore > 10) {
            errors.add("Ocena nastroju (moodScore) musi być w zakresie 1-10");
            breakdown.addRule("MOOD_SCORE_CHECK: nieprawidłowa wartość (" + moodScore + ")");
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        breakdown.addRule("MOOD_SCORE_CHECK: ok (" + moodScore + "/10)");

        String determinedLabel = determineMoodLabel(moodScore);
        breakdown.addRule("MOOD_LABEL: " + determinedLabel + " (score=" + moodScore + ")");
        breakdown.addNote("moodLabel=" + determinedLabel);

        List<RequestEntity> activeExisting = activeExistingFor(resource, input.existingRequests());
        boolean dayOccupied = !activeExisting.isEmpty();
        if (dayOccupied) {
            errors.add("Wpis nastroju dla dnia " + entryDate + " już istnieje");
            breakdown.addRule("DAY_COLLISION_CHECK: kolizja (" + entryDate + ")");
        } else {
            breakdown.addRule("DAY_COLLISION_CHECK: brak kolizji");
        }

        WeekAverageResult weekResult = computeWeekStats(entryDate, moodScore, breakdown);
        if (weekResult != null) {
            breakdown.addNote("weeklyAverage=" + weekResult.average.toPlainString());
            breakdown.addNote("weeklyEntryCount=" + weekResult.count);
            breakdown.addNote("moodDistribution=" + weekResult.distribution.toString());
        }

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }
        return DomainAlgorithmResult.success(BigDecimal.valueOf(moodScore), resource.getId(), breakdown.build());
    }

    private String determineMoodLabel(int score) {
        if (score >= 9) return "GREAT";
        if (score >= 7) return "GOOD";
        if (score >= 5) return "NEUTRAL";
        if (score >= 3) return "BAD";
        return "AWFUL";
    }

    private WeekAverageResult computeWeekStats(LocalDate entryDate, int currentScore,
                                                AlgorithmBreakdownBuilder breakdown) {
        WeekFields weekFields = WeekFields.ISO;
        int targetWeek = entryDate.get(weekFields.weekOfWeekBasedYear());
        int targetYear = entryDate.get(weekFields.weekBasedYear());

        List<RequestEntity> allRequests = requestRepository.findAll();
        List<RequestEntity> weekRequests = new ArrayList<>();

        for (RequestEntity req : allRequests) {
            if (req.getStatus() == null || !req.getStatus().isActive()) continue;
            ResourceEntity reqResource = resourceRepository.findById(req.getResourceId()).orElse(null);
            if (reqResource == null) continue;
            String reqDateStr = readString(reqResource.getMetadata(), "entryDate");
            if (reqDateStr == null) continue;
            try {
                LocalDate reqDate = LocalDate.parse(reqDateStr.length() > 10 ? reqDateStr.substring(0, 10) : reqDateStr);
                if (reqDate.get(weekFields.weekOfWeekBasedYear()) == targetWeek
                        && reqDate.get(weekFields.weekBasedYear()) == targetYear) {
                    weekRequests.add(req);
                }
            } catch (DateTimeParseException ignored) {
            }
        }

        if (weekRequests.isEmpty()) {
            breakdown.addRule("WEEKLY_AVERAGE: tylko bieżący wpis (brak innych w tym tygodniu)");
            return new WeekAverageResult(BigDecimal.valueOf(currentScore), 1, Map.of(determineMoodLabel(currentScore), 1));
        }

        List<Integer> scores = new ArrayList<>();
        Map<String, Integer> distribution = new HashMap<>();
        for (RequestEntity req : weekRequests) {
            Integer score = readInteger(req.getMetadata(), "moodScore");
            if (score != null && score >= 1 && score <= 10) {
                scores.add(score);
                String label = determineMoodLabel(score);
                distribution.merge(label, 1, Integer::sum);
            }
        }

        if (scores.isEmpty()) {
            breakdown.addRule("WEEKLY_AVERAGE: tylko bieżący wpis (brak ocen w innych)");
            return new WeekAverageResult(BigDecimal.valueOf(currentScore), 1, Map.of(determineMoodLabel(currentScore), 1));
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (int s : scores) {
            sum = sum.add(BigDecimal.valueOf(s));
        }
        BigDecimal avg = sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
        breakdown.addRule("WEEKLY_AVERAGE: " + avg.toPlainString() + " (z " + scores.size() + " wpisów)");
        return new WeekAverageResult(avg, scores.size(), distribution);
    }

    private record WeekAverageResult(BigDecimal average, int count, Map<String, Integer> distribution) {}

    private boolean isMoodDiaryInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "moodScore", "moodLabel", "notes", "activities", "sleepHours")
                || (input.resource() != null && hasAny(input.resource().getMetadata(), "entryDate"));
    }

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

    private int orDefault(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private String orDefault(String value, String fallback) {
        return value == null ? fallback : value;
    }
}
