# Preset: vr-arcade

Rezerwacja stanowisk / gogli VR.

| Pojęcie generyczne | Znaczenie domenowe |
|--------------------|--------------------|
| `Resource`         | Strefa VR z pulą gogli |
| `Request`          | Rezerwacja gogli dla grupy |
| `calculatedValue`  | Koszt rezerwacji (PLN) |

## Model

- **AlgorithmMode:** `CAPACITY_MATCHING`
- **PricingUnit:** `PER_UNIT`
- `requiresTimeWindow = false`, `requiresQuantity = true`

### Resource `metadata_json`
`zoneName`, `totalHeadsets`, `gameTypes` (CSV), `maxPlayers`, `hasMultiplayer`.
`baseValue` = cena za gogle, `capacityValue` = liczba gogli.

### Request `metadata_json`
`playersCount`, `gameType`, `customerName`, `qrCode`.

## Algorytm

1. `RESOURCE_CHECK` – strefa musi być `ACTIVE`.
2. Pula gogli: `currentlyReserved` = suma zarezerwowanych gogli (`reduce`),
   `remainingHeadsets` = `totalHeadsets` − `currentlyReserved`.
3. `HEADSET_POOL_CHECK` – `requestedHeadsets` ≤ `remainingHeadsets`, inaczej conflict.
4. `GAME_TYPE_CHECK` – strefa musi oferować wybraną grę.
5. `QR_CODE` – kod odbioru (wygenerowany, jeśli brak).
6. Wycena: `headsetPrice` = cena × liczba gogli.

`appliedRules`: `TOTAL_HEADSETS`, `CURRENTLY_RESERVED`, `REQUESTED_HEADSETS`, `REMAINING_HEADSETS`,
`HEADSET_POOL_CHECK`, `GAME_TYPE_CHECK`, `QR_CODE`, `HEADSET_PRICE`, `TOTAL_PRICE`.

## Role

- **USER (klient):** widzi rezerwację i kod QR.
- **OPERATOR (obsługa):** skanuje QR i wydaje sprzęt.
- **ADMIN:** zarządza strefami i wszystkimi rezerwacjami.

## Jak zastosować

```sh
./scripts/apply-preset.sh vr-arcade
cd backend && ./mvnw test
cd frontend && npm run build
```

Logowania demo: `admin@zpo.local/admin123`, `operator@zpo.local/operator123`, `user@zpo.local/user123`.
