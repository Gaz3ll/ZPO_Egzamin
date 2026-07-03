# Test cases: fitness-classes

### Testy algorytmu

1. **Wolne miejsce** - zapis na zajęcia z wolnym miejscem → sukces
2. **Brak miejsc** - zapis na pełne zajęcia → waitlist
3. **Promocja z listy** - anulowanie → pierwszy z listy dostaje miejsce
4. **Limit miejsc** - capacity check poprawnie odrzuca

### Testy security

5. **USER**: widzi tylko swoje zapisy
6. **ADMIN**: widzi wszystkie zapisy i listę rezerwową

```bash
cd backend && ./mvnw test
```
