package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("animalSpecies", "Gatunek zwierząt", FieldType.TEXT, true,
                        List.of(), "Dominujący gatunek albo grupa gatunków"),
                new DomainFieldConfig("animalCount", "Liczba zwierząt", FieldType.NUMBER, true,
                        List.of(), "Aktualna liczba zwierząt w sektorze"),
                new DomainFieldConfig("dangerLevel", "Poziom zagrożenia", FieldType.SELECT, true,
                        List.of("LOW", "MEDIUM", "HIGH", "CRITICAL"), "Ryzyko pracy w sektorze"),
                new DomainFieldConfig("feedingType", "Typ karmienia", FieldType.SELECT, true,
                        List.of("HERBIVORE", "CARNIVORE", "OMNIVORE", "AQUATIC", "SPECIAL"), "Rodzaj żywienia"),
                new DomainFieldConfig("cleaningDifficulty", "Trudność sprzątania", FieldType.SELECT, true,
                        List.of("LOW", "MEDIUM", "HIGH", "EXTREME"), "Wpływa na szacowany nakład pracy"),
                new DomainFieldConfig("keeperZone", "Strefa opiekunów", FieldType.TEXT, true,
                        List.of(), "Strefa organizacyjna zoo"),
                new DomainFieldConfig("isQuarantine", "Kwarantanna", FieldType.BOOLEAN, false,
                        List.of(), "Sektor objęty reżimem kwarantanny"),
                new DomainFieldConfig("lastInspectionDate", "Ostatnia kontrola", FieldType.DATE, false,
                        List.of(), "Data ostatniego przeglądu")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("taskType", "Typ zadania", FieldType.SELECT, true,
                        List.of("FEEDING", "CLEANING", "VET_CHECK", "TRANSFER", "TECHNICAL_INSPECTION"),
                        "Rodzaj pracy administracyjnej"),
                new DomainFieldConfig("priority", "Priorytet", FieldType.SELECT, true,
                        List.of("LOW", "NORMAL", "HIGH", "URGENT"), "Priorytet operacyjny"),
                new DomainFieldConfig("requiresVet", "Wymaga weterynarza", FieldType.BOOLEAN, false,
                        List.of(), "Czy zadanie wymaga udziału weterynarza"),
                new DomainFieldConfig("requiresTwoKeepers", "Wymaga dwóch opiekunów", FieldType.BOOLEAN, false,
                        List.of(), "Czy zadanie zaplanowano z podwójną obsadą"),
                new DomainFieldConfig("animalHealthRisk", "Ryzyko zdrowotne", FieldType.SELECT, false,
                        List.of("LOW", "NORMAL", "HIGH", "CRITICAL"), "Ryzyko zdrowotne zwierząt"),
                new DomainFieldConfig("notes", "Notatki", FieldType.TEXTAREA, false,
                        List.of(), "Dodatkowy opis zadania")
        );

        return new DomainProfile(
                "Zoo Admin",
                "Wybieg", "Wybiegi",
                "Zadanie", "Zadania",
                "PLN",
                AlgorithmMode.TIME_AVAILABILITY_AND_CALCULATION,
                PricingUnit.FLAT,
                true,
                false,
                resourceFields,
                requestFields
        );
    }
}
