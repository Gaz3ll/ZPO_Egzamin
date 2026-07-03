package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("lockerCode", "Kod skrytki", FieldType.TEXT, true,
                        List.of(), "Unikalny kod skrytki (np. WAW-01-S1)"),
                new DomainFieldConfig("location", "Lokalizacja", FieldType.TEXT, true,
                        List.of(), "Adres paczkomatu (np. Warszawa, ul. Prosta 1)"),
                new DomainFieldConfig("lockerSize", "Rozmiar skrytki", FieldType.SELECT, true,
                        List.of("S", "M", "L", "XL"), "S-maĹ‚a | M-Ĺ›rednia | L-duĹĽa | XL-najwiÄ™ksza"),
                new DomainFieldConfig("maxWeight", "Maksymalna waga (kg)", FieldType.NUMBER, true,
                        List.of(), "Limit wagowy skrytki w kilogramach"),
                new DomainFieldConfig("isOccupied", "ZajÄ™ta operacyjnie", FieldType.BOOLEAN, false,
                        List.of(), "Blokada staĹ‚a skrytki (np. uszkodzenie)")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("receiverName", "Odbiorca", FieldType.TEXT, true,
                        List.of(), "ImiÄ™ i nazwisko odbiorcy przesyĹ‚ki"),
                new DomainFieldConfig("receiverEmail", "Email odbiorcy", FieldType.TEXT, true,
                        List.of(), "Adres email do powiadomienia o dostarczeniu"),
                new DomainFieldConfig("parcelSize", "Rozmiar paczki", FieldType.SELECT, true,
                        List.of("S", "M", "L", "XL"), "S-koperta | M-pudeĹ‚ko | L-duĹĽe pudĹ‚o | XL-maks. gabaryt"),
                new DomainFieldConfig("weight", "Waga paczki (kg)", FieldType.NUMBER, true,
                        List.of(), "Waga w kg. PowyĹĽej 5kg: +1.50zĹ‚/kg dopĹ‚aty"),
                new DomainFieldConfig("pickupCode", "Kod odbioru", FieldType.TEXT, false,
                        List.of(), "6-cyfrowy kod odbioru (generowany auto)")
        );

        return new DomainProfile(
                "System paczkomatĂłw",
                "Skrytka", "Skrytki",
                "Paczka", "Paczki",
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
