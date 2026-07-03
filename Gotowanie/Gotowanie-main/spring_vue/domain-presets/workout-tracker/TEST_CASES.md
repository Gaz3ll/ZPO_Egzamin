# Test cases – Tracker treningów

## 1. Sukces – pełne dane treningu siłowego

**Request metadata:**
```json
{ "sets": 4, "reps": 10, "weight": 60, "durationMinutes": 45, "workoutDate": "2026-07-02", "notes": "Dobry trening" }
```

**Oczekiwany wynik:**
- success = true
- calculatedValue = 2400 (4 × 10 × 60)
- appliedRules zawiera: TOTAL_VOLUME: 2400 kg, TOTAL_DURATION: 45 min

---

## 2. Sukces – trening z masą ciała (weight = 0)

**Request metadata:**
```json
{ "sets": 3, "reps": 12, "weight": 0, "durationMinutes": 30, "workoutDate": "2026-07-02" }
```

**Oczekiwany wynik:**
- success = true
- calculatedValue = 0 (3 × 12 × 0)
- appliedRules zawiera: TOTAL_VOLUME: 0 kg

---

## 3. Sukces – mała objętość

**Request metadata:**
```json
{ "sets": 2, "reps": 5, "weight": 20, "durationMinutes": 15, "workoutDate": "2026-07-02" }
```

**Oczekiwany wynik:**
- success = true
- calculatedValue = 200 (2 × 5 × 20)

---

## 4. Błąd – brak liczby serii

**Request metadata:**
```json
{ "reps": 10, "weight": 60, "durationMinutes": 45, "workoutDate": "2026-07-02" }
```

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Liczba serii musi być liczbą dodatnią"

---

## 5. Błąd – brak liczby powtórzeń

**Request metadata:**
```json
{ "sets": 4, "weight": 60, "durationMinutes": 45, "workoutDate": "2026-07-02" }
```

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Liczba powtórzeń musi być liczbą dodatnią"

---

## 6. Błąd – brak ciężaru

**Request metadata:**
```json
{ "sets": 4, "reps": 10, "durationMinutes": 45, "workoutDate": "2026-07-02" }
```

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Ciężar musi być liczbą nieujemną"

---

## 7. Błąd – ujemny ciężar

**Request metadata:**
```json
{ "sets": 4, "reps": 10, "weight": -5, "durationMinutes": 45, "workoutDate": "2026-07-02" }
```

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Ciężar musi być liczbą nieujemną"

---

## 8. Błąd – brak czasu trwania

**Request metadata:**
```json
{ "sets": 4, "reps": 10, "weight": 60, "workoutDate": "2026-07-02" }
```

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Czas trwania musi być liczbą dodatnią"

---

## 9. Błąd – brak daty treningu

**Request metadata:**
```json
{ "sets": 4, "reps": 10, "weight": 60, "durationMinutes": 45 }
```

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Data treningu jest wymagana"

---

## 10. Błąd – sets = 0

**Request metadata:**
```json
{ "sets": 0, "reps": 10, "weight": 60, "durationMinutes": 45, "workoutDate": "2026-07-02" }
```

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Liczba serii musi być liczbą dodatnią"

---

## 11. Błąd – reps = 0

**Request metadata:**
```json
{ "sets": 4, "reps": 0, "weight": 60, "durationMinutes": 45, "workoutDate": "2026-07-02" }
```

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Liczba powtórzeń musi być liczbą dodatnią"

---

## 12. Błąd – resource jest null

**Request metadata:** dowolne poprawne

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Ćwiczenie jest wymagane"

---

## 13. Błąd – resource nieaktywny

**Request metadata:** dowolne poprawne, resource.status = INACTIVE

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Ćwiczenie nie jest aktywne"

---

## 14. Bezpieczeństwo – USER widzi tylko swoje treningi

- Krzysztof Nowak (USER) loguje się i widzi listę requestów
- Powinien zobaczyć tylko swoje treningi (3 seedowane)
- Nie powinien widzieć treningów innych użytkowników ani panelu admina

---

## 15. Bezpieczeństwo – ADMIN widzi wszystkie treningi

- Admin loguje się i wchodzi do panelu administratora
- Powinien zobaczyć listę wszystkich treningów wszystkich użytkowników
- Powinien móc tworzyć/edytować/usuwać ćwiczenia

---

## 16. Bezpieczeństwo – TRENER (OPERATOR) zarządza ćwiczeniami

- Trener loguje się
- Powinien móc dodawać/edytować/usuwać ćwiczenia w bazie
- Nie powinien mieć dostępu do treningów użytkowników

---

## 17. Historia treningów – wiele sesji tego samego ćwiczenia

- Użytkownik ma 3 seedowane treningi (Wyciskanie, Przysiad, Podciąganie)
- W widoku "Moje treningi" powinny być widoczne wszystkie 3
- Każdy trening powinien pokazywać: nazwę ćwiczenia, serie, powtórzenia, ciężar, czas, datę i całkowitą objętość

---

## 18. Podsumowanie tygodniowe – sumowanie czasu

- Dla istniejących treningów w bieżącym tygodniu algorytm sumuje `durationMinutes`
- W breakdownie pojawia się `WEEKLY_TOTAL` z łącznym czasem w minutach
