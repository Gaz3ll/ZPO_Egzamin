package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("animalName", "Imię zwierzęcia", FieldType.TEXT, true, List.of(), "Imię"),
                new DomainFieldConfig("species", "Gatunek", FieldType.TEXT, true, List.of(), "DOG, CAT, RABBIT"),
                new DomainFieldConfig("size", "Wielkość", FieldType.TEXT, true, List.of(), "SMALL, MEDIUM, LARGE"),
                new DomainFieldConfig("temperament", "Charakter", FieldType.TEXT, true, List.of(), "CALM, ACTIVE, SHY"),
                new DomainFieldConfig("needsGarden", "Wymaga ogrodu", FieldType.BOOLEAN, false, List.of(), "Czy ogród jest wymagany"),
                new DomainFieldConfig("monthlyCost", "Koszt miesięczny", FieldType.NUMBER, true, List.of(), "Szacowany koszt opieki")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("adopterName", "Adoptujący", FieldType.TEXT, true, List.of(), "Imię i nazwisko"),
                new DomainFieldConfig("preferredSpecies", "Preferowany gatunek", FieldType.TEXT, true, List.of(), "DOG, CAT, RABBIT"),
                new DomainFieldConfig("homeType", "Dom", FieldType.TEXT, true, List.of(), "FLAT, HOUSE"),
                new DomainFieldConfig("hasGarden", "Ogród", FieldType.BOOLEAN, false, List.of(), "Czy jest ogród"),
                new DomainFieldConfig("experienceLevel", "Doświadczenie", FieldType.TEXT, true, List.of(), "LOW, MEDIUM, HIGH"),
                new DomainFieldConfig("budgetMonthly", "Budżet miesięczny", FieldType.NUMBER, true, List.of(), "PLN")
        );

        return new DomainProfile(
                "Adopcje zwierząt",
                "Zwierzę", "Zwierzęta",
                "Wniosek", "Wnioski",
                "PLN",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.FLAT,
                false,
                false,
                resourceFields,
                requestFields
        );
    }
}
