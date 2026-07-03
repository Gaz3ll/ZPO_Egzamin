# Preset: fitness-classes

Temat: **Zapisy na zajęcia fitness** - lista zajęć z ograniczoną ilością miejsc oraz listą rezerwową.

## Specyfikacja wykładowcy

Apka z listą zajęć, które mogą mieć ograniczoną ilość miejsc oraz listę rezerwową (osób, które wskoczą na miejsce osób, które zrezygnują).

| Pojęcie generyczne | Znaczenie domenowe |
|--------------------|--------------------|
| `Resource` | Zajęcia fitness (nazwa, trener, sala, poziom, max uczestników) |
| `Request` | Zapis uczestnika |
| `calculatedValue` | Cena zapisu w PLN |

## Algorytm

1. Sprawdza dostępność miejsc (capacity check)
2. Jeśli miejsca są - zapisuje uczestnika
3. Jeśli brak miejsc - dodaje na listę rezerwową (waitlist)
4. Przy rezygnacji - promuje pierwszą osobę z listy rezerwowej
5. `appliedRules`: CAPACITY_CHECK, WAITLIST, PROMOTED

## Role

- **USER**: zapisuje się na zajęcia, widzi swoje zapisy
- **ADMIN/OPERATOR**: zarządza zajęciami, widzi listy obecności

```bash
./scripts/apply-preset.sh fitness-classes
```
