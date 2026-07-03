# PRESETS_GUIDE

## Czym są presety

Presety to gotowe warianty domenowe projektu. Każdy preset zawiera konfigurację backendu, seed danych, algorytm, teksty frontendu oraz mapowanie na ZPO dla konkretnego tematu.

**Każdy preset zawiera teraz:**
- Szczegółowe `helpText` w formularzach - pokazuje dostępne opcje i ich skutki (ceny, zniżki)
- `TEST_CASES.md` z mapowaniem na punktację ZPO
- `README_PRESET.md` z tabelą wymagań ZPO

Jeżeli prowadzący poda inny temat, zajrzyj do `TOPIC_BLUEPRINTS.md`. Tam jest bank 20 gotowych mapowań z opisem algorytmu, security i testów. Z takiego blueprintu robisz nowy preset przez skopiowanie najbliższego istniejącego katalogu z `domain-presets/`.

Pełna lista presetów jest w `domain-presets/README.md` (obecnie ponad 30 katalogów).
Skrypt działa dla każdego katalogu z `domain-presets/` — wystarczy podać jego nazwę.

## 15 tematów ZPO (od wykładowcy)

```bash
./scripts/apply-preset.sh movie-ratings              # 1. Ocena filmów 1-5
./scripts/apply-preset.sh conference-room-booking    # 2. Rezerwacja salek konferencyjnych
./scripts/apply-preset.sh fitness-classes            # 3. Zapisy na zajęcia fitness
./scripts/apply-preset.sh employee-scheduler         # 4. Zarządzanie pracownikami
./scripts/apply-preset.sh meal-planning-catering     # 5. Planowanie posiłków catering
./scripts/apply-preset.sh animal-shelter             # 6. Schronisko dla zwierząt
./scripts/apply-preset.sh online-course-platform     # 7. Platforma kursów online
./scripts/apply-preset.sh player-ranking             # 8. System rankingowy graczy
./scripts/apply-preset.sh habit-tracker              # 9. Habit tracker
./scripts/apply-preset.sh library-book-rental        # 10. Wypożyczalnia książek
./scripts/apply-preset.sh estate-issue-reporting     # 11. Zgłoszenia administracji
./scripts/apply-preset.sh workout-tracker            # 12. Rejestr treningów
./scripts/apply-preset.sh exam-study-planner         # 13. Plan nauki na egzamin
./scripts/apply-preset.sh office-plant-care          # 14. Adopcja roślin biurowych
./scripts/apply-preset.sh mood-diary                 # 15. Dziennik nastroju
```

## Dlaczego główny kod nie ma nazw domenowych

Główny projekt jest generyczny. Nazwy klas i endpointów zostają neutralne:

- `Resource`
- `Request`
- `DomainAlgorithm`
- `/api/resources`
- `/api/requests`

Dzięki temu nie trzeba tworzyć osobnych kontrolerów, encji ani komponentów dla każdego tematu. Temat zmienia się przez metadane, profil domeny i konfigurację UI.

## Jak zastosować preset

Z katalogu głównego projektu:

```bash
./scripts/apply-preset.sh car-rental
```

Przykłady:

```bash
./scripts/apply-preset.sh habit-tracker
./scripts/apply-preset.sh exam-study-planner
./scripts/apply-preset.sh office-plant-care
./scripts/apply-preset.sh employee-scheduler
./scripts/apply-preset.sh meal-planning-catering
./scripts/apply-preset.sh car-rental
```

Skrypt robi kopię aktualnych plików w `.preset-backups/<timestamp>/`, a potem nadpisuje cztery pliki projektu.

## Jak wrócić do innego presetu

Wystarczy uruchomić skrypt ponownie z inną nazwą:

```bash
./scripts/apply-preset.sh parcel-locker
```

Jeżeli baza ma dane z poprzedniego tematu, najlepiej wyczyścić bazę albo użyć nowej. Seed demo działa tylko wtedy, gdy tabela użytkowników jest pusta.

## Które pliki są kopiowane

Skrypt kopiuje:

```text
domain-presets/<preset>/backend/DomainProfileProvider.java
  -> backend/src/main/java/pl/zpo/app/domain/config/DomainProfileProvider.java

domain-presets/<preset>/backend/DataInitializer.java
  -> backend/src/main/java/pl/zpo/app/config/DataInitializer.java

domain-presets/<preset>/backend/DefaultDomainAlgorithm.java
  -> backend/src/main/java/pl/zpo/app/domain/algorithm/DefaultDomainAlgorithm.java

domain-presets/<preset>/frontend/domain.config.ts
  -> frontend/src/config/domain.config.ts
```

## Jak uruchomić projekt po zmianie presetu

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

Adresy:

- Frontend: <http://localhost:5173>
- Swagger: <http://localhost:8080/swagger-ui/index.html>

## Jak sprawdzić, czy preset działa

1. Wejdź na frontend.
2. Sprawdź, czy etykiety zasobów i requestów odpowiadają presetowi.
3. Zaloguj się jako `user@zpo.local` / `user123`.
4. Utwórz request.
5. Sprawdź, czy backend zwraca `calculatedValue` i `AlgorithmBreakdown`.
6. Zaloguj się jako `admin@zpo.local` / `admin123`.
7. Sprawdź panel admina i listę wszystkich requestów.

Komendy kontrolne:

```bash
cd backend && ./mvnw test
cd frontend && npm run build
```
