package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Office plant care profile. Resource = an office plant available for adoption, Request = a care
 * task (watering, fertilizing, repotting, health check) or an adoption. baseValue = base care
 * difficulty/cost score, capacityValue = watering frequency in days (care demand level).
 */
@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("species", "Gatunek", FieldType.TEXT, true,
                        List.of(), "Np. Monstera deliciosa"),
                new DomainFieldConfig("location", "Lokalizacja", FieldType.TEXT, true,
                        List.of(), "Gdzie stoi roślina, np. recepcja"),
                new DomainFieldConfig("lightRequirement", "Światło", FieldType.SELECT, true,
                        List.of("LOW", "MEDIUM", "HIGH"), "Zapotrzebowanie na światło"),
                new DomainFieldConfig("waterFrequencyDays", "Podlewanie co (dni)", FieldType.NUMBER, true,
                        List.of(), "Co ile dni podlewać"),
                new DomainFieldConfig("fertilizeFrequencyDays", "Nawożenie co (dni)", FieldType.NUMBER, false,
                        List.of(), "Co ile dni nawozić"),
                new DomainFieldConfig("repotFrequencyMonths", "Przesadzanie co (mies.)", FieldType.NUMBER, false,
                        List.of(), "Co ile miesięcy przesadzać"),
                new DomainFieldConfig("difficulty", "Trudność opieki", FieldType.SELECT, true,
                        List.of("EASY", "MEDIUM", "HARD"), "Jak wymagająca jest roślina"),
                new DomainFieldConfig("isAdopted", "Zaadoptowana", FieldType.BOOLEAN, false,
                        List.of(), "Czy roślina ma już opiekuna"),
                new DomainFieldConfig("healthStatus", "Stan zdrowia", FieldType.SELECT, false,
                        List.of("GOOD", "OK", "BAD", "CRITICAL"), "Kondycja rośliny")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("careType", "Typ zadania", FieldType.SELECT, true,
                        List.of("WATERING", "FERTILIZING", "REPOTTING", "HEALTH_CHECK", "ADOPTION"),
                        "Rodzaj opieki albo adopcja"),
                new DomainFieldConfig("lastCareAt", "Ostatnia pielęgnacja", FieldType.DATE, false,
                        List.of(), "Kiedy ostatnio wykonano tę czynność"),
                new DomainFieldConfig("plantCondition", "Stan rośliny", FieldType.TEXT, false,
                        List.of(), "Obserwacje, np. żółknące liście"),
                new DomainFieldConfig("notes", "Notatki", FieldType.TEXTAREA, false,
                        List.of(), "Dodatkowe uwagi"),
                new DomainFieldConfig("isCompleted", "Wykonane", FieldType.BOOLEAN, false,
                        List.of(), "Czy zadanie zostało wykonane"),
                new DomainFieldConfig("adopterName", "Opiekun", FieldType.TEXT, true,
                        List.of(), "Imię i nazwisko opiekuna")
        );

        return new DomainProfile(
                "Office Plant Care",
                "Roślina", "Rośliny",
                "Zadanie opieki", "Zadania opieki",
                "PKT",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.FLAT,
                /* requiresTimeWindow */ false,
                /* requiresQuantity  */ false,
                resourceFields,
                requestFields
        );
    }
}
