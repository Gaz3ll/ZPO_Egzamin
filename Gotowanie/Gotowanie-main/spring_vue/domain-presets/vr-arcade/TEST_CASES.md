# Test cases: vr-arcade

| # | Scenariusz | Wejście | Oczekiwany wynik |
|---|-----------|---------|------------------|
| 1 | Mieści się w puli | 8 gogli, zajęte 3, żądane 2 | `HEADSET_POOL_CHECK: ok`, `remainingHeadsets` ≥ 0 |
| 2 | Przekroczenie puli | 8 gogli, zajęte 7, żądane 3 | `success=false`, `HEADSET_POOL_CHECK: przekroczono pulę` |
| 3 | Kod QR istnieje | Rezerwacja bez `qrCode` | wygenerowany `QR_CODE: VR-...` |
| 4 | Niedostępna gra | `gameType` spoza `gameTypes` | `success=false`, `GAME_TYPE_CHECK: mismatch` |
| 5 | Obsługa widzi wydania | OPERATOR pobiera listę | wszystkie rezerwacje z kodami QR |

## Ręczna weryfikacja

1. `./scripts/apply-preset.sh vr-arcade`
2. `cd backend && ./mvnw test` – `DefaultDomainAlgorithmTest` przechodzi (ścieżka generyczna).
3. Zarezerwuj więcej gogli niż wolnych → oczekiwany conflict.
4. Zarezerwuj poprawnie → sprawdź `QR_CODE` i `remainingHeadsets` w breakdownie.
