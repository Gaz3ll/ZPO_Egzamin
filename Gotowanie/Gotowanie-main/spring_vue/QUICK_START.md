# QUICK_START

## Krok 1

```bash
docker compose up -d
```

## Krok 2

```bash
cd backend
./mvnw spring-boot:run
```

## Krok 3

```bash
cd frontend
npm install
npm run dev
```

## Krok 4

Otwórz:

- <http://localhost:5173>
- <http://localhost:8080/swagger-ui/index.html>

## Krok 5

Zaloguj się:

- `admin@zpo.local` / `admin123`
- `user@zpo.local` / `user123`
- `operator@zpo.local` / `operator123`

## Zmiana tematu (15 tematów ZPO)

```bash
# Wybierz jeden z 15 tematów:
./scripts/apply-preset.sh movie-ratings              # Ocena filmów 1-5
./scripts/apply-preset.sh conference-room-booking    # Rezerwacja salek konferencyjnych
./scripts/apply-preset.sh fitness-classes            # Zapisy na zajęcia fitness
./scripts/apply-preset.sh employee-scheduler         # Zarządzanie pracownikami
./scripts/apply-preset.sh meal-planning-catering     # Planowanie posiłków
./scripts/apply-preset.sh animal-shelter             # Schronisko dla zwierząt
./scripts/apply-preset.sh online-course-platform     # Platforma kursów online
./scripts/apply-preset.sh player-ranking             # System rankingowy graczy
./scripts/apply-preset.sh habit-tracker              # Habit tracker
./scripts/apply-preset.sh library-book-rental        # Wypożyczalnia książek
./scripts/apply-preset.sh estate-issue-reporting     # Zgłoszenia administracji
./scripts/apply-preset.sh workout-tracker            # Rejestr treningów
./scripts/apply-preset.sh exam-study-planner         # Plan nauki na egzamin
./scripts/apply-preset.sh office-plant-care          # Adopcja roślin biurowych
./scripts/apply-preset.sh mood-diary                 # Dziennik nastroju
```

Po zmianie presetu:

```bash
docker compose down -v   # wyczyść starą bazę
docker compose up -d     # uruchom nową
cd backend && ./mvnw spring-boot:run   # reseed danych
```
