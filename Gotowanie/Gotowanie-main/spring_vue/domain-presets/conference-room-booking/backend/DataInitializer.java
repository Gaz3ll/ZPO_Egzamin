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
        log.info("Seeding conference-room-booking demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Operator", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Jan Kowalski", "user@zpo.local", "user123", Role.USER);

        ResourceEntity salaA = saveResource("Sala A", "Sala dla malych zespolow",
                ResourceStatus.ACTIVE, new BigDecimal("300.00"),
                roomMetadata("Sala A", 10, "1", false, false, "300.00"));
        ResourceEntity salaB = saveResource("Sala B", "Sala z projektorem",
                ResourceStatus.ACTIVE, new BigDecimal("500.00"),
                roomMetadata("Sala B", 25, "2", true, false, "500.00"));
        saveResource("Sala C", "Duza sala z full wyposazeniem",
                ResourceStatus.ACTIVE, new BigDecimal("800.00"),
                roomMetadata("Sala C", 50, "1", true, true, "800.00"));
        saveResource("Sala D", "Sala szkoleniowa",
                ResourceStatus.ACTIVE, new BigDecimal("600.00"),
                roomMetadata("Sala D", 30, "2", true, true, "600.00"));
        saveResource("Sala VIP", "Sala executive",
                ResourceStatus.ACTIVE, new BigDecimal("1200.00"),
                roomMetadata("Sala VIP", 6, "3", true, true, "1200.00"));

        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        seedRequest(user, salaA, today.plus(2, ChronoUnit.DAYS), today.plus(3, ChronoUnit.DAYS),
                RequestStatus.CONFIRMED,
                requestMetadata("Jan Nowak", "jan@firma.pl", "123456789", "Spotkanie projektowe", 5, ""));
        seedRequest(user, salaB, today.plus(4, ChronoUnit.DAYS), today.plus(5, ChronoUnit.DAYS),
                RequestStatus.PENDING,
                requestMetadata("Anna Kowalska", "anna@firma.pl", "987654321", "Szkolenie BHP", 20, "Potrzebny rzutnik"));

        log.info("Seed complete. Logins: admin@zpo.local/admin123, operator@zpo.local/operator123, user@zpo.local/user123");
    }

    private Map<String, Object> roomMetadata(String roomName, int capacity, String floor,
                                              boolean hasProjector, boolean hasVideoConference, String dailyRate) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomName", roomName);
        map.put("capacity", capacity);
        map.put("floor", floor);
        map.put("hasProjector", hasProjector);
        map.put("hasVideoConference", hasVideoConference);
        map.put("dailyRate", dailyRate);
        return map;
    }

    private Map<String, Object> requestMetadata(String renterName, String renterEmail, String renterPhone,
                                                 String meetingTitle, int attendeeCount, String notes) {
        Map<String, Object> map = new HashMap<>();
        map.put("renterName", renterName);
        map.put("renterEmail", renterEmail);
        map.put("renterPhone", renterPhone);
        map.put("meetingTitle", meetingTitle);
        map.put("attendeeCount", attendeeCount);
        map.put("notes", notes);
        return map;
    }

    private ResourceEntity saveResource(String name, String description, ResourceStatus status,
                                         BigDecimal baseValue, Map<String, Object> metadata) {
        ResourceEntity entity = new ResourceEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setType("ROOM");
        entity.setStatus(status);
        entity.setBaseValue(baseValue);
        entity.setCapacityValue(null);
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
