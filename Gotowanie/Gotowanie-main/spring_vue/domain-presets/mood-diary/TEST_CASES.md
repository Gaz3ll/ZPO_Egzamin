# Test Cases — Dziennik nastroju

## TC-01: Utworzenie poprawnego wpisu nastroju

**Kroki:**
1. Zaloguj się jako `kasia@mood.local`
2. Wybierz dzień (np. poniedziałek bieżącego tygodnia)
3. Wprowadź `moodScore = 8`
4. Wybierz `moodLabel = GOOD`
5. Wpisz notatki: "Produktywny dzień w pracy"
6. Kliknij "Dodaj wpis nastroju"

**Oczekiwany rezultat:**
- Wpis utworzony pomyślnie (status PENDING lub COMPLETED)
- `calculatedValue = 8`
- Breakdown zawiera: `MOOD_SCORE_CHECK: ok (8/10)`, `MOOD_LABEL: GOOD`
- `DAY_COLLISION_CHECK: brak kolizji`

---

## TC-02: Walidacja moodScore poza zakresem

**Kroki:**
1. Wybierz dzień
2. Wprowadź `moodScore = 0` (lub 11, lub -5)

**Oczekiwany rezultat:**
- Błąd walidacji: "Ocena nastroju (moodScore) musi być w zakresie 1-10"
- `MOOD_SCORE_CHECK: nieprawidłowa wartość`
- Wynik `success = false`

---

## TC-03: Auto-etykieta dla skrajnych wartości

**Kroki:**
1. Utwórz wpis z `moodScore = 10`

**Oczekiwany rezultat:**
- Breakdown notka: `moodLabel=GREAT`
- Reguła: `MOOD_LABEL: GREAT (score=10)`

**Kroki:**
2. Utwórz wpis z `moodScore = 1`

**Oczekiwany rezultat:**
- Breakdown notka: `moodLabel=AWFUL`
- Reguła: `MOOD_LABEL: AWFUL (score=1)`

**Mapowanie:**
| Score | Etykieta |
|---|---|
| 9–10 | GREAT |
| 7–8 | GOOD |
| 5–6 | NEUTRAL |
| 3–4 | BAD |
| 1–2 | AWFUL |

---

## TC-04: Kolizja — drugi wpis tego samego dnia

**Kroki:**
1. Utwórz wpis dla poniedziałku
2. Ponownie wybierz ten sam poniedziałek i spróbuj utworzyć kolejny wpis

**Oczekiwany rezultat:**
- Błąd: "Wpis nastroju dla dnia ... już istnieje"
- `DAY_COLLISION_CHECK: kolizja`
- Wynik `success = false`

---

## TC-05: Brak wymaganego moodScore

**Kroki:**
1. Wybierz dzień
2. Nie podawaj `moodScore` (pozostaw puste)

**Oczekiwany rezultat:**
- Błąd walidacji: "Ocena nastroju (moodScore) musi być w zakresie 1-10"
- `MOOD_SCORE_CHECK: nieprawidłowa wartość (null)`

---

## TC-06: Wpis z polami opcjonalnymi

**Kroki:**
1. Utwórz wpis z `moodScore = 7`, `moodLabel = GOOD`
2. Wypełnij wszystkie pola opcjonalne:
   - notes: "Relaksujący weekend"
   - activities: "spacer, kino, gotowanie"
   - sleepHours: 8.5

**Oczekiwany rezultat:**
- Wpis utworzony pomyślnie
- Wszystkie pola opcjonalne zapisane w metadata
- Algorytm ignoruje pola opcjonalne (tylko przechowuje)

---

## TC-07: Średnia tygodniowa

**Kroki:**
1. Utwórz wpisy w różnych dniach tego samego tygodnia:
   - Poniedziałek: score 8 (GOOD)
   - Środa: score 5 (NEUTRAL)
   - Piątek: score 9 (GREAT)
2. Utwórz kolejny wpis (np. w sobotę)

**Oczekiwany rezultat:**
- Breakdown nowego wpisu zawiera: `weeklyAverage`
- Średnia = (8 + 5 + 9 + nowy) / liczba wpisów
- `moodDistribution` zawiera zliczenia etykiet: GOOD=1, NEUTRAL=1, GREAT=1

---

## TC-08: Brak innych wpisów w tygodniu

**Kroki:**
1. Jako nowy użytkownik utwórz pierwszy wpis

**Oczekiwany rezultat:**
- Reguła: `WEEKLY_AVERAGE: tylko bieżący wpis (brak innych w tym tygodniu)`
- `weeklyAverage = moodScore` (wartość bieżącego wpisu)

---

## TC-09: Nieaktywny dzień (zasób)

**Kroki:**
1. Ustaw status dnia na INACTIVE lub UNAVAILABLE
2. Spróbuj utworzyć wpis dla tego dnia

**Oczekiwany rezultat:**
- Błąd: "Dzień nie jest aktywny"
- `RESOURCE_CHECK: dzień nieaktywny`

---

## TC-10: Zabezpieczenia — USER widzi tylko swoje wpisy

**Kroki:**
1. Zaloguj się jako `kasia@mood.local`
2. Wyświetl listę "Moje wpisy"

**Oczekiwany rezultat:**
- Widoczne tylko wpisy użytkownika Kasia
- Brak wpisów innych użytkowników

**Kroki:**
3. Zaloguj się jako `admin@mood.local`

**Oczekiwany rezultat:**
- Widoczne wszystkie wpisy wszystkich użytkowników

---

## TC-11: Wpis bez notatek / aktywności / snu

**Kroki:**
1. Utwórz wpis z samym `moodScore` i `moodLabel` (pola opcjonalne puste)

**Oczekiwany rezultat:**
- Wpis utworzony pomyślnie
- `notes`, `activities`, `sleepHours` = null w metadata

---

## TC-12: Rozkład nastrojów (moodDistribution)

**Kroki:**
1. Utwórz wpisy z różnymi etykietami w tym samym tygodniu

**Oczekiwany rezultat:**
- Breakdown notka: `moodDistribution={GREAT=2, GOOD=1, NEUTRAL=1}` (przykład)
- Format mapy JSON z kluczami = etykiety, wartościami = liczba wpisów
