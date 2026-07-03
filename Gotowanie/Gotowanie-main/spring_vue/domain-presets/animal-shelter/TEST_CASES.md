# Test cases: animal-shelter

| # | Scenariusz | Wejście | Oczekiwany wynik |
|---|-----------|---------|------------------|
| 1 | Pasujący gatunek | `preferredSpecies=DOG`, zwierzę `DOG` | `SPECIES_MATCH: ok` |
| 2 | Brak ogrodu | Zwierzę wymaga ogrodu, wniosek bez ogrodu | `success=false`, `GARDEN_REQUIREMENT: failed` |
| 3 | Zbyt niski budżet | `budgetMonthly < monthlyCost` | `success=false`, `BUDGET_CHECK: too low` |
| 4 | Dobry profil | Gatunek, ogród i budżet pasują | `MATCHING_SCORE` dodatni |
| 5 | Zwierzę niedostępne | `ResourceStatus.UNAVAILABLE` | `success=false` |
| 6 | Koszt | Akceptowany wniosek | `calculatedValue` = opłata adopcyjna + pakiet |
