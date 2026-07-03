# Preset: escape-room

Rezerwacja pokoi escape room.

| Pojęcie generyczne | Znaczenie domenowe |
|--------------------|--------------------|
| `Resource`         | Pokój escape room |
| `Request`          | Rezerwacja pokoju na godzinę |
| `calculatedValue`  | Cena sesji (PLN) |

## Model

- **AlgorithmMode:** `TIME_AVAILABILITY_AND_CALCULATION`
- **PricingUnit:** `FLAT`
- `requiresTimeWindow = true`, `requiresQuantity = false`

### Resource `metadata_json`
`roomName`, `difficulty`, `theme`, `durationMinutes`, `maxPlayers`, `depositRequired`.
`baseValue` = cena sesji.

### Request `metadata_json`
`teamName`, `playersCount`, `preferredDifficulty`, `preferredTheme`, `depositPaid`.

## Algorytm

1. `RESOURCE_CHECK` – pokój musi być `ACTIVE`.
2. `TIME_COLLISION_CHECK` – kolizja terminu; przy zajętości `ALTERNATIVE_ROOM`
   sugeruje pokój o tej samej trudności w wolnym terminie (notatka w breakdownie).
3. `PLAYERS_CHECK` – `playersCount` ≤ `maxPlayers`.
4. Wycena: `basePrice` (flat) + `difficultyModifier` (MEDIUM ×1.1, HARD ×1.25, EXPERT ×1.4)
   oraz informacja o zaliczce (`DEPOSIT`).

`appliedRules`: `ROOM_DIFFICULTY`, `ROOM_THEME`, `TIME_COLLISION_CHECK`, `ALTERNATIVE_ROOM`,
`PLAYERS_CHECK`, `BASE_PRICE`, `DIFFICULTY_MODIFIER`, `DEPOSIT`, `TOTAL_PRICE`.

## Role

- **USER (gracz):** widzi swoje rezerwacje.
- **OPERATOR (Game Master):** widzi harmonogram i zaliczki.
- **ADMIN:** zarządza pokojami i wszystkimi rezerwacjami.

## Jak zastosować

```sh
./scripts/apply-preset.sh escape-room
cd backend && ./mvnw test
cd frontend && npm run build
```

Logowania demo: `admin@zpo.local/admin123`, `operator@zpo.local/operator123`, `user@zpo.local/user123`.
