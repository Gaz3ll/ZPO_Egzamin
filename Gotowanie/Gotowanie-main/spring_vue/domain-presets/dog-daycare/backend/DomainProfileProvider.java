package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("zoneName", "Strefa", FieldType.TEXT, true, List.of(), "Nazwa strefy opieki"),
                new DomainFieldConfig("dailyCapacityPoints", "Punkty pojemnoĹ›ci", FieldType.NUMBER, true, List.of(), "Dzienna pojemnoĹ›Ä‡ punktowa"),
                new DomainFieldConfig("acceptedDogSizes", "Rozmiary psĂłw", FieldType.TEXT, true, List.of(), "SMALL, MEDIUM, LARGE"),
                new DomainFieldConfig("hasOutdoorRun", "Wybieg", FieldType.BOOLEAN, false, List.of(), "Czy jest wybieg"),
                new DomainFieldConfig("careLevel", "Poziom opieki", FieldType.TEXT, true, List.of(), "STANDARD, ACTIVE, MEDICAL"),
                new DomainFieldConfig("staffCount", "Opiekunowie", FieldType.NUMBER, true, List.of(), "Liczba opiekunĂłw")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("dogName", "Pies", FieldType.TEXT, true, List.of(), "ImiÄ™ psa"),
                new DomainFieldConfig("dogSize", "WielkoĹ›Ä‡", FieldType.TEXT, true, List.of(), "SMALL, MEDIUM, LARGE"),
                new DomainFieldConfig("dogWeight", "Waga", FieldType.NUMBER, false, List.of(), "kg"),
                new DomainFieldConfig("stayHours", "Godziny", FieldType.NUMBER, true, List.of(), "Czas pobytu"),
                new DomainFieldConfig("needsMedication", "Leki", FieldType.BOOLEAN, false, List.of(), "Czy wymaga lekĂłw"),
                new DomainFieldConfig("extraWalk", "Dodatkowy spacer", FieldType.BOOLEAN, false, List.of(), "Czy dodaÄ‡ spacer"),
                new DomainFieldConfig("feedingNotes", "Karmienie", FieldType.TEXTAREA, false, List.of(), "Instrukcje karmienia")
        );

        return new DomainProfile(
                "Przedszkole dla psĂłw",
                "Strefa", "Strefy",
                "Pobyt", "Pobyty",
                "PLN",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.PER_UNIT,
                false,
                false,
                resourceFields,
                requestFields
        );
    }
}
