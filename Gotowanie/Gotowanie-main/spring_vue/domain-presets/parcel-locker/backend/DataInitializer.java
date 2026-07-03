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
        log.info("Seeding parcel-locker demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Operator", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Jan Kowalski", "user@zpo.local", "user123", Role.USER);

        ResourceEntity s1 = saveResource("Skrytka WAW-01-S1", "Mała skrytka przy wejściu",
                "S", ResourceStatus.ACTIVE, new BigDecimal("12.00"), 1,
                metadata("WAW-01-S1", "Warszawa, ul. Prosta 1", "S", "5.00", false));
        ResourceEntity s2 = saveResource("Skrytka WAW-01-S2", "Mała skrytka serwisowa",
                "S", ResourceStatus.ACTIVE, new BigDecimal("12.00"), 1,
                metadata("WAW-01-S2", "Warszawa, ul. Prosta 1", "S", "5.00", true));
        saveResource("Skrytka WAW-01-M1", "Średnia skrytka",
                "M", ResourceStatus.ACTIVE, new BigDecimal("14.00"), 1,
                metadata("WAW-01-M1", "Warszawa, ul. Prosta 1", "M", "12.00", false));
        saveResource("Skrytka WAW-01-M2", "Średnia skrytka zajęta operacyjnie",
                "M", ResourceStatus.ACTIVE, new BigDecimal("14.00"), 1,
                metadata("WAW-01-M2", "Warszawa, ul. Prosta 1", "M", "12.00", true));
        saveResource("Skrytka WAW-01-L1", "Duża skrytka",
                "L", ResourceStatus.ACTIVE, new BigDecimal("18.00"), 1,
                metadata("WAW-01-L1", "Warszawa, ul. Prosta 1", "L", "20.00", false));
        saveResource("Skrytka WAW-01-XL1", "Największa skrytka",
                "XL", ResourceStatus.ACTIVE, new BigDecimal("25.00"), 1,
                metadata("WAW-01-XL1", "Warszawa, ul. Prosta 1", "XL", "30.00", false));

        seedRequest(user, s1, RequestStatus.CONFIRMED,
                requestMetadata("Anna Nowak", "anna@example.com", "S", "2.20", "111222"));
        seedRequest(user, s2, RequestStatus.PENDING,
                requestMetadata("Piotr Zieliński", "piotr@example.com", "M", "7.50", "333444"));

        log.info("Seed complete. Demo logins: admin@zpo.local/admin123, operator@zpo.local/operator123, user@zpo.local/user123");
    }

    private Map<String, Object> metadata(String lockerCode, String location, String lockerSize,
                                         String maxWeight, boolean occupied) {
        Map<String, Object> map = new HashMap<>();
        map.put("lockerCode", lockerCode);
        map.put("location", location);
        map.put("lockerSize", lockerSize);
        map.put("maxWeight", maxWeight);
        map.put("isOccupied", occupied);
        return map;
    }

    private Map<String, Object> requestMetadata(String receiverName, String receiverEmail, String parcelSize,
                                                String weight, String pickupCode) {
        Map<String, Object> map = new HashMap<>();
        map.put("receiverName", receiverName);
        map.put("receiverEmail", receiverEmail);
        map.put("parcelSize", parcelSize);
        map.put("weight", weight);
        if (pickupCode != null) {
            map.put("pickupCode", pickupCode);
        }
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

    private void seedRequest(UserEntity owner, ResourceEntity startingResource, RequestStatus status,
                             Map<String, Object> metadata) {
        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(
                startingResource.getId(), RequestStatus.activeStatuses());
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                startingResource, null, null, null, metadata, existing, profile));

        Long assignedResourceId = result.assignedResourceId() != null
                ? result.assignedResourceId()
                : startingResource.getId();

        RequestEntity entity = new RequestEntity();
        entity.setOwnerId(owner.getId());
        entity.setResourceId(assignedResourceId);
        entity.setStatus(status);
        entity.setMetadata(metadata);
        entity.setCalculatedValue(result.calculatedValue());
        entity.setAlgorithmBreakdown(result.breakdown());
        requestRepository.save(entity);
    }
}
