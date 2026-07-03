package pl.zpo.app.config;

import java.math.BigDecimal;
import java.time.Instant;
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

    public DataInitializer(UserService userService, UserRepository userRepository,
                           ResourceRepository resourceRepository, RequestRepository requestRepository,
                           DomainAlgorithm algorithm, DomainProfile profile,
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
        log.info("Seeding library-book-rental demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Operator", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity janek = userService.ensureUser("Jan Kowalski", "user@zpo.local", "user123", Role.USER);
        UserEntity anna = userService.ensureUser("Anna Nowak", "anna@zpo.local", "anna123", Role.USER);

        ResourceEntity k1 = saveBook("Władca Pierścieni", "J.R.R. Tolkien", "978-83-7180-001-0", 3, 3);
        ResourceEntity k2 = saveBook("Hobbit", "J.R.R. Tolkien", "978-83-7180-002-7", 2, 1);
        ResourceEntity k3 = saveBook("Lśnienie", "Stephen King", "978-83-7180-003-4", 2, 2);
        ResourceEntity k4 = saveBook("Zbrodnia i kara", "Fiodor Dostojewski", "978-83-7180-004-1", 4, 4);
        ResourceEntity k5 = saveBook("Mistrz i Małgorzata", "Michaił Bułhakow", "978-83-7180-005-8", 2, 2);
        saveBook("Rok 1984", "George Orwell", "978-83-7180-006-5", 3, 3);
        saveBook("Duma i uprzedzenie", "Jane Austen", "978-83-7180-007-2", 1, 1);
        saveBook("Sto lat samotności", "Gabriel Garcia Marquez", "978-83-7180-008-9", 2, 2);

        seedBorrowing(janek, k2, "Jan Kowalski", RequestStatus.CONFIRMED);
        seedBorrowing(anna, k3, "Anna Nowak", RequestStatus.PENDING);

        log.info("Seed complete. Logins: admin@zpo.local/admin123, user@zpo.local/user123, anna@zpo.local/anna123");
    }

    private ResourceEntity saveBook(String title, String author, String isbn, int total, int available) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("author", author);
        meta.put("isbn", isbn);
        meta.put("totalCopies", total);
        meta.put("availableCopies", available);
        ResourceEntity e = new ResourceEntity();
        e.setName(title);
        e.setDescription(author);
        e.setType("BOOK");
        e.setStatus(ResourceStatus.ACTIVE);
        e.setBaseValue(BigDecimal.ZERO);
        e.setCapacityValue(null);
        e.setMetadata(meta);
        return resourceRepository.save(e);
    }

    private void seedBorrowing(UserEntity owner, ResourceEntity book, String borrower, RequestStatus status) {
        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(
                book.getId(), RequestStatus.activeStatuses());
        Map<String, Object> meta = new HashMap<>();
        meta.put("borrowerName", borrower);
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                book, Instant.now(), Instant.now(), null, meta, existing, profile));

        RequestEntity e = new RequestEntity();
        e.setOwnerId(owner.getId());
        e.setResourceId(book.getId());
        e.setStatus(status);
        e.setStartAt(Instant.now());
        e.setEndAt(null);
        e.setMetadata(meta);
        e.setCalculatedValue(result.calculatedValue());
        e.setAlgorithmBreakdown(result.breakdown());
        requestRepository.save(e);
    }
}
