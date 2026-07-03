# PROJECT_OVERVIEW

## Co to jest

To system obsługi warsztatu samochodowego. Aplikacja pozwala zarządzać stanowiskami serwisowymi i zleceniami naprawy pojazdów.

W aktualnym temacie:

- `ResourceEntity` oznacza stanowisko serwisowe.
- `RequestEntity` oznacza zlecenie warsztatowe.

Nazwy techniczne klas pozostają neutralne, ale UI, seed danych i algorytm są ustawione pod warsztat samochodowy.

## Technologie

- Backend: Java 21, Spring Boot, Spring Web, Spring Security, Spring Data JPA, Hibernate.
- Baza danych: PostgreSQL uruchamiany przez Docker Compose.
- Frontend: Vue 3, TypeScript, Vite, Pinia, Vue Router, Tailwind CSS.
- API docs: Swagger / OpenAPI.
- Testy: JUnit 5.

## Baza danych

Baza działa w PostgreSQL:

- `docker-compose.yml`
- `backend/docker-compose.yml`

Główne encje:

- `UserEntity`
- `ResourceEntity`
- `RequestEntity`

Metadane warsztatowe są w JSONB:

- `ResourceEntity.metadata` - kod stanowiska, specjalizacja, typ podnośnika, masa pojazdu.
- `RequestEntity.metadata` - dane pojazdu, typ usługi, priorytet, części.
- `RequestEntity.algorithmBreakdown` - rozbicie wyceny.

## Security

Security jest w `backend/src/main/java/pl/zpo/app/security/`.

Projekt używa:

- Spring Security,
- JWT,
- BCrypt,
- ról `USER`, `ADMIN`, `OPERATOR`,
- polityk `RequestAccessPolicy` i `AdminPolicy`.

USER widzi swoje zlecenia. ADMIN widzi wszystkie zlecenia.

## Algorytm

Algorytm jest w:

```text
backend/src/main/java/pl/zpo/app/domain/algorithm/DefaultDomainAlgorithm.java
```

Dla warsztatu sprawdza:

- kolizję terminu stanowiska,
- dopasowanie stanowiska do typu usługi,
- koszt robocizny,
- mnożniki usługi, priorytetu i trudności,
- koszt części,
- breakdown decyzji.

## Swagger

Swagger:

<http://localhost:8080/swagger-ui/index.html>

## Testy

```bash
cd backend && ./mvnw test
```

Testy obejmują algorytm, kolizje, capacity i polityki dostępu.

## Presety

Mechanizm presetów pozwala zmienić temat bez przebudowy endpointów i encji:

```bash
./scripts/apply-preset.sh mechanic-workshop
```
