# FINAL_CHECKLIST

Krótka checklista przed oddaniem projektu albo pokazem prowadzącemu.

## Komendy kontrolne

- Backend testy: `cd backend && ./mvnw test`
- Frontend build: `cd frontend && npm run build`
- Docker Postgres: `docker compose up -d`
- Backend start: `cd backend && ./mvnw spring-boot:run`
- Frontend start: `cd frontend && npm run dev`

## Adresy

- Swagger: <http://localhost:8080/swagger-ui/index.html>
- Frontend: <http://localhost:5173>

## Konta demo

- Login USER: `user@zpo.local` / `user123`
- Login ADMIN: `admin@zpo.local` / `admin123`
- Login OPERATOR: `operator@zpo.local` / `operator123`

## Rzeczy do pokazania

- USER nie ma dostępu do admin endpointów.
- ADMIN widzi wszystkie requesty.
- Algorytm zwraca breakdown.
- Presety można zastosować przez `scripts/apply-preset.sh`.

## Presety

```bash
./scripts/apply-preset.sh car-rental
./scripts/apply-preset.sh parcel-locker
./scripts/apply-preset.sh cinema-booking
./scripts/apply-preset.sh vet-appointments
./scripts/apply-preset.sh zoo-administration
./scripts/apply-preset.sh mechanic-workshop
```
