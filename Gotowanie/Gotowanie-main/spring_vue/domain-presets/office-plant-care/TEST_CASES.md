# Test cases: office-plant-care

## Testy algorytmu

| # | Scenariusz | Wejście | Oczekiwany wynik |
|---|-----------|---------|------------------|
| 1 | Adopcja dostępnej rośliny | `careType=ADOPTION`, `isAdopted=false` | `success=true`, `ADOPTION_CHECK: dostępna do adopcji` |
| 2 | Blokada adopcji zaadoptowanej | `careType=ADOPTION`, `isAdopted=true` | `success=false`, `ADOPTION_CHECK: roślina już zaadoptowana` |
| 3 | Podlewanie po terminie zwiększa priorytet | `waterFrequencyDays=7`, `lastCareAt` 14 dni temu | `OVERDUE_FACTOR: × 2.00`, wyższy `carePriorityScore` |
| 4 | Zdrowie CRITICAL zwiększa priorytet | `healthStatus=CRITICAL` | `HEALTH_MULTIPLIER: × 2.00` |
| 5 | Nawożenie liczone wg częstotliwości | `careType=FERTILIZING`, `fertilizeFrequencyDays=30` | `RECOMMENDED_FREQUENCY: co 30 dni` |
| 6 | Przesadzanie liczone z miesięcy | `careType=REPOTTING`, `repotFrequencyMonths=12` | `RECOMMENDED_FREQUENCY: co 360 dni` |
| 7 | Następna zalecana data | `lastCareAt=2026-07-01`, co 7 dni | `NEXT_RECOMMENDED_DATE: 2026-07-08` |

## Testy security / policy

| # | Scenariusz | Oczekiwany wynik |
|---|-----------|------------------|
| 8  | USER widzi tylko swoje zadania | `GET /api/requests` zwraca wyłącznie zadania zalogowanego |
| 9  | ADMIN widzi wszystkie zadania | panel admina listuje zadania wszystkich pracowników |
| 10 | USER nie zarządza rejestrem roślin | `POST /api/admin/resources` → 403 |
| 11 | Brak tokenu | `GET /api/requests` bez JWT → 401 |

## Testy brzegowe

| # | Scenariusz | Oczekiwany wynik |
|---|-----------|------------------|
| 12 | Roślina w kwarantannie | `UNAVAILABLE` → `success=false`, `RESOURCE_CHECK` |
| 13 | Brak `careType` | `success=false`, `CARE_TYPE: brak` |
| 14 | Brak `lastCareAt` | przyjęta zalecana częstotliwość, `OVERDUE_FACTOR: × 1.00` |
| 15 | Bardzo dawna pielęgnacja | overdue factor ograniczony do ×3.00 (cap) |
| 16 | Brak częstotliwości w metadanych | fallback: WATERING 7 dni, FERTILIZING 30, REPOTTING 12 mies. |
| 17 | Adopcja nie liczy terminu | `careType=ADOPTION` → brak `DAYS_SINCE_LAST_CARE`, score bez overdue |

## Ręczna weryfikacja

1. `./scripts/apply-preset.sh office-plant-care`
2. `cd backend && ./mvnw test` – `DefaultDomainAlgorithmTest` przechodzi (ścieżka generyczna).
3. Jako `user@zpo.local` zaadoptuj „Paprotkę w łazience" → sukces.
4. Spróbuj zaadoptować „Monsterę przy oknie" (`isAdopted=true`) → conflict.
5. Dodaj podlewanie Fikusa z datą sprzed 2 tygodni → wysoki priorytet (po terminie + BAD).
6. Jako `admin@zpo.local` sprawdź wszystkie zadania w panelu.
