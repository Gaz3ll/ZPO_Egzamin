# ZPO - Generic Resource & Request Management System

Aplikacja zaliczeniowa ZPO: Spring Boot + PostgreSQL + Vue 3. System generyczny z 20 gotowymi szablonami domenowymi (grafik pracowniczy, sale konferencyjne, fitness, schronisko, filmy, biblioteka...).

## Szybki start

```bash
# 1. Baza danych (Docker)
docker compose up -d

# 2. Backend (Java 21, Maven)
cd backend
./mvnw spring-boot:run

# 3. Frontend (drugi terminal)
cd frontend
npm install
npm run dev
```

| Adres | Opis |
|-------|------|
| http://localhost:5173 | Frontend |
| http://localhost:8080/swagger-ui/index.html | API / Swagger |

## Konta demo

| Rola | Email | Hasło |
|------|-------|-------|
| ADMIN | admin@zpo.local | admin123 |
| OPERATOR | operator@zpo.local | operator123 |
| USER | user@zpo.local | user123 |

## Zmiana szablonu (20 dostępnych)

```powershell
# Lista wszystkich szablonow
.\scripts\apply-preset.ps1 -List

# Przelacz na inny (np. sale konferencyjne)
.\scripts\apply-preset.ps1 conference-room-booking

# Wroc do grafiku
.\scripts\apply-preset.ps1 employee-scheduler
```

Skrypt automatycznie backupuje obecne pliki i podmienia 4 kluczowe pliki:
- `DomainProfileProvider.java` - definicja pól domenowych
- `DefaultDomainAlgorithm.java` - algorytm walidacji i kalkulacji
- `DataInitializer.java` - seed danych demo
- `domain.config.ts` - konfiguracja UI frontendu

## Testy i build

```bash
# Backend testy
cd backend && ./mvnw test

# Frontend build
cd frontend && npm run build
```

## Architektura

| Warstwa | Technologia |
|---------|------------|
| Backend | Java 21, Spring Boot 3, Spring Security + JWT, JPA/Hibernate |
| Baza danych | PostgreSQL 16 (Docker Compose) |
| Frontend | Vue 3, TypeScript, Vite, Pinia, Vue Router, Tailwind CSS |
| API | REST + Swagger/OpenAPI |
| Testy | JUnit 5 |

## Struktura generyczna

System jest domain-agnostic - ten sam kod obsługuje dowolny temat (grafik, rezerwacje, oceny, schronisko...). Specyficzne dla domeny dane są w JSONB (`metadata`), a logika w profilu + algorytmie.

```
Encje:
  UserEntity    - konto użytkownika
  ResourceEntity - zasób (pracownik, sala, książka, zwierzę...)
  RequestEntity  - zgłoszenie/rezerwacja (zmiana, rezerwacja, wypożyczenie...)
```
