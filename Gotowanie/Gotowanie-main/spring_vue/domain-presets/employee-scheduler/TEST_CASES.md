# Test cases: employee-scheduler

### Testy algorytmu

1. **Poprawny grafik** - przypisanie bez kolizji → sukces
2. **Kolizja czasu** - dwóch pracowników na tę samą godzinę → błąd
3. **Nadgodziny** - >40h/tydzień → ostrzeżenie w breakdown
4. **Edycja wpisu** - zmiana godzin po zapisie

### Testy security

5. **USER**: widzi tylko swój grafik
6. **ADMIN**: widzi wszystkich pracowników

```bash
cd backend && ./mvnw test
```
