# Egzamin ZPO - Projekty

Repozytorium zawiera projekty egzaminacyjne z przedmiotu Zaawansowane Programowanie Obiektowe zaimplementowane w trzech technologiach: **Java (Spring Boot)**, **Python (FastAPI)** oraz **NodeJS (Express)**.

Wszystkie algorytmy biznesowe (ocenianie quizów, listy rezerwowe fitness, dobór stolika w restauracji, wyliczanie BMR i sumowanie kalorii) zostały napisane w **paradygmacie programowania funkcyjnego** (całkowity brak pętli `for`/`while` oraz instrukcji warunkowych `if`; zastosowano Stream API, lambdy, `.map()`, `.filter()`, `.reduce()`, operatory logiczne oraz wyrażenia trójargumentowe).

---

## Ścieżki projektów i plików źródłowych

### 1. Platforma E-Learningowa
* **Wersja Java**: [e-learning-platform/java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java)
  - Konfiguracja: [SecurityConfig.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/config/SecurityConfig.java), [application.properties](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/resources/application.properties)
  - Logika i Serwisy: [QuizService.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/service/QuizService.java), [ScoreCalculator.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/service/ScoreCalculator.java)
  - Kontrolery: [QuizController.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/controller/QuizController.java), [QuizApiController.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/controller/QuizApiController.java)
  - Modele: [Question.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/model/Question.java), [QuizResult.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/model/QuizResult.java)
  - Widoki HTML: [quiz.html](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/resources/templates/quiz.html), [summary.html](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/resources/templates/summary.html), [results.html](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/resources/templates/results.html)
  - Testy: [ScoreCalculatorTest.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/test/java/com/elearning/ScoreCalculatorTest.java)
* **Wersja Python**: [e-learning-platform/python](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python)
  - Uruchomienie i Routery: [app.py](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/app.py), [quiz.py](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/routers/quiz.py), [auth.py](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/routers/auth.py)
  - Serwisy: [score_calculator.py](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/services/score_calculator.py)
  - Szablony Jinja2: [quiz.html](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/templates/quiz.html), [summary.html](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/templates/summary.html), [results.html](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/templates/results.html)
  - Testy: [test_score_calculator.py](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/tests/test_score_calculator.py)
* **Wersja NodeJS**: [e-learning-platform/nodejs](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs)
  - Serwer i Trasy: [server.js](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/server.js), [quiz.js](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/routes/quiz.js), [auth.js](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/middleware/auth.js)
  - Serwisy: [scoreCalculator.js](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/services/scoreCalculator.js)
  - Szablony EJS: [quiz.ejs](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/views/quiz.ejs), [summary.ejs](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/views/summary.ejs), [results.ejs](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/views/results.ejs)
  - Testy: [scoreCalculator.test.js](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/tests/scoreCalculator.test.js)

### 2. Rejestracja Fitness (Zajęcia & Kolejki)
* **Wersja Java**: [fitness-registration/java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java)
  - Konfiguracja: [SecurityConfig.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/java/com/fitness/config/SecurityConfig.java)
  - Logika: [RegistrationService.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/java/com/fitness/service/RegistrationService.java)
  - Kontrolery: [FitnessController.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/java/com/fitness/controller/FitnessController.java), [FitnessApiController.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/java/com/fitness/controller/FitnessApiController.java)
  - Widok HTML: [fitness.html](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/resources/templates/fitness.html)
  - Testy: [RegistrationServiceTest.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/test/java/com/fitness/RegistrationServiceTest.java)
* **Wersja Python**: [fitness-registration/python](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python)
  - Konfiguracja i Routery: [app.py](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python/app.py), [fitness.py](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python/routers/fitness.py), [auth.py](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python/routers/auth.py)
  - Serwis Kolejki: [registration_service.py](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python/services/registration_service.py)
  - Szablon: [fitness.html](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python/templates/fitness.html)
  - Testy: [test_registration.py](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python/tests/test_registration.py)
* **Wersja NodeJS**: [fitness-registration/nodejs](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs)
  - Serwer i Trasy: [server.js](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/server.js), [fitness.js](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/routes/fitness.js), [auth.js](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/middleware/auth.js)
  - Serwis Kolejki: [registrationService.js](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/services/registrationService.js)
  - Szablon EJS: [fitness.ejs](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/views/fitness.ejs)
  - Testy: [registrationService.test.js](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/tests/registrationService.test.js)

### 3. Rezerwacja Stolików w Restauracji
* **Wersja Java**: [restaurant-management/java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java)
  - Konfiguracja: [SecurityConfig.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/java/com/restaurant/config/SecurityConfig.java)
  - Serwis Dopasowania: [RestaurantService.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/java/com/restaurant/service/RestaurantService.java)
  - Kontrolery: [RestaurantController.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/java/com/restaurant/controller/RestaurantController.java), [RestaurantApiController.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/java/com/restaurant/controller/RestaurantApiController.java)
  - Widok HTML: [restaurant.html](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/resources/templates/restaurant.html)
  - Testy: [RestaurantServiceTest.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/test/java/com/restaurant/RestaurantServiceTest.java)
* **Wersja Python**: [restaurant-management/python](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/python)
  - Routery: [restaurant.py](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/python/routers/restaurant.py), [auth.py](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/python/routers/auth.py)
  - Serwis Dopasowania: [restaurant_service.py](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/python/services/restaurant_service.py)
  - Szablon: [restaurant.html](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/python/templates/restaurant.html)
  - Testy: [test_restaurant.py](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/python/tests/test_restaurant.py)
* **Wersja NodeJS**: [restaurant-management/nodejs](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs)
  - Trasy: [restaurant.js](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/routes/restaurant.js), [auth.js](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/middleware/auth.js)
  - Serwis Dopasowania: [restaurantService.js](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/services/restaurantService.js)
  - Szablon EJS: [restaurant.ejs](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/views/restaurant.ejs)
  - Testy: [restaurantService.test.js](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/tests/restaurantService.test.js)

### 4. Dziennik Dietetyczny i BMR
* **Wersja Java**: [diet-tracker/java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java)
  - Konfiguracja: [SecurityConfig.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/config/SecurityConfig.java)
  - Serwis Diety: [DietService.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/service/DietService.java)
  - Kontrolery: [DietController.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/controller/DietController.java), [DietApiController.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/controller/DietApiController.java)
  - Widok HTML: [diet.html](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/resources/templates/diet.html)
  - Testy: [DietServiceTest.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/test/java/com/diet/DietServiceTest.java)
* **Wersja Python**: [diet-tracker/python](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/python)
  - Routery: [diet.py](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/python/routers/diet.py), [auth.py](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/python/routers/auth.py)
  - Serwis Diety: [diet_service.py](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/python/services/diet_service.py)
  - Szablon: [diet.html](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/python/templates/diet.html)
  - Testy: [test_diet.py](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/python/tests/test_diet.py)
* **Wersja NodeJS**: [diet-tracker/nodejs](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs)
  - Trasy: [diet.js](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/routes/diet.js), [auth.js](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/middleware/auth.js)
  - Serwis Diety: [dietService.js](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/services/dietService.js)
  - Szablon EJS: [diet.ejs](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/views/diet.ejs)
  - Testy: [dietService.test.js](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/tests/dietService.test.js)

---

## Dane logowania

### e-learning-platform
- Student: `student1` / `pass`
- Nauczyciel: `teacher1` / `pass`
- H2 Console Admin (Java): `admin` / `admin` (Rola: ADMIN)

### fitness-registration & diet-tracker
- Użytkownik 1: `user1` / `pass`
- Użytkownik 2: `user2` / `pass`
- H2 Console Admin (Java): `admin` / `admin` (Rola: ADMIN)

### restaurant-management
- Gość 1: `guest1` / `pass`
- Gość 2: `guest2` / `pass`
- Kelner 1: `waiter1` / `pass`
- Kelner 2: `waiter2` / `pass`
- H2 Console Admin (Java): `admin` / `admin` (Rola: ADMIN)

---

## Porty aplikacji i Swagger UI

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

## Uruchamianie projektów i testowanie

### Java (Maven)
```bash
mvn spring-boot:run
mvn test
```

### Python (uvicorn & pytest)
```bash
pip install -r requirements.txt
uvicorn app:app --reload --port <PORT>
pytest
```

### NodeJS (npm)
```bash
npm install
npm start
npm test
```
