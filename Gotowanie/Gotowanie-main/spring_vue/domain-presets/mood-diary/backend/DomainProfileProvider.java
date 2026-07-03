package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("entryDate", "Data (dzień)", FieldType.TEXT, true,
                        List.of(), "Data oznaczająca konkretny dzień (np. 2025-06-15). Każdy dzień to osobny zasób.")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("moodScore", "Ocena samopoczucia (1-10)", FieldType.NUMBER, true,
                        List.of(), "Punktowa ocena nastroju: 1 (najgorzej) - 10 (najlepiej)"),
                new DomainFieldConfig("moodLabel", "Etykieta nastroju", FieldType.SELECT, true,
                        List.of("GREAT", "GOOD", "NEUTRAL", "BAD", "AWFUL"),
                        "GREAT-świetnie | GOOD-dobrze | NEUTRAL-neutralnie | BAD-źle | AWFUL-bardzo źle"),
                new DomainFieldConfig("notes", "Notatki", FieldType.TEXTAREA, false,
                        List.of(), "Opcjonalny opis wydarzeń, myśli lub przyczyn danego nastroju"),
                new DomainFieldConfig("activities", "Aktywności", FieldType.TEXT, false,
                        List.of(), "Wypisz aktywności dnia (np. spacer, czytanie, trening)"),
                new DomainFieldConfig("sleepHours", "Godziny snu", FieldType.NUMBER, false,
                        List.of(), "Ilość godzin snu poprzedniej nocy (opcjonalnie, np. 7.5)"),
                new DomainFieldConfig("trigger", "Przyczyna", FieldType.TEXT, false,
                        List.of(), "Co wywołało ten nastrój")
        );

        return new DomainProfile(
                "Dziennik nastroju",
                "Dzień", "Dni",
                "Wpis", "Wpisy",
                "PCT",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.FLAT,
                false,
                false,
                resourceFields,
                requestFields
        );
    }
}
