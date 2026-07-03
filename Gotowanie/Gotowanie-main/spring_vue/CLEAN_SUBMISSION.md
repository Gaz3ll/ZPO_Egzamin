# CLEAN_SUBMISSION

Co wrzucić do ZIP/repozytorium przed oddaniem.

## Dołączyć

- `backend/`
- `frontend/`
- `domain-presets/`
- `scripts/`
- `docker-compose.yml`
- `README.md`
- `ZALICZENIE.md`
- `QUICK_START.md`
- `DEMO_SCRIPT.md`
- `DEFENSE_POINTS.md`
- `PRESETS_GUIDE.md`
- `FINAL_CHECKLIST.md`
- `PROJECT_OVERVIEW.md`
- `TOPIC_BLUEPRINTS.md`
- `frontend/.env.example`

## Nie dołączać

- `node_modules/`
- `target/`
- `dist/`
- `.idea/`
- `.vscode/` opcjonalnie
- `.env` z prawdziwymi sekretami
- wolumenów PostgreSQL i danych Dockera
- plików logów
- `.DS_Store`

## Przed spakowaniem

Uruchom:

```bash
cd backend && ./mvnw test
cd frontend && npm run build
```

Sprawdź, czy ZIP nie zawiera:

```text
frontend/node_modules/
frontend/dist/
backend/target/
.idea/
.env
```
