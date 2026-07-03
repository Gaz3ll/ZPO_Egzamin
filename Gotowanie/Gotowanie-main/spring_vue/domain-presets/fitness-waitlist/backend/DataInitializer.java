package pl.zpo.app.config;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private final ResourceRepository resourceRepository;
    private final RequestRepository requestRepository;
    private final DomainAlgorithm algorithm;
    private final DomainProfile profile;
    private final boolean seedEnabled;

    public DataInitializer(UserService us, UserRepository ur, ResourceRepository rr,
                           RequestRepository qr, DomainAlgorithm a, DomainProfile p,
                           @Value("${app.seed.enabled:true}") boolean se) {
        this.userService = us; this.resourceRepository = rr;
        this.requestRepository = qr; this.algorithm = a; this.profile = p; this.seedEnabled = se;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled || !PresetSeedSupport.prepareDomainSeed(resourceRepository, requestRepository, profile, log)) return;
        log.info("Seeding fitness-waitlist demo data...");

        UserEntity admin = userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Operator", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Jan Kowalski", "user@zpo.local", "user123", Role.USER);
        UserEntity anna = userService.ensureUser("Anna Nowak", "anna@zpo.local", "anna123", Role.USER);

        ResourceEntity c1 = saveClass("Joga poranna", "Joga", 20);
        ResourceEntity c2 = saveClass("CrossFit intensywny", "CrossFit", 15);
        saveClass("Zumba latino", "Zumba", 25);

        // Main list registrations
        for (int i = 0; i < 18; i++) {
            seedRegistration(user, c1, "Uczestnik " + (i + 1), "GŁÓWNA", 0, RequestStatus.CONFIRMED);
        }
        // Waitlist registrations
        seedRegistration(anna, c1, "Jan Kowalski (waitlist)", "REZERWOWA", 1, RequestStatus.PENDING);
        seedRegistration(user, c2, "Anna Nowak", "GŁÓWNA", 0, RequestStatus.CONFIRMED);

        log.info("Seed complete.");
    }

    private ResourceEntity saveClass(String name, String type, int capacity) {
        Map<String, Object> m = new HashMap<>();
        m.put("type", type);
        m.put("maxCapacity", capacity);
        ResourceEntity e = new ResourceEntity();
        e.setName(name); e.setDescription(type + ", max " + capacity + " osób");
        e.setType("CLASS"); e.setStatus(ResourceStatus.ACTIVE);
        e.setBaseValue(BigDecimal.ZERO); e.setCapacityValue(null); e.setMetadata(m);
        return resourceRepository.save(e);
    }

    private void seedRegistration(UserEntity owner, ResourceEntity cls, String userName,
                                   String listType, int pos, RequestStatus status) {
        Map<String, Object> m = new HashMap<>();
        m.put("userName", userName);
        m.put("waitlistPosition", pos);
        m.put("listType", listType);
        List<RequestEntity> ex = requestRepository.findAllByResourceIdAndStatusIn(
                cls.getId(), RequestStatus.activeStatuses());
        DomainAlgorithmResult r = algorithm.evaluate(new DomainAlgorithmInput(cls, null, null, null, m, ex, profile));
        RequestEntity e = new RequestEntity();
        e.setOwnerId(owner.getId()); e.setResourceId(cls.getId()); e.setStatus(status);
        e.setMetadata(m); e.setCalculatedValue(r.calculatedValue()); e.setAlgorithmBreakdown(r.breakdown());
        requestRepository.save(e);
    }
}
