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
        log.info("Seeding movie-ratings demo data...");

        userService.ensureUser("Administrator", "admin@filmy.local", "admin123", Role.ADMIN);
        userService.ensureUser("Moderator", "mod@filmy.local", "mod123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Jan Kowalski", "user@filmy.local", "user123", Role.USER);

        ResourceEntity inception = saveResource("Incepcja",
                "Genialny thriller sci-fi o śnieniu na wielu poziomach",
                "SCI_FI", ResourceStatus.ACTIVE, null, null,
                metadata("SCI_FI", 2010, "Christopher Nolan", 148));
        ResourceEntity intouchables = saveResource("Nietykalni",
                "Wzruszająca historia przyjaźni sparaliżowanego arystokraty i opiekuna z trudnej dzielnicy",
                "DRAMA", ResourceStatus.ACTIVE, null, null,
                metadata("DRAMA", 2011, "Olivier Nakache", 112));
        ResourceEntity shrek = saveResource("Shrek",
                "Kultowa animacja o ogrze, który uczy się kochać",
                "ANIMATION", ResourceStatus.ACTIVE, null, null,
                metadata("ANIMATION", 2001, "Andrew Adamson", 90));
        ResourceEntity shawshank = saveResource("Skazani na Shawshank",
                "Dramat więzienny o nadziei i przyjaźni",
                "DRAMA", ResourceStatus.ACTIVE, null, null,
                metadata("DRAMA", 1994, "Frank Darabont", 142));
        ResourceEntity pulpFiction = saveResource("Pulp Fiction",
                "Kultowa opowieść o płatnych mordercach i przestępczym półświatku Los Angeles",
                "THRILLER", ResourceStatus.ACTIVE, null, null,
                metadata("THRILLER", 1994, "Quentin Tarantino", 154));
        ResourceEntity lionKing = saveResource("Król Lew",
                "Animowana opowieść o lwie Simbie i jego drodze do objęcia tronu",
                "ANIMATION", ResourceStatus.ACTIVE, null, null,
                metadata("ANIMATION", 1994, "Roger Allers", 88));

        seedRequest(user, inception, RequestStatus.COMPLETED, requestMetadata("5", "Arcydzieło kinematografii!", "2024-01-15", "Netflix"));
        seedRequest(user, intouchables, RequestStatus.COMPLETED, requestMetadata("5", "Wzruszający i zabawny jednocześnie", "2024-02-10", "Kino"));
        seedRequest(user, shrek, RequestStatus.COMPLETED, requestMetadata("4", "Ponadczasowa klasyka", "2024-03-05", "Disney+"));

        log.info("Seed complete. Demo logins: admin@filmy.local/admin123, mod@filmy.local/mod123, user@filmy.local/user123");
    }

    private Map<String, Object> metadata(String genre, int releaseYear, String director, int durationMinutes) {
        Map<String, Object> map = new HashMap<>();
        map.put("genre", genre);
        map.put("releaseYear", releaseYear);
        map.put("director", director);
        map.put("durationMinutes", durationMinutes);
        return map;
    }

    private Map<String, Object> requestMetadata(String rating, String review, String watchDate, String platform) {
        Map<String, Object> map = new HashMap<>();
        map.put("rating", rating);
        if (review != null) {
            map.put("review", review);
        }
        if (watchDate != null) {
            map.put("watchDate", watchDate);
        }
        if (platform != null) {
            map.put("platform", platform);
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
