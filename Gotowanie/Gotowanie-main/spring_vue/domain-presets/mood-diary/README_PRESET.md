# Dziennik nastroju / emocji — Preset

## Opis

Aplikacja do monitorowania i podsumowywania nastroju. Użytkownik tworzy codzienne wpisy z oceną punktową samopoczucia (1–10) oraz etykietą (GREAT / GOOD / NEUTRAL / BAD / AWFUL). System udostępnia kalendarz z historią nastroju, wykres kołowy rozkładu etykiet oraz średnią tygodniową.

## Model danych

| Element | Opis |
|---|---|
| **Zasób (Resource)** | Dzień — każdy dzień to osobny zasób z polem `entryDate` (TEXT, format RRRR-MM-DD) |
| **Zgłoszenie (Request)** | Wpis nastroju — zawiera ocenę, etykietę, notatki, aktywności i godziny snu |

### Pola zasobu

| Klucz | Typ | Wymagane | Opis |
|---|---|---|---|
| entryDate | TEXT | tak | Data w formacie ISO (np. 2025-06-15) |

### Pola zgłoszenia

| Klucz | Typ | Wymagane | Opis |
|---|---|---|---|
| moodScore | NUMBER (1-10) | tak | Punktowa ocena samopoczucia |
| moodLabel | SELECT | tak | Etykieta: GREAT, GOOD, NEUTRAL, BAD, AWFUL |
| notes | TEXTAREA | nie | Opis myśli / wydarzeń |
| activities | TEXT | nie | Lista aktywności dnia |
| sleepHours | NUMBER | nie | Godziny snu poprzedniej nocy |

## Algorytm

Działa w trybie **FLAT** (PricingUnit.FLAT) z **VALUE_CALCULATION_ONLY** (brak sprawdzania dostępności czasowej / pojemności).

1. **Walidacja moodScore** — wartość musi mieścić się w przedziale 1–10.
2. **Auto-etykieta** — na podstawie score:
   - 9–10 → GREAT
   - 7–8 → GOOD
   - 5–6 → NEUTRAL
   - 3–4 → BAD
   - 1–2 → AWFUL
3. **Kolizja dni** — jeden wpis na dzień (zasób); drugi wpis dla tego samego dnia jest odrzucany.
4. **Średnia tygodniowa** — algorytm agreguje wszystkie wpisy w tym samym tygodniu ISO (na podstawie `entryDate` zasobu) i oblicza średnią arytmetyczną ocen.
5. **Rozkład nastrojów** — zlicza wystąpienia każdej etykiety w bieżącym tygodniu (dane do wykresu kołowego).
6. **Wynik** — `calculatedValue` = `moodScore`, w breakdownie dostępne są: `weeklyAverage`, `moodDistribution`, `moodLabel`.

## Konfiguracja profilu

```java
AlgorithmMode.VALUE_CALCULATION_ONLY
PricingUnit.FLAT
requiresTimeWindow = false
requiresQuantity = false
```

## Zabezpieczenia

- **USER** — widzi tylko własne wpisy.
- **ADMIN / OPERATOR** — widzi wszystkie wpisy (dla terapeuty / przełożonego).

## Seed (dane demo)

Inicjalizator (`DataInitializer`) tworzy:
- 3 użytkowników: Administrator, Terapeuta i Kasia (zwykły użytkownik)
- 7 zasobów-dni (poniedziałek–niedziela bieżącego tygodnia)
- 4 wpisy nastroju z różnymi ocenami (8, 5, 9, 7) i etykietami

Loginy demo:
- `admin@mood.local` / `admin123` (ADMIN)
- `terapeuta@mood.local` / `therapist123` (OPERATOR)
- `kasia@mood.local` / `user123` (USER)
