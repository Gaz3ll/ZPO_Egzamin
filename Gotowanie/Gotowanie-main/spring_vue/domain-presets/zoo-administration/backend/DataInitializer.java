package pl.zpo.app.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
        log.info("Seeding zoo-administration demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Operator", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Jan Kowalski", "user@zpo.local", "user123", Role.USER);

        ResourceEntity savanna = saveResource("Sawanna A", "Wybieg dużych kotów z oddzielną strefą karmienia",
                "ENCLOSURE", ResourceStatus.ACTIVE, new BigDecimal("90.00"), 8,
                metadata("lwy", 4, "CRITICAL", "CARNIVORE", "HIGH", "ZONE_A", false, "2026-06-20"));
        ResourceEntity primates = saveResource("Małpiarnia", "Sektor małp z konstrukcjami wspinaczkowymi",
                "ENCLOSURE", ResourceStatus.ACTIVE, new BigDecimal("70.00"), 20,
                metadata("małpy", 12, "MEDIUM", "OMNIVORE", "HIGH", "ZONE_B", false, "2026-06-18"));
        saveResource("Akwarium tropikalne", "Zbiorniki ryb tropikalnych z systemem filtracji",
                "AQUARIUM", ResourceStatus.ACTIVE, new BigDecimal("55.00"), 60,
                metadata("ryby tropikalne", 45, "LOW", "AQUATIC", "MEDIUM", "ZONE_C", false, "2026-06-25"));
        saveResource("Woliera ptaków drapieżnych", "Woliera dla ptaków wymagających zabezpieczeń",
                "AVIARY", ResourceStatus.ACTIVE, new BigDecimal("80.00"), 12,
                metadata("ptaki drapieżne", 7, "HIGH", "CARNIVORE", "MEDIUM", "ZONE_D", false, "2026-06-22"));
        saveResource("Terrarium gadów", "Sektor gadów jadowitych i dusicieli",
                "TERRARIUM", ResourceStatus.ACTIVE, new BigDecimal("85.00"), 15,
                metadata("węże", 9, "HIGH", "CARNIVORE", "EXTREME", "ZONE_E", false, "2026-06-19"));
        ResourceEntity quarantine = saveResource("Kwarantanna", "Izolowany sektor dla zwierząt wymagających obserwacji",
                "ENCLOSURE", ResourceStatus.ACTIVE, new BigDecimal("110.00"), 10,
                metadata("różne gatunki", 3, "HIGH", "SPECIAL", "EXTREME", "ZONE_Q", true, "2026-06-28"));

        Instant base = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        seedRequest(user, savanna, base.plus(8, ChronoUnit.HOURS), base.plus(9, ChronoUnit.HOURS),
                4, RequestStatus.CONFIRMED,
                requestMetadata("FEEDING", "HIGH", false, true, "NORMAL", "Karmienie lwów z podwójną obsadą"));
        seedRequest(user, primates, base.plus(10, ChronoUnit.HOURS), base.plus(12, ChronoUnit.HOURS),
                12, RequestStatus.PENDING,
                requestMetadata("CLEANING", "NORMAL", false, false, "LOW", "Sprzątanie małpiarni po porannym karmieniu"));
        seedRequest(user, quarantine, base.plus(13, ChronoUnit.HOURS), base.plus(14, ChronoUnit.HOURS),
                3, RequestStatus.CONFIRMED,
                requestMetadata("VET_CHECK", "URGENT", true, true, "HIGH", "Kontrola weterynaryjna kwarantanny"));

        log.info("Seed complete. Demo logins: admin@zpo.local/admin123, operator@zpo.local/operator123, user@zpo.local/user123");
    }

    private Map<String, Object> metadata(String animalSpecies, int animalCount, String dangerLevel,
                                         String feedingType, String cleaningDifficulty, String keeperZone,
                                         boolean quarantine, String lastInspectionDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("animalSpecies", animalSpecies);
        map.put("animalCount", animalCount);
        map.put("dangerLevel", dangerLevel);
        map.put("feedingType", feedingType);
        map.put("cleaningDifficulty", cleaningDifficulty);
        map.put("keeperZone", keeperZone);
        map.put("isQuarantine", quarantine);
        map.put("lastInspectionDate", lastInspectionDate);
        return map;
    }

    private Map<String, Object> requestMetadata(String taskType, String priority, boolean requiresVet,
                                                boolean requiresTwoKeepers, String animalHealthRisk, String notes) {
        Map<String, Object> map = new HashMap<>();
        map.put("taskType", taskType);
        map.put("priority", priority);
        map.put("requiresVet", requiresVet);
        map.put("requiresTwoKeepers", requiresTwoKeepers);
        map.put("animalHealthRisk", animalHealthRisk);
        map.put("notes", notes);
        return map;
    }

    private ResourceEntity saveResource(String name, String description, String type,
                                        ResourceStatus status, BigDecimal baseValue, Integer capacity,
                                        Map<String, Object> metadata) {
        ResourceEntity entity = new ResourceEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setType(type);
        entity.setStatus(status);
        entity.setBaseValue(baseValue);
        entity.setCapacityValue(capacity);
        entity.setMetadata(metadata);
        return resourceRepository.save(entity);
    }

    private void seedRequest(UserEntity owner, ResourceEntity resource, Instant start, Instant end,
                             Integer quantity, RequestStatus status, Map<String, Object> metadata) {
        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(
                resource.getId(), RequestStatus.activeStatuses());
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, start, end, quantity, metadata, existing, profile));

        RequestEntity entity = new RequestEntity();
        entity.setOwnerId(owner.getId());
        entity.setResourceId(resource.getId());
        entity.setStatus(status);
        entity.setStartAt(start);
        entity.setEndAt(end);
        entity.setQuantity(quantity);
        entity.setMetadata(metadata);
        entity.setCalculatedValue(result.calculatedValue());
        entity.setAlgorithmBreakdown(result.breakdown());
        requestRepository.save(entity);
    }
}
