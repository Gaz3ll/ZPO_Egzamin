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

    public DataInitializer(UserService userService, UserRepository userRepository,
                           ResourceRepository resourceRepository, RequestRepository requestRepository,
                           DomainAlgorithm algorithm, DomainProfile profile,
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
        log.info("Seeding animal-shelter demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Opiekun adopcji", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Kandydat", "user@zpo.local", "user123", Role.USER);

        ResourceEntity luna = saveResource("Luna", "Spokojny pies do domu z ogrodem", ResourceStatus.ACTIVE,
                new BigDecimal("150.00"), metadata("Luna", "DOG", "LARGE", "CALM", true, "420"));
        ResourceEntity fika = saveResource("Fika", "Kotka do mieszkania", ResourceStatus.ACTIVE,
                new BigDecimal("80.00"), metadata("Fika", "CAT", "SMALL", "SHY", false, "180"));
        ResourceEntity max = saveResource("Max", "Aktywny pies dla doświadczonej osoby", ResourceStatus.ACTIVE,
                new BigDecimal("120.00"), metadata("Max", "DOG", "MEDIUM", "ACTIVE", false, "350"));
        saveResource("Tosia", "Królik domowy", ResourceStatus.ACTIVE,
                new BigDecimal("50.00"), metadata("Tosia", "RABBIT", "SMALL", "CALM", false, "120"));
        saveResource("Borys", "Duży pies po szkoleniu", ResourceStatus.ACTIVE,
                new BigDecimal("160.00"), metadata("Borys", "DOG", "LARGE", "ACTIVE", true, "480"));
        saveResource("Mila", "Kotka w trakcie leczenia", ResourceStatus.UNAVAILABLE,
                new BigDecimal("90.00"), metadata("Mila", "CAT", "SMALL", "CALM", false, "260"));

        seedRequest(user, luna, RequestStatus.PENDING,
                requestMetadata("Kandydat", "DOG", "HOUSE", true, "MEDIUM", 500));
        seedRequest(user, fika, RequestStatus.CONFIRMED,
                requestMetadata("Kandydat", "CAT", "FLAT", false, "LOW", 250));
        seedRequest(user, max, RequestStatus.PENDING,
                requestMetadata("Kandydat", "DOG", "FLAT", false, "HIGH", 400));

        log.info("Seed complete. Logins: admin@zpo.local/admin123, operator@zpo.local/operator123, user@zpo.local/user123");
    }

    private Map<String, Object> metadata(String animalName, String species, String size,
                                         String temperament, boolean needsGarden, String monthlyCost) {
        Map<String, Object> map = new HashMap<>();
        map.put("animalName", animalName);
        map.put("species", species);
        map.put("size", size);
        map.put("temperament", temperament);
        map.put("needsGarden", needsGarden);
        map.put("monthlyCost", monthlyCost);
        return map;
    }

    private Map<String, Object> requestMetadata(String adopterName, String preferredSpecies, String homeType,
                                                boolean hasGarden, String experienceLevel, int budgetMonthly) {
        Map<String, Object> map = new HashMap<>();
        map.put("adopterName", adopterName);
        map.put("preferredSpecies", preferredSpecies);
        map.put("homeType", homeType);
        map.put("hasGarden", hasGarden);
        map.put("experienceLevel", experienceLevel);
        map.put("budgetMonthly", budgetMonthly);
        return map;
    }

    private ResourceEntity saveResource(String name, String description, ResourceStatus status,
                                        BigDecimal baseValue, Map<String, Object> metadata) {
        ResourceEntity entity = new ResourceEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setType("ANIMAL");
        entity.setStatus(status);
        entity.setBaseValue(baseValue);
        entity.setCapacityValue(1);
        entity.setMetadata(metadata);
        return resourceRepository.save(entity);
    }

    private void seedRequest(UserEntity owner, ResourceEntity resource, RequestStatus status,
                             Map<String, Object> metadata) {
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, null, null, null, metadata, List.of(), profile));
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
