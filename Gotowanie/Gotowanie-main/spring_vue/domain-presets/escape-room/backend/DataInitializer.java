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
        log.info("Seeding escape-room demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Game Master", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Jan Kowalski", "user@zpo.local", "user123", Role.USER);

        ResourceEntity horror = saveResource("Nawiedzony Dwór", "Pokój grozy dla odważnych",
                "ROOM", ResourceStatus.ACTIVE, new BigDecimal("160.00"),
                metadata("Nawiedzony Dwór", "HARD", "HORROR", 60, 5, true));
        ResourceEntity heist = saveResource("Skok na Bank", "Napad w stylu heist",
                "ROOM", ResourceStatus.ACTIVE, new BigDecimal("150.00"),
                metadata("Skok na Bank", "MEDIUM", "HEIST", 60, 6, true));
        saveResource("Laboratorium", "Ucieczka z laboratorium",
                "ROOM", ResourceStatus.ACTIVE, new BigDecimal("140.00"),
                metadata("Laboratorium", "MEDIUM", "SCIENCE", 60, 4, false));
        saveResource("Piramida", "Zagadki starożytnego Egiptu",
                "ROOM", ResourceStatus.ACTIVE, new BigDecimal("155.00"),
                metadata("Piramida", "HARD", "ADVENTURE", 75, 6, true));
        saveResource("Pokój Dziecięcy", "Łatwy pokój rodzinny",
                "ROOM", ResourceStatus.ACTIVE, new BigDecimal("120.00"),
                metadata("Pokój Dziecięcy", "EASY", "FAMILY", 45, 5, false));
        saveResource("Więzienie", "Ucieczka z celi (w remoncie)",
                "ROOM", ResourceStatus.UNAVAILABLE, new BigDecimal("150.00"),
                metadata("Więzienie", "EXPERT", "PRISON", 60, 4, true));

        Instant base = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS).plus(17, ChronoUnit.HOURS);
        seedRequest(user, horror, base, base.plus(1, ChronoUnit.HOURS), RequestStatus.CONFIRMED,
                requestMetadata("Nietoperze", 4, "HARD", "HORROR", true));
        seedRequest(user, heist, base.plus(1, ChronoUnit.HOURS), base.plus(2, ChronoUnit.HOURS),
                RequestStatus.PENDING, requestMetadata("Rabusie", 6, "MEDIUM", "HEIST", false));
        seedRequest(user, horror, base.plus(2, ChronoUnit.HOURS), base.plus(3, ChronoUnit.HOURS),
                RequestStatus.CONFIRMED, requestMetadata("Duchy", 3, "HARD", "HORROR", true));

        log.info("Seed complete. Logins: admin@zpo.local/admin123, operator@zpo.local/operator123, user@zpo.local/user123");
    }

    private Map<String, Object> metadata(String roomName, String difficulty, String theme,
                                         int durationMinutes, int maxPlayers, boolean depositRequired) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomName", roomName);
        map.put("difficulty", difficulty);
        map.put("theme", theme);
        map.put("durationMinutes", durationMinutes);
        map.put("maxPlayers", maxPlayers);
        map.put("depositRequired", depositRequired);
        return map;
    }

    private Map<String, Object> requestMetadata(String teamName, int playersCount, String preferredDifficulty,
                                                String preferredTheme, boolean depositPaid) {
        Map<String, Object> map = new HashMap<>();
        map.put("teamName", teamName);
        map.put("playersCount", playersCount);
        map.put("preferredDifficulty", preferredDifficulty);
        map.put("preferredTheme", preferredTheme);
        map.put("depositPaid", depositPaid);
        return map;
    }

    private ResourceEntity saveResource(String name, String description, String type,
                                        ResourceStatus status, BigDecimal baseValue,
                                        Map<String, Object> metadata) {
        ResourceEntity entity = new ResourceEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setType(type);
        entity.setStatus(status);
        entity.setBaseValue(baseValue);
        entity.setCapacityValue((Integer) metadata.get("maxPlayers"));
        entity.setMetadata(metadata);
        return resourceRepository.save(entity);
    }

    private void seedRequest(UserEntity owner, ResourceEntity resource, Instant start, Instant end,
                             RequestStatus status, Map<String, Object> metadata) {
        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(
                resource.getId(), RequestStatus.activeStatuses());
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, start, end, null, metadata, existing, profile));

        RequestEntity entity = new RequestEntity();
        entity.setOwnerId(owner.getId());
        entity.setResourceId(resource.getId());
        entity.setStatus(status);
        entity.setStartAt(start);
        entity.setEndAt(end);
        entity.setMetadata(metadata);
        entity.setCalculatedValue(result.calculatedValue());
        entity.setAlgorithmBreakdown(result.breakdown());
        requestRepository.save(entity);
    }
}
