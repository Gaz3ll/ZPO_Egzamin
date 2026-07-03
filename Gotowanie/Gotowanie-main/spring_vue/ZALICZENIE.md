# ZALICZENIE - system obsługi warsztatu samochodowego

## Temat

System obsługi warsztatu samochodowego: stanowiska serwisowe, zlecenia naprawy, role użytkowników, wycena algorytmiczna i panel administracyjny.

## Co pokazuję

- Frontend Vue: `http://localhost:5173`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- Backend Spring Boot: `http://localhost:8080`
- PostgreSQL Docker: `localhost:5432`

## Konta

- USER: `user@zpo.local` / `user123`
- ADMIN: `admin@zpo.local` / `admin123`
- OPERATOR: `operator@zpo.local` / `operator123`

## Mapowanie domeny

- `ResourceEntity` = stanowisko serwisowe.
- `RequestEntity` = zlecenie warsztatowe.
- `metadata_json` stanowiska = kod, specjalizacja, podnośnik, masa pojazdu, mnożnik trudności.
- `metadata_json` zlecenia = pojazd, rejestracja, typ usługi, priorytet, części, opis usterki.
- `algorithm_breakdown_json` = rozbicie wyceny i reguł algorytmu.

Nazwy techniczne klas są neutralne, ale aktualny temat aplikacji to warsztat samochodowy.

## Uruchomienie

```bash
docker compose up -d
cd backend
./mvnw spring-boot:run
```

W drugim terminalu:

```bash
cd frontend
npm install
npm run dev
```

## Algorytm

Plik:

```text
backend/src/main/java/pl/zpo/app/domain/algorithm/DefaultDomainAlgorithm.java
```

Algorytm:

- sprawdza, czy stanowisko jest aktywne,
- sprawdza kolizje terminów,
- sprawdza dopasowanie specjalizacji stanowiska do typu usługi,
- liczy robociznę po czasie i stawce stanowiska,
- dolicza mnożnik typu usługi,
- dolicza mnożnik priorytetu,
- dolicza mnożnik trudności stanowiska,
- dolicza części i koszt obsługi części,
- zapisuje breakdown.

## Security

- Spring Security.
- JWT.
- BCrypt.
- Role: `USER`, `ADMIN`, `OPERATOR`.
- USER widzi tylko swoje zlecenia.
- ADMIN widzi wszystkie zlecenia.
- Backend zwraca 401/403 przy braku uprawnień.

## Baza danych

- PostgreSQL 16 w Dockerze.
- Spring Data JPA / Hibernate.
- Encje:
  - `UserEntity`
  - `ResourceEntity`
  - `RequestEntity`
- JSONB:
  - `metadata_json`
  - `algorithm_breakdown_json`
- Indeksy na statusach, zasobie, właścicielu i czasie.

## REST API

- `AuthController`
- `ResourceController`
- `RequestController`
- `AdminResourceController`
- `AdminRequestController`
- `ConfigController`

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

## Testy

```bash
cd backend && ./mvnw test
cd frontend && npm run build
```

Aktualnie backend ma 29 testów: algorytm, kolizje, capacity i polityki dostępu.

## Krótki scenariusz prezentacji

1. Uruchamiam frontend.
2. Loguję się jako USER.
3. Pokazuję stanowiska serwisowe.
4. Tworzę zlecenie dla pojazdu.
5. Pokazuję breakdown wyceny.
6. Loguję się jako ADMIN.
7. Pokazuję panel kierownika warsztatu i wszystkie zlecenia.
8. Pokazuję Swagger.
9. Pokazuję wynik testów.
