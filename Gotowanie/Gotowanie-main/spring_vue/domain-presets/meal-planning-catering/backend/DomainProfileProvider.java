package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Meal planning catering profile. Resource = a single meal in the catering menu, Request = a
 * weekly diet plan order built from the menu. baseValue = base price per portion (PLN),
 * capacityValue = number of available portions.
 */
@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("mealType", "Rodzaj posiĹ‚ku", FieldType.SELECT, true,
                        List.of("BREAKFAST", "LUNCH", "DINNER", "SNACK"), "Ĺšniadanie / obiad / kolacja"),
                new DomainFieldConfig("dietType", "Dieta", FieldType.SELECT, true,
                        List.of("STANDARD", "VEGE", "VEGAN", "SPORT", "LOW_CARB"), "Rodzaj diety"),
                new DomainFieldConfig("calories", "Kalorie", FieldType.NUMBER, true,
                        List.of(), "kcal na porcjÄ™ 1.0"),
                new DomainFieldConfig("protein", "BiaĹ‚ko (g)", FieldType.NUMBER, false,
                        List.of(), "MakroskĹ‚adnik"),
                new DomainFieldConfig("carbs", "WÄ™glowodany (g)", FieldType.NUMBER, false,
                        List.of(), "MakroskĹ‚adnik"),
                new DomainFieldConfig("fat", "TĹ‚uszcz (g)", FieldType.NUMBER, false,
                        List.of(), "MakroskĹ‚adnik"),
                new DomainFieldConfig("allergens", "Alergeny", FieldType.TEXT, false,
                        List.of(), "Lista po przecinku, np. GLUTEN,NUTS"),
                new DomainFieldConfig("isVegetarian", "WegetariaĹ„ski", FieldType.BOOLEAN, false,
                        List.of(), "Czy posiĹ‚ek jest wegetariaĹ„ski"),
                new DomainFieldConfig("isVegan", "WegaĹ„ski", FieldType.BOOLEAN, false,
                        List.of(), "Czy posiĹ‚ek jest wegaĹ„ski"),
                new DomainFieldConfig("portionOptions", "DostÄ™pne porcje", FieldType.TEXT, false,
                        List.of(), "Np. 0.5,1.0,2.0")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("dietType", "Dieta", FieldType.SELECT, true,
                        List.of("STANDARD", "VEGE", "VEGAN", "SPORT", "LOW_CARB"), "Wybrany rodzaj diety"),
                new DomainFieldConfig("targetCalories", "Cel kaloryczny (dziennie)", FieldType.NUMBER, true,
                        List.of(), "Docelowe kcal na dzieĹ„"),
                new DomainFieldConfig("mealsPerDay", "PosiĹ‚ki dziennie", FieldType.NUMBER, true,
                        List.of(), "Ile posiĹ‚kĂłw dziennie"),
                new DomainFieldConfig("portionMultiplier", "Porcja", FieldType.SELECT, true,
                        List.of("0.5", "1.0", "2.0"), "PoĹ‚owa / normalna / podwĂłjna"),
                new DomainFieldConfig("daysCount", "Liczba dni", FieldType.NUMBER, true,
                        List.of(), "DĹ‚ugoĹ›Ä‡ planu, np. 7"),
                new DomainFieldConfig("excludedAllergens", "Wykluczone alergeny", FieldType.TEXT, false,
                        List.of(), "Lista po przecinku"),
                new DomainFieldConfig("preferredMealTypes", "Preferowane posiĹ‚ki", FieldType.TEXT, false,
                        List.of(), "Np. BREAKFAST,LUNCH,DINNER")
        );

        return new DomainProfile(
                "Catering Planner",
                "PosiĹ‚ek", "PosiĹ‚ki",
                "Plan diety", "Plany diety",
                "PLN",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.PER_UNIT,
                /* requiresTimeWindow */ false,
                /* requiresQuantity  */ false,
                resourceFields,
                requestFields
        );
    }
}
