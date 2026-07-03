package pl.zpo.app.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE;

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
        log.info("Seeding mood-diary demo data...");

        userService.ensureUser("Administrator", "admin@mood.local", "admin123", Role.ADMIN);
        userService.ensureUser("Terapeuta", "terapeuta@mood.local", "therapist123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Kasia", "kasia@mood.local", "user123", Role.USER);

        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        ResourceEntity dayMon = saveDay(monday, "Początek tygodnia – nowe cele");
        ResourceEntity dayTue = saveDay(monday.plusDays(1), "Wtorek – praca i rozwój");
        ResourceEntity dayWed = saveDay(monday.plusDays(2), "Środa – środek tygodnia");
        ResourceEntity dayThu = saveDay(monday.plusDays(3), "Czwartek – prawie weekend");
        ResourceEntity dayFri = saveDay(monday.plusDays(4), "Piątek – koniec tygodnia");
        ResourceEntity daySat = saveDay(monday.plusDays(5), "Sobota – odpoczynek");
        ResourceEntity daySun = saveDay(monday.plusDays(6), "Niedziela – refleksja");

        seedMoodEntry(user, dayMon, RequestStatus.COMPLETED,
                requestMeta(8, "GOOD", "Produktywny dzień w pracy, zdążyłam ze wszystkim", "praca, spacer, gotowanie", 7.5, "Udało się zamknąć ważny projekt"));
        seedMoodEntry(user, dayWed, RequestStatus.COMPLETED,
                requestMeta(5, "NEUTRAL", "Średni dzień – ani dobry, ani zły", "praca, zakupy", 6.0, "Dużo monotonnych zadań"));
        seedMoodEntry(user, dayFri, RequestStatus.COMPLETED,
                requestMeta(9, "GREAT", "Super piątek! Premia w pracy i spotkanie z przyjaciółmi", "praca, siłownia, kino, kolacja", 8.0, "Premia i czas z bliskimi"));
        seedMoodEntry(user, daySat, RequestStatus.PENDING,
                requestMeta(7, "GOOD", "Relaksująca sobota", "spanie, czytanie, spacer w parku", 9.0, "Dobra pogoda"));

        log.info("Seed complete. Demo logins: admin@mood.local/admin123, kasia@mood.local/user123");
    }

    private ResourceEntity saveDay(LocalDate date, String description) {
        ResourceEntity entity = new ResourceEntity();
        entity.setName(date.format(FMT));
        entity.setDescription(description);
        entity.setType("MOOD_DAY");
        entity.setStatus(ResourceStatus.ACTIVE);
        entity.setBaseValue(BigDecimal.ZERO);
        entity.setCapacityValue(1);
        entity.setMetadata(Map.of("entryDate", date.format(FMT)));
        return resourceRepository.save(entity);
    }

    private Map<String, Object> requestMeta(int moodScore, String moodLabel, String notes,
                                              String activities, double sleepHours, String trigger) {
        Map<String, Object> map = new HashMap<>();
        map.put("moodScore", moodScore);
        map.put("moodLabel", moodLabel);
        map.put("notes", notes);
        map.put("activities", activities);
        map.put("sleepHours", sleepHours);
        if (trigger != null) {
            map.put("trigger", trigger);
        }
        return map;
    }

    private void seedMoodEntry(UserEntity owner, ResourceEntity day,
                               RequestStatus status, Map<String, Object> metadata) {
        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(
                day.getId(), RequestStatus.activeStatuses());
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                day, null, null, null, metadata, existing, profile));

        RequestEntity entity = new RequestEntity();
        entity.setOwnerId(owner.getId());
        entity.setResourceId(day.getId());
        entity.setStatus(status);
        entity.setMetadata(metadata);
        entity.setCalculatedValue(result.calculatedValue());
        entity.setAlgorithmBreakdown(result.breakdown());
        requestRepository.save(entity);
    }
}
