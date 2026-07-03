# Egzamin ZPO - Projekty

## Struktura plików

```
├── e-learning-platform/
│   ├── java/                              # Spring Boot
│   │   ├── pom.xml
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/elearning/
│   │       │   │   ├── ElearningApplication.java
│   │       │   │   ├── config/
│   │       │   │   │   ├── SecurityConfig.java
│   │       │   │   │   └── SwaggerConfig.java
│   │       │   │   ├── controller/
│   │       │   │   │   ├── QuizApiController.java
│   │       │   │   │   ├── QuizController.java
│   │       │   │   │   └── TeacherController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── QuizResultResponse.java
│   │       │   │   │   └── QuizSubmitRequest.java
│   │       │   │   ├── model/
│   │       │   │   │   ├── Question.java
│   │       │   │   │   └── QuizResult.java
│   │       │   │   ├── repository/
│   │       │   │   │   ├── QuestionRepository.java
│   │       │   │   │   └── QuizResultRepository.java
│   │       │   │   └── service/
│   │       │   │       ├── QuizService.java
│   │       │   │       └── ScoreCalculator.java
│   │       │   └── resources/
│   │       │       ├── application.properties
│   │       │       ├── data.sql
│   │       │       └── templates/
│   │       │           ├── quiz.html
│   │       │           ├── results.html
│   │       │           └── summary.html
│   │       └── test/java/com/elearning/
│   │           └── ScoreCalculatorTest.java
│   │
│   ├── python/                             # FastAPI
│   │   ├── requirements.txt
│   │   ├── app.py
│   │   ├── models.py
│   │   ├── schemas.py
│   │   ├── routers/
│   │   │   ├── __init__.py
│   │   │   ├── auth.py
│   │   │   ├── quiz.py
│   │   │   └── teacher.py
│   │   ├── services/
│   │   │   ├── __init__.py
│   │   │   └── score_calculator.py
│   │   ├── static/
│   │   ├── templates/
│   │   │   ├── quiz.html
│   │   │   ├── results.html
│   │   │   └── summary.html
│   │   └── tests/
│   │       ├── __init__.py
│   │       └── test_score_calculator.py
│   │
│   └── nodejs/                             # Express
│       ├── package.json
│       ├── server.js
│       ├── config/
│       │   └── config.json
│       ├── middleware/
│       │   └── auth.js
│       ├── models/
│       │   ├── index.js
│       │   ├── question.js
│       │   └── quizResult.js
│       ├── routes/
│       │   ├── quiz.js
│       │   └── teacher.js
│       ├── services/
│       │   └── scoreCalculator.js
│       ├── static/
│       ├── tests/
│       │   └── scoreCalculator.test.js
│       └── views/
│           ├── quiz.ejs
│           ├── results.ejs
│           └── summary.ejs
│
└── fitness-registration/
    ├── java/                               # Spring Boot
    │   ├── pom.xml
    │   └── src/
    │       ├── main/
    │       │   ├── java/com/fitness/
    │       │   │   ├── FitnessApplication.java
    │       │   │   ├── config/
    │       │   │   │   ├── SecurityConfig.java
    │       │   │   │   └── SwaggerConfig.java
    │       │   │   ├── controller/
    │       │   │   │   ├── FitnessApiController.java
    │       │   │   │   └── FitnessController.java
    │       │   │   ├── model/
    │       │   │   │   ├── FitnessClass.java
    │       │   │   │   └── Registration.java
    │       │   │   ├── repository/
    │       │   │   │   ├── FitnessClassRepository.java
    │       │   │   │   └── RegistrationRepository.java
    │       │   │   └── service/
    │       │   │       └── RegistrationService.java
    │       │   └── resources/
    │       │       ├── application.properties
    │       │       ├── data.sql
    │       │       └── templates/
    │       │           └── fitness.html
    │       └── test/java/com/fitness/
    │           └── RegistrationServiceTest.java
    │
    ├── python/                              # FastAPI
    │   ├── requirements.txt
    │   ├── app.py
    │   ├── models.py
    │   ├── routers/
    │   │   ├── __init__.py
    │   │   ├── auth.py
    │   │   └── fitness.py
    │   ├── services/
    │   │   ├── __init__.py
    │   │   └── registration_service.py
    │   ├── static/
    │   ├── templates/
    │   │   └── fitness.html
    │   └── tests/
    │       ├── __init__.py
    │       └── test_registration.py
    │
    └── nodejs/                              # Express
        ├── package.json
        ├── server.js
        ├── config/
        │   └── config.json
        ├── middleware/
        │   └── auth.js
        ├── models/
        │   ├── index.js
        │   ├── fitnessClass.js
        │   └── registration.js
        ├── routes/
        │   └── fitness.js
        ├── services/
        │   └── registrationService.js
        ├── static/
        ├── tests/
        │   └── registrationService.test.js
        └── views/
            └── fitness.ejs
```

## Opis projektów

### e-learning-platform (Ocenianie quizów)

| Komponent | Java | Python | NodeJS |
|-----------|------|--------|--------|
| Framework | Spring Boot 3.2 | FastAPI | Express |
| ORM | JPA/Hibernate | SQLAlchemy | Sequelize |
| Baza | H2 (memory) | SQLite | SQLite |
| UI | Thymeleaf | Jinja2 | EJS |
| Security | Spring Security | Basic Auth | express-basic-auth |
| API docs | springdoc (Swagger) | FastAPI docs | (manual) |
| Test | JUnit 5 | pytest | Jest |

**Funkcjonalności:**
- Baza: `Question` (treść, poprawna odpowiedź, punkty, opcje A-D), `QuizResult` (userId, score, percentage, passed)
- UI: rozwiązywanie quizu (formularz z radio buttonami), podsumowanie z wynikiem
- Algorytm: +1 za poprawną, -0.5 za złą, 0 za brak; próg zaliczenia >50%
- REST: `POST /api/quiz/submit` przyjmuje odpowiedzi, zwraca procentowy wynik
- Security: tylko STUDENT może wysyłać odpowiedzi, TEACHER widzi wyniki wszystkich
- Test: 8 przypadków testowych kalkulatora punktacji

### fitness-registration (Listy rezerwowe)

| Komponent | Java | Python | NodeJS |
|-----------|------|--------|--------|
| Framework | Spring Boot 3.2 | FastAPI | Express |
| ORM | JPA/Hibernate | SQLAlchemy | Sequelize |
| Baza | H2 (memory) | SQLite | SQLite |
| UI | Thymeleaf | Jinja2 | EJS |
| Security | Spring Security | Basic Auth | express-basic-auth |
| API docs | springdoc (Swagger) | FastAPI docs | (manual) |
| Test | JUnit 5 | pytest | Jest |

**Funkcjonalności:**
- Baza: `FitnessClass` (typ, dzień, godzina, max pojemność), `Registration` (userId, classId, status: MAIN/WAITING, position)
- UI: lista zajęć w tygodniu z przyciskami "Register" / "Unregister"
- Algorytm: max 20 osób na liście głównej, 21. trafia na rezerwę z pozycją; wypisanie się wciąga pierwszeństwo z rezerwy
- REST: `POST /api/fitness/unregister/{classId}` uruchamia logikę kolejkowania
- Security: tylko USER może się zapisywać/wypisywać
- Test: przejście z listy rezerwowej na główną po zwolnieniu miejsca

## Uruchomienie

### Java
```bash
cd e-learning-platform/java
mvn spring-boot:run    # http://localhost:8080

cd fitness-registration/java
mvn spring-boot:run    # http://localhost:8081
```

### Python
```bash
cd e-learning-platform/python
pip install -r requirements.txt
uvicorn app:app --reload    # http://localhost:8000

cd fitness-registration/python
pip install -r requirements.txt
uvicorn app:app --reload    # http://localhost:8000
```

### NodeJS
```bash
cd e-learning-platform/nodejs
npm install
npm start                  # http://localhost:3000

cd fitness-registration/nodejs
npm install
npm start                  # http://localhost:3001
```

## Testy

```bash
# Java
cd e-learning-platform/java
mvn test

# Python
cd e-learning-platform/python
pytest

# NodeJS
cd e-learning-platform/nodejs
npm test
```

## Dane logowania

### e-learning-platform
| Użytkownik | Hasło | Rola |
|-----------|-------|------|
| student1 | pass | STUDENT |
| teacher1 | pass | TEACHER |

### fitness-registration
| Użytkownik | Hasło | Rola |
|-----------|-------|------|
| user1 | pass | USER |
| user2 | pass | USER |

## Swagger UI (Java)
- e-learning: `http://localhost:8080/swagger-ui.html`
- fitness: `http://localhost:8081/swagger-ui.html`
