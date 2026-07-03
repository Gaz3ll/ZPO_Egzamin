package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("gameName", "Nazwa gry", FieldType.TEXT, true,
                        List.of(), "Nazwa gry planszowej lub tytuł turnieju esportowego (np. Catan, League of Legends)"),
                new DomainFieldConfig("gameType", "Typ gry", FieldType.SELECT, true,
                        List.of("BOARD_GAME", "ESPORT"),
                        "BOARD_GAME-gra planszowa | ESPORT-gra komputerowa / esport"),
                new DomainFieldConfig("maxPlayers", "Maks. liczba graczy", FieldType.NUMBER, true,
                        List.of(), "Maksymalna liczba graczy w turnieju (np. 2, 4, 10)"),
                new DomainFieldConfig("tournamentDate", "Data turnieju", FieldType.DATE, true,
                        List.of(), "Data rozegrania turnieju")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("playerName", "Nazwa gracza", FieldType.TEXT, true,
                        List.of(), "Pseudonim lub imię i nazwisko gracza"),
                new DomainFieldConfig("score", "Wynik", FieldType.NUMBER, true,
                        List.of(), "Liczba punktów zdobyta w meczu (np. 95, 78, 120)"),
                new DomainFieldConfig("rank", "Pozycja", FieldType.NUMBER, true,
                        List.of(), "Miejsce w rankingu turnieju (1, 2, 3, ...)"),
                new DomainFieldConfig("opponentName", "Przeciwnik", FieldType.TEXT, true,
                        List.of(), "Nazwa przeciwnika (lub 'AI' dla gry z komputerem)"),
                new DomainFieldConfig("matchDate", "Data meczu", FieldType.DATE, true,
                        List.of(), "Data rozegrania meczu"),
                new DomainFieldConfig("result", "Wynik", FieldType.SELECT, true,
                        List.of("Zwycięstwo", "Porazka", "Remis"), "Wynik meczu"),
                new DomainFieldConfig("notes", "Notatki", FieldType.TEXTAREA, false,
                        List.of(), "Dodatkowe informacje o meczu")
        );

        return new DomainProfile(
                "System rankingowy graczy",
                "Turniej", "Turnieje",
                "Mecz", "Mecze",
                "PTS",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.FLAT,
                false,
                false,
                resourceFields,
                requestFields
        );
    }
}
