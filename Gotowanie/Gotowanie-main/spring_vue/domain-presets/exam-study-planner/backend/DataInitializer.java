package pl.zpo.app.config;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        log.info("Seeding exam-study-planner demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Opiekun roku", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Jan Kowalski", "user@zpo.local", "user123", Role.USER);

        LocalDate today = LocalDate.now();
        ResourceEntity discreteMath = saveResource("Matematyka dyskretna", "Egzamin pisemny, teoria + zadania",
                "EXAM", ResourceStatus.ACTIVE, new BigDecimal("1.50"),
                metadata(today.plusDays(14), "Matematyka dyskretna", "HARD", 24, "TOPICS",
                        "Logika, Zbiory, Relacje, Grafy, Kombinatoryka, Rekurencje", 180, "HIGH"));
        ResourceEntity programming = saveResource("Programowanie", "Egzamin praktyczny przy komputerze",
                "EXAM", ResourceStatus.ACTIVE, new BigDecimal("1.25"),
                metadata(today.plusDays(10), "Programowanie", "MEDIUM", 12, "CHAPTERS",
                        "Kolekcje, Wyjątki, Strumienie, Testy, Wzorce projektowe", 120, "MEDIUM"));
        ResourceEntity databases = saveResource("Bazy danych", "Kolokwium z SQL i normalizacji",
                "COLLOQUIUM", ResourceStatus.ACTIVE, new BigDecimal("1.00"),
                metadata(today.plusDays(7), "Bazy danych", "EASY", 90, "PAGES",
                        "SQL, Normalizacja, Transakcje, Indeksy", 90, "LOW"));
        saveResource("Algorytmy i struktury danych", "Egzamin ustny",
                "EXAM", ResourceStatus.ACTIVE, new BigDecimal("1.75"),
                metadata(today.plusDays(21), "Algorytmy", "HARD", 30, "TOPICS",
                        "Sortowanie, Drzewa, Grafy, Programowanie dynamiczne, Złożoność", 150, "HIGH"));
        saveResource("Systemy operacyjne", "Test jednokrotnego wyboru",
                "TEST", ResourceStatus.ACTIVE, new BigDecimal("1.10"),
                metadata(today.plusDays(5), "Systemy operacyjne", "MEDIUM", 8, "CHAPTERS",
                        "Procesy, Wątki, Pamięć, Systemy plików", 120, "MEDIUM"));

        seedPlan(user, discreteMath, RequestStatus.CONFIRMED,
                planMetadata("Logika, Zbiory, Relacje", 180, "HIGH", false, "Start od podstaw", today.toString(), 5));
        seedPlan(user, programming, RequestStatus.PENDING,
                planMetadata("Kolekcje, Strumienie", 120, "NORMAL", false, null, today.toString(), 2));
        seedPlan(user, databases, RequestStatus.CONFIRMED,
                planMetadata("SQL, Normalizacja", 90, "NORMAL", true, "Powtórka przed kolokwium", today.toString(), 10));

        log.info("Seed complete. Logins: admin@zpo.local/admin123, operator@zpo.local/operator123, user@zpo.local/user123");
    }

    private Map<String, Object> metadata(LocalDate examDate, String subject, String difficulty,
                                          int materialCount, String materialUnit, String topics,
                                          int dailyStudyLimitMinutes, String importance) {
        Map<String, Object> map = new HashMap<>();
        map.put("examDate", examDate.toString());
        map.put("subject", subject);
        map.put("difficulty", difficulty);
        map.put("materialCount", materialCount);
        map.put("materialUnit", materialUnit);
        map.put("topics", topics);
        map.put("dailyStudyLimitMinutes", dailyStudyLimitMinutes);
        map.put("importance", importance);
        return map;
    }

    private Map<String, Object> planMetadata(String selectedTopics, int studyMinutes, String priority,
                                              boolean isRevision, String notes, String studyDate, int materialDone) {
        Map<String, Object> map = new HashMap<>();
        map.put("selectedTopics", selectedTopics);
        map.put("studyMinutes", studyMinutes);
        map.put("priority", priority);
        map.put("isRevision", isRevision);
        map.put("completed", false);
        if (notes != null) {
            map.put("notes", notes);
        }
        map.put("studyDate", studyDate);
        map.put("materialDone", materialDone);
        return map;
    }

    private ResourceEntity saveResource(String name, String description, String type,
                                        ResourceStatus status, BigDecimal baseValue,
                                        Map<String, Object> metadata) {
        ResourceEntity entity = new ResourceEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setType(type);
        entity.setStatus(status);
        entity.setBaseValue(baseValue);
        entity.setCapacityValue(4);
        entity.setMetadata(metadata);
        return resourceRepository.save(entity);
    }

    private void seedPlan(UserEntity owner, ResourceEntity resource, RequestStatus status,
                          Map<String, Object> metadata) {
        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(
                resource.getId(), RequestStatus.activeStatuses());
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, null, null, null, metadata, existing, profile));

        RequestEntity entity = new RequestEntity();
        entity.setOwnerId(owner.getId());
        entity.setResourceId(resource.getId());
        entity.setStatus(status);
        entity.setMetadata(metadata);
        entity.setCalculatedValue(result.calculatedValue());
        entity.setAlgorithmBreakdown(result.breakdown());
        requestRepository.save(entity);
    }
}
