# Test cases: meal-planning-catering

## Testy algorytmu

| # | Scenariusz | Wejście | Oczekiwany wynik |
|---|-----------|---------|------------------|
| 1 | Dobiera posiłki zgodne z dietą | posiłek `VEGAN`, plan `VEGAN` | `success=true`, `DIET_FILTER: ok` |
| 2 | Odrzuca alergeny | posiłek z `NUTS`, `excludedAllergens=NUTS` | `success=false`, `ALLERGEN_FILTER: konflikt [NUTS]` |
| 3 | Porcja 0.5 zmniejsza kalorie | 650 kcal, `portionMultiplier=0.5`, 3 posiłki | `DAILY_CALORIES: 975 kcal` |
| 4 | Porcja 2.0 zwiększa kalorie | 420 kcal, `portionMultiplier=2.0`, 3 posiłki | `DAILY_CALORIES: 2520 kcal` i wyższa cena |
| 5 | Zbliża się do targetCalories | `targetCalories=2000`, dzienne 1950 | `CALORIE_MATCH: ok (różnica -50 kcal)` |
| 6 | Odchylenie od celu | `targetCalories=1500`, dzienne 2520 | `CALORIE_MATCH: poza tolerancją ±300 kcal` + sugestia |
| 7 | Brak posiłków dla diety zwraca conflict | posiłek `STANDARD` (mięsny), plan `VEGAN` | `success=false`, `DIET_FILTER: brak dopasowania` |
| 8 | VEGE akceptuje posiłek wegański | posiłek `VEGAN`, plan `VEGE` | `DIET_FILTER: ok` |
| 9 | Cena planu | 24 PLN × 21 porcji × 1.0 | `calculatedValue=504.00` |

## Testy security / policy

| # | Scenariusz | Oczekiwany wynik |
|---|-----------|------------------|
| 10 | USER widzi swoje plany | `GET /api/requests` zwraca wyłącznie plany zalogowanego |
| 11 | ADMIN widzi wszystkie | panel cateringu listuje plany wszystkich klientów |
| 12 | USER nie zarządza bazą posiłków | `POST /api/admin/resources` → 403 |
| 13 | Brak tokenu | `GET /api/requests` bez JWT → 401 |

## Testy brzegowe

| # | Scenariusz | Oczekiwany wynik |
|---|-----------|------------------|
| 14 | Posiłek poza sezonem | `INACTIVE` → `success=false`, `RESOURCE_CHECK` |
| 15 | Nieprawidłowa porcja | `portionMultiplier=1.5` → `success=false`, `PORTION_MULTIPLIER: nieprawidłowa` |
| 16 | Za mało porcji | `capacityValue=10`, plan 3×7=21 porcji | `success=false`, `CAPACITY_CHECK: przekroczono (21>10)` |
| 17 | Brak `targetCalories` | plan liczy się bez reguły `CALORIE_MATCH` |
| 18 | Brak `capacityValue` | `CAPACITY_CHECK: ok (…/∞)` — bez limitu |
| 19 | Puste `excludedAllergens` | `ALLERGEN_FILTER: ok` |

## Ręczna weryfikacja

1. `./scripts/apply-preset.sh meal-planning-catering`
2. `cd backend && ./mvnw test` – `DefaultDomainAlgorithmTest` przechodzi (ścieżka generyczna).
3. Jako `user@zpo.local` wygeneruj plan VEGAN dla „Curry z ciecierzycy" → sukces + kalorie.
4. Wygeneruj plan VEGAN dla „Kurczak z ryżem" → conflict (dieta).
5. Wyklucz `NUTS` i wybierz „Jogurt z granolą" → conflict (alergen).
6. Zmień porcję na 2.0 → cena i kalorie ×2 w podsumowaniu planu.
7. Jako `admin@zpo.local` przejrzyj wszystkie plany w panelu cateringu.
