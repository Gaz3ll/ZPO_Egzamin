package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("className", "ZajÄ™cia", FieldType.TEXT, true,
                        List.of(), "Nazwa zajÄ™Ä‡"),
                new DomainFieldConfig("trainerName", "Trener", FieldType.TEXT, true,
                        List.of(), "ProwadzÄ…cy"),
                new DomainFieldConfig("difficultyLevel", "Poziom", FieldType.TEXT, true,
                        List.of(), "BEGINNER, INTERMEDIATE, ADVANCED"),
                new DomainFieldConfig("capacity", "Liczba miejsc", FieldType.NUMBER, true,
                        List.of(), "Maksymalna liczba uczestnikĂłw"),
                new DomainFieldConfig("equipmentRequired", "SprzÄ™t", FieldType.TEXT, false,
                        List.of(), "SprzÄ™t wymagany na sali"),
                new DomainFieldConfig("dropInPrice", "Cena wejĹ›cia", FieldType.NUMBER, true,
                        List.of(), "Cena pojedynczych zajÄ™Ä‡")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("memberName", "Uczestnik", FieldType.TEXT, true,
                        List.of(), "ImiÄ™ i nazwisko"),
                new DomainFieldConfig("preferredDifficulty", "Preferowany poziom", FieldType.TEXT, true,
                        List.of(), "BEGINNER, INTERMEDIATE, ADVANCED"),
                new DomainFieldConfig("passType", "Karnet", FieldType.TEXT, true,
                        List.of(), "DROP_IN, MULTISPORT, MONTHLY"),
                new DomainFieldConfig("needsEquipment", "WypoĹĽycza sprzÄ™t", FieldType.BOOLEAN, false,
                        List.of(), "Czy doliczyÄ‡ wypoĹĽyczenie"),
                new DomainFieldConfig("healthNotes", "Uwagi zdrowotne", FieldType.TEXTAREA, false,
                        List.of(), "Informacje dla trenera")
        );

        return new DomainProfile(
                "Rezerwacja zajÄ™Ä‡ fitness",
                "ZajÄ™cia", "ZajÄ™cia",
                "Zapis", "Zapisy",
                "PLN",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.PER_UNIT,
                false,
                true,
                resourceFields,
                requestFields
        );
    }
}
