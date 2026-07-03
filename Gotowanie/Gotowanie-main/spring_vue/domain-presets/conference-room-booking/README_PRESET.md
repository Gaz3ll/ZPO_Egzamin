# Preset: conference-room-booking

Temat: **System rezerwacji salek konferencyjnych** - możliwość rezerwowania sali konferencyjnej przez użytkownika na daną godzinę z uwzględnieniem kompatybilności godzin i pojemności.

## Mapowanie ZPO

| Wymaganie | Status |
|-----------|--------|
| Baza danych (ceny, rezerwacje) | `UserEntity`, `ResourceEntity`(sala), `RequestEntity`(rezerwacja) w PostgreSQL |
| UI - widok dostępnych sal | Lista sal z filtrowaniem, karta z wyposażeniem i stawką |
| UI - formularz rezerwacji | Wybór sali, zakres godzin, tytuł spotkania, liczba uczestników |
| Algorytm - kolizje czasowe | Detekcja nakładających się rezerwacji dla tej samej sali |
| Algorytm - pojemność | Sprawdzenie czy `attendeeCount <= capacityValue` sali |
| Algorytm - koszt | `hourlyRate × liczba godzin` |
| Endpoint REST POST | `POST /api/requests` do tworzenia rezerwacji |
| Swagger | Dokumentacja OpenAPI na `/swagger-ui/index.html` |
| Security - USER | Widzi tylko swoje rezerwacje |
| Security - ADMIN | Widzi wszystkie rezerwacje |
| Test jednostkowy algorytmu | JUnit 5 - kolizje, pojemność, koszt, security |

## Mapowanie domeny

- `Resource` = **Sala konferencyjna** (roomName, capacity, floor, hasProjector, hasVideoConference, hourlyRate)
- `Request` = **Rezerwacja** (meetingTitle, attendeeCount, startAt/endAt)
- `DomainAlgorithm` = kalkulacja kosztu z uwzględnieniem kolizji i pojemności

## Logika algorytmu

1. Sprawdza czy sala jest aktywna (`ACTIVE`)
2. Weryfikuje poprawność zakresu `startAt`/`endAt`
3. Wykrywa kolizje czasowe z aktywnymi rezerwacjami dla tej samej sali
4. Sprawdza czy `attendeeCount <= capacityValue` sali
5. Liczy koszt:
   - `cost = hourlyRate × liczba godzin (durationUnits)`
   - gdzie `durationUnits = ceil(minutes / 60)`
6. Zwraca breakdown decyzji

## Jak zastosować

```bash
./scripts/apply-preset.sh conference-room-booking
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
