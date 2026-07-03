package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("genre", "Gatunek", FieldType.SELECT, true,
                        List.of("ACTION", "COMEDY", "DRAMA", "HORROR", "SCI_FI", "ROMANCE", "THRILLER", "ANIMATION"),
                        "ACTION-akcja | COMEDY-komedia | DRAMA-dramat | HORROR-horror | SCI_FI-sci-fi | ROMANCE-romans | THRILLER-thriller | ANIMATION-animacja"),
                new DomainFieldConfig("releaseYear", "Rok produkcji", FieldType.NUMBER, true,
                        List.of(), "Rok wydania filmu (np. 2024)"),
                new DomainFieldConfig("director", "ReĹĽyser", FieldType.TEXT, true,
                        List.of(), "ImiÄ™ i nazwisko reĹĽysera"),
                new DomainFieldConfig("durationMinutes", "Czas trwania (min)", FieldType.NUMBER, false,
                        List.of(), "DĹ‚ugoĹ›Ä‡ filmu w minutach")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("rating", "Ocena", FieldType.NUMBER, true,
                        List.of(), "Ocena od 1 do 5 gwiazdek"),
                new DomainFieldConfig("review", "Recenzja", FieldType.TEXTAREA, false,
                        List.of(), "Opcjonalna recenzja tekstowa filmu"),
                new DomainFieldConfig("watchDate", "Data obejrzenia", FieldType.DATE, true,
                        List.of(), "Data obejrzenia filmu"),
                new DomainFieldConfig("platform", "Platforma", FieldType.TEXT, false,
                        List.of(), "Platforma / kino gdzie obejrzano")
        );

        return new DomainProfile(
                "Ocena filmĂłw",
                "Film", "Filmy",
                "Ocena", "Oceny",
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