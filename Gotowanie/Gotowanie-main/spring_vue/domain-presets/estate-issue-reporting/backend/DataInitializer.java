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
        log.info("Seeding estate-issue-reporting demo data...");

        userService.ensureUser("Administrator Osiedla", "admin@zpo.local", "admin123", Role.ADMIN);
        UserEntity user = userService.ensureUser("Michał Nowak", "user@zpo.local", "user123", Role.USER);

        ResourceEntity awariaWody = saveResource("Awaria wody",
                "Problemy z dostawą wody, pęknięte rury, wycieki",
                "AWARIA_WODY", ResourceStatus.ACTIVE, null, null,
                resourceMetadata("AWARIA_WODY", "Budynek A", "HIGH"));
        ResourceEntity awariaPradu = saveResource("Awaria prądu",
                "Brak prądu, problemy z instalacją elektryczną",
                "AWARIA_PRADU", ResourceStatus.ACTIVE, null, null,
                resourceMetadata("AWARIA_PRADU", "Budynek B", "CRITICAL"));
        ResourceEntity usterkaWindy = saveResource("Usterka windy",
                "Problemy z windą osobową/towarową",
                "USTERKA_WINDA", ResourceStatus.ACTIVE, null, null,
                resourceMetadata("USTERKA_WINDA", "Budynek A", "HIGH"));
        saveResource("Inne",
                "Inne zgłoszenia niepasujące do powyższych kategorii",
                "INNE", ResourceStatus.ACTIVE, null, null,
                resourceMetadata("INNE", "Budynek B", "MEDIUM"));

        seedRequest(user, awariaWody, RequestStatus.PENDING,
                requestMetadata("Wyciek wody w piwnicy",
                        "Zauważyłem wyciek wody w piwnicy budynku A, woda zalewa korytarz",
                        "REPORTED", "Piwnica, Budynek A", "Michał Nowak", "Michał Nowak", "500-600-700", "HIGH", "Woda płynie szybko"));
        seedRequest(user, usterkaWindy, RequestStatus.CONFIRMED,
                requestMetadata("Winda nie działa",
                        "Winda w budynku A nie reaguje na przyciski, drzwi się nie otwierają od dwóch dni",
                        "IN_PROGRESS", "Budynek A, klatka 1", "Michał Nowak", "Michał Nowak", "500-600-700", "CRITICAL", "Sąsiedzi skarżą się na hałas"));
        seedRequest(user, awariaPradu, RequestStatus.COMPLETED,
                requestMetadata("Brak prądu na klatce schodowej",
                        "Oświetlenie na klatce schodowej nie działa od wczorajszego wieczoru",
                        "RESOLVED", "Klatka schodowa, piętro 2, Budynek B", "Michał Nowak", "Michał Nowak", "500-600-700", "MEDIUM", "Naprawiono bezpieczniki"));

        log.info("Seed complete. Demo logins: admin@zpo.local/admin123, user@zpo.local/user123");
    }

    private Map<String, Object> resourceMetadata(String categoryName, String building, String defaultPriority) {
        Map<String, Object> map = new HashMap<>();
        map.put("categoryName", categoryName);
        map.put("building", building);
        map.put("defaultPriority", defaultPriority);
        return map;
    }

    private Map<String, Object> requestMetadata(String title, String description, String status,
                                                  String location, String tenantName, String reportedBy,
                                                  String contactPhone, String urgency, String additionalInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("description", description);
        map.put("status", status);
        map.put("location", location);
        map.put("tenantName", tenantName);
        map.put("reportedBy", reportedBy);
        map.put("contactPhone", contactPhone);
        map.put("urgency", urgency);
        if (additionalInfo != null) {
            map.put("additionalInfo", additionalInfo);
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
                             RequestStatus entityStatus, Map<String, Object> metadata) {
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, null, null, null, metadata, List.of(), profile));

        RequestEntity entity = new RequestEntity();
        entity.setOwnerId(owner.getId());
        entity.setResourceId(resource.getId());
        entity.setStatus(entityStatus);
        entity.setMetadata(metadata);
        entity.setCalculatedValue(result.calculatedValue());
        entity.setAlgorithmBreakdown(result.breakdown());
        requestRepository.save(entity);
    }
}
