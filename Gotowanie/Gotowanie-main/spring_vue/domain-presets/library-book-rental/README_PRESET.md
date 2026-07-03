# Preset: library-book-rental

Temat: **Biblioteka - wypożyczalnia** - system do zarządzania wypożyczaniem książek w bibliotece z własnym rejestrem książek, możliwością wypożyczenia użytkownikowi z karą biblioteczną i naliczania opłat za opóźnienie oraz z listą aktualnych i przeterminowanych wypożyczeń.

## Mapowanie ZPO

| Wymaganie | Status |
|-----------|--------|
| Baza danych (książki, wypożyczenia) | `UserEntity`, `ResourceEntity`(książka), `RequestEntity`(wypożyczenie) w PostgreSQL |
| UI - widok katalogu książek | Lista książek z filtrowaniem po kategorii |
| UI - formularz wypożyczenia | Wybór książki, czytelnik, daty |
| Algorytm - dostępność egzemplarzy | Sprawdza liczbę wolnych egzemplarzy |
| Algorytm - kara za opóźnienie | 2zł/dzień opóźnienia przy zwrocie |
| Algorytm - przeterminowane wypożyczenia | Oznacza wypożyczenia po terminie zwrotu |
| Endpoint REST POST | `POST /api/requests` do tworzenia wypożyczenia |
| Swagger | Dokumentacja OpenAPI na `/swagger-ui/index.html` |
| Security - USER | Widzi tylko swoje wypożyczenia |
| Security - ADMIN (bibliotekarz) | Widzi wszystkie wypożyczenia, zarządza książkami |
| Test jednostkowy algorytmu | JUnit 5 - dostępność, kary, przeterminowanie |

## Mapowanie domeny

- `Resource` = **Książka** (tytuł, autor, ISBN, kategoria, liczba egzemplarzy)
- `Request` = **Wypożyczenie** (czytelnik, data wypożyczenia, termin zwrotu, data zwrotu, kara)
- `DomainAlgorithm` = kalkulacja dostępności egzemplarzy i kary za opóźnienie

## Logika algorytmu

1. Sprawdza czy książka jest aktywna
2. Sprawdza dostępność egzemplarzy: `totalCopies - activeBorrows > 0`
3. Jeśli `returnDate > dueDate`:
   - `lateFee = daysLate × 2,00 zł`
4. Oznacza wypożyczenia przeterminowane (brak `returnDate` i `dueDate` wcześniejsza niż dziś)
5. Zwraca breakdown decyzji

## Jak zastosować

```bash
./scripts/apply-preset.sh library-book-rental
```

## Konta demo

- `admin@zpo.local` / `admin123` (bibliotekarka)
- `operator@zpo.local` / `operator123` (pomocnik)
- `user@zpo.local` / `user123` (czytelnik)

## Uruchomienie testów

```bash
cd backend && ./mvnw test
cd frontend && npm run build
```
