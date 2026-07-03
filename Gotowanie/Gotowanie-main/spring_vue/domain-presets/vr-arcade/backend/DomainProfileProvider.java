package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("zoneName", "Nazwa strefy", FieldType.TEXT, true,
                        List.of(), "Nazwa strefy VR"),
                new DomainFieldConfig("totalHeadsets", "Liczba gogli", FieldType.NUMBER, true,
                        List.of(), "Pula gogli w strefie"),
                new DomainFieldConfig("gameTypes", "Gry", FieldType.TEXT, true,
                        List.of(), "Lista gier po przecinku"),
                new DomainFieldConfig("maxPlayers", "Max graczy", FieldType.NUMBER, true,
                        List.of(), "Limit graczy jednoczeĹ›nie"),
                new DomainFieldConfig("hasMultiplayer", "Multiplayer", FieldType.BOOLEAN, false,
                        List.of(), "Czy gra wieloosobowa")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("playersCount", "Liczba graczy", FieldType.NUMBER, true,
                        List.of(), "Liczba gogli do rezerwacji"),
                new DomainFieldConfig("gameType", "Gra", FieldType.TEXT, true,
                        List.of(), "Wybrana gra"),
                new DomainFieldConfig("customerName", "RezerwujÄ…cy", FieldType.TEXT, true,
                        List.of(), "ImiÄ™ i nazwisko"),
                new DomainFieldConfig("qrCode", "Kod QR", FieldType.TEXT, false,
                        List.of(), "Kod odbioru sprzÄ™tu")
        );

        return new DomainProfile(
                "Strefa VR â€“ rezerwacja gogli",
                "Strefa", "Strefy",
                "Rezerwacja", "Rezerwacje",
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
