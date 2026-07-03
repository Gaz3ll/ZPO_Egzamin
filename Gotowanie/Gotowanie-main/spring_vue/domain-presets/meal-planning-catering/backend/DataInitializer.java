package pl.zpo.app.config;

import java.math.BigDecimal;
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
        log.info("Seeding meal-planning-catering demo data...");

        userService.ensureUser("Administrator", "admin@zpo.local", "admin123", Role.ADMIN);
        userService.ensureUser("Dietetyk", "operator@zpo.local", "operator123", Role.OPERATOR);
        UserEntity user = userService.ensureUser("Jan Kowalski", "user@zpo.local", "user123", Role.USER);

        // Śniadania
        ResourceEntity owsianka = saveMeal("Owsianka z owocami", "Płatki owsiane, banan, borówki, miód",
                "BREAKFAST", new BigDecimal("14.00"),
                mealMetadata("BREAKFAST", "STANDARD", 420, 14, 68, 10, "GLUTEN", true, false));
        saveMeal("Omlet białkowy", "Omlet z 4 białek z warzywami",
                "BREAKFAST", new BigDecimal("16.00"),
                mealMetadata("BREAKFAST", "SPORT", 380, 32, 12, 18, "EGG", true, false));
        saveMeal("Tofucznica z pieczywem", "Tofu z kurkumą, szczypiorek, chleb żytni",
                "BREAKFAST", new BigDecimal("15.00"),
                mealMetadata("BREAKFAST", "VEGAN", 390, 20, 44, 14, "GLUTEN,SOY", true, true));
        saveMeal("Jajecznica z awokado", "Jajka, awokado, bez pieczywa",
                "BREAKFAST", new BigDecimal("17.00"),
                mealMetadata("BREAKFAST", "LOW_CARB", 450, 22, 8, 36, "EGG", true, false));
        saveMeal("Jogurt z granolą", "Jogurt naturalny, granola orzechowa",
                "BREAKFAST", new BigDecimal("12.00"),
                mealMetadata("BREAKFAST", "VEGE", 360, 15, 42, 14, "DAIRY,NUTS,GLUTEN", true, false));

        // Obiady
        ResourceEntity kurczak = saveMeal("Kurczak z ryżem i brokułami", "Grillowana pierś, ryż jaśminowy",
                "LUNCH", new BigDecimal("24.00"),
                mealMetadata("LUNCH", "STANDARD", 650, 45, 70, 15, "", false, false));
        saveMeal("Wołowina teriyaki", "Wołowina, makaron soba, warzywa",
                "LUNCH", new BigDecimal("29.00"),
                mealMetadata("LUNCH", "SPORT", 780, 52, 74, 22, "GLUTEN,SOY", false, false));
        ResourceEntity curry = saveMeal("Curry z ciecierzycy", "Ciecierzyca, mleko kokosowe, ryż basmati",
                "LUNCH", new BigDecimal("22.00"),
                mealMetadata("LUNCH", "VEGAN", 620, 20, 82, 20, "", true, true));
        saveMeal("Łosoś z warzywami", "Pieczony łosoś, cukinia, papryka, bez skrobi",
                "LUNCH", new BigDecimal("32.00"),
                mealMetadata("LUNCH", "LOW_CARB", 560, 38, 14, 38, "FISH", false, false));
        saveMeal("Pierogi ruskie", "Pierogi z twarogiem i ziemniakami, okrasa cebulowa",
                "LUNCH", new BigDecimal("21.00"),
                mealMetadata("LUNCH", "VEGE", 700, 22, 96, 24, "GLUTEN,DAIRY", true, false));

        // Kolacje
        ResourceEntity salatka = saveMeal("Sałatka z grillowanym kurczakiem", "Mix sałat, kurczak, dressing jogurtowy",
                "DINNER", new BigDecimal("19.00"),
                mealMetadata("DINNER", "STANDARD", 430, 34, 20, 22, "DAIRY", false, false));
        saveMeal("Wrap proteinowy", "Tortilla pełnoziarnista, indyk, hummus",
                "DINNER", new BigDecimal("18.00"),
                mealMetadata("DINNER", "SPORT", 520, 38, 48, 16, "GLUTEN,SESAME", false, false));
        saveMeal("Buddha bowl", "Komosa, bataty, jarmuż, tahini",
                "DINNER", new BigDecimal("23.00"),
                mealMetadata("DINNER", "VEGAN", 540, 18, 66, 22, "SESAME", true, true));
        saveMeal("Zapiekanka z cukinii", "Cukinia, ser, sos pomidorowy, bez makaronu",
                "DINNER", new BigDecimal("20.00"),
                mealMetadata("DINNER", "LOW_CARB", 410, 24, 16, 26, "DAIRY", true, false));
        saveMeal("Krem z pomidorów z grzankami", "Zupa krem, grzanki czosnkowe",
                "DINNER", new BigDecimal("15.00"),
                mealMetadata("DINNER", "VEGE", 350, 10, 46, 12, "GLUTEN,DAIRY", true, false));

        // Przekąski + pozycja wycofana
        saveMeal("Baton energetyczny daktylowy", "Daktyle, orzechy, kakao",
                "SNACK", new BigDecimal("8.00"),
                mealMetadata("SNACK", "VEGAN", 210, 5, 28, 9, "NUTS", true, true));
        saveInactiveMeal("Sezonowa sałatka szparagowa", "Poza sezonem",
                "DINNER", new BigDecimal("26.00"),
                mealMetadata("DINNER", "VEGE", 380, 12, 30, 22, "", true, false));

        seedPlan(user, kurczak, RequestStatus.CONFIRMED,
                planMetadata("STANDARD", 2000, 3, "1.0", 7, "NUTS", "BREAKFAST,LUNCH,DINNER"));
        seedPlan(user, curry, RequestStatus.PENDING,
                planMetadata("VEGAN", 1800, 3, "1.0", 5, "GLUTEN", "BREAKFAST,LUNCH,DINNER"));
        seedPlan(user, salatka, RequestStatus.CONFIRMED,
                planMetadata("STANDARD", 1500, 2, "0.5", 7, "", "LUNCH,DINNER"));
        seedPlan(user, owsianka, RequestStatus.PENDING,
                planMetadata("STANDARD", 2600, 3, "2.0", 7, "", "BREAKFAST,LUNCH,DINNER"));

        log.info("Seed complete. Logins: admin@zpo.local/admin123, operator@zpo.local/operator123, user@zpo.local/user123");
    }

    private Map<String, Object> mealMetadata(String mealType, String dietType, int calories,
                                             int protein, int carbs, int fat, String allergens,
                                             boolean isVegetarian, boolean isVegan) {
        Map<String, Object> map = new HashMap<>();
        map.put("mealType", mealType);
        map.put("dietType", dietType);
        map.put("calories", calories);
        map.put("protein", protein);
        map.put("carbs", carbs);
        map.put("fat", fat);
        map.put("allergens", allergens);
        map.put("isVegetarian", isVegetarian);
        map.put("isVegan", isVegan);
        map.put("portionOptions", "0.5,1.0,2.0");
        return map;
    }

    private Map<String, Object> planMetadata(String dietType, int targetCalories, int mealsPerDay,
                                             String portionMultiplier, int daysCount,
                                             String excludedAllergens, String preferredMealTypes) {
        Map<String, Object> map = new HashMap<>();
        map.put("dietType", dietType);
        map.put("targetCalories", targetCalories);
        map.put("mealsPerDay", mealsPerDay);
        map.put("portionMultiplier", portionMultiplier);
        map.put("daysCount", daysCount);
        map.put("excludedAllergens", excludedAllergens);
        map.put("preferredMealTypes", preferredMealTypes);
        return map;
    }

    private ResourceEntity saveMeal(String name, String description, String type,
                                    BigDecimal baseValue, Map<String, Object> metadata) {
        return saveResource(name, description, type, ResourceStatus.ACTIVE, baseValue, metadata);
    }

    private void saveInactiveMeal(String name, String description, String type,
                                  BigDecimal baseValue, Map<String, Object> metadata) {
        saveResource(name, description, type, ResourceStatus.INACTIVE, baseValue, metadata);
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
        entity.setCapacityValue(60);
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
