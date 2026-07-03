package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("position", "Stanowisko", FieldType.TEXT, true,
                        List.of(), "Stanowisko pracy (np. Kasjer, Magazynier)"),
                new DomainFieldConfig("department", "Dział", FieldType.SELECT, true,
                        List.of("SALES", "LOGISTICS", "IT", "HR", "SUPPORT"),
                        "SALES-sprzedaż | LOGISTICS-logistyka | IT-informatyka | SUPPORT-wsparcie | HR-kadry"),
                new DomainFieldConfig("contractType", "Typ umowy", FieldType.SELECT, true,
                        List.of("UOP", "B2B", "UZ"),
                        "UOP-umowa o pracę | B2B-kontrakt | UZ-umowa zlecenie"),
                new DomainFieldConfig("maxHoursPerWeek", "Limit godzin/tydzień", FieldType.NUMBER, true,
                        List.of(), "Maksymalna liczba godzin w tygodniu"),
                new DomainFieldConfig("hourlyRate", "Stawka godzinowa", FieldType.NUMBER, true,
                        List.of(), "Stawka za godzinę pracy (PLN)")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("shiftType", "Typ zmiany", FieldType.SELECT, true,
                        List.of("MORNING", "EVENING"),
                        "MORNING-poranna (7-15) | EVENING-wieczorna (15-23)"),
                new DomainFieldConfig("taskName", "Zadanie", FieldType.TEXT, false,
                        List.of(), "Co pracownik ma zrobić podczas zmiany"),
                new DomainFieldConfig("notes", "Notatki", FieldType.TEXTAREA, false,
                        List.of(), "Dodatkowe informacje o zmianie")
        );

        return new DomainProfile(
                "Employee Scheduler",
                "Pracownik", "Pracownicy",
                "Wpis grafiku", "Zmiany i zadania",
                "PLN",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.FLAT,
                /* requiresTimeWindow */ true,
                /* requiresQuantity  */ false,
                resourceFields,
                requestFields
        );
    }
}
