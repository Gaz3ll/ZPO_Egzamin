# Test cases: movie-ratings (Ocena filmów)

Zgodność z punktacją ZPO:
- **Algorytm (60% punktów)**: testy 1-4 pokrywają logikę walidacji i obliczania średniej
- **Testy jednostkowe (1-2 pkt)**: 6 testów pokrywających algorytm i security
- **Security (4-5 pkt)**: testy 5-6 weryfikują polityki dostępu

## Testy algorytmu

### 1. Ocena w zakresie 1-5
- Nowa ocena z `rating=4` dla aktywnego filmu.
- Oczekiwane: sukces, reguła `RATING_CHECK: 4`, brak błędów.
- **Cel**: weryfikacja akceptacji poprawnej oceny

### 2. Ocena poza zakresem
- Próba dodania oceny z `rating=0` oraz `rating=6`.
- Oczekiwane: błąd "Ocena musi być w zakresie 1-5", reguła `RATING_CHECK: nieprawidłowa`.
- **Cel**: sprawdzenie walidacji zakresu oceny

### 3. Średnia z wielu ocen
- Dla filmu z istniejącymi ocenami (np. 5, 5) dodanie nowej oceny 4.
- Oczekiwane: `existingAverage = 5.00` (z 2 ocen), `newAverage = 4.67` (z 3 ocen).
- **Cel**: weryfikacja poprawnego liczenia średniej

### 4. Pierwsza ocena filmu
- Dla filmu bez wcześniejszych ocen dodanie `rating=3`.
- Oczekiwane: `newAverage = 3.00`, nota `Brak wcześniejszych ocen dla tego filmu`.
- **Cel**: sprawdzenie działania dla filmu bez ocen

### 5. Nieaktywny film
- Próba dodania oceny dla filmu ze statusem `INACTIVE`.
- Oczekiwane: błąd "Film nie jest aktywny", reguła `RESOURCE_CHECK: film nieaktywny`.
- **Cel**: weryfikacja sprawdzania statusu filmu

### 6. Brak filmu
- Wywołanie algorytmu z `resource=null`.
- Oczekiwane: błąd "Film jest wymagany", reguła `RESOURCE_CHECK: brak filmu`.
- **Cel**: weryfikacja obsługi brakującego zasobu

## Testy security

### 7. USER widzi tylko swoje oceny
- Zalogowany jako Jan Kowalski (`user@filmy.local`).
- Żądanie listy ocen zwraca tylko oceny należące do tego użytkownika.
- **Cel**: weryfikacja `RequestAccessPolicy`

### 8. ADMIN widzi wszystkie oceny
- Zalogowany jako Administrator (`admin@filmy.local`).
- Żądanie listy ocen zwraca wszystkie oceny w systemie.
- **Cel**: weryfikacja `AdminPolicy`

## Uruchomienie

```bash
cd backend && ./mvnw test
```
