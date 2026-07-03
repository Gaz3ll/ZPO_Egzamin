package pl.zpo.app.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.algorithm.DomainAlgorithm;
import pl.zpo.app.domain.algorithm.DomainAlgorithmInput;
import pl.zpo.app.domain.algorithm.DomainAlgorithmResult;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.request.RequestRepository;
import pl.zpo.app.domain.request.RequestStatus;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceRepository;
import pl.zpo.app.domain.resource.ResourceStatus;
import pl.zpo.app.users.Role;
import pl.zpo.app.users.UserEntity;
import pl.zpo.app.users.UserRepository;
import pl.zpo.app.users.UserService;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final RequestRepository requestRepository;
    private final DomainAlgorithm algorithm;
    private final DomainProfile profile;
    private final boolean seedEnabled;

    public DataInitializer(UserService userService,
                           UserRepository userRepository,
                           ResourceRepository resourceRepository,
                           RequestRepository requestRepository,
                           DomainAlgorithm algorithm,
                           DomainProfile profile,
                           @Value("${app.seed.enabled:true}") boolean seedEnabled) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.requestRepository = requestRepository;
        this.algorithm = algorithm;
        this.profile = profile;
        this.seedEnabled = seedEnabled;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled || !PresetSeedSupport.prepareDomainSeed(resourceRepository, requestRepository, profile, log)) {
            return;
        }
        log.info("Seeding office-plant-care demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Office manager", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Jan Kowalski", "user@zpo.local", "user123", Role.USER);

        LocalDate today = LocalDate.now();
        ResourceEntity monstera = savePlant("Monstera przy oknie", "Duża monstera w sali kreatywnej",
                new BigDecimal("10.00"), 7,
                plantMetadata("Monstera deliciosa", "Sala kreatywna", "HIGH", 7, 30, 18, "MEDIUM", true, "GOOD"));
        ResourceEntity zamiokulkas = savePlant("Zamiokulkas w recepcji", "Odporny na zapominalskich",
                new BigDecimal("6.00"), 14,
                plantMetadata("Zamioculcas zamiifolia", "Recepcja", "LOW", 14, 60, 24, "EASY", false, "GOOD"));
        ResourceEntity sansewieria = savePlant("Sansewieria w open space", "Wężownica przy biurkach zespołu backendu",
                new BigDecimal("6.00"), 14,
                plantMetadata("Sansevieria trifasciata", "Open space", "LOW", 14, 45, 24, "EASY", true, "OK"));
        ResourceEntity fikus = savePlant("Fikus w kuchni", "Wymaga regularnej mgiełki",
                new BigDecimal("12.00"), 5,
                plantMetadata("Ficus benjamina", "Kuchnia", "MEDIUM", 5, 21, 12, "HARD", true, "BAD"));
        savePlant("Paprotka w łazience", "Lubi wilgoć, nie lubi przeciągów",
                new BigDecimal("8.00"), 3,
                plantMetadata("Nephrolepis exaltata", "Łazienka", "MEDIUM", 3, 30, 12, "MEDIUM", false, "OK"));
        savePlant("Sukulent na parapecie", "Mini ogródek na parapecie południowym",
                new BigDecimal("4.00"), 21,
                plantMetadata("Echeveria elegans", "Parapet południowy", "HIGH", 21, 90, 24, "EASY", false, "GOOD"));
        savePlant("Skrzydłokwiat w sali konferencyjnej", "Kwitnie, gdy jest zadbany",
                new BigDecimal("9.00"), 7,
                plantMetadata("Spathiphyllum", "Sala konferencyjna", "MEDIUM", 7, 30, 12, "MEDIUM", false, "CRITICAL"));
        saveUnavailablePlant("Bluszcz na korytarzu", "W trakcie kwarantanny po przędziorku",
                new BigDecimal("7.00"), 7,
                plantMetadata("Hedera helix", "Korytarz", "LOW", 7, 30, 12, "MEDIUM", false, "BAD"));

        seedTask(user, monstera, RequestStatus.CONFIRMED,
                taskMetadata("WATERING", today.minusDays(9), "Liście lekko opadnięte", null, false, "Jan Kowalski"));
        seedTask(user, zamiokulkas, RequestStatus.CONFIRMED,
                taskMetadata("ADOPTION", null, null, "Adoptuję na stałe do recepcji", false, "Jan Kowalski"));
        seedTask(user, fikus, RequestStatus.PENDING,
                taskMetadata("FERTILIZING", today.minusDays(30), "Żółknące liście", null, false, "Jan Kowalski"));
        seedTask(user, sansewieria, RequestStatus.PENDING,
                taskMetadata("HEALTH_CHECK", today.minusDays(40), "Sprawdzić korzenie", null, false, "Jan Kowalski"));

        log.info("Seed complete. Logins: admin@zpo.local/admin123, operator@zpo.local/operator123, user@zpo.local/user123");
    }

    private Map<String, Object> plantMetadata(String species, String location, String lightRequirement,
                                              int waterFrequencyDays, int fertilizeFrequencyDays,
                                              int repotFrequencyMonths, String difficulty,
                                              boolean isAdopted, String healthStatus) {
        Map<String, Object> map = new HashMap<>();
        map.put("species", species);
        map.put("location", location);
        map.put("lightRequirement", lightRequirement);
        map.put("waterFrequencyDays", waterFrequencyDays);
        map.put("fertilizeFrequencyDays", fertilizeFrequencyDays);
        map.put("repotFrequencyMonths", repotFrequencyMonths);
        map.put("difficulty", difficulty);
        map.put("isAdopted", isAdopted);
        map.put("healthStatus", healthStatus);
        return map;
    }

    private Map<String, Object> taskMetadata(String careType, LocalDate lastCareAt, String plantCondition,
                                              String notes, boolean isCompleted, String adopterName) {
        Map<String, Object> map = new HashMap<>();
        map.put("careType", careType);
        if (lastCareAt != null) {
            map.put("lastCareAt", lastCareAt.toString());
        }
        if (plantCondition != null) {
            map.put("plantCondition", plantCondition);
        }
        if (notes != null) {
            map.put("notes", notes);
        }
        map.put("isCompleted", isCompleted);
        map.put("adopterName", adopterName);
        return map;
    }

    private ResourceEntity savePlant(String name, String description, BigDecimal baseValue,
                                     int capacityValue, Map<String, Object> metadata) {
        return saveResource(name, description, ResourceStatus.ACTIVE, baseValue, capacityValue, metadata);
    }

    private void saveUnavailablePlant(String name, String description, BigDecimal baseValue,
                                      int capacityValue, Map<String, Object> metadata) {
        saveResource(name, description, ResourceStatus.UNAVAILABLE, baseValue, capacityValue, metadata);
    }

    private ResourceEntity saveResource(String name, String description, ResourceStatus status,
                                        BigDecimal baseValue, int capacityValue, Map<String, Object> metadata) {
        ResourceEntity entity = new ResourceEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setType("PLANT");
        entity.setStatus(status);
        entity.setBaseValue(baseValue);
        entity.setCapacityValue(capacityValue);
        entity.setMetadata(metadata);
        return resourceRepository.save(entity);
    }

    private void seedTask(UserEntity owner, ResourceEntity resource, RequestStatus status,
                          Map<String, Object> metadata) {
        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(
                resource.getId(), RequestStatus.activeStatuses());
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, null, null, null, metadata, existing, profile));

        RequestEntity entity = new RequestEntity();
        entity.setOwnerId(owner.getId());
        entity.setResourceId(resource.getId());
        entity.setStatus(status);
        entity.setMetadata(metadata);
        entity.setCalculatedValue(result.calculatedValue());
        entity.setAlgorithmBreakdown(result.breakdown());
        requestRepository.save(entity);
    }
}
