package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("type", "Typ zajęć", FieldType.SELECT, true,
                        List.of("Joga", "CrossFit", "Zumba", "Pilates", "Spinning", "Aerobik"),
                        "Rodzaj zajęć fitness"),
                new DomainFieldConfig("maxCapacity", "Maks. uczestników", FieldType.NUMBER, true,
                        List.of(), "Maksymalna liczba osób na liście głównej")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("userName", "Imię i nazwisko", FieldType.TEXT, true,
                        List.of(), "Osoba zapisująca się"),
                new DomainFieldConfig("waitlistPosition", "Pozycja na liście rezerwowej", FieldType.NUMBER, false,
                        List.of(), "0 = lista główna, >0 = rezerwowa"),
                new DomainFieldConfig("listType", "Lista", FieldType.SELECT, false,
                        List.of("GŁÓWNA", "REZERWOWA"),
                        "Czy uczestnik jest na liście głównej czy rezerwowej")
        );

        return new DomainProfile(
                "Rejestracja na zajęcia Fitness",
                "Zajęcia", "Zajęcia",
                "Zapis", "Zapisy",
                "PLN",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.FLAT,
                /* requiresTimeWindow */ false,
                /* requiresQuantity */ false,
                resourceFields,
                requestFields
        );
    }
}
