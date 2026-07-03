package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("questionCount", "Liczba pytań", FieldType.NUMBER, true,
                        List.of(), "Ile pytań w quizie"),
                new DomainFieldConfig("passThreshold", "Próg zaliczenia (%)", FieldType.NUMBER, true,
                        List.of(), "Minimalny procent do zaliczenia (np. 50)")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("studentName", "Imię i nazwisko", FieldType.TEXT, true,
                        List.of(), "Imię i nazwisko studenta"),
                new DomainFieldConfig("correctAnswers", "Poprawne odpowiedzi", FieldType.NUMBER, true,
                        List.of(), "Liczba poprawnych odpowiedzi"),
                new DomainFieldConfig("wrongAnswers", "Błędne odpowiedzi", FieldType.NUMBER, false,
                        List.of(), "Liczba błędnych odpowiedzi"),
                new DomainFieldConfig("score", "Wynik punktowy", FieldType.NUMBER, false,
                        List.of(), "Obliczany automatycznie"),
                new DomainFieldConfig("percentage", "Procent", FieldType.NUMBER, false,
                        List.of(), "Obliczany automatycznie"),
                new DomainFieldConfig("passed", "Zaliczony", FieldType.BOOLEAN, false,
                        List.of(), "Czy zaliczył (obliczane automatycznie)")
        );

        return new DomainProfile(
                "Platforma e-learningowa - Quizy",
                "Quiz", "Quizy",
                "Podejście", "Podejścia",
                "%",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.FLAT,
                /* requiresTimeWindow */ false,
                /* requiresQuantity */ false,
                resourceFields,
                requestFields
        );
    }
}
