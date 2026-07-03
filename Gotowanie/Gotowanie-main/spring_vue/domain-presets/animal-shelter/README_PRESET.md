# Preset: animal-shelter

Temat: **Apka schroniska dla zwierząt** - przeglądanie listy zwierzaków, status adopcji, formularz zgłoszeniowy.

## Specyfikacja wykładowcy

Apka dla schroniska, która umożliwia przeglądanie listy zwierzaków, pokazywać status adopcji danego pupila i umożliwić wysłanie formularza zgłoszeniowego o chęć adopcji.

| Pojęcie generyczne | Znaczenie domenowe |
|--------------------|--------------------|
| `Resource` | Zwierzę do adopcji (imię, gatunek, wiek, wielkość, status) |
| `Request` | Wniosek adopcyjny |
| `calculatedValue` | Koszt adopcji / opłata |

## Algorytm

1. Filtruje zwierzęta po preferencjach (gatunek, wielkość, dzieci)
2. Odrzuca ryzykowne dopasowania
3. Jeśli brak dopasowań - sugestia kontaktu z behawiorystą
4. `appliedRules`: SIZE_CHECK, CHILD_FRIENDLY, EXPERIENCE_MATCH

## Role

- **USER**: przegląda zwierzęta, wysyła wnioski
- **ADMIN (wolontariusz)**: zarządza zwierzętami, widzi wnioski

```bash
./scripts/apply-preset.sh animal-shelter
```
