# Preset: habit-tracker

Temat: **Habit tracker** - śledzenie codziennych nawyków z widokiem tygodniowym/miesięcznym i wykresem.

## Specyfikacja wykładowcy

Apka umożliwiająca tworzenie i codzienne śledzenie postępów w realizacji nawyków. Dodatkowo widok tygodniowy albo miesięczny oraz wykres z podsumowaniem postępów.

| Pojęcie generyczne | Znaczenie domenowe |
|--------------------|--------------------|
| `Resource` | Nawyki / cele (nazwa, kategoria, częstotliwość) |
| `Request` | Codzienny wpis postępu (wykonane TAK/NIE) |
| `calculatedValue` | Procent realizacji / streak dni |

## Algorytm

1. Sprawdza czy nawyk wykonany danego dnia
2. Liczy streak (ciągłość dni)
3. Oblicza procent realizacji w okresie (tydzień/miesiąc)
4. `appliedRules`: DAILY_CHECK, STREAK_COUNT, COMPLETION_PERCENT

## Role

- **USER**: tworzy nawyki, robi codzienne wpisy
- **ADMIN**: widzi statystyki wszystkich

```bash
./scripts/apply-preset.sh habit-tracker
```
