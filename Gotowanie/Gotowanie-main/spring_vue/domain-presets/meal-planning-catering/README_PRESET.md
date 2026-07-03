# Preset: meal-planning-catering

Temat: **Apka do planowania posiłków dla cateringu dietetycznego** - wybór diety, kaloryczności i generowanie planu tygodniowego.

## Specyfikacja wykładowcy

Apka do tworzenia diety dla klienta. Pozwala klientowi wybrać rodzaj diety, jedną z dostępnych porcji kalorycznych po czym generuje plan tygodniowy z listy posiłków (obiady, kolacje i śniadania). Każdy posiłek ma mieć swoją kaloryczność (z możliwością jej podwojenia albo zmniejszenia o pół) oraz rodzaj posiłku.

| Pojęcie generyczne | Znaczenie domenowe |
|--------------------|--------------------|
| `Resource` | Posiłek (nazwa, kalorie, typ: śniadanie/obiad/kolacja, dieta) |
| `Request` | Plan tygodniowy klienta |
| `calculatedValue` | Łączna kaloryczność / koszt |

## Algorytm

1. Filtruje posiłki po typie diety i alergenach
2. Składa dzienny zestaw do celu kalorycznego (śniadanie + obiad + kolacja)
3. Pozwala skalować porcję (x0.5, x1.0, x2.0)
4. Sumuje kalorie przez redukcję
5. `appliedRules`: DIET_FILTER, ALLERGEN_CHECK, CALORIE_TARGET, PORTION_SCALE

## Role

- **USER (klient)**: wybiera dietę, widzi plan
- **ADMIN (dietetyk)**: zarządza menu i posiłkami

```bash
./scripts/apply-preset.sh meal-planning-catering
```
