# Preset: workout-tracker

Aplikacja do rejestrowania treningów siłowych dla sportowca. Umożliwia dodawanie ćwiczeń z bazy oraz zapisywanie sesji treningowych z czasem trwania, ilością serii i ciężarem.

| Pojęcie generyczne | Znaczenie domenowe |
|--------------------|--------------------|
| `Resource`         | Ćwiczenie („Wyciskanie sztangi", „Przysiad", „Martwy ciąg") |
| `Request`          | Sesja treningowa / wpis treningowy |
| `calculatedValue`  | Całkowita objętość treningowa w kg (totalVolume) |

## Model

- **AlgorithmMode:** `VALUE_CALCULATION_ONLY` (brak kolizji czasowych i pojemności)
- **PricingUnit:** `FLAT`
- `requiresTimeWindow = false`, `requiresQuantity = false`

### Resource `metadata_json`
`exerciseName` (nazwa ćwiczenia), `muscleGroup` (CHEST/BACK/LEGS/SHOULDERS/ARMS/CORE/CARDIO), `difficulty` (BEGINNER/INTERMEDIATE/ADVANCED). Ćwiczenie nie ma ceny bazowej (`baseValue = null`).

### Request `metadata_json`
`sets` (liczna serii), `reps` (liczba powtórzeń), `weight` (ciężar w kg), `durationMinutes` (czas trwania w minutach), `workoutDate` (data treningu), `notes` (opcjonalne notatki). `startAt` = data/czas rozpoczęcia treningu.

## Algorytm

1. `RESOURCE_CHECK` – ćwiczenie musi istnieć i być `ACTIVE`.
2. `SETS_CHECK` – liczba serii > 0.
3. `REPS_CHECK` – liczba powtórzeń > 0.
4. `WEIGHT_CHECK` – ciężar >= 0.
5. `DURATION_CHECK` – czas trwania > 0.
6. `DATE_CHECK` – data treningu wymagana.
7. `TOTAL_VOLUME` – `sets × reps × weight` → `calculatedValue`.
8. `TOTAL_DURATION` – czas trwania sesji w minutach.
9. `WEEKLY_TOTAL` – sumaryczny czas treningów w tygodniu (liczony z istniejących wpisów dla tego ćwiczenia).

`appliedRules`: `RESOURCE_CHECK`, `SETS_CHECK`, `REPS_CHECK`, `WEIGHT_CHECK`, `DURATION_CHECK`, `DATE_CHECK`, `TOTAL_VOLUME`, `TOTAL_DURATION`, `WEEKLY_TOTAL`.
Breakdown zawiera: `totalVolume` i `totalDuration` oraz notatki z wartościami poszczególnych pól i łącznym czasem tygodniowym.

## Role

- **USER:** dodaje własne treningi, widzi tylko swoje wpisy i podsumowanie tygodniowe.
- **OPERATOR (trener):** zarządza katalogiem ćwiczeń.
- **ADMIN:** pełny wgląd we wszystkie treningi i ćwiczenia wszystkich użytkowników.

## Jak wytłumaczyć prowadzącemu mapowanie Resource/Request

Projekt ma generyczny rdzeń, w którym `Resource` to dowolny obiekt, a `Request` to zgłoszenie z metadanymi w JSONB. W tym presecie `Resource` jest ćwiczeniem — jego nazwa, grupa mięśniowa i poziom trudności siedzą w `name`, `type` i `metadata_json`. `Request` jest pojedynczą sesją treningową: liczba serii, powtórzeń, ciężar i czas trwania są przechowywane w `metadata_json`, a `startAt` rejestruje datę treningu. Wymienny `DomainAlgorithm` wylicza `calculatedValue` jako całkowitą objętość treningową (sets × reps × weight). Frontend nie ma komponentu „WorkoutX" — wszystkie etykiety, formularze i podsumowania pochodzą z `domain.config.ts`, a pola formularza renderuje `DynamicFieldRenderer`. Zmiana tematu na inny sprowadza się do podmiany czterech plików konfiguracyjnych.

## Jak zastosować

```sh
./scripts/apply-preset.sh workout-tracker
cd backend && ./mvnw test
cd frontend && npm run build
```

Logowania demo: `admin@trening.local/admin123`, `trener@trening.local/trener123`, `user@trening.local/user123`.

## Co pokazać prowadzącemu

- Listę ćwiczeń z podziałem na grupy mięśniowe i poziom trudności.
- Dodanie sesji treningowej dla wybranego ćwiczenia z parametrami (serie, powtórzenia, ciężar).
- Obliczoną objętość treningową w breakdownie algorytmu.
- Podsumowanie tygodniowego czasu treningów.
- Panel administratora ze wszystkimi treningami użytkowników.
