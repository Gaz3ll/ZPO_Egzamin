# DEMO_SCRIPT

Gotowy skrypt prezentacji projektu warsztatu samochodowego.

## Wersja 3-minutowa

### 1. Opis projektu

To system obsługi warsztatu samochodowego. Backend jest w Spring Boot, frontend w Vue 3, a baza danych to PostgreSQL w Dockerze.

Aplikacja obsługuje stanowiska serwisowe i zlecenia naprawy pojazdów. Użytkownik tworzy zlecenie, a backend sprawdza termin, dopasowanie stanowiska i wylicza koszt.

### 2. Frontend

Pokazuję:

- pulpit warsztatu,
- stanowiska serwisowe,
- tworzenie zlecenia,
- moje zlecenia,
- panel kierownika warsztatu.

Frontend:

<http://localhost:5173>

### 3. Logowanie USER/ADMIN

USER:

- `user@zpo.local` / `user123`

ADMIN:

- `admin@zpo.local` / `admin123`

USER widzi swoje zlecenia. ADMIN widzi wszystkie zlecenia i panel administracyjny.

### 4. Zlecenie

Tworzę zlecenie dla pojazdu: marka, model, rejestracja, typ usługi, priorytet i ewentualny koszt części.

### 5. Security

Pokazuję, że USER nie ma dostępu do panelu administratora. Backend pilnuje ról przez Spring Security i JWT.

### 6. Algorytm

Po utworzeniu zlecenia backend uruchamia algorytm. Wynik zawiera szacowany koszt oraz breakdown: robocizna, typ usługi, priorytet, trudność stanowiska i części.

### 7. Testy

```bash
cd backend && ./mvnw test
```

## Wersja 7-minutowa

### 1. Architektura

Projekt ma backend Spring Boot, frontend Vue 3 i bazę PostgreSQL. Technicznie główne moduły są nazwane neutralnie, ale aktualna konfiguracja domenowa ustawia system jako warsztat samochodowy.

### 2. Baza danych

Główne encje:

- `UserEntity`,
- `ResourceEntity` jako stanowisko serwisowe,
- `RequestEntity` jako zlecenie warsztatowe.

Metadane warsztatowe są w JSONB, więc stanowisko i zlecenie mają pola specyficzne dla warsztatu bez dokładania nowych tabel.

### 3. UI

Pokazuję stanowiska, formularz zlecenia, moje zlecenia, szczegóły z breakdownem i panel administratora.

### 4. REST API i Swagger

Swagger:

<http://localhost:8080/swagger-ui/index.html>

Pokazuję logowanie, endpointy zasobów, requestów i admina.

### 5. Security

Projekt używa JWT, BCrypt i ról `USER`, `ADMIN`, `OPERATOR`. Autoryzacja jest po stronie backendu.

### 6. Algorytm

Algorytm sprawdza kolizję terminu, zgodność specjalizacji stanowiska i wylicza koszt. Breakdown pokazuje, dlaczego koszt wyszedł taki, a nie inny.

### 7. Docker Postgres

```bash
docker compose up -d
```

### 8. Presety

Aktualny aktywny preset:

```bash
./scripts/apply-preset.sh mechanic-workshop
```

To pozwala w razie potrzeby zmienić temat projektu, ale na prezentacji pokazuję gotowy system warsztatu.
