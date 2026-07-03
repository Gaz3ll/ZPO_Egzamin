# Preset: System rankingowy graczy (player-ranking)

Aplikacja dająca możliwość dodania meczu z konkretną grą i zawodnikami,
możliwość szybkiego uzupełniania rankingu oraz punktacji.
Aplikacja przechowuje historię gier, w jakie gracz zagrał oraz jego wynik.

## Model danych

### Resource (Turniej)
| Pole            | Typ     | Opis                                                |
|-----------------|---------|-----------------------------------------------------|
| `gameName`      | TEXT    | Nazwa gry planszowej lub tytuł turnieju esportowego |
| `gameType`      | SELECT  | BOARD_GAME (planszowa) / ESPORT (komputerowa)       |
| `maxPlayers`    | NUMBER  | Maksymalna liczba graczy w turnieju                 |
| `tournamentDate`| DATE    | Data rozegrania turnieju                            |

### Request (Mecz / Wynik)
| Pole           | Typ    | Opis                                                |
|----------------|--------|-----------------------------------------------------|
| `playerName`   | TEXT   | Pseudonim lub imię i nazwisko gracza                |
| `score`        | NUMBER | Liczba punktów zdobyta w meczu                      |
| `rank`         | NUMBER | Miejsce w rankingu turnieju (1, 2, 3, ...)          |
| `opponentName` | TEXT   | Nazwa przeciwnika (lub "AI" dla gry z komputerem)   |

## Algorytm punktacji

```
points = score + rankBonus

rankBonus:
  - 1. miejsce → +100 pkt
  - 2. miejsce → +50 pkt
  - 3. miejsce → +25 pkt
  - 4. miejsce i niższe → 0 pkt
```

## Role i uprawnienia

- **USER** – widzi własne mecze, może dodawać wyniki
- **ADMIN** – zarządza turniejami (CRUD), widzi wszystkie mecze i wyniki wszystkich graczy

## Konfiguracja algorytmu

- **AlgorithmMode**: `VALUE_CALCULATION_ONLY` – brak sprawdzania czasu i pojemności
- **PricingUnit**: `FLAT` – jednostkowa kalkulacja (punkty nie są mnożone przez czas/ilość)
- **Waluta**: `PTS`
