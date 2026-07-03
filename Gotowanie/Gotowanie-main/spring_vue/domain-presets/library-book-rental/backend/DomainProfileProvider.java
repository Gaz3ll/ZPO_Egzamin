package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("author", "Autor", FieldType.TEXT, true,
                        List.of(), "Imię i nazwisko autora"),
                new DomainFieldConfig("isbn", "ISBN", FieldType.TEXT, false,
                        List.of(), "Numer ISBN książki"),
                new DomainFieldConfig("totalCopies", "Liczba egzemplarzy", FieldType.NUMBER, true,
                        List.of(), "Całkowita liczba egzemplarzy"),
                new DomainFieldConfig("availableCopies", "Dostępne egzemplarze", FieldType.NUMBER, true,
                        List.of(), "Aktualnie dostępne egzemplarze")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("borrowerName", "Wypożyczający", FieldType.TEXT, false,
                        List.of(), "Imię i nazwisko osoby wypożyczającej")
        );

        return new DomainProfile(
                "Biblioteka - wypożyczalnia książek",
                "Książka", "Książki",
                "Wypożyczenie", "Wypożyczenia",
                "PLN",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.PER_UNIT,
                /* requiresTimeWindow */ false,
                /* requiresQuantity */ false,
                resourceFields,
                requestFields
        );
    }
}
