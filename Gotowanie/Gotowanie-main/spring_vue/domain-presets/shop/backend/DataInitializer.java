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
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final RequestRepository requestRepository;
    private final DomainAlgorithm algorithm;
    private final DomainProfile profile;
    private final boolean seedEnabled;

    public DataInitializer(UserService us, UserRepository ur, ResourceRepository rr,
                           RequestRepository qr, DomainAlgorithm a, DomainProfile p,
                           @Value("${app.seed.enabled:true}") boolean se) {
        this.userService = us; this.userRepository = ur; this.resourceRepository = rr;
        this.requestRepository = qr; this.algorithm = a; this.profile = p; this.seedEnabled = se;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled || !PresetSeedSupport.prepareDomainSeed(resourceRepository, requestRepository, profile, log)) return;
        log.info("Seeding shop demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Operator", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Jan Kowalski", "user@zpo.local", "user123", Role.USER);

        ResourceEntity p1 = saveProduct("Słuchawki Bluetooth", "Elektronika", 149.99, 15);
        ResourceEntity p2 = saveProduct("Powerbank 20000mAh", "Elektronika", 89.99, 25);
        ResourceEntity p3 = saveProduct("Karma dla psa 10kg", "Spożywcze", 59.99, 40);
        ResourceEntity p4 = saveProduct("Bluza bawełniana", "Odzież", 79.99, 30);
        saveProduct("Lampa biurkowa LED", "Dom", 129.99, 10);
        saveProduct("Piłka nożna", "Sport", 49.99, 20);
        saveProduct("Czekolada belgijska", "Spożywcze", 12.99, 100);
        saveProduct("Koszulka sportowa", "Odzież", 39.99, 50);
        saveProduct("Mata do jogi", "Sport", 69.99, 15);
        saveProduct("Kubek termiczny", "Dom", 44.99, 35);

        seedOrder(user, p1, "Jan Kowalski", 1, RequestStatus.CONFIRMED);
        seedOrder(user, p3, "Jan Kowalski", 2, RequestStatus.PENDING);
        seedOrder(user, p4, "Jan Kowalski", 1, RequestStatus.COMPLETED);

        log.info("Seed complete.");
    }

    private ResourceEntity saveProduct(String name, String category, double price, int stock) {
        Map<String, Object> m = new HashMap<>();
        m.put("price", BigDecimal.valueOf(price));
        m.put("category", category);
        m.put("stock", stock);
        ResourceEntity e = new ResourceEntity();
        e.setName(name); e.setDescription(category); e.setType("PRODUCT");
        e.setStatus(ResourceStatus.ACTIVE); e.setBaseValue(BigDecimal.valueOf(price));
        e.setCapacityValue(null); e.setMetadata(m);
        return resourceRepository.save(e);
    }

    private void seedOrder(UserEntity owner, ResourceEntity product, String customer, int qty, RequestStatus status) {
        Map<String, Object> m = new HashMap<>();
        m.put("customerName", customer);
        m.put("quantity", qty);
        List<RequestEntity> ex = new ArrayList<>();
        DomainAlgorithmResult r = algorithm.evaluate(new DomainAlgorithmInput(product, null, null, null, m, ex, profile));
        RequestEntity e = new RequestEntity();
        e.setOwnerId(owner.getId()); e.setResourceId(product.getId()); e.setStatus(status);
        e.setMetadata(m); e.setCalculatedValue(r.calculatedValue()); e.setAlgorithmBreakdown(r.breakdown());
        requestRepository.save(e);
    }
}
