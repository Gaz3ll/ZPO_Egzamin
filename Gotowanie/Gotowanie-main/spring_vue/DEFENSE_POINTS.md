# DEFENSE_POINTS

Mapowanie projektu na wymagania i punktację ZPO.

## System punktacji ZPO

```
11 pkt = ocena 3    15 pkt = ocena 4    19 pkt = ocena 5
50% za drugie podejście
x1.5 za sprzedanie tematu
x1.2 za wszystkich obecnych (niemożliwe)
x1.3 za szybkie oddanie
```

## Punktacja składowa

| Kategoria | Punkty | Status |
|-----------|--------|--------|
| Baza danych (PostgreSQL, JPA, JSONB, indeksy) | 1-2 | 2/2 |
| UI i algorytm (Vue + Spring, ~60% wagi) | ~60% | Pokrycie: dashboard, CRUD, algorytm, breakdown |
| Security (Spring Security, JWT, BCrypt, role, polityki) | 4-5 | 5/5 |
| Testy (JUnit 5 - algorytm, kolizje, security) | 1-2 | 2/2 |
| REST API (Swagger, endpointy CRUD) | 1-2 | 2/2 |

## 15 presetów ZPO (tematy wykładowcy)

| # | Preset | Temat | Algorytm |
|---|--------|-------|----------|
| 1 | `movie-ratings` | Ocena filmów | Średnia ocen, top po gatunku |
| 2 | `conference-room-booking` | Rezerwacja salek | Kolizje, pojemność, cena/h |
| 3 | `fitness-classes` | Zajęcia fitness | Limit miejsc, lista rezerwowa |
| 4 | `employee-scheduler` | Zarządzanie pracownikami | Grafiki, nadgodziny |
| 5 | `meal-planning-catering` | Planowanie posiłków | Kalorie, dieta, skalowanie |
| 6 | `animal-shelter` | Schronisko | Dopasowanie adopcyjne |
| 7 | `online-course-platform` | Kursy online | Postęp %, ukończenie |
| 8 | `player-ranking` | Ranking graczy | Punkty, historia meczów |
| 9 | `habit-tracker` | Habit tracker | Streak, % realizacji |
| 10 | `library-book-rental` | Wypożyczalnia książek | Kary, dostępność kopii |
| 11 | `estate-issue-reporting` | Zgłoszenia osiedlowe | Auto-priorytet, status |
| 12 | `workout-tracker` | Rejestr treningów | Objętość, czas tyg. |
| 13 | `exam-study-planner` | Plan nauki | Podział materiału |
| 14 | `office-plant-care` | Adopcja roślin | Terminarz pielęgnacji |
| 15 | `mood-diary` | Dziennik nastroju | Średnia, rozkład |

Zmiana tematu: `./scripts/apply-preset.sh <nazwa>` (działa dla wszystkich powyższych)

## Baza danych

- PostgreSQL Docker przez `docker-compose.yml`.
- JPA/Hibernate w backendzie Spring Boot.
- Encje:
  - `UserEntity`
  - `ResourceEntity`
  - `RequestEntity`
- Relacje logiczne:
  - `RequestEntity.ownerId` wskazuje użytkownika.
  - `RequestEntity.resourceId` wskazuje zasób.
- Enumy:
  - role użytkowników,
  - statusy zasobów,
  - statusy requestów,
  - tryby algorytmu,
  - jednostki wyceny.
- JSONB metadata:
  - `ResourceEntity.metadata`
  - `RequestEntity.metadata`
  - `RequestEntity.algorithmBreakdown`
- Indeksy:
  - status zasobu,
  - typ zasobu,
  - właściciel requestu,
  - zasób requestu,
  - status requestu,
  - zakres czasu requestu.

## UI

- Vue 3 + TypeScript + Vite.
- Dashboard.
- Lista zasobów.
- Tworzenie requestu.
- Moje requesty.
- Szczegóły requestu.
- Admin panel.
- Tabele administracyjne zasobów i requestów.
- Widok breakdownu algorytmu.
- Dynamiczne pola metadanych z `domain.config.ts`.

## Algorytm

- Główna klasa: `DefaultDomainAlgorithm.java`.
- Interfejs: `DomainAlgorithm`.
- Wynik: `DomainAlgorithmResult`.
- Dostępność: `AvailabilityService`.
- Kolizje czasu: `TimeCollisionDetector`.
- Capacity matching: `CapacityMatcher`.
- Wyjaśnienie decyzji: `AlgorithmBreakdown`.
- Testy jednostkowe algorytmu.

## Security

- Spring Security.
- JWT.
- BCrypt.
- Role:
  - `USER`
  - `ADMIN`
  - `OPERATOR`
- `RequestAccessPolicy` do kontroli dostępu do requestów.
- `AdminPolicy` do logiki uprawnień admin/operator.
- Obsługa 401/403.
- Autoryzacja jest wymuszana po stronie backendu.

## REST API

- `ResourceController`
- `RequestController`
- `AdminResourceController`
- `AdminRequestController`
- `AuthController`
- `ConfigController`
- Swagger/OpenAPI:
  - <http://localhost:8080/swagger-ui/index.html>

## Testy

- 29 testów backendu.
- Zakres:
  - algorytm,
  - kolizje,
  - capacity,
  - request policy,
  - admin policy.
- Uruchomienie:

```bash
cd backend && ./mvnw test
```

## Generyczność

- Neutralne nazwy klas.
- Neutralne endpointy:
  - `/api/resources`
  - `/api/requests`
- `DomainProfileProvider` definiuje aktywny profil domeny.
- Frontend używa `frontend/src/config/domain.config.ts`.
- Metadane domenowe są w JSONB zamiast w osobnych kolumnach dla konkretnego tematu.
- Presety są w `domain-presets/`.
- Skrypt `scripts/apply-preset.sh` podmienia wyłącznie pliki konfiguracyjno-domenowe.
