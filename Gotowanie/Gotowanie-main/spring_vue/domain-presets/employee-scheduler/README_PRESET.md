# Preset: employee-scheduler

Temat: **Apka do zarządzania pracownikami** - grafiki pracowników, godziny pracy, zadania.

## Specyfikacja wykładowcy

Apka do tworzenia grafików pracowników (godziny pracy, zadania itp.). Możliwość dodania tygodniowego terminarza działalności pracowników. Możliwość edytowania wpisów.

| Pojęcie generyczne | Znaczenie domenowe |
|--------------------|--------------------|
| `Resource` | Pracownik / zmiana |
| `Request` | Przypisanie zadania / grafiku |
| `calculatedValue` | Suma godzin w tygodniu |

## Algorytm

1. Sprawdza kolizje czasowe dla pracownika
2. Sumuje godziny pracy w tygodniu
3. Sprawdza limit nadgodzin (>40h → ostrzeżenie)
4. `appliedRules`: TIME_COLLISION, WEEKLY_HOURS, OVERTIME_WARNING

## Role

- **USER (pracownik)**: widzi swój grafik i zadania
- **ADMIN (manager)**: tworzy grafiki, widzi wszystkich

```bash
./scripts/apply-preset.sh employee-scheduler
```
