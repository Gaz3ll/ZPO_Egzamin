# Test cases: library-book-rental (Biblioteka - wypożyczalnia)

Zgodność z punktacją ZPO:
- **Algorytm (60% punktów)**: testy 1-5 pokrywają logikę dostępności, kary i przeterminowania
- **Testy jednostkowe (1-2 pkt)**: 7 testów pokrywających algorytm i security
- **Security (4-5 pkt)**: testy 6-7 weryfikują polityki dostępu

## Testy algorytmu

### 1. Dostępność egzemplarzy
- Książka "Pan Tadeusz", `totalCopies=5`, 1 aktywne wypożyczenie (bez daty zwrotu).
- Oczekiwane: `AVAILABLE_COPIES: 4 / 5`, reguła `CAPACITY_CHECK: ok`.
- **Cel**: weryfikacja poprawnego liczenia dostępnych egzemplarzy

### 2. Brak dostępnych egzemplarzy
- Książka `totalCopies=2`, 2 aktywne wypożyczenia.
- Oczekiwane: błąd "Brak dostępnych egzemplarzy", reguła `CAPACITY_CHECK: brak egzemplarzy`.
- **Cel**: sprawdzenie blokady wypożyczenia przy braku wolnych egzemplarzy

### 3. Kara za opóźnienie
- `dueDate=2026-06-20`, `returnDate=2026-06-25` (5 dni opóźnienia).
- Oczekiwane: `lateFee=10.00` (5 × 2,00 zł), reguła `LATE_FEE: 5 dni, naliczono 10.00 zł`.
- **Cel**: weryfikacja naliczania kary 2zł/dzień

### 4. Zwrot w terminie
- `dueDate=2026-06-25`, `returnDate=2026-06-20` (zwrot przed terminem).
- Oczekiwane: `lateFee=0`, reguła `LATE_FEE: brak opóźnienia (zwrot w terminie)`.
- **Cel**: brak kary przy zwrocie przed terminem

### 5. Przeterminowane wypożyczenie
- `dueDate=2026-06-15`, brak `returnDate`, dzisiejsza data = 2026-07-02.
- Oczekiwane: reguła `OVERDUE: przeterminowane 17 dni`.
- **Cel**: wykrywanie wypożyczeń po terminie

### 6. Wypożyczenie w terminie
- `dueDate=2026-07-10`, brak `returnDate`, dzisiejsza data = 2026-07-02.
- Oczekiwane: reguła `OVERDUE: w terminie`.
- **Cel**: poprawne oznaczenie aktualnych wypożyczeń

## Testy security

### 7. USER widzi tylko swoje wypożyczenia
- Zalogowany jako Jan Kowalski (`user@zpo.local`).
- Żądanie listy wypożyczeń zwraca tylko wypożyczenia należące do tego użytkownika.
- **Cel**: weryfikacja `RequestAccessPolicy`

### 8. LIBRARIAN (ADMIN) widzi wszystkie wypożyczenia
- Zalogowany jako Bibliotekarka (`admin@zpo.local`).
- Żądanie listy wypożyczeń zwraca wszystkie wypożyczenia w systemie.
- **Cel**: weryfikacja `AdminPolicy`

## Uruchomienie

```bash
cd backend && ./mvnw test
```
