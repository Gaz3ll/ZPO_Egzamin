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
        log.info("Seeding employee-scheduler demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Kierownik zmiany", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Jan Kowalski", "user@zpo.local", "user123", Role.USER);

        ResourceEntity anna = saveEmployee("Anna Nowak", "Kasjerka, obsĹ‚uga klienta",
                new BigDecimal("32.00"),
                employeeMetadata("Kasjerka", "SALES", "UOP", 40, "32.00"));
        ResourceEntity piotr = saveEmployee("Piotr WiĹ›niewski", "Magazynier z uprawnieniami na wĂłzek",
                new BigDecimal("35.00"),
                employeeMetadata("Magazynier", "LOGISTICS", "UOP", 40, "35.00"));
        ResourceEntity marta = saveEmployee("Marta Kowalczyk", "Helpdesk i sieci",
                new BigDecimal("55.00"),
                employeeMetadata("Wsparcie IT", "IT", "B2B", 40, "55.00"));
        saveEmployee("Tomasz ZieliĹ„ski", "Sprzedawca, dziaĹ‚ elektronika",
                new BigDecimal("30.00"),
                employeeMetadata("Sprzedawca", "SALES", "UZ", 30, "30.00"));
        ResourceEntity jakub = saveEmployee("Jakub Lewandowski", "Serwisant",
                new BigDecimal("45.00"),
                employeeMetadata("Serwisant", "SUPPORT", "UOP", 40, "45.00"));
        saveInactiveEmployee("Ewa KamiĹ„ska", "Specjalistka HR (urlop macierzyĹ„ski)",
                new BigDecimal("50.00"),
                employeeMetadata("Specjalistka HR", "HR", "UOP", 40, "50.00"));

        Instant base = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        seedShift(user, anna, base.plus(7, ChronoUnit.HOURS),
                RequestStatus.CONFIRMED,
                shiftMetadata("MORNING", "ObsĹ‚uga kasy nr 2", ""));
        seedShift(user, piotr, base.plus(15, ChronoUnit.HOURS),
                RequestStatus.CONFIRMED,
                shiftMetadata("EVENING", "RozĹ‚adunek dostawy", "Magazyn A"));
        seedShift(user, jakub, base.plus(15, ChronoUnit.HOURS),
                RequestStatus.PENDING,
                shiftMetadata("EVENING", "PrzeglÄ…d instalacji", "Hala A"));
        seedShift(user, marta, base.plus(7, ChronoUnit.HOURS),
                RequestStatus.CONFIRMED,
                shiftMetadata("MORNING", "Wsparcie uĹĽytkownikĂłw", "Biuro gĹ‚Ăłwne"));

        log.info("Seed complete. Logins: admin@zpo.local/admin123, operator@zpo.local/operator123, user@zpo.local/user123");
    }

    private Map<String, Object> employeeMetadata(String position, String department, String contractType,
                                                  int maxHoursPerWeek, String hourlyRate) {
        Map<String, Object> map = new HashMap<>();
        map.put("position", position);
        map.put("department", department);
        map.put("contractType", contractType);
        map.put("maxHoursPerWeek", maxHoursPerWeek);
        map.put("hourlyRate", hourlyRate);
        return map;
    }

    private Map<String, Object> shiftMetadata(String shiftType, String taskName, String notes) {
        Map<String, Object> map = new HashMap<>();
        map.put("shiftType", shiftType);
        map.put("taskName", taskName);
        map.put("notes", notes);
        return map;
    }

    private ResourceEntity saveEmployee(String name, String description, BigDecimal hourlyRate,
                                        Map<String, Object> metadata) {
        return saveResource(name, description, ResourceStatus.ACTIVE, hourlyRate, metadata);
    }

    private void saveInactiveEmployee(String name, String description, BigDecimal hourlyRate,
                                      Map<String, Object> metadata) {
        saveResource(name, description, ResourceStatus.INACTIVE, hourlyRate, metadata);
    }

    private ResourceEntity saveResource(String name, String description, ResourceStatus status,
                                        BigDecimal baseValue, Map<String, Object> metadata) {
        ResourceEntity entity = new ResourceEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setType("EMPLOYEE");
        entity.setStatus(status);
        entity.setBaseValue(baseValue);
        entity.setCapacityValue(8);
        entity.setMetadata(metadata);
        return resourceRepository.save(entity);
    }

    private void seedShift(UserEntity owner, ResourceEntity resource, Instant start,
                           RequestStatus status, Map<String, Object> metadata) {
        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(
                resource.getId(), RequestStatus.activeStatuses());
        Instant end = start.plus(8, ChronoUnit.HOURS);
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
