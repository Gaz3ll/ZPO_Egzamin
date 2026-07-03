# Test cases: habit-tracker

## Testy algorytmu

| # | Scenariusz | Wejście | Oczekiwany wynik |
|---|-----------|---------|------------------|
| 1 | Wpis wykonany nalicza punkty | `completed=true`, baza 10, EASY, `quantity=1` | `success=true`, `totalPoints=10.00` |
| 2 | Wpis niewykonany nie nalicza punktów | `completed=false`, `skippedReason=Brak czasu` | `success=true`, `calculatedValue=0`, `COMPLETION_CHECK: niewykonane` |
| 3 | Difficulty zwiększa punkty | baza 10, HARD, `quantity=1` | `totalPoints=15.00`, `DIFFICULTY_MULTIPLIER: × 1.50` |
| 4 | Quantity wpływa na wynik | baza 5, EASY, `quantity=25` (stron) | `basePoints=125.00`, nota `quantityFactor=25` |
| 5 | Kolizja wpisu tego samego dnia | drugi wpis nawyku w tym samym dniu | `success=false`, `DAY_COLLISION_CHECK: kolizja` |
| 6 | Brak kolizji w różne dni | wpisy wczoraj i dziś | `DAY_COLLISION_CHECK: brak kolizji` |
| 7 | progressPercent liczony poprawnie | cel 4/tydz., 1 wykonany wpis w tygodniu + ten | `PROGRESS_PERCENT: 50%` |
| 8 | Bonus za serię | `streakGoal=2`, wczoraj wpis `completed=true` | `STREAK_BONUS: +25% (seria 2/2)` |

## Testy security / policy

| # | Scenariusz | Oczekiwany wynik |
|---|-----------|------------------|
| 9  | USER widzi tylko swoje wpisy | `GET /api/requests` zwraca wyłącznie wpisy zalogowanego |
| 10 | ADMIN widzi wszystkie wpisy | panel admina listuje wpisy wszystkich użytkowników |
| 11 | USER nie zarządza nawykami | `POST /api/admin/resources` → 403 |
| 12 | Brak tokenu | `GET /api/requests` bez JWT → 401 |

## Testy brzegowe

| # | Scenariusz | Oczekiwany wynik |
|---|-----------|------------------|
| 13 | Nieaktywny nawyk | nawyk `INACTIVE` → `success=false`, `RESOURCE_CHECK` |
| 14 | `quantity <= 0` | `success=false`, `QUANTITY_CHECK: nieprawidłowa wartość` |
| 15 | Brak dnia wpisu | `startAt=null` → `success=false`, `DAY_CHECK` |
| 16 | Brak `streakGoal` | bonus nienaliczany, `STREAK_BONUS: brak (…, cel nieustawiony)` |
| 17 | Cel tygodniowy nieustawiony | `PROGRESS_PERCENT` liczony z `capacityValue`, a bez niego 0% |

## Ręczna weryfikacja

1. `./scripts/apply-preset.sh habit-tracker`
2. `cd backend && ./mvnw test` – `DefaultDomainAlgorithmTest` przechodzi (ścieżka generyczna).
3. Jako `user@zpo.local` dodaj wpis „wykonane" dla Siłowni → punkty z mnożnikiem ×1.5.
4. Dodaj drugi wpis Siłowni tego samego dnia → oczekiwany conflict.
5. Dodaj wpis „niewykonane" z powodem → 0 punktów, powód w breakdownie.
6. Jako `admin@zpo.local` sprawdź, że panel pokazuje wpisy wszystkich.
