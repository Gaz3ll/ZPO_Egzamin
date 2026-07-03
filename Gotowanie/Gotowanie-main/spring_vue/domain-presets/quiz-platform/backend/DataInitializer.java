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
        log.info("Seeding quiz-platform demo data...");

        UserEntity teacher = userService.ensureUser("Nauczyciel", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Operator", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity student = userService.ensureUser("Jan Kowalski", "user@zpo.local", "user123", Role.USER);

        ResourceEntity q1 = saveQuiz("Matematyka - podstawy", 10, 50);
        ResourceEntity q2 = saveQuiz("Java - test wiedzy", 20, 60);
        saveQuiz("Historia Polski", 15, 40);
        saveQuiz("Angielski B2", 25, 70);

        seedAttempt(student, q1, "Jan Kowalski", 8, 1, RequestStatus.COMPLETED);
        seedAttempt(student, q2, "Jan Kowalski", 14, 3, RequestStatus.COMPLETED);

        log.info("Seed complete.");
    }

    private ResourceEntity saveQuiz(String name, int questions, int threshold) {
        Map<String, Object> m = new HashMap<>();
        m.put("questionCount", questions);
        m.put("passThreshold", threshold);
        ResourceEntity e = new ResourceEntity();
        e.setName(name); e.setDescription(questions + " pytań, próg " + threshold + "%");
        e.setType("QUIZ"); e.setStatus(ResourceStatus.ACTIVE);
        e.setBaseValue(BigDecimal.ZERO); e.setCapacityValue(null); e.setMetadata(m);
        return resourceRepository.save(e);
    }

    private void seedAttempt(UserEntity student, ResourceEntity quiz, String name, int correct, int wrong, RequestStatus status) {
        Map<String, Object> m = new HashMap<>();
        m.put("studentName", name);
        m.put("correctAnswers", correct);
        m.put("wrongAnswers", wrong);
        List<RequestEntity> ex = new ArrayList<>();
        DomainAlgorithmResult r = algorithm.evaluate(new DomainAlgorithmInput(quiz, null, null, null, m, ex, profile));
        RequestEntity e = new RequestEntity();
        e.setOwnerId(student.getId()); e.setResourceId(quiz.getId()); e.setStatus(status);
        e.setMetadata(m); e.setCalculatedValue(r.calculatedValue()); e.setAlgorithmBreakdown(r.breakdown());
        requestRepository.save(e);
    }
}
