# Preset: exam-study-planner

Program do generowania planu nauki na egzamin z kalendarzem egzaminów. Użytkownik dodaje egzamin
z datą i zakresem materiału, a aplikacja rozdziela materiał na dni pozostałe do egzaminu.

| Pojęcie generyczne | Znaczenie domenowe |
|--------------------|--------------------|
| `Resource`         | Egzamin (data, przedmiot, zakres materiału) |
| `Request`          | Wygenerowany plan nauki / sesja nauki |
| `calculatedValue`  | Szacowany łączny czas nauki (minuty) |

## Model

- **AlgorithmMode:** `VALUE_CALCULATION_ONLY`
- **PricingUnit:** `FLAT`
- `requiresTimeWindow = false`, `requiresQuantity = false`

### Resource `metadata_json`
`examDate`, `subject`, `difficulty` (EASY/MEDIUM/HARD), `materialCount`, `materialUnit`
(TOPICS/CHAPTERS/PAGES), `topics`, `dailyStudyLimitMinutes`.
`baseValue` = szacowana trudność egzaminu jako mnożnik (1.0 łatwy … 2.0 bardzo trudny),
`capacityValue` = dostępne bloki nauki dziennie.

### Request `metadata_json`
`selectedTopics`, `studyMinutes` (dostępny czas dziennie), `priority`, `isRevision`,
`completed`, `notes`.

## Algorytm

1. `RESOURCE_CHECK` – egzamin musi być `ACTIVE`.
2. `DAYS_UNTIL_EXAM` – liczba dni do `examDate`; egzamin w przeszłości / dzisiaj → błąd.
3. `REVISION_DAYS` – ≥7 dni → 2 dni powtórek, ≥3 dni → 1 dzień, mniej → 0.
4. `DAILY_MATERIAL` – `ceil(materialCount / dniNauki)` (dni nauki = dni do egzaminu − powtórki).
5. `DIFFICULTY_MULTIPLIER` – `baseValue` jako mnożnik (fallback: `difficulty`).
6. `ESTIMATED_DAILY_MINUTES` – materiał dzienny × minuty/jednostkę (TOPICS 45, CHAPTERS 60,
   PAGES 5) × trudność.
7. `OVERLOAD_CHECK` – dzienny szacunek > `studyMinutes`/`dailyStudyLimitMinutes` → ostrzeżenie
   o przeciążeniu w breakdownie.
8. Suma: `studyMinutes` (dni nauki × dzienny czas) + `revisionMinutes` (dni powtórek × 50%).

`appliedRules`: `RESOURCE_CHECK`, `DAYS_UNTIL_EXAM`, `MATERIAL_COUNT`, `REVISION_DAYS`,
`DAILY_MATERIAL`, `DIFFICULTY_MULTIPLIER`, `ESTIMATED_DAILY_MINUTES`, `OVERLOAD_CHECK`,
`TOTAL_STUDY_MINUTES`. Notatki: `daysUntilExam`, `materialCount`, `dailyMaterial`,
`difficultyMultiplier`, `revisionDays`, `estimatedDailyMinutes`, `overloadWarning`,
`totalStudyMinutes`.

## Role

- **USER (student):** generuje i przegląda swoje plany nauki.
- **OPERATOR (opiekun roku):** zarządza kalendarzem egzaminów.
- **ADMIN:** pełny wgląd we wszystkie plany.

## Jak wytłumaczyć prowadzącemu mapowanie Resource/Request

Rdzeń projektu jest generyczny: `Resource` to obiekt, którego dotyczą zgłoszenia użytkowników,
a `Request` to zgłoszenie z dynamicznymi metadanymi w kolumnie JSONB. W tym presecie `Resource`
jest egzaminem — data, przedmiot, trudność i ilość materiału trafiają do `metadata_json`, a
`baseValue` przechowuje szacowaną trudność jako mnożnik czasu nauki. `Request` jest wygenerowanym
planem nauki: algorytm dzieli materiał na dni pozostałe do egzaminu, rezerwuje dni powtórkowe i
zwraca `calculatedValue` jako łączny szacowany czas nauki w minutach. Wymienny `DomainAlgorithm`
zgłasza błąd dla egzaminu w przeszłości i ostrzega w breakdownie, gdy dzienny czas przekracza
limit. Frontend nie ma żadnego komponentu „ExamX" — kalendarz egzaminów, formularz planu i
rozkład materiału to te same generyczne komponenty zasilane innym `domain.config.ts`. Zmiana
tematu wymaga podmiany tylko czterech plików konfiguracyjnych, bez dotykania encji, kontrolerów
ani komponentów Vue.

## Jak zastosować

```sh
./scripts/apply-preset.sh exam-study-planner
cd backend && ./mvnw test
cd frontend && npm run build
```

Logowania demo: `admin@zpo.local/admin123`, `operator@zpo.local/operator123`, `user@zpo.local/user123`.

## Co pokazać prowadzącemu

- Kalendarz egzaminów (5 przedmiotów z datami i zakresem materiału).
- Wygenerowanie planu nauki i breakdown: dni do egzaminu, materiał dziennie, dni powtórek.
- Ostrzeżenie o przeciążeniu przy małej liczbie dni i dużym materiale.
- Błąd dla egzaminu z datą w przeszłości.
- Panel administratora ze wszystkimi planami.
