# Test cases: online-course-platform (Platforma kursów online)

Zgodność z punktacją ZPO:
- **Algorytm (60% punktów)**: testy 1-4 pokrywają logikę wyliczania postępu i walidację
- **Testy jednostkowe (1-2 pkt)**: 6 testów pokrywających algorytm i security
- **Security (4-5 pkt)**: testy 5-6 weryfikują polityki dostępu

## Testy algorytmu

### 1. Postęp 0%
- Kurs: `totalLessons=20`, `lessonsCompleted=0`.
- Oczekiwane: `progressPercent=0.00`, `completed=false`, sukces.
- **Cel**: weryfikacja poprawnego liczenia dla braku postępu

### 2. Postęp częściowy
- Kurs: `totalLessons=30`, `lessonsCompleted=12`.
- Oczekiwane: `progressPercent=40.00` ((12/30)×100), `completed=false`.
- **Cel**: sprawdzenie wyliczania procentu dla typowego scenariusza

### 3. Ukończenie kursu (100%)
- Kurs: `totalLessons=25`, `lessonsCompleted=25`.
- Oczekiwane: `progressPercent=100.00`, `completed=true`.
- **Cel**: weryfikacja flagi ukończenia przy 100% postępu

### 4. Przekroczenie limitu lekcji
- Kurs: `totalLessons=15`, `lessonsCompleted=20`.
- Oczekiwane: błąd walidacji "Liczba ukończonych lekcji przekracza łączną liczbę lekcji w kursie".
- **Cel**: sprawdzenie walidacji brzegowej

### 5. Ujemna liczba lekcji
- Kurs: `totalLessons=10`, `lessonsCompleted=-1`.
- Oczekiwane: błąd "Liczba ukończonych lekcji nie może być ujemna".
- **Cel**: weryfikacja odrzucania niepoprawnych danych wejściowych

### 6. Zaokrąglenie procentu
- Kurs: `totalLessons=7`, `lessonsCompleted=2`.
- Oczekiwane: `progressPercent=28.57` (2/7×100 = 28.5714... zaokrąglone do 2 miejsc).
- **Cel**: sprawdzenie precyzji zaokrąglania

## Testy security

### 7. USER widzi tylko swoje postępy
- Zalogowany jako Anna Nowak (`user@zpo.local`).
- Żądanie listy postępów zwraca tylko wpisy należące do tego użytkownika.
- **Cel**: weryfikacja `RequestAccessPolicy`

### 8. ADMIN widzi wszystkie postępy
- Zalogowany jako Administrator (`admin@zpo.local`).
- Żądanie listy postępów zwraca wszystkie postępy w systemie.
- **Cel**: weryfikacja `AdminPolicy`

## Uruchomienie

```bash
cd backend && ./mvnw test
```
