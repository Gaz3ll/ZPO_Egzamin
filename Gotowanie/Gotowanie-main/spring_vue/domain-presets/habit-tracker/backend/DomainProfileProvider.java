package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("habitName", "Nazwa nawyku", FieldType.TEXT, true,
                        List.of(), "Nazwa nawyku (np. Picie wody, Czytanie ksiÄ…ĹĽek)"),
                new DomainFieldConfig("category", "Kategoria", FieldType.SELECT, true,
                        List.of("Zdrowie", "RozwĂłj", "Praca", "Sport", "Dom", "Inne"),
                        "Kategoria nawyku"),
                new DomainFieldConfig("frequency", "CzÄ™stotliwoĹ›Ä‡", FieldType.SELECT, true,
                        List.of("Codziennie", "Co drugi dzieĹ„", "Raz w tygodniu", "W dni powszednie"),
                        "Jak czÄ™sto naleĹĽy wykonywaÄ‡ nawyk"),
                new DomainFieldConfig("targetPerDay", "Cel dzienny", FieldType.NUMBER, true,
                        List.of(), "Docelowa dzienna liczba powtĂłrzeĹ„"),
                new DomainFieldConfig("unit", "Jednostka", FieldType.TEXT, true,
                        List.of(), "Jednostka (np. szklanki, minuty, strony)"),
                new DomainFieldConfig("colorTag", "Kolor etykiety", FieldType.TEXT, false,
                        List.of(), "Kolor HEX, np. #FF5733")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("date", "Data", FieldType.DATE, true,
                        List.of(), "Data wpisu"),
                new DomainFieldConfig("value", "WartoĹ›Ä‡", FieldType.NUMBER, true,
                        List.of(), "Liczba wykonanych powtĂłrzeĹ„ w danym dniu"),
                new DomainFieldConfig("note", "Notatka", FieldType.TEXTAREA, false,
                        List.of(), "Notatka do wpisu"),
                new DomainFieldConfig("skipped", "PominiÄ™ty", FieldType.BOOLEAN, false,
                        List.of(), "Zaznacz jeĹ›li dzieĹ„ zostaĹ‚ pominiÄ™ty")
        );

        return new DomainProfile(
                "Habit Tracker",
                "Nawyk", "Nawyki",
                "Wpis", "Wpisy",
                "PKT",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.FLAT,
                /* requiresTimeWindow */ true,
                /* requiresQuantity  */ true,
                resourceFields,
                requestFields
        );
    }
}
