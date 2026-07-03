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
        log.info("Seeding dog-daycare demo data...");
        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Opiekun", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Właściciel", "user@zpo.local", "user123", Role.USER);

        ResourceEntity small = saveResource("Strefa Mała", "Małe psy i spokojna opieka", ResourceStatus.ACTIVE, new BigDecimal("12.00"),
                metadata("Strefa Mała", 12, "SMALL,MEDIUM", true, "STANDARD", 2));
        ResourceEntity active = saveResource("Strefa Aktywna", "Duży wybieg", ResourceStatus.ACTIVE, new BigDecimal("15.00"),
                metadata("Strefa Aktywna", 18, "MEDIUM,LARGE", true, "ACTIVE", 3));
        ResourceEntity medical = saveResource("Strefa Medyczna", "Opieka z lekami", ResourceStatus.ACTIVE, new BigDecimal("18.00"),
                metadata("Strefa Medyczna", 10, "SMALL,MEDIUM,LARGE", false, "MEDICAL", 2));
        saveResource("Strefa Senior", "Spokojne psy", ResourceStatus.ACTIVE, new BigDecimal("14.00"),
                metadata("Strefa Senior", 9, "SMALL,MEDIUM", false, "STANDARD", 1));
        saveResource("Strefa Park", "Duży dzienny wybieg", ResourceStatus.ACTIVE, new BigDecimal("16.00"),
                metadata("Strefa Park", 20, "SMALL,MEDIUM,LARGE", true, "ACTIVE", 4));
        saveResource("Strefa remont", "Wyłączona", ResourceStatus.UNAVAILABLE, new BigDecimal("10.00"),
                metadata("Strefa remont", 6, "SMALL", false, "STANDARD", 1));

        seedRequest(user, small, RequestStatus.CONFIRMED,
                requestMetadata("Loki", "SMALL", 8, 6, false, true, "karma rano"));
        seedRequest(user, active, RequestStatus.PENDING,
                requestMetadata("Bruno", "LARGE", 32, 8, false, true, "bez drobiu"));
        seedRequest(user, medical, RequestStatus.CONFIRMED,
                requestMetadata("Mela", "MEDIUM", 18, 5, true, false, "tabletka o 13"));
        log.info("Seed complete. Logins: admin@zpo.local/admin123, operator@zpo.local/operator123, user@zpo.local/user123");
    }

    private Map<String, Object> metadata(String zoneName, int dailyCapacityPoints, String acceptedDogSizes,
                                         boolean hasOutdoorRun, String careLevel, int staffCount) {
        Map<String, Object> map = new HashMap<>();
        map.put("zoneName", zoneName);
        map.put("dailyCapacityPoints", dailyCapacityPoints);
        map.put("acceptedDogSizes", acceptedDogSizes);
        map.put("hasOutdoorRun", hasOutdoorRun);
        map.put("careLevel", careLevel);
        map.put("staffCount", staffCount);
        return map;
    }

    private Map<String, Object> requestMetadata(String dogName, String dogSize, int dogWeight, int stayHours,
                                                boolean needsMedication, boolean extraWalk, String feedingNotes) {
        Map<String, Object> map = new HashMap<>();
        map.put("dogName", dogName);
        map.put("dogSize", dogSize);
        map.put("dogWeight", dogWeight);
        map.put("stayHours", stayHours);
        map.put("needsMedication", needsMedication);
        map.put("extraWalk", extraWalk);
        map.put("feedingNotes", feedingNotes);
        return map;
    }

    private ResourceEntity saveResource(String name, String description, ResourceStatus status,
                                        BigDecimal baseValue, Map<String, Object> metadata) {
        ResourceEntity entity = new ResourceEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setType("CARE_ZONE");
        entity.setStatus(status);
        entity.setBaseValue(baseValue);
        entity.setCapacityValue((Integer) metadata.get("dailyCapacityPoints"));
        entity.setMetadata(metadata);
        return resourceRepository.save(entity);
    }

    private void seedRequest(UserEntity owner, ResourceEntity resource, RequestStatus status,
                             Map<String, Object> metadata) {
        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(resource.getId(), RequestStatus.activeStatuses());
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(resource, null, null, null, metadata, existing, profile));
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
