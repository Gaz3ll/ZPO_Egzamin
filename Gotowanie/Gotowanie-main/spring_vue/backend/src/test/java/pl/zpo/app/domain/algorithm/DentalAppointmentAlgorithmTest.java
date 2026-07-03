package pl.zpo.app.domain.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.zpo.app.domain.availability.TimeCollisionDetector;
import pl.zpo.app.domain.config.AlgorithmMode;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.config.PricingUnit;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.request.RequestStatus;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;

class DentalAppointmentAlgorithmTest {

    private static final DomainProfile DENTAL_PROFILE = new DomainProfile(
            "Dental Clinic", "Dentist", "Dentists",
            "Appointment", "Appointments", "PLN",
            AlgorithmMode.TIME_AVAILABILITY_AND_CALCULATION,
            PricingUnit.PER_HOUR, true, false,
            List.of(), List.of());

    private final DentalAppointmentAlgorithm algorithm =
            new DentalAppointmentAlgorithm(new TimeCollisionDetector());

    @Test
    @DisplayName("Checkup with a specialist dentist — success with correct breakdown")
    void checkupWithSpecialist() {
        ResourceEntity dentist = dentist(1L, ResourceStatus.ACTIVE, new BigDecimal("120.00"),
                "GENERAL_DENTISTRY", "SPECIALIST", null);

        var start = java.time.Instant.parse("2026-07-01T10:00:00Z");
        var end = java.time.Instant.parse("2026-07-01T11:00:00Z");

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                dentist, start, end, null,
                Map.of("treatmentType", "CHECKUP"),
                List.of(), DENTAL_PROFILE));

        assertThat(result.success()).isTrue();
        assertThat(result.errors()).isEmpty();
        // Base: 120 × 1h = 120
        // Treatment multiplier: CHECKUP × 1.0 → no change
        // Experience: SPECIALIST × 1.8 → 120 × 1.8 = 216
        // (Base fee 120 + Experience premium 96)
        assertThat(result.calculatedValue()).isEqualByComparingTo("216.00");
        assertThat(result.breakdown().lines()).hasSize(2);
    }

    @Test
    @DisplayName("Root canal with junior dentist — correct treatment multiplier")
    void rootCanalWithJunior() {
        ResourceEntity dentist = dentist(1L, ResourceStatus.ACTIVE, new BigDecimal("80.00"),
                "ENDODONTICS", "JUNIOR", null);

        var start = java.time.Instant.parse("2026-07-01T10:00:00Z");
        var end = java.time.Instant.parse("2026-07-01T12:00:00Z");

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                dentist, start, end, null,
                Map.of("treatmentType", "ROOT_CANAL"),
                List.of(), DENTAL_PROFILE));

        assertThat(result.success()).isTrue();
        // Base: 80 × 2h = 160
        // Treatment: ROOT_CANAL × 3.0 → 480
        // Experience: JUNIOR × 1.0 → no change
        assertThat(result.calculatedValue()).isEqualByComparingTo("480.00");
    }

    @Test
    @DisplayName("Emergency extraction with senior surgeon — emergency surcharge applied")
    void emergencyExtractionWithSenior() {
        ResourceEntity dentist = dentist(1L, ResourceStatus.ACTIVE, new BigDecimal("130.00"),
                "SURGERY", "SENIOR", null);

        var start = java.time.Instant.parse("2026-07-01T14:00:00Z");
        var end = java.time.Instant.parse("2026-07-01T15:00:00Z");

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                dentist, start, end, null,
                Map.of("treatmentType", "EXTRACTION", "isEmergency", true),
                List.of(), DENTAL_PROFILE));

        assertThat(result.success()).isTrue();
        // Base: 130 × 1h = 130
        // Treatment: EXTRACTION × 2.0 → 260
        // Experience: SENIOR × 1.3 → 338
        // Emergency: +50% → 338 + 169 = 507
        assertThat(result.calculatedValue()).isEqualByComparingTo("507.00");
        assertThat(result.breakdown().lines()).hasSize(4); // base + treatment + experience + emergency
    }

    @Test
    @DisplayName("Time collision with existing appointment → failure")
    void timeCollisionDetected() {
        ResourceEntity dentist = dentist(1L, ResourceStatus.ACTIVE, new BigDecimal("100.00"),
                "GENERAL_DENTISTRY", "JUNIOR", null);

        var existing = List.of(existingAppointment(10L, 1L,
                java.time.Instant.parse("2026-07-01T10:00:00Z"),
                java.time.Instant.parse("2026-07-01T11:00:00Z"),
                RequestStatus.CONFIRMED));

        var start = java.time.Instant.parse("2026-07-01T10:30:00Z");
        var end = java.time.Instant.parse("2026-07-01T11:30:00Z");

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                dentist, start, end, null,
                Map.of("treatmentType", "FILLING"),
                existing, DENTAL_PROFILE));

        assertThat(result.success()).isFalse();
        assertThat(result.errors()).isNotEmpty();
        assertThat(result.errors().get(0)).contains("conflict");
    }

    @Test
    @DisplayName("Unavailable dentist → failure")
    void unavailableDentist() {
        ResourceEntity dentist = dentist(1L, ResourceStatus.UNAVAILABLE, new BigDecimal("100.00"),
                "GENERAL_DENTISTRY", "JUNIOR", null);

        var start = java.time.Instant.parse("2026-07-01T10:00:00Z");
        var end = java.time.Instant.parse("2026-07-01T11:00:00Z");

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                dentist, start, end, null,
                Map.of("treatmentType", "CHECKUP"),
                List.of(), DENTAL_PROFILE));

        assertThat(result.success()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }

    @Test
    @DisplayName("No treatment type specified — uses default multiplier 1.0")
    void noTreatmentType() {
        ResourceEntity dentist = dentist(1L, ResourceStatus.ACTIVE, new BigDecimal("100.00"),
                "GENERAL_DENTISTRY", "JUNIOR", null);

        var start = java.time.Instant.parse("2026-07-01T10:00:00Z");
        var end = java.time.Instant.parse("2026-07-01T10:30:00Z");

        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                dentist, start, end, null,
                Map.of(),
                List.of(), DENTAL_PROFILE));

        assertThat(result.success()).isTrue();
        // Base: 100 × 1h (30 min rounds up to 1) = 100
        // No treatment type → default 1.0
        // Experience: JUNIOR × 1.0 → no change
        assertThat(result.calculatedValue()).isEqualByComparingTo("100.00");
    }

    private static ResourceEntity dentist(Long id, ResourceStatus status, BigDecimal baseValue,
                                          String specialization, String experienceLevel,
                                          Map<String, Object> extraMetadata) {
        Map<String, Object> meta = new java.util.HashMap<>();
        meta.put("specialization", specialization);
        meta.put("experienceLevel", experienceLevel);
        meta.put("room", "101");
        if (extraMetadata != null) {
            meta.putAll(extraMetadata);
        }
        ResourceEntity e = new ResourceEntity();
        e.setId(id);
        e.setName("Dr. Test");
        e.setStatus(status);
        e.setBaseValue(baseValue);
        e.setMetadata(meta);
        return e;
    }

    private static RequestEntity existingAppointment(Long id, Long resourceId,
                                                     java.time.Instant start, java.time.Instant end,
                                                     RequestStatus status) {
        RequestEntity e = new RequestEntity();
        e.setId(id);
        e.setResourceId(resourceId);
        e.setOwnerId(99L);
        e.setStartAt(start);
        e.setEndAt(end);
        e.setStatus(status);
        return e;
    }
}
