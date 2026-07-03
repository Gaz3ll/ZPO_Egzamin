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
        log.info("Seeding online-course-platform demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Instruktor", "instructor@zpo.local", "instructor123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Anna Nowak", "user@zpo.local", "user123", Role.USER);

        ResourceEntity javaBasics = saveResource("Podstawy Javy",
                "Kurs wprowadzający do języka Java – zmienne, pętle, klasy i obiekty",
                "PROGRAMMING", ResourceStatus.ACTIVE, BigDecimal.ZERO, 0,
                metadata("Podstawy Javy", "PROGRAMMING", "BEGINNER", 20, 40));
        ResourceEntity pythonData = saveResource("Python w analizie danych",
                "Pandas, NumPy i wizualizacja danych w Pythonie",
                "PROGRAMMING", ResourceStatus.ACTIVE, BigDecimal.ZERO, 0,
                metadata("Python w analizie danych", "PROGRAMMING", "INTERMEDIATE", 30, 60));
        saveResource("Matematyka dyskretna",
                "Rachunek zdań, kombinatoryka, teoria grafów",
                "MATH", ResourceStatus.ACTIVE, BigDecimal.ZERO, 0,
                metadata("Matematyka dyskretna", "MATH", "ADVANCED", 25, 50));
        saveResource("Angielski biznesowy",
                "Business English – słownictwo, e-maile, prezentacje",
                "LANGUAGE", ResourceStatus.ACTIVE, BigDecimal.ZERO, 0,
                metadata("Angielski biznesowy", "LANGUAGE", "BEGINNER", 15, 30));
        saveResource("Projektowanie UI/UX",
                "Figma, prototypowanie, badania użytkowników",
                "DESIGN", ResourceStatus.ACTIVE, BigDecimal.ZERO, 0,
                metadata("Projektowanie UI/UX", "DESIGN", "INTERMEDIATE", 18, 36));

        seedProgress(user, javaBasics, requestMetadata(20, 100, true));
        seedProgress(user, javaBasics, requestMetadata(12, 60, false));

        log.info("Seed complete. Demo logins: admin@zpo.local/admin123, instructor@zpo.local/instructor123, user@zpo.local/user123");
    }

    private Map<String, Object> metadata(String title, String category, String difficulty,
                                         int totalLessons, int estimatedHours) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("category", category);
        map.put("difficulty", difficulty);
        map.put("totalLessons", totalLessons);
        map.put("estimatedHours", estimatedHours);
        return map;
    }

    private Map<String, Object> requestMetadata(int lessonsCompleted, int progressPercent, boolean completed) {
        Map<String, Object> map = new HashMap<>();
        map.put("lessonsCompleted", lessonsCompleted);
        map.put("progressPercent", progressPercent);
        map.put("completed", completed);
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

    private void seedProgress(UserEntity owner, ResourceEntity resource, Map<String, Object> metadata) {
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, null, null, null, metadata, List.of(), profile));

        RequestEntity entity = new RequestEntity();
        entity.setOwnerId(owner.getId());
        entity.setResourceId(resource.getId());
        entity.setStatus(RequestStatus.CONFIRMED);
        entity.setMetadata(metadata);
        entity.setCalculatedValue(result.calculatedValue());
        entity.setAlgorithmBreakdown(result.breakdown());
        requestRepository.save(entity);
    }
}
