# Preset: online-course-platform

Temat: **Platforma kursów online** - system do śledzenia postępów w nauce z algorytmicznym wyliczaniem procentu ukończenia.

## Mapowanie ZPO

| Wymaganie | Status |
|-----------|--------|
| Baza danych (kursy, postępy) | `UserEntity`, `ResourceEntity`(kurs), `RequestEntity`(postęp) w PostgreSQL |
| UI - lista dostępnych kursów | Lista kursów z kategorią, poziomem trudności i liczbą lekcji |
| UI - formularz dodawania postępu | Wybór kursu, liczba ukończonych lekcji, automatyczne wyliczenie procentu |
| Algorytm - procent ukończenia | `progressPercent = lessonsCompleted / totalLessons × 100` |
| Algorytm - status ukończenia | `completed = true` gdy `progressPercent ≥ 100%` |
| Endpoint REST POST | `POST /api/requests` do rejestracji postępu |
| Swagger | Dokumentacja OpenAPI na `/swagger-ui/index.html` |
| Security - USER | Widzi tylko swoje postępy |
| Security - ADMIN | Widzi wszystkie postępy i zarządza kursami |
| Test jednostkowy algorytmu | JUnit 5 - wyliczanie procentu, walidacja limitów |

## Mapowanie domeny

- `Resource` = **Kurs** (tytuł, kategoria, trudność, liczba lekcji, szacowany czas)
- `Request` = **Postęp** (ukończone lekcje, procent ukończenia, status ukończenia)
- `DomainAlgorithm` = wyliczenie procentu ukończenia i flagi `completed`

## Logika algorytmu

1. Sprawdza aktywność kursu
2. Pobiera `totalLessons` z metadanych zasobu
3. Pobiera `lessonsCompleted` z metadanych żądania
4. Waliduje: `0 ≤ lessonsCompleted ≤ totalLessons`
5. Oblicza `progressPercent = (lessonsCompleted / totalLessons) × 100` (z dokładnością do 2 miejsc po przecinku)
6. Ustawia `completed = true` gdy `progressPercent ≥ 100`
7. Zwraca `progressPercent` jako `calculatedValue`

## Jak zastosować

```bash
./scripts/apply-preset.sh online-course-platform
```

## Konta demo

- `admin@zpo.local` / `admin123`
- `instructor@zpo.local` / `instructor123`
- `user@zpo.local` / `user123`

## Uruchomienie testów

```bash
cd backend && ./mvnw test
cd frontend && npm run build
```
