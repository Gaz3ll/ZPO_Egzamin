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
        log.info("Seeding workout-tracker demo data...");

        userService.ensureUser("Administrator", "admin@trening.local", "admin123", Role.ADMIN);
        userService.ensureUser("Trener", "trener@trening.local", "trener123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Krzysztof Nowak", "user@trening.local", "user123", Role.USER);

        ResourceEntity benchPress = saveResource("Wyciskanie sztangi", "Podstawowe ćwiczenie na klatkę piersiową",
                "CHEST", ResourceStatus.ACTIVE, null, null,
                metadata("Wyciskanie sztangi", "CHEST", "STRENGTH", "INTERMEDIATE", "Sztanga, ławka"));
        ResourceEntity squat = saveResource("Przysiad", "Król ćwiczeń - angażuje głównie nogi",
                "LEGS", ResourceStatus.ACTIVE, null, null,
                metadata("Przysiad", "LEGS", "STRENGTH", "INTERMEDIATE", "Sztanga, stojaki"));
        ResourceEntity deadlift = saveResource("Martwy ciąg", "Złożone ćwiczenie na plecy i nogi",
                "BACK", ResourceStatus.ACTIVE, null, null,
                metadata("Martwy ciąg", "BACK", "STRENGTH", "ADVANCED", "Sztanga, talerze"));
        ResourceEntity pullup = saveResource("Podciąganie", "Ćwiczenie na górną część pleców",
                "BACK", ResourceStatus.ACTIVE, null, null,
                metadata("Podciąganie", "BACK", "CALISTHENICS", "BEGINNER", "Drążek"));
        ResourceEntity plank = saveResource("Plank", "Izometryczne ćwiczenie na korpus",
                "CORE", ResourceStatus.ACTIVE, null, null,
                metadata("Plank", "CORE", "CALISTHENICS", "BEGINNER", "Mata"));

        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        seedWorkout(user, benchPress, today.minus(2, ChronoUnit.DAYS).plus(8, ChronoUnit.HOURS),
                RequestStatus.CONFIRMED, workoutMetadata(4, 10, 60, 45, "2026-06-30", "Dobry trening, progresja ciężaru"));
        seedWorkout(user, squat, today.minus(1, ChronoUnit.DAYS).plus(8, ChronoUnit.HOURS),
                RequestStatus.CONFIRMED, workoutMetadata(5, 8, 80, 50, "2026-07-01", "Ciężko ale satysfakcjonująco"));
        seedWorkout(user, pullup, today.plus(9, ChronoUnit.HOURS),
                RequestStatus.PENDING, workoutMetadata(3, 12, 0, 30, "2026-07-02", "Trening pleców"));

        log.info("Seed complete. Demo logins: admin@trening.local/admin123, trener@trening.local/trener123, user@trening.local/user123");
    }

    private Map<String, Object> metadata(String exerciseName, String muscleGroup, String exerciseType, String difficulty, String equipment) {
        Map<String, Object> map = new HashMap<>();
        map.put("exerciseName", exerciseName);
        map.put("muscleGroup", muscleGroup);
        map.put("exerciseType", exerciseType);
        map.put("difficulty", difficulty);
        map.put("equipment", equipment);
        return map;
    }

    private Map<String, Object> workoutMetadata(int sets, int reps, int weight, int durationMinutes,
                                                String workoutDate, String notes) {
        Map<String, Object> map = new HashMap<>();
        map.put("sets", sets);
        map.put("reps", reps);
        map.put("weight", weight);
        map.put("durationMinutes", durationMinutes);
        map.put("workoutDate", workoutDate);
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

    private void seedWorkout(UserEntity owner, ResourceEntity resource, Instant start,
                             RequestStatus status, Map<String, Object> metadata) {
        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(
                resource.getId(), RequestStatus.activeStatuses());
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, start, null, null, metadata, existing, profile));

        RequestEntity entity = new RequestEntity();
        entity.setOwnerId(owner.getId());
        entity.setResourceId(resource.getId());
        entity.setStatus(status);
        entity.setStartAt(start);
        entity.setMetadata(metadata);
        entity.setCalculatedValue(result.calculatedValue());
        entity.setAlgorithmBreakdown(result.breakdown());
        requestRepository.save(entity);
    }
}
