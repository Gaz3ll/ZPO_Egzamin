# Preset: parcel-locker

Temat: **System paczkomatów** - nadawanie paczek z automatycznym doborem skrytki.

## Mapowanie ZPO

| Wymaganie | Status |
|-----------|--------|
| Baza danych (paczki, zajętość, pojemność) | `UserEntity`, `ResourceEntity`(skrytka), `RequestEntity`(paczka) w PostgreSQL |
| UI - formularz nadawania | Dane odbiorcy, rozmiar, waga, kod odbioru |
| UI - potwierdzenie | Podsumowanie z wybraną skrytką i kosztem |
| Algorytm - dobór skrytki | Najmniejsza możliwa skrytka pasująca do gabarytów paczki |
| Endpoint REST POST | `POST /api/requests` do nadawania paczki |
| Security - USER | Widzi tylko swoje paczki |
| Security - KURIER/OPERATOR | Widzi paczki do wyciągnięcia |
| Test jednostkowy algorytmu | JUnit 5 - dobór skrytki, waga, rozmiar |

## Mapowanie domeny

- `Resource` = **Skrytka paczkomatu** (kod, lokalizacja, rozmiar S/M/L/XL, max waga)
- `Request` = **Paczka** (odbiorca, email, rozmiar, waga, kod odbioru)
- `DomainAlgorithm` = dobór najmniejszej wolnej skrytki pasującej do paczki

## Logika algorytmu

1. Weryfikuje rozmiar paczki (S/M/L/XL) i wagę
2. Pobiera wszystkie aktywne skrytki
3. Odrzuca skrytki zajęte (aktywna paczka lub flaga `isOccupied`)
4. Odrzuca skrytki za małe na dany rozmiar paczki
5. Odrzuca skrytki z za małym limitem wagowym
6. Wybiera **najmniejszą pasującą skrytkę** (oszczędność miejsca)
7. Liczy koszt:
   - `baseShippingCost` = bazowa opłata skrytki
   - `sizeFee` = S(0zł), M(4zł), L(8zł), XL(12zł)
   - `weightFee` = powyżej 5kg: +1.50zł/kg

## Jak zastosować

```bash
./scripts/apply-preset.sh parcel-locker
```

## Konta demo

- `admin@zpo.local` / `admin123`
- `operator@zpo.local` / `operator123`
- `user@zpo.local` / `user123`

## Uruchomienie testów

```bash
cd backend && ./mvnw test
cd frontend && npm run build
```
