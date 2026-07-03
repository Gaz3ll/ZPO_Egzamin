package pl.zpo.app.config;

import java.math.BigDecimal;
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
        log.info("Seeding fitness-classes demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Recepcja fitness", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Anna Nowak", "user@zpo.local", "user123", Role.USER);

        ResourceEntity yoga = saveResource("Poranna joga", "Spokojne zajęcia dla początkujących",
                ResourceStatus.ACTIVE, new BigDecimal("45.00"),
                metadata("Poranna joga", "Marta Zielińska", "BEGINNER", 12, "MAT", "45.00"));
        ResourceEntity hiit = saveResource("HIIT Express", "Intensywny trening obwodowy",
                ResourceStatus.ACTIVE, new BigDecimal("55.00"),
                metadata("HIIT Express", "Piotr Wójcik", "ADVANCED", 10, "DUMBBELLS", "55.00"));
        ResourceEntity pilates = saveResource("Pilates core", "Stabilizacja i mobilność",
                ResourceStatus.ACTIVE, new BigDecimal("50.00"),
                metadata("Pilates core", "Julia Lis", "INTERMEDIATE", 14, "MAT,BALL", "50.00"));
        saveResource("Zdrowy kręgosłup", "Zajęcia rehabilitacyjne", ResourceStatus.ACTIVE,
                new BigDecimal("40.00"), metadata("Zdrowy kręgosłup", "Adam Maj", "BEGINNER", 16, "MAT", "40.00"));
        saveResource("Cycling", "Trening na rowerach", ResourceStatus.ACTIVE,
                new BigDecimal("60.00"), metadata("Cycling", "Ola Krawczyk", "INTERMEDIATE", 18, "BIKE", "60.00"));
        saveResource("Sala techniczna", "Zajęcia chwilowo zawieszone", ResourceStatus.UNAVAILABLE,
                new BigDecimal("35.00"), metadata("Sala techniczna", "Rezerwa", "BEGINNER", 8, "MAT", "35.00"));

        seedRequest(user, yoga, RequestStatus.CONFIRMED,
                requestMetadata("Anna Nowak", "BEGINNER", "MONTHLY", false, "Brak"));
        seedRequest(user, hiit, RequestStatus.PENDING,
                requestMetadata("Anna Nowak", "ADVANCED", "DROP_IN", true, "Kontuzja kolana"));
        seedRequest(user, pilates, RequestStatus.CONFIRMED,
                requestMetadata("Anna Nowak", "INTERMEDIATE", "MULTISPORT", false, ""));

        log.info("Seed complete. Logins: admin@zpo.local/admin123, operator@zpo.local/operator123, user@zpo.local/user123");
    }

    private Map<String, Object> metadata(String className, String trainerName, String difficultyLevel,
                                         int capacity, String equipmentRequired, String dropInPrice) {
        Map<String, Object> map = new HashMap<>();
        map.put("className", className);
        map.put("trainerName", trainerName);
        map.put("difficultyLevel", difficultyLevel);
        map.put("capacity", capacity);
        map.put("equipmentRequired", equipmentRequired);
        map.put("dropInPrice", dropInPrice);
        return map;
    }

    private Map<String, Object> requestMetadata(String memberName, String preferredDifficulty, String passType,
                                                boolean needsEquipment, String healthNotes) {
        Map<String, Object> map = new HashMap<>();
        map.put("memberName", memberName);
        map.put("preferredDifficulty", preferredDifficulty);
        map.put("passType", passType);
        map.put("needsEquipment", needsEquipment);
        map.put("healthNotes", healthNotes);
        return map;
    }

    private ResourceEntity saveResource(String name, String description, ResourceStatus status,
                                        BigDecimal baseValue, Map<String, Object> metadata) {
        ResourceEntity entity = new ResourceEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setType("CLASS");
        entity.setStatus(status);
        entity.setBaseValue(baseValue);
        entity.setCapacityValue((Integer) metadata.get("capacity"));
        entity.setMetadata(metadata);
        return resourceRepository.save(entity);
    }

    private void seedRequest(UserEntity owner, ResourceEntity resource, RequestStatus status,
                             Map<String, Object> metadata) {
        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(
                resource.getId(), RequestStatus.activeStatuses());
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, null, null, 1, metadata, existing, profile));

        RequestEntity entity = new RequestEntity();
        entity.setOwnerId(owner.getId());
        entity.setResourceId(resource.getId());
        entity.setStatus(status);
        entity.setQuantity(1);
        entity.setMetadata(metadata);
        entity.setCalculatedValue(result.calculatedValue());
        entity.setAlgorithmBreakdown(result.breakdown());
        requestRepository.save(entity);
    }
}
