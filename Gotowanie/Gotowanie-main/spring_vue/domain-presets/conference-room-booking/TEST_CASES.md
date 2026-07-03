# Test cases: conference-room-booking (System rezerwacji salek konferencyjnych)

Zgodność z punktacją ZPO:
- **Algorytm (60% punktów)**: testy 1-5 pokrywają logikę kosztu, pojemności, kolizji i walidacji czasu
- **Testy jednostkowe (1-2 pkt)**: 7 testów pokrywających algorytm i security
- **Security (4-5 pkt)**: testy 6-7 weryfikują polityki dostępu

## Testy algorytmu

### 1. Koszt podstawowy
- Sala: `Sala A`, `hourlyRate=50`, zakres 2 godzin (np. 10:00-12:00), `attendeeCount=3`.
- Oczekiwane: `cost=100` (50 × 2h), reguła `COST_CALCULATION: 50 x 2 = 100`.
- **Cel**: weryfikacja poprawnego liczenia kosztu (hourlyRate × godziny)

### 2. Zaokrąglanie czasu
- Sala `Sala B`, `hourlyRate=80`, zakres 2.5 godziny (np. 10:00-12:30).
- Oczekiwane: `durationUnits=3` (zaokrąglenie w górę ceil(150min/60) = 3), `cost=240`.
- **Cel**: sprawdzenie zaokrąglania czasu przy niepełnych godzinach

### 3. Przekroczenie pojemności
- Sala `Sala A` (pojemność 10), `attendeeCount=15`.
- Oczekiwane: błąd `CAPACITY_CHECK: przekroczono (15 > 10)`, reguła `CAPACITY_CHECK: przekroczono`.
- **Cel**: weryfikacja sprawdzania pojemności sali

### 4. Kolizja terminów
- Druga rezerwacja tej samej sali nachodzi na potwierdzoną rezerwację (np. 10:00-14:00 istnieje, nowa 11:00-13:00).
- Oczekiwane: błąd i reguła `TIME_COLLISION_CHECK: kolizja`.
- **Cel**: sprawdzenie detekcji kolizji czasowych

### 5. Brak kolizji
- Nowa rezerwacja zaczyna się dokładnie w momencie zakończenia poprzedniej (np. 12:00-14:00 po 10:00-12:00).
- Oczekiwane: sukces, reguła `TIME_COLLISION_CHECK: brak kolizji`.
- **Cel**: weryfikacja że stykające się terminy nie są kolizją

### 6. Niekompletny zakres czasu
- Brak `startAt` lub `endAt`.
- Oczekiwane: błąd `TIME_RANGE_CHECK: niekompletny zakres`.
- **Cel**: sprawdzenie walidacji wymaganego zakresu czasu

### 7. Sala nieaktywna
- Rezerwacja na salę o statusie `INACTIVE` lub `UNAVAILABLE`.
- Oczekiwane: błąd `RESOURCE_CHECK: sala nieaktywna`, reguła `RESOURCE_CHECK: nieaktywna`.
- **Cel**: weryfikacja sprawdzania statusu sali

## Testy security

### 8. USER widzi tylko swoje rezerwacje
- Zalogowany jako Jan Kowalski (`user@zpo.local`).
- Żądanie listy rezerwacji zwraca tylko rezerwacje należące do tego użytkownika.
- **Cel**: weryfikacja `RequestAccessPolicy`

### 9. ADMIN widzi wszystkie rezerwacje
- Zalogowany jako Administrator (`admin@zpo.local`).
- Żądanie listy rezerwacji zwraca wszystkie rezerwacje w systemie.
- **Cel**: weryfikacja `AdminPolicy`

## Uruchomienie

```bash
cd backend && ./mvnw test
```
