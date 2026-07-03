# Preset: office-plant-care

Temat: **Rejestr adopcji roślin w biurze (opieka nad roślinkami)** - terminarz podlewania, przesadzania, nawożenia.

## Specyfikacja wykładowcy

Aplikacja która może pomóc pracownikom pamiętać o swoich zaadoptowanych roślinkach. W apce ma być rejestr roślin z opcją do adopcji. Użytkownicy widzą swoje zaadoptowane rośliny i mogą mieć wyświetlany terminarz ich podlewania, przesadzania i nawożenia (w zależności od gatunku roślinki).

| Pojęcie generyczne | Znaczenie domenowe |
|--------------------|--------------------|
| `Resource` | Roślina (gatunek, częstotliwość podlewania, nawożenia, przesadzania) |
| `Request` | Adopcja / zadanie pielęgnacyjne |
| `calculatedValue` | Następny termin pielęgnacji |

## Algorytm

1. Oblicza następny termin podlewania (ostatnie podlewanie + interwał)
2. Sprawdza czy roślina wymaga nawożenia / przesadzania
3. Generuje terminarz pielęgnacji
4. `appliedRules`: WATERING_SCHEDULE, FERTILIZER_CHECK, REPOTTING_DUE

## Role

- **USER (pracownik)**: adoptuje roślinę, widzi terminarz
- **ADMIN**: zarządza rejestrem roślin

```bash
./scripts/apply-preset.sh office-plant-care
```
