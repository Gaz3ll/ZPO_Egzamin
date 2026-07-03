# Test cases – System rankingowy graczy

## 1. Sukces – pełne dane, gracz na 1. miejscu

**Request metadata:**
```json
{ "playerName": "Alice", "score": 95, "rank": 1, "opponentName": "Bob" }
```

**Oczekiwany wynik:**
- success = true
- calculatedValue = 195 (95 + 100)
- appliedRules zawiera: RANK_BONUS: 100, TOTAL_POINTS: 195

---

## 2. Sukces – gracz na 2. miejscu

**Request metadata:**
```json
{ "playerName": "Bob", "score": 72, "rank": 2, "opponentName": "Alice" }
```

**Oczekiwany wynik:**
- success = true
- calculatedValue = 122 (72 + 50)

---

## 3. Sukces – gracz na 3. miejscu

**Request metadata:**
```json
{ "playerName": "Charlie", "score": 60, "rank": 3, "opponentName": "Alice" }
```

**Oczekiwany wynik:**
- success = true
- calculatedValue = 85 (60 + 25)

---

## 4. Sukces – gracz poza podium (brak bonusu)

**Request metadata:**
```json
{ "playerName": "Dave", "score": 40, "rank": 4, "opponentName": "Alice" }
```

**Oczekiwany wynik:**
- success = true
- calculatedValue = 40 (40 + 0)

---

## 5. Błąd – brak nazwy gracza

**Request metadata:**
```json
{ "score": 95, "rank": 1, "opponentName": "Bob" }
```

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Nazwa gracza jest wymagana"

---

## 6. Błąd – brak wyniku (score)

**Request metadata:**
```json
{ "playerName": "Alice", "rank": 1, "opponentName": "Bob" }
```

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Wynik (score) musi być liczbą nieujemną"

---

## 7. Błąd – ujemny wynik

**Request metadata:**
```json
{ "playerName": "Alice", "score": -10, "rank": 1, "opponentName": "Bob" }
```

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Wynik (score) musi być liczbą nieujemną"

---

## 8. Błąd – brak pozycji (rank)

**Request metadata:**
```json
{ "playerName": "Alice", "score": 80, "opponentName": "Bob" }
```

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Pozycja (rank) musi być liczbą dodatnią"

---

## 9. Błąd – rank = 0 (nieprawidłowy)

**Request metadata:**
```json
{ "playerName": "Alice", "score": 80, "rank": 0, "opponentName": "Bob" }
```

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Pozycja (rank) musi być liczbą dodatnią"

---

## 10. Błąd – brak przeciwnika

**Request metadata:**
```json
{ "playerName": "Alice", "score": 80, "rank": 1 }
```

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Nazwa przeciwnika jest wymagana"

---

## 11. Błąd – resource jest null

**Request metadata:** dowolne poprawne

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Turniej jest wymagany"

---

## 12. Błąd – resource nieaktywny

**Request metadata:** dowolne poprawne, resource.status = INACTIVE

**Oczekiwany wynik:**
- success = false
- errors zawiera: "Turniej nie jest aktywny"

---

## 13. Bezpieczeństwo – USER widzi tylko swoje mecze

- Alice (USER) loguje się i widzi listę requestów
- Powinna zobaczyć tylko mecze, w których jest ownerem
- Nie powinna widzieć meczów Boba ani admin panelu

---

## 14. Bezpieczeństwo – ADMIN widzi wszystkie mecze

- Admin loguje się i wchodzi do panelu administratora
- Powinien zobaczyć listę wszystkich meczów wszystkich graczy
- Powinien móc tworzyć/edytywać/usawać turnieje

---

## 15. Historia gracza – wiele meczów tego samego gracza

- Alice ma 3 mecze (szachy, catan, cs2)
- W widoku "Moje mecze" powinny być widoczne wszystkie 3
- Każdy mecz powinien pokazywać: nazwę turnieju, wynik, pozycję, przeciwnika i punkty rankingowe
