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
        log.info("Seeding habit-tracker demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Trener nawyków", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Jan Kowalski", "user@zpo.local", "user123", Role.USER);

        ResourceEntity gym = saveResource("Siłownia", "Trening siłowy min. 45 minut",
                "Sport", ResourceStatus.ACTIVE, new BigDecimal("10.00"), 4,
                metadata("Siłownia", "Sport", "Co drugi dzień", 1, "treningi", "#ef4444"));
        ResourceEntity reading = saveResource("Czytanie", "Minimum 20 stron dziennie",
                "Rozwój", ResourceStatus.ACTIVE, new BigDecimal("5.00"), 7,
                metadata("Czytanie", "Rozwój", "Codziennie", 20, "strony", "#3b82f6"));
        ResourceEntity english = saveResource("Nauka angielskiego", "Fiszki i konwersacje po 30 minut",
                "Rozwój", ResourceStatus.ACTIVE, new BigDecimal("8.00"), 5,
                metadata("Nauka angielskiego", "Rozwój", "Codziennie", 30, "minuty", "#8b5cf6"));
        saveResource("Medytacja", "10 minut uważności",
                "Zdrowie", ResourceStatus.ACTIVE, new BigDecimal("4.00"), 7,
                metadata("Medytacja", "Zdrowie", "Codziennie", 10, "minuty", "#22c55e"));
        saveResource("Projekt poboczny", "Praca nad własnym projektem",
                "Praca", ResourceStatus.ACTIVE, new BigDecimal("12.00"), 3,
                metadata("Projekt poboczny", "Praca", "Raz w tygodniu", 1, "sesje", "#f59e0b"));
        saveResource("Bieganie", "Wstrzymane na czas kontuzji",
                "Sport", ResourceStatus.INACTIVE, new BigDecimal("9.00"), 3,
                metadata("Bieganie", "Sport", "Co drugi dzień", 1, "kilometry", "#14b8a6"));

        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        seedEntry(user, gym, today.minus(2, ChronoUnit.DAYS).plus(7, ChronoUnit.HOURS), 1,
                RequestStatus.CONFIRMED, entryMetadata(today.minus(2, ChronoUnit.DAYS), 1, "Dziś klatka", null));
        seedEntry(user, gym, today.minus(1, ChronoUnit.DAYS).plus(7, ChronoUnit.HOURS), 1,
                RequestStatus.CONFIRMED, entryMetadata(today.minus(1, ChronoUnit.DAYS), 1, "Nogi — ciężko", null));
        seedEntry(user, reading, today.minus(1, ChronoUnit.DAYS).plus(21, ChronoUnit.HOURS), 25,
                RequestStatus.CONFIRMED, entryMetadata(today.minus(1, ChronoUnit.DAYS), 25, "Wiedźmin, rozdz. 4", null));
        seedEntry(user, english, today.minus(1, ChronoUnit.DAYS).plus(16, ChronoUnit.HOURS), 30,
                RequestStatus.PENDING, entryMetadata(today.minus(1, ChronoUnit.DAYS), 30, null, null));

        log.info("Seed complete. Logins: admin@zpo.local/admin123, operator@zpo.local/operator123, user@zpo.local/user123");
    }

    private Map<String, Object> metadata(String habitName, String category, String frequency,
                                         int targetPerDay, String unit, String colorTag) {
        Map<String, Object> map = new HashMap<>();
        map.put("habitName", habitName);
        map.put("category", category);
        map.put("frequency", frequency);
        map.put("targetPerDay", targetPerDay);
        map.put("unit", unit);
        map.put("colorTag", colorTag);
        return map;
    }

    private Map<String, Object> entryMetadata(Instant date, int value, String note, Boolean skipped) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", date.toString());
        map.put("value", value);
        if (note != null) {
            map.put("note", note);
        }
        if (skipped != null) {
            map.put("skipped", skipped);
        }
        return map;
    }

    private ResourceEntity saveResource(String name, String description, String type,
                                        ResourceStatus status, BigDecimal baseValue, int capacityValue,
                                        Map<String, Object> metadata) {
        ResourceEntity entity = new ResourceEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setType(type);
        entity.setStatus(status);
        entity.setBaseValue(baseValue);
        entity.setCapacityValue(capacityValue);
        entity.setMetadata(metadata);
        return resourceRepository.save(entity);
    }

    private void seedEntry(UserEntity owner, ResourceEntity resource, Instant start, int quantity,
                           RequestStatus status, Map<String, Object> metadata) {
        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(
                resource.getId(), RequestStatus.activeStatuses());
        Instant end = start.plus(30, ChronoUnit.MINUTES);
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, start, end, quantity, metadata, existing, profile));

        RequestEntity entity = new RequestEntity();
        entity.setOwnerId(owner.getId());
        entity.setResourceId(resource.getId());
        entity.setStatus(status);
        entity.setStartAt(start);
        entity.setEndAt(end);
        entity.setQuantity(quantity);
        entity.setMetadata(metadata);
        entity.setCalculatedValue(result.calculatedValue());
        entity.setAlgorithmBreakdown(result.breakdown());
        requestRepository.save(entity);
    }
}
