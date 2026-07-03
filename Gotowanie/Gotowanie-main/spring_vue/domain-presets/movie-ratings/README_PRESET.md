# Preset: movie-ratings

Temat: **Ocena filmów** - prosta apka do oceniania filmów w systemie 1-5. Pokazywanie średniej oraz top najlepszych dla danego gatunku lub ogólnego.

## Mapowanie ZPO

| Wymaganie | Status |
|-----------|--------|
| Baza danych (filmy, oceny) | `UserEntity`, `ResourceEntity`(film), `RequestEntity`(ocena) w PostgreSQL |
| UI - widok dostępnych filmów | Lista filmów z filtrowaniem po gatunku, karta z metadanymi |
| UI - formularz oceny | Wybór filmu, ocena 1-5, opcjonalna recenzja |
| Algorytm - walidacja | Sprawdza czy ocena mieści się w zakresie 1-5 |
| Algorytm - średnia | Liczy średnią ocen dla filmu ze wszystkich ocen |
| Algorytm - breakdown | Zwraca podział: twoja ocena, poprzednia średnia, nowa średnia, liczba ocen |
| Endpoint REST POST | `POST /api/requests` do dodawania oceny |
| Swagger | Dokumentacja OpenAPI na `/swagger-ui/index.html` |
| Security - USER | Widzi tylko swoje oceny |
| Security - ADMIN | Widzi wszystkie oceny |
| Test jednostkowy algorytmu | JUnit 5 - walidacja, średnia, brak wcześniejszych ocen |

## Mapowanie domeny

- `Resource` = **Film** (gatunek, rok produkcji, reżyser, czas trwania)
- `Request` = **Ocena** (ocena 1-5, recenzja)
- `DomainAlgorithm` = obliczanie średniej oceny z breakdownem

## Logika algorytmu

1. Sprawdza czy film istnieje i jest aktywny
2. Waliduje ocenę (1-5)
3. Zbiera wszystkie aktywne oceny dla tego filmu
4. Liczy średnią = (suma wszystkich ocen + nowa ocena) / (liczba ocen + 1)
5. Zwraca breakdown: twoja ocena, poprzednia średnia, nowa średnia, liczba ocen

## Jak zastosować

```bash
./scripts/apply-preset.sh movie-ratings
```

## Konta demo

- `admin@filmy.local` / `admin123`
- `mod@filmy.local` / `mod123`
- `user@filmy.local` / `user123`

## Uruchomienie testów

```bash
cd backend && ./mvnw test
cd frontend && npm run build
```
