# Egzamin ZPO - Projekty

Repozytorium zawiera projekty egzaminacyjne z przedmiotu Zaawansowane Programowanie Obiektowe zaimplementowane w trzech technologiach: **Java (Spring Boot)**, **Python (FastAPI)** oraz **NodeJS (Express)**.

Wszystkie algorytmy biznesowe (ocenianie quizów, zarządzanie listami rezerwowymi fitness, dobór stolika w restauracji, wyliczanie BMR i sumowanie kalorii w diecie) zostały zaimplementowane w **paradygmacie programowania funkcyjnego** (brak tradycyjnych pętli `for`/`while` oraz instrukcji warunkowych `if`; zastosowano Stream API, lambdy, `.map()`, `.filter()`, `.reduce()`, operatory logiczne oraz wyrażenia trójargumentowe).

---

## Struktura plików

```
├── e-learning-platform/
│   ├── java/                              # Spring Boot
│   ├── python/                            # FastAPI
│   └── nodejs/                            # Express
│
├── fitness-registration/
│   ├── java/                              # Spring Boot
│   ├── python/                            # FastAPI
│   └── nodejs/                            # Express
│
├── restaurant-management/
│   ├── java/                              # Spring Boot
│   ├── python/                            # FastAPI
│   └── nodejs/                            # Express
│
└── diet-tracker/
    ├── java/                              # Spring Boot
    ├── python/                            # FastAPI
    └── nodejs/                            # Express
```

---

## Opis projektów

### 1. e-learning-platform (Ocenianie quizów)

| Komponent | Java | Python | NodeJS |
|-----------|------|--------|--------|
| Framework | Spring Boot 3.2 | FastAPI | Express |
| Baza | H2 (in-memory) | SQLite | SQLite |
| UI | Thymeleaf | Jinja2 | EJS |
| Security | Spring Security | Basic Auth | express-basic-auth |
| API docs | Swagger UI | Swagger UI | Swagger UI |
| Testy | JUnit 5 | pytest | Jest |

**Funkcjonalności:**
- Baza: `Question` (treść, opcje A-D, poprawna odpowiedź, punkty), `QuizResult` (userId, score, percentage, passed)
- UI: Widok rozwiązywania quizu oraz podsumowanie z wynikiem i statusem zaliczenia.
- Algorytm (funkcyjny): Obliczanie oceny (poprawna +1, brak 0, błędna -0.5), zaliczenie przy >50%.
- REST: `POST /api/quiz/submit` przyjmuje odpowiedzi, zwraca procentowy wynik.
- Security: Tylko STUDENT może wysłać odpowiedzi. TEACHER widzi wyniki wszystkich.

---

### 2. fitness-registration (Listy rezerwowe)

| Komponent | Java | Python | NodeJS |
|-----------|------|--------|--------|
| Framework | Spring Boot 3.2 | FastAPI | Express |
| Baza | H2 (in-memory) | SQLite | SQLite |
| UI | Thymeleaf | Jinja2 | EJS |
| Security | Spring Security | Basic Auth | express-basic-auth |
| API docs | Swagger UI | Swagger UI | Swagger UI |
| Testy | JUnit 5 | pytest | Jest |

**Funkcjonalności:**
- Baza: `FitnessClass` (typ, max pojemność), `Registration` (userId, status: MAIN/WAITING, pozycja w kolejce)
- UI: Lista dostępnych zajęć w tygodniu z przyciskami "Zapisz się" oraz "Wypisz się".
- Algorytm (funkcyjny): Obsługa kolejki rezerwowej. Wypisanie się z listy głównej automatycznie promuje osobę z pozycji 1 na liście rezerwowej i przesuwa w górę pozostałych oczekujących.
- REST: `POST /api/fitness/unregister/{classId}` wyrejestrowuje użytkownika i reorganizuje kolejki.
- Security: Dostępne tylko dla zalogowanych użytkowników z rolą USER.

---

### 3. restaurant-management (Zarządzanie stolikami)

| Komponent | Java | Python | NodeJS |
|-----------|------|--------|--------|
| Framework | Spring Boot 3.2 | FastAPI | Express |
| Baza | H2 (in-memory) | SQLite | SQLite |
| UI | Thymeleaf | Jinja2 | EJS |
| Security | Spring Security | Basic Auth | express-basic-auth |
| API docs | Swagger UI | Swagger UI | Swagger UI |
| Testy | JUnit 5 | pytest | Jest |

**Funkcjonalności:**
- Baza: `Table` (liczba miejsc, lokalizacja wewnątrz/ogródek), `Reservation` (godzina, liczba gości, userId)
- UI: Wyszukiwanie stolika na konkretną godzinę dla X osób z opcją rezerwacji.
- Algorytm (funkcyjny - Best-Fit): Dobór optymalnego wolnego stolika o jak najmniejszej pasującej pojemności (np. dla 3 osób szuka najpierw wolnego stolika 3- lub 4-osobowego zamiast blokować 8-osobowy).
- REST: `GET /api/restaurant/tables/search` zwraca optymalny wolny stolik spełniający kryteria.
- Security: Rezerwować mogą tylko zalogowani goście (`GUEST`). Kelner (`WAITER`) widzi podsumowanie rezerwacji na cały dzień.

---

### 4. diet-tracker (Zapotrzebowanie kaloryczne)

| Komponent | Java | Python | NodeJS |
|-----------|------|--------|--------|
| Framework | Spring Boot 3.2 | FastAPI | Express |
| Baza | H2 (in-memory) | SQLite | SQLite |
| UI | Thymeleaf | Jinja2 | EJS |
| Security | Spring Security | Basic Auth | express-basic-auth |
| API docs | Swagger UI | Swagger UI | Swagger UI |
| Testy | JUnit 5 | pytest | Jest |

**Funkcjonalności:**
- Baza: `Meal` (nazwa, makroskładniki, kalorie), `DailyLog` (data, mealId, userId), `UserProfile` (waga, wzrost, wiek, płeć)
- UI: Widok dodawania posiłków do wybranego dnia, modyfikacja parametrów wagi/wzrostu i estetyczny pasek postępu kalorii.
- Algorytm (funkcyjny): Obliczanie dziennego zapotrzebowania BMR (równanie Harrisa-Benedicta) oraz procentowa realizacja celu kalorycznego.
- REST: `GET /api/diet/logs/summary` pobiera podsumowanie kalorii i makroskładników na wybrany dzień.
- Security: Użytkownik (`USER`) widzi wyłącznie swój profil oraz swój dziennik posiłków.

---

## Porty aplikacji i Swagger UI

Każda aplikacja uruchamia się na dedykowanym porcie, co pozwala na ich równoległe testowanie.

| Projekt | Język | Port | Swagger UI URL |
|---------|-------|------|----------------|
| **e-learning-platform** | Java | `8080` | `http://localhost:8080/swagger-ui/index.html` |
| | Python | `8000` | `http://localhost:8000/docs` |
| | NodeJS | `3000` | `http://localhost:3000/api-docs` |
| **fitness-registration** | Java | `8081` | `http://localhost:8081/swagger-ui/index.html` |
| | Python | `8001` | `http://localhost:8001/docs` |
| | NodeJS | `3001` | `http://localhost:3001/api-docs` |
| **restaurant-management** | Java | `8082` | `http://localhost:8082/swagger-ui/index.html` |
| | Python | `8002` | `http://localhost:8002/docs` |
| | NodeJS | `3002` | `http://localhost:3002/api-docs` |
| **diet-tracker** | Java | `8083` | `http://localhost:8083/swagger-ui/index.html` |
| | Python | `8003` | `http://localhost:8003/docs` |
| | NodeJS | `3003` | `http://localhost:3003/api-docs` |

---

## Dane logowania

### e-learning-platform
- Student: `student1` / `pass`
- Nauczyciel: `teacher1` / `pass`

### fitness-registration & diet-tracker
- Użytkownik 1: `user1` / `pass`
- Użytkownik 2: `user2` / `pass`

### restaurant-management
- Gość 1: `guest1` / `pass`
- Gość 2: `guest2` / `pass`
- Kelner 1: `waiter1` / `pass`

---

## Uruchamianie projektów

### Java (Maven)
Przejdź do folderu wybranego projektu i uruchom:
```bash
mvn spring-boot:run
```

### Python (uvicorn)
Przejdź do folderu projektu, zainstaluj zależności i uruchom:
```bash
pip install -r requirements.txt
uvicorn app:app --reload --port <PORT>
```

### NodeJS (Express)
Przejdź do folderu projektu, zainstaluj zależności i uruchom:
```bash
npm install
npm start
```

---

## Testowanie (Uruchamianie testów jednostkowych)

### Java
```bash
mvn test
```

### Python
```bash
pytest
```

### NodeJS
```bash
npm test
```
