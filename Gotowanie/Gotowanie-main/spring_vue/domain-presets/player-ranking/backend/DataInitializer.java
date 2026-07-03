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
        log.info("Seeding player-ranking demo data...");

        userService.ensureUser("Administrator", "admin@ranking.local", "admin123", Role.ADMIN);
        userService.ensureUser("Moderator", "mod@ranking.local", "mod123", Role.OPERATOR);
        UserEntity alice = userService.ensureUser("Alice", "alice@ranking.local", "user123", Role.USER);
        UserEntity bob = userService.ensureUser("Bob", "bob@ranking.local", "user123", Role.USER);
        UserEntity charlie = userService.ensureUser("Charlie", "charlie@ranking.local", "user123", Role.USER);

        ResourceEntity chess = saveResource("Mistrzostwa Szachowe 2025",
                "Turniej szachowy o puchar prezesa klubu",
                "BOARD_GAME", ResourceStatus.ACTIVE, null, 2,
                metadata("Mistrzostwa Szachowe 2025", "BOARD_GAME", 2, "2025-06-15"));
        ResourceEntity catan = saveResource("Turniej Catan o Puchar Watahy",
                "Klasyczna rozgrywka w Osadników z Catanu",
                "BOARD_GAME", ResourceStatus.ACTIVE, null, 4,
                metadata("Turniej Catan o Puchar Watahy", "BOARD_GAME", 4, "2025-07-01"));
        ResourceEntity lol = saveResource("League of Legends: Zimowy Turniej",
                "Turniej 5v5 w League of Legends",
                "ESPORT", ResourceStatus.ACTIVE, null, 10,
                metadata("League of Legends: Zimowy Turniej", "ESPORT", 10, "2025-08-20"));
        ResourceEntity cs2 = saveResource("Puchar Kart Graficznych - CS2",
                "Turniej Counter-Strike 2",
                "ESPORT", ResourceStatus.ACTIVE, null, 10,
                metadata("Puchar Kart Graficznych - CS2", "ESPORT", 10, "2025-09-05"));
        ResourceEntity uno = saveResource("Turniej Uno w Stylu Latynoskim",
                "Imprezowy turniej Uno z muzyką i nagrodami",
                "BOARD_GAME", ResourceStatus.ACTIVE, null, 6,
                metadata("Turniej Uno w Stylu Latynoskim", "BOARD_GAME", 6, "2025-10-12"));

        seedRequest(alice, chess, RequestStatus.COMPLETED, requestMetadata("Alice", 95, 1, "Bob", "2025-06-15", "Zwycięstwo", "Dominacja na szachownicy"));
        seedRequest(bob, chess, RequestStatus.COMPLETED, requestMetadata("Bob", 78, 2, "Alice", "2025-06-15", "Porazka", "Blisko wygranej"));
        seedRequest(alice, catan, RequestStatus.COMPLETED, requestMetadata("Alice", 82, 3, "Charlie", "2025-07-01", "Zwycięstwo", "Szybka kolonizacja"));
        seedRequest(charlie, catan, RequestStatus.COMPLETED, requestMetadata("Charlie", 88, 1, "Alice", "2025-07-01", "Zwycięstwo", "Mistrzowski handel"));
        seedRequest(bob, lol, RequestStatus.PENDING, requestMetadata("Bob", 60, 2, "ProPlayerX", "2025-08-20", "Porazka", "Twardy przeciwnik"));
        seedRequest(alice, cs2, RequestStatus.CONFIRMED, requestMetadata("Alice", 45, 4, "FragMaster", "2025-09-05", "Porazka", "Brak zgrania"));

        log.info("Seed complete. Demo logins: admin@ranking.local/admin123, alice@ranking.local/user123, bob@ranking.local/user123, charlie@ranking.local/user123");
    }

    private Map<String, Object> metadata(String gameName, String gameType, int maxPlayers, String tournamentDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("gameName", gameName);
        map.put("gameType", gameType);
        map.put("maxPlayers", maxPlayers);
        map.put("tournamentDate", tournamentDate);
        return map;
    }

    private Map<String, Object> requestMetadata(String playerName, int score, int rank, String opponentName, String matchDate, String result, String notes) {
        Map<String, Object> map = new HashMap<>();
        map.put("playerName", playerName);
        map.put("score", score);
        map.put("rank", rank);
        map.put("opponentName", opponentName);
        map.put("matchDate", matchDate);
        map.put("result", result);
        if (notes != null) {
            map.put("notes", notes);
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

    private void seedRequest(UserEntity owner, ResourceEntity resource,
                             RequestStatus status, Map<String, Object> metadata) {
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
