# Test cases: escape-room

| # | Scenariusz | Wejście | Oczekiwany wynik |
|---|-----------|---------|------------------|
| 1 | Wolny pokój | Termin bez kolizji, drużyna ≤ limit | `success`, `TIME_COLLISION_CHECK: brak kolizji` |
| 2 | Zajęty pokój → alternatywa | Termin nakłada się na inną rezerwację | `success=false`, notatka `alternativeRoom=...` |
| 3 | Za dużo graczy | `playersCount` > `maxPlayers` | `success=false`, `PLAYERS_CHECK: przekroczono` |
| 4 | Modyfikator trudności | Pokój HARD | `difficultyModifier` = ×1.25 |
| 5 | Game Master widzi harmonogram | OPERATOR pobiera listę | wszystkie rezerwacje dnia |

## Ręczna weryfikacja

1. `./scripts/apply-preset.sh escape-room`
2. `cd backend && ./mvnw test` – `DefaultDomainAlgorithmTest` przechodzi (ścieżka generyczna).
3. Zarezerwuj zajęty pokój → sprawdź notatkę `alternativeRoom` w breakdownie.
4. Zarezerwuj pokój dla drużyny większej niż limit → oczekiwany conflict.
