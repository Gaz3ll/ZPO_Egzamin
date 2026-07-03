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
        log.info("Seeding vr-arcade demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Obsługa", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Jan Kowalski", "user@zpo.local", "user123", Role.USER);

        ResourceEntity arena = saveResource("Strefa Arena", "Strefa multiplayer FPS",
                "ZONE", ResourceStatus.ACTIVE, new BigDecimal("35.00"), 8,
                metadata("Arena", 8, "FPS,BATTLE_ROYALE,COOP", 8, true));
        ResourceEntity chill = saveResource("Strefa Chill", "Gry relaksacyjne i przygodowe",
                "ZONE", ResourceStatus.ACTIVE, new BigDecimal("30.00"), 4,
                metadata("Chill", 4, "ADVENTURE,PUZZLE,RELAX", 4, false));
        saveResource("Strefa Racing", "Symulatory wyścigów",
                "ZONE", ResourceStatus.ACTIVE, new BigDecimal("40.00"), 6,
                metadata("Racing", 6, "RACING,SIMULATOR", 6, true));
        saveResource("Strefa Horror", "Gry grozy VR",
                "ZONE", ResourceStatus.ACTIVE, new BigDecimal("38.00"), 4,
                metadata("Horror", 4, "HORROR,ESCAPE", 4, true));
        saveResource("Strefa Kids", "Gry rodzinne",
                "ZONE", ResourceStatus.ACTIVE, new BigDecimal("25.00"), 5,
                metadata("Kids", 5, "FAMILY,PUZZLE", 5, false));
        saveResource("Strefa serwisowa", "Strefa w serwisie",
                "ZONE", ResourceStatus.UNAVAILABLE, new BigDecimal("30.00"), 4,
                metadata("Serwis", 4, "FPS", 4, false));

        seedRequest(user, arena, 3, RequestStatus.CONFIRMED, requestMetadata(3, "FPS", "Jan Kowalski", "VR-A-001"));
        seedRequest(user, arena, 2, RequestStatus.PENDING, requestMetadata(2, "COOP", "Jan Kowalski", "VR-A-002"));
        seedRequest(user, chill, 2, RequestStatus.CONFIRMED, requestMetadata(2, "PUZZLE", "Jan Kowalski", "VR-C-001"));

        log.info("Seed complete. Logins: admin@zpo.local/admin123, operator@zpo.local/operator123, user@zpo.local/user123");
    }

    private Map<String, Object> metadata(String zoneName, int totalHeadsets, String gameTypes,
                                         int maxPlayers, boolean hasMultiplayer) {
        Map<String, Object> map = new HashMap<>();
        map.put("zoneName", zoneName);
        map.put("totalHeadsets", totalHeadsets);
        map.put("gameTypes", gameTypes);
        map.put("maxPlayers", maxPlayers);
        map.put("hasMultiplayer", hasMultiplayer);
        return map;
    }

    private Map<String, Object> requestMetadata(int playersCount, String gameType, String customerName, String qrCode) {
        Map<String, Object> map = new HashMap<>();
        map.put("playersCount", playersCount);
        map.put("gameType", gameType);
        map.put("customerName", customerName);
        map.put("qrCode", qrCode);
        return map;
    }

    private ResourceEntity saveResource(String name, String description, String type,
                                        ResourceStatus status, BigDecimal baseValue, int totalHeadsets,
                                        Map<String, Object> metadata) {
        ResourceEntity entity = new ResourceEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setType(type);
        entity.setStatus(status);
        entity.setBaseValue(baseValue);
        entity.setCapacityValue(totalHeadsets);
        entity.setMetadata(metadata);
        return resourceRepository.save(entity);
    }

    private void seedRequest(UserEntity owner, ResourceEntity resource, int players,
                             RequestStatus status, Map<String, Object> metadata) {
        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(
                resource.getId(), RequestStatus.activeStatuses());
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, null, null, players, metadata, existing, profile));

        RequestEntity entity = new RequestEntity();
        entity.setOwnerId(owner.getId());
        entity.setResourceId(resource.getId());
        entity.setStatus(status);
        entity.setQuantity(players);
        entity.setMetadata(metadata);
        entity.setCalculatedValue(result.calculatedValue());
        entity.setAlgorithmBreakdown(result.breakdown());
        requestRepository.save(entity);
    }
}
