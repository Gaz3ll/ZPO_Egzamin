package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("roomName", "Nazwa pokoju", FieldType.TEXT, true,
                        List.of(), "Nazwa escape roomu"),
                new DomainFieldConfig("difficulty", "Trudność", FieldType.SELECT, true,
                        List.of("EASY", "MEDIUM", "HARD", "EXPERT"), "Poziom trudności"),
                new DomainFieldConfig("theme", "Motyw", FieldType.TEXT, true,
                        List.of(), "Tematyka pokoju"),
                new DomainFieldConfig("durationMinutes", "Czas gry (min)", FieldType.NUMBER, true,
                        List.of(), "Długość sesji"),
                new DomainFieldConfig("maxPlayers", "Max graczy", FieldType.NUMBER, true,
                        List.of(), "Limit drużyny"),
                new DomainFieldConfig("depositRequired", "Zaliczka", FieldType.BOOLEAN, false,
                        List.of(), "Czy wymagana zaliczka")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("teamName", "Nazwa drużyny", FieldType.TEXT, true,
                        List.of(), "Drużyna"),
                new DomainFieldConfig("playersCount", "Liczba graczy", FieldType.NUMBER, true,
                        List.of(), "Wielkość drużyny"),
                new DomainFieldConfig("preferredDifficulty", "Preferowana trudność", FieldType.SELECT, false,
                        List.of("EASY", "MEDIUM", "HARD", "EXPERT"), "Opcjonalnie"),
                new DomainFieldConfig("preferredTheme", "Preferowany motyw", FieldType.TEXT, false,
                        List.of(), "Opcjonalnie"),
                new DomainFieldConfig("depositPaid", "Zaliczka opłacona", FieldType.BOOLEAN, false,
                        List.of(), "Status zaliczki")
        );

        return new DomainProfile(
                "Escape room",
                "Pokój", "Pokoje",
                "Rezerwacja", "Rezerwacje",
                "PLN",
                AlgorithmMode.TIME_AVAILABILITY_AND_CALCULATION,
                PricingUnit.FLAT,
                true,
                false,
                resourceFields,
                requestFields
        );
    }
}
