package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("price", "Cena (PLN)", FieldType.NUMBER, true,
                        List.of(), "Cena produktu w PLN"),
                new DomainFieldConfig("category", "Kategoria", FieldType.SELECT, true,
                        List.of("Elektronika", "Spożywcze", "Odzież", "Dom", "Sport", "Inne"),
                        "Kategoria produktu"),
                new DomainFieldConfig("stock", "Stan magazynowy", FieldType.NUMBER, true,
                        List.of(), "Aktualna liczba sztuk na stanie")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("customerName", "Imię i nazwisko", FieldType.TEXT, true,
                        List.of(), "Imię i nazwisko klienta"),
                new DomainFieldConfig("customerPhone", "Telefon", FieldType.TEXT, true,
                        List.of(), "Numer telefonu"),
                new DomainFieldConfig("customerAddress", "Adres wysyłki", FieldType.TEXT, true,
                        List.of(), "Adres do wysyłki"),
                new DomainFieldConfig("quantity", "Ilość sztuk", FieldType.NUMBER, true,
                        List.of(), "Zamawiana ilość")
        );

        return new DomainProfile(
                "Sklep internetowy",
                "Produkt", "Produkty",
                "Zamówienie", "Zamówienia",
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
