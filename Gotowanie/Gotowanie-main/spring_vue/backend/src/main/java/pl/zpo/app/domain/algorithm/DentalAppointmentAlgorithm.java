package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.availability.TimeCollisionDetector;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;

@Component
@Primary
public class DentalAppointmentAlgorithm implements DomainAlgorithm {

    private static final Map<String, BigDecimal> TREATMENT_MULTIPLIERS = Map.ofEntries(
            Map.entry("CHECKUP", new BigDecimal("1.0")),
            Map.entry("FILLING", new BigDecimal("1.5")),
            Map.entry("ROOT_CANAL", new BigDecimal("3.0")),
            Map.entry("EXTRACTION", new BigDecimal("2.0")),
            Map.entry("CLEANING", new BigDecimal("0.8")),
            Map.entry("CROWN", new BigDecimal("4.0")),
            Map.entry("WHITENING", new BigDecimal("2.5"))
    );

    private static final Map<String, BigDecimal> EXPERIENCE_MULTIPLIERS = Map.of(
            "JUNIOR", BigDecimal.ONE,
            "SENIOR", new BigDecimal("1.3"),
            "SPECIALIST", new BigDecimal("1.8")
    );

    private static final BigDecimal EMERGENCY_RATE = new BigDecimal("0.5");

    private final TimeCollisionDetector collisionDetector;

    public DentalAppointmentAlgorithm(TimeCollisionDetector collisionDetector) {
        this.collisionDetector = collisionDetector;
    }

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        AlgorithmBreakdownBuilder br = new AlgorithmBreakdownBuilder(input.profile().currency());
        List<String> errors = new ArrayList<>();

        ResourceEntity dentist = Optional.ofNullable(input.resource()).orElse(null);
        if (dentist == null || dentist.getStatus() != ResourceStatus.ACTIVE) {
            String msg = dentist == null ? "Dentist is required" : "Dentist is not available";
            errors.add(msg);
            br.addRule("DENTIST_CHECK: failed - " + msg);
            return DomainAlgorithmResult.failure(errors, br.build());
        }
        br.addRule("DENTIST_CHECK: ok (" + dentist.getName() + ")");

        List<RequestEntity> existing = filterActiveFor(dentist, input.existingRequests());

        long durationHours = 1;
        Instant start = input.startAt();
        Instant end = input.endAt();

        if (start != null && end != null) {
            if (!start.isBefore(end)) {
                errors.add("Appointment start must be before end");
                br.addRule("TIME_RANGE: invalid");
            } else {
                durationHours = Math.max(1, ceilDiv(Duration.between(start, end).toMinutes(), 60));
                br.addRule("TIME_RANGE: ok (" + durationHours + " h)");

                List<RequestEntity> collisions = collisionDetector.findCollisions(start, end, existing);
                if (!collisions.isEmpty()) {
                    String ids = collisions.stream()
                            .map(r -> String.valueOf(r.getId()))
                            .collect(Collectors.joining(", "));
                    errors.add("Time slot conflicts with existing appointments: " + ids);
                    br.addRule("COLLISION: " + collisions.size() + " conflict(s)");
                    br.addNote("Conflicts with appointments: " + ids);
                } else {
                    br.addRule("COLLISION: none");
                }
            }
        } else if (input.profile().requiresTimeWindow()) {
            errors.add("Appointment time window is required");
            br.addRule("TIME_RANGE: missing");
        }

        BigDecimal calculatedValue = null;
        if (input.profile().algorithmMode().calculatesValue()) {
            calculatedValue = calculateCost(dentist, input.requestMetadata(), durationHours, br);
        }

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, br.build());
        }
        return DomainAlgorithmResult.success(calculatedValue, dentist.getId(), br.build());
    }

    private BigDecimal calculateCost(ResourceEntity dentist, Map<String, Object> requestMetadata,
                                     long hours, AlgorithmBreakdownBuilder br) {
        BigDecimal baseRate = Optional.ofNullable(dentist.getBaseValue())
                .filter(v -> v.signum() > 0)
                .orElse(BigDecimal.ZERO);

        BigDecimal baseCost = baseRate.multiply(BigDecimal.valueOf(hours));
        br.addLine("Base fee", baseCost, baseRate + " PLN/h × " + hours + " h");
        br.addRule("COST_BASE: " + baseRate + " × " + hours);

        BigDecimal treatmentMultiplier = readString(requestMetadata, "treatmentType")
                .map(TREATMENT_MULTIPLIERS::get)
                .orElse(BigDecimal.ONE);

        BigDecimal afterTreatment = baseCost.multiply(treatmentMultiplier);
        if (treatmentMultiplier.compareTo(BigDecimal.ONE) != 0) {
            BigDecimal diff = afterTreatment.subtract(baseCost);
            br.addLine("Treatment adjustment", diff,
                    "× " + treatmentMultiplier + " (" + readString(requestMetadata, "treatmentType").orElse("?") + ")");
            br.addRule("COST_TREATMENT: × " + treatmentMultiplier);
        }

        BigDecimal expMultiplier = readString(dentist.getMetadata(), "experienceLevel")
                .map(EXPERIENCE_MULTIPLIERS::get)
                .orElse(BigDecimal.ONE);

        BigDecimal afterExperience = afterTreatment.multiply(expMultiplier);
        if (expMultiplier.compareTo(BigDecimal.ONE) > 0) {
            BigDecimal premium = afterExperience.subtract(afterTreatment);
            br.addLine("Experience premium", premium,
                    "× " + expMultiplier + " (" + readString(dentist.getMetadata(), "experienceLevel").orElse("?") + ")");
            br.addRule("COST_EXPERIENCE: × " + expMultiplier);
        }

        BigDecimal total = afterExperience;

        boolean emergency = Optional.ofNullable(requestMetadata)
                .map(m -> m.get("isEmergency"))
                .map(v -> v instanceof Boolean b ? b : "true".equals(String.valueOf(v)))
                .orElse(false);

        if (emergency) {
            BigDecimal surcharge = afterExperience.multiply(EMERGENCY_RATE)
                    .setScale(2, RoundingMode.HALF_UP);
            total = total.add(surcharge);
            br.addLine("Emergency surcharge", surcharge, "+50% for emergency");
            br.addRule("COST_EMERGENCY: +50%");
        }

        return br.total().amount();
    }

    private List<RequestEntity> filterActiveFor(ResourceEntity dentist, List<RequestEntity> existing) {
        return Optional.ofNullable(existing).orElse(List.<RequestEntity>of()).stream()
                .filter(r -> r.getStatus() != null && r.getStatus().isActive())
                .filter(r -> dentist.getId() != null && dentist.getId().equals(r.getResourceId()))
                .collect(Collectors.toList());
    }

    private Optional<String> readString(Map<String, Object> metadata, String key) {
        return Optional.ofNullable(metadata)
                .map(m -> m.get(key))
                .filter(v -> v instanceof String)
                .map(String.class::cast)
                .filter(s -> !s.isEmpty());
    }

    private static long ceilDiv(long value, long divisor) {
        return (value + divisor - 1) / divisor;
    }
}
