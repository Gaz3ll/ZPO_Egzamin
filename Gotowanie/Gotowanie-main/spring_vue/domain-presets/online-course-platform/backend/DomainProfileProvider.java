package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("title", "Tytuł", FieldType.TEXT, true,
                        List.of(), "Tytuł kursu/lekcji (np. Podstawy Javy, Wprowadzenie do Pythona)"),
                new DomainFieldConfig("category", "Kategoria", FieldType.SELECT, true,
                        List.of("PROGRAMMING", "MATH", "LANGUAGE", "BUSINESS", "DESIGN"),
                        "PROGRAMMING-programowanie | MATH-matematyka | LANGUAGE-języki | BUSINESS-biznes | DESIGN-projektowanie"),
                new DomainFieldConfig("difficulty", "Poziom trudności", FieldType.SELECT, true,
                        List.of("BEGINNER", "INTERMEDIATE", "ADVANCED"),
                        "BEGINNER-początkujący | INTERMEDIATE-średniozaawansowany | ADVANCED-zaawansowany"),
                new DomainFieldConfig("totalLessons", "Liczba lekcji", FieldType.NUMBER, true,
                        List.of(), "Łączna liczba lekcji w kursie"),
                new DomainFieldConfig("estimatedHours", "Szacowany czas (h)", FieldType.NUMBER, false,
                        List.of(), "Orientacyjny czas ukończenia kursu w godzinach")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("lessonsCompleted", "Ukończone lekcje", FieldType.NUMBER, true,
                        List.of(), "Liczba lekcji już przerobionych przez użytkownika"),
                new DomainFieldConfig("progressPercent", "Postęp (%)", FieldType.NUMBER, false,
                        List.of(), "Procent ukończenia kursu (automatycznie wyliczany)"),
                new DomainFieldConfig("completed", "Ukończony", FieldType.BOOLEAN, false,
                        List.of(), "Czy kurs został ukończony (automatycznie gdy postęp = 100%)")
        );

        return new DomainProfile(
                "Platforma kursów online",
                "Kurs", "Kursy",
                "Postęp", "Postępy",
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
