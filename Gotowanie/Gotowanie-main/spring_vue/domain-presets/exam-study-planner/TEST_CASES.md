# Test cases: exam-study-planner

## Testy algorytmu

| # | Scenariusz | Wejście | Oczekiwany wynik |
|---|-----------|---------|------------------|
| 1 | Materiał dzieli się na dni | 24 tematy, egzamin za 14 dni (2 dni powtórek) | `DAILY_MATERIAL: 2 TOPICS/dzień` |
| 2 | Trudność zwiększa czas nauki | `baseValue=1.50` vs `1.00` przy tym samym materiale | wyższy `estimatedDailyMinutes` i `totalStudyMinutes` |
| 3 | Limit dzienny wykrywa przeciążenie | 90 stron, egzamin za 3 dni, limit 60 min | `OVERLOAD_CHECK: przeciążenie` w breakdownie |
| 4 | Dzień powtórki jest dodany | egzamin za 7+ dni | `REVISION_DAYS: 2` i linia `revisionMinutes` |
| 5 | Egzamin w przeszłości daje błąd | `examDate` wczoraj | `success=false`, `EXAM_DATE_CHECK: egzamin w przeszłości` |
| 6 | Krótszy termin → mniej powtórek | egzamin za 2 dni | `REVISION_DAYS: 0` |
| 7 | calculatedValue = czas nauki | dowolny poprawny plan | `calculatedValue = totalStudyMinutes` |

## Testy security / policy

| # | Scenariusz | Oczekiwany wynik |
|---|-----------|------------------|
| 8  | USER widzi tylko swoje plany | `GET /api/requests` zwraca wyłącznie plany zalogowanego |
| 9  | ADMIN widzi wszystkie plany | panel admina listuje plany wszystkich studentów |
| 10 | USER nie zarządza egzaminami | `POST /api/admin/resources` → 403 |
| 11 | Brak tokenu | `GET /api/requests` bez JWT → 401 |

## Testy brzegowe

| # | Scenariusz | Oczekiwany wynik |
|---|-----------|------------------|
| 12 | Brak `examDate` | `success=false`, `EXAM_DATE_CHECK: brak lub nieprawidłowa data` |
| 13 | Nieprawidłowy format daty | jak wyżej — data nie parsuje się |
| 14 | Egzamin nieaktywny | `INACTIVE` → `success=false`, `RESOURCE_CHECK` |
| 15 | Brak limitu dziennego | `OVERLOAD_CHECK: ok (limit nieustawiony)` |
| 16 | `materialCount` brak/0 | przyjęty minimalny materiał (1+), plan liczy się dalej |
| 17 | Egzamin jutro | 1 dzień nauki, 0 powtórek, cały materiał na jeden dzień |

## Ręczna weryfikacja

1. `./scripts/apply-preset.sh exam-study-planner`
2. `cd backend && ./mvnw test` – `DefaultDomainAlgorithmTest` przechodzi (ścieżka generyczna).
3. Jako `user@zpo.local` wygeneruj plan dla „Matematyka dyskretna" → sprawdź `DAILY_MATERIAL`
   i `REVISION_DAYS` w rozkładzie materiału.
4. Ustaw `studyMinutes=30` przy dużym materiale → oczekiwane ostrzeżenie o przeciążeniu.
5. Jako `admin@zpo.local` dodaj egzamin z datą wsteczną i spróbuj wygenerować plan → błąd.
