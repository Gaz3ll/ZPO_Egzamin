# Egzamin ZPO - Projekty

Repozytorium zawiera projekty egzaminacyjne z przedmiotu Zaawansowane Programowanie Obiektowe zaimplementowane w trzech technologiach: **Java (Spring Boot)**, **Python (FastAPI)** oraz **NodeJS (Express)**.

Wszystkie algorytmy biznesowe (ocenianie quizów, listy rezerwowe fitness, dobór stolika w restauracji, wyliczanie BMR i sumowanie kalorii) zostały napisane w **paradygmacie programowania funkcyjnego** (całkowity brak pętli `for`/`while` oraz instrukcji warunkowych `if`; zastosowano Stream API, lambdy, `.map()`, `.filter()`, `.reduce()`, operatory logiczne oraz wyrażenia trójargumentowe).

---

## Pełna struktura plików (Schemat Tree)

Poniżej znajduje się kompletny schemat struktury plików w repozytorium (wszystkie nazwy plików są bezpośrednimi odnośnikami do ich lokalizacji):

```
├── e-learning-platform/
│   ├── java/
│   │   ├── pom.xml (link: [pom.xml](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/pom.xml))
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/elearning/
│   │       │   │   ├── ElearningApplication.java (link: [ElearningApplication.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/ElearningApplication.java))
│   │       │   │   ├── config/
│   │       │   │   │   ├── SecurityConfig.java (link: [SecurityConfig.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/config/SecurityConfig.java))
│   │       │   │   │   └── SwaggerConfig.java (link: [SwaggerConfig.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/config/SwaggerConfig.java))
│   │       │   │   ├── controller/
│   │       │   │   │   ├── QuizApiController.java (link: [QuizApiController.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/controller/QuizApiController.java))
│   │       │   │   │   ├── QuizController.java (link: [QuizController.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/controller/QuizController.java))
│   │       │   │   │   └── TeacherController.java (link: [TeacherController.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/controller/TeacherController.java))
│   │       │   │   ├── dto/
│   │       │   │   │   ├── QuizResultResponse.java (link: [QuizResultResponse.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/dto/QuizResultResponse.java))
│   │       │   │   │   └── QuizSubmitRequest.java (link: [QuizSubmitRequest.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/dto/QuizSubmitRequest.java))
│   │       │   │   ├── model/
│   │       │   │   │   ├── Question.java (link: [Question.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/model/Question.java))
│   │       │   │   │   └── QuizResult.java (link: [QuizResult.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/model/QuizResult.java))
│   │       │   │   ├── repository/
│   │       │   │   │   ├── QuestionRepository.java (link: [QuestionRepository.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/repository/QuestionRepository.java))
│   │       │   │   │   └── QuizResultRepository.java (link: [QuizResultRepository.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/repository/QuizResultRepository.java))
│   │       │   │   └── service/
│   │       │   │       ├── QuizService.java (link: [QuizService.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/service/QuizService.java))
│   │       │   │       └── ScoreCalculator.java (link: [ScoreCalculator.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/java/com/elearning/service/ScoreCalculator.java))
│   │       │   └── resources/
│   │       │       ├── application.properties (link: [application.properties](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/resources/application.properties))
│   │       │       ├── data.sql (link: [data.sql](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/resources/data.sql))
│   │       │       └── templates/
│   │       │           ├── quiz.html (link: [quiz.html](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/resources/templates/quiz.html))
│   │       │           ├── results.html (link: [results.html](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/resources/templates/results.html))
│   │       │           └── summary.html (link: [summary.html](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/main/resources/templates/summary.html))
│   │       └── test/java/com/elearning/
│   │           └── ScoreCalculatorTest.java (link: [ScoreCalculatorTest.java](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/java/src/test/java/com/elearning/ScoreCalculatorTest.java))
│   │
│   ├── python/
│   │   ├── requirements.txt (link: [requirements.txt](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/requirements.txt))
│   │   ├── app.py (link: [app.py](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/app.py))
│   │   ├── models.py (link: [models.py](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/models.py))
│   │   ├── schemas.py (link: [schemas.py](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/schemas.py))
│   │   ├── routers/
│   │   │   ├── auth.py (link: [auth.py](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/routers/auth.py))
│   │   │   ├── quiz.py (link: [quiz.py](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/routers/quiz.py))
│   │   │   └── teacher.py (link: [teacher.py](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/routers/teacher.py))
│   │   ├── services/
│   │   │   └── score_calculator.py (link: [score_calculator.py](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/services/score_calculator.py))
│   │   ├── templates/
│   │   │   ├── quiz.html (link: [quiz.html](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/templates/quiz.html))
│   │   │   ├── results.html (link: [results.html](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/templates/results.html))
│   │   │   └── summary.html (link: [summary.html](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/templates/summary.html))
│   │   └── tests/
│   │       └── test_score_calculator.py (link: [test_score_calculator.py](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/python/tests/test_score_calculator.py))
│   │
│   └── nodejs/
│       ├── package.json (link: [package.json](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/package.json))
│       ├── server.js (link: [server.js](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/server.js))
│       ├── config/
│       │   └── config.json (link: [config.json](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/config/config.json))
│       ├── middleware/
│       │   └── auth.js (link: [auth.js](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/middleware/auth.js))
│       ├── models/
│       │   ├── index.js (link: [index.js](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/models/index.js))
│       │   ├── question.js (link: [question.js](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/models/question.js))
│       │   └── quizResult.js (link: [quizResult.js](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/models/quizResult.js))
│       ├── routes/
│       │   ├── quiz.js (link: [quiz.js](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/routes/quiz.js))
│       │   └── teacher.js (link: [teacher.js](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/routes/teacher.js))
│       ├── services/
│       │   └── scoreCalculator.js (link: [scoreCalculator.js](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/services/scoreCalculator.js))
│       ├── tests/
│       │   └── scoreCalculator.test.js (link: [scoreCalculator.test.js](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/tests/scoreCalculator.test.js))
│       └── views/
│           ├── quiz.ejs (link: [quiz.ejs](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/views/quiz.ejs))
│           ├── results.ejs (link: [results.ejs](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/views/results.ejs))
│           └── summary.ejs (link: [summary.ejs](file:///c:/Users/wikto/ZPO_Egzamin/e-learning-platform/nodejs/views/summary.ejs))
│
├── fitness-registration/
│   ├── java/
│   │   ├── pom.xml (link: [pom.xml](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/pom.xml))
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/fitness/
│   │       │   │   ├── FitnessApplication.java (link: [FitnessApplication.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/java/com/fitness/FitnessApplication.java))
│   │       │   │   ├── config/
│   │       │   │   │   ├── SecurityConfig.java (link: [SecurityConfig.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/java/com/fitness/config/SecurityConfig.java))
│   │       │   │   │   └── SwaggerConfig.java (link: [SwaggerConfig.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/java/com/fitness/config/SwaggerConfig.java))
│   │       │   │   ├── controller/
│   │       │   │   │   ├── FitnessApiController.java (link: [FitnessApiController.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/java/com/fitness/controller/FitnessApiController.java))
│   │       │   │   │   └── FitnessController.java (link: [FitnessController.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/java/com/fitness/controller/FitnessController.java))
│   │       │   │   ├── model/
│   │       │   │   │   ├── FitnessClass.java (link: [FitnessClass.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/java/com/fitness/model/FitnessClass.java))
│   │       │   │   │   └── Registration.java (link: [Registration.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/java/com/fitness/model/Registration.java))
│   │       │   │   ├── repository/
│   │       │   │   │   ├── FitnessClassRepository.java (link: [FitnessClassRepository.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/java/com/fitness/repository/FitnessClassRepository.java))
│   │       │   │   │   └── RegistrationRepository.java (link: [RegistrationRepository.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/java/com/fitness/repository/RegistrationRepository.java))
│   │       │   │   └── service/
│   │       │   │       └── RegistrationService.java (link: [RegistrationService.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/java/com/fitness/service/RegistrationService.java))
│   │       │   └── resources/
│   │       │       ├── application.properties (link: [application.properties](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/resources/application.properties))
│   │       │       ├── data.sql (link: [data.sql](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/resources/data.sql))
│   │       │       └── templates/
│   │       │           └── fitness.html (link: [fitness.html](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/main/resources/templates/fitness.html))
│   │       └── test/java/com/fitness/
│   │           └── RegistrationServiceTest.java (link: [RegistrationServiceTest.java](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/java/src/test/java/com/fitness/RegistrationServiceTest.java))
│   │
│   ├── python/
│   │   ├── requirements.txt (link: [requirements.txt](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python/requirements.txt))
│   │   ├── app.py (link: [app.py](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python/app.py))
│   │   ├── models.py (link: [models.py](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python/models.py))
│   │   ├── routers/
│   │   │   ├── auth.py (link: [auth.py](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python/routers/auth.py))
│   │   │   └── fitness.py (link: [fitness.py](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python/routers/fitness.py))
│   │   ├── services/
│   │   │   └── registration_service.py (link: [registration_service.py](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python/services/registration_service.py))
│   │   ├── templates/
│   │   │   └── fitness.html (link: [fitness.html](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python/templates/fitness.html))
│   │   └── tests/
│   │       └── test_registration.py (link: [test_registration.py](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/python/tests/test_registration.py))
│   │
│   └── nodejs/
│       ├── package.json (link: [package.json](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/package.json))
│       ├── server.js (link: [server.js](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/server.js))
│       ├── config/
│       │   └── config.json (link: [config.json](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/config/config.json))
│       ├── middleware/
│       │   └── auth.js (link: [auth.js](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/middleware/auth.js))
│       ├── models/
│       │   ├── index.js (link: [index.js](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/models/index.js))
│       │   ├── fitnessClass.js (link: [fitnessClass.js](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/models/fitnessClass.js))
│       │   └── registration.js (link: [registration.js](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/models/registration.js))
│       ├── routes/
│       │   └── fitness.js (link: [fitness.js](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/routes/fitness.js))
│       ├── services/
│       │   └── registrationService.js (link: [registrationService.js](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/services/registrationService.js))
│       ├── tests/
│       │   └── registrationService.test.js (link: [registrationService.test.js](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/tests/registrationService.test.js))
│       └── views/
│           └── fitness.ejs (link: [fitness.ejs](file:///c:/Users/wikto/ZPO_Egzamin/fitness-registration/nodejs/views/fitness.ejs))
│
├── restaurant-management/
│   ├── java/
│   │   ├── pom.xml (link: [pom.xml](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/pom.xml))
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/restaurant/
│   │       │   │   ├── RestaurantApplication.java (link: [RestaurantApplication.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/java/com/restaurant/RestaurantApplication.java))
│   │       │   │   ├── config/
│   │       │   │   │   ├── SecurityConfig.java (link: [SecurityConfig.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/java/com/restaurant/config/SecurityConfig.java))
│   │       │   │   │   └── SwaggerConfig.java (link: [SwaggerConfig.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/java/com/restaurant/config/SwaggerConfig.java))
│   │       │   │   ├── controller/
│   │       │   │   │   ├── RestaurantApiController.java (link: [RestaurantApiController.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/java/com/restaurant/controller/RestaurantApiController.java))
│   │       │   │   │   └── RestaurantController.java (link: [RestaurantController.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/java/com/restaurant/controller/RestaurantController.java))
│   │       │   │   ├── model/
│   │       │   │   │   ├── Table.java (link: [Table.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/java/com/restaurant/model/Table.java))
│   │       │   │   │   └── Reservation.java (link: [Reservation.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/java/com/restaurant/model/Reservation.java))
│   │       │   │   ├── repository/
│   │       │   │   │   ├── TableRepository.java (link: [TableRepository.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/java/com/restaurant/repository/TableRepository.java))
│   │       │   │   │   └── ReservationRepository.java (link: [ReservationRepository.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/java/com/restaurant/repository/ReservationRepository.java))
│   │       │   │   └── service/
│   │       │   │       └── RestaurantService.java (link: [RestaurantService.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/java/com/restaurant/service/RestaurantService.java))
│   │       │   └── resources/
│   │       │       ├── application.properties (link: [application.properties](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/resources/application.properties))
│   │       │       ├── data.sql (link: [data.sql](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/resources/data.sql))
│   │       │       └── templates/
│   │       │           └── restaurant.html (link: [restaurant.html](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/main/resources/templates/restaurant.html))
│   │       └── test/java/com/restaurant/
│   │           └── RestaurantServiceTest.java (link: [RestaurantServiceTest.java](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/java/src/test/java/com/restaurant/RestaurantServiceTest.java))
│   │
│   ├── python/
│   │   ├── requirements.txt (link: [requirements.txt](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/python/requirements.txt))
│   │   ├── app.py (link: [app.py](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/python/app.py))
│   │   ├── models.py (link: [models.py](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/python/models.py))
│   │   ├── routers/
│   │   │   ├── auth.py (link: [auth.py](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/python/routers/auth.py))
│   │   │   └── restaurant.py (link: [restaurant.py](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/python/routers/restaurant.py))
│   │   ├── services/
│   │   │   └── restaurant_service.py (link: [restaurant_service.py](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/python/services/restaurant_service.py))
│   │   ├── templates/
│   │   │   └── restaurant.html (link: [restaurant.html](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/python/templates/restaurant.html))
│   │   └── tests/
│   │       └── test_restaurant.py (link: [test_restaurant.py](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/python/tests/test_restaurant.py))
│   │
│   └── nodejs/
│       ├── package.json (link: [package.json](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/package.json))
│       ├── server.js (link: [server.js](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/server.js))
│       ├── config/
│       │   └── config.json (link: [config.json](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/config/config.json))
│       ├── middleware/
│       │   └── auth.js (link: [auth.js](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/middleware/auth.js))
│       ├── models/
│       │   ├── index.js (link: [index.js](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/models/index.js))
│       │   ├── table.js (link: [table.js](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/models/table.js))
│       │   └── reservation.js (link: [reservation.js](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/models/reservation.js))
│       ├── routes/
│       │   └── restaurant.js (link: [restaurant.js](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/routes/restaurant.js))
│       ├── services/
│       │   └── restaurantService.js (link: [restaurantService.js](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/services/restaurantService.js))
│       ├── tests/
│       │   └── restaurantService.test.js (link: [restaurantService.test.js](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/tests/restaurantService.test.js))
│       └── views/
│           └── restaurant.ejs (link: [restaurant.ejs](file:///c:/Users/wikto/ZPO_Egzamin/restaurant-management/nodejs/views/restaurant.ejs))
│
└── diet-tracker/
    ├── java/
    │   ├── pom.xml (link: [pom.xml](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/pom.xml))
    │   └── src/
    │       ├── main/
    │       │   ├── java/com/diet/
    │       │   │   ├── DietApplication.java (link: [DietApplication.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/DietApplication.java))
    │       │   │   ├── config/
    │       │   │   │   ├── SecurityConfig.java (link: [SecurityConfig.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/config/SecurityConfig.java))
    │       │   │   │   └── SwaggerConfig.java (link: [SwaggerConfig.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/config/SwaggerConfig.java))
    │       │   │   ├── controller/
    │       │   │   │   ├── DietApiController.java (link: [DietApiController.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/controller/DietApiController.java))
    │       │   │   │   └── DietController.java (link: [DietController.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/controller/DietController.java))
    │       │   │   ├── model/
    │       │   │   │   ├── Meal.java (link: [Meal.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/model/Meal.java))
    │       │   │   │   ├── DailyLog.java (link: [DailyLog.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/model/DailyLog.java))
    │       │   │   │   └── UserProfile.java (link: [UserProfile.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/model/UserProfile.java))
    │       │   │   ├── repository/
    │       │   │   │   ├── MealRepository.java (link: [MealRepository.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/repository/MealRepository.java))
    │       │   │   │   ├── DailyLogRepository.java (link: [DailyLogRepository.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/repository/DailyLogRepository.java))
    │       │   │   │   └── UserProfileRepository.java (link: [UserProfileRepository.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/repository/UserProfileRepository.java))
    │       │   │   └── service/
    │       │   │       └── DietService.java (link: [DietService.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/java/com/diet/service/DietService.java))
    │       │   └── resources/
    │       │       ├── application.properties (link: [application.properties](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/resources/application.properties))
    │       │       ├── data.sql (link: [data.sql](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/resources/data.sql))
    │       │       └── templates/
    │       │           └── diet.html (link: [diet.html](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/main/resources/templates/diet.html))
    │       └── test/java/com/diet/
    │           └── DietServiceTest.java (link: [DietServiceTest.java](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/java/src/test/java/com/diet/DietServiceTest.java))
    │
    ├── python/
    │   ├── requirements.txt (link: [requirements.txt](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/python/requirements.txt))
    │   ├── app.py (link: [app.py](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/python/app.py))
    │   ├── models.py (link: [models.py](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/python/models.py))
    │   ├── routers/
    │   │   ├── auth.py (link: [auth.py](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/python/routers/auth.py))
    │   │   └── diet.py (link: [diet.py](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/python/routers/diet.py))
    │   ├── services/
    │   │   └── diet_service.py (link: [diet_service.py](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/python/services/diet_service.py))
    │   ├── templates/
    │   │   └── diet.html (link: [diet.html](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/python/templates/diet.html))
    │   └── tests/
    │       └── test_diet.py (link: [test_diet.py](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/python/tests/test_diet.py))
    │
    └── nodejs/
        ├── package.json (link: [package.json](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/package.json))
        ├── server.js (link: [server.js](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/server.js))
        ├── config/
        │   └── config.json (link: [config.json](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/config/config.json))
        ├── middleware/
        │   └── auth.js (link: [auth.js](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/middleware/auth.js))
        ├── models/
        │   ├── index.js (link: [index.js](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/models/index.js))
        │   ├── meal.js (link: [meal.js](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/models/meal.js))
        │   ├── dailyLog.js (link: [dailyLog.js](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/models/dailyLog.js))
        │   └── userProfile.js (link: [userProfile.js](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/models/userProfile.js))
        ├── routes/
        │   └── diet.js (link: [diet.js](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/routes/diet.js))
        ├── services/
        │   └── dietService.js (link: [dietService.js](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/services/dietService.js))
        ├── tests/
        │   └── dietService.test.js (link: [dietService.test.js](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/tests/dietService.test.js))
        └── views/
            └── diet.ejs (link: [diet.ejs](file:///c:/Users/wikto/ZPO_Egzamin/diet-tracker/nodejs/views/diet.ejs))
```

---

## Opis projektów i algorytmów funkcyjnych

### 1. e-learning-platform (Ocenianie quizów)
* **Algorytm**: zaimplementowany w paradygmacie funkcyjnym (brak tradycyjnych pętli i instrukcji warunkowych `if`; użycie Stream API, lambda, `.map()`, `.filter()`, `.reduce()` oraz wyrażeń trójargumentowych). Punktacja: +1 za poprawną, -0.5 za złą, 0 za brak; próg zaliczenia >50%.
* ** REST API **: `POST /api/quiz/submit` przyjmuje odpowiedzi, zwraca procentowy wynik.
* ** Bezpieczeństwo **: logowanie (hashowane BCrypt w Javie oraz SHA-256 w NodeJS/Python). STUDENT może wysyłać odpowiedzi, TEACHER widzi wyniki wszystkich.

### 2. fitness-registration (Listy rezerwowe)
* **Algorytm**: zaimplementowany w paradygmacie funkcyjnym (brak tradycyjnych pętli i instrukcji warunkowych `if`; użycie Stream API, lambda, `.map()`, `.filter()`, `.reduce()` oraz wyrażeń trójargumentowych). Zarządzanie kolejką: max 20 osób na liście głównej, 21. trafia na rezerwę z pozycją; wypisanie się kogoś z listy głównej "wciąga" pierwszą osobę z rezerwy.
* ** REST API **: `POST /api/fitness/unregister/{classId}` wyrejestrowuje użytkownika i reorganizuje kolejki.

### 3. restaurant-management (Zarządzanie stolikami)
* **Algorytm**: zaimplementowany w paradygmacie funkcyjnym (brak tradycyjnych pętli i instrukcji warunkowych `if`; użycie Stream API, lambda, `.map()`, `.filter()`, `.reduce()` oraz wyrażeń trójargumentowych). Dobór optymalnego wolnego stolika o jak najmniejszej pasującej pojemności (np. dla 3 osób szuka najpierw wolnego stolika 3- lub 4-osobowego zamiast blokować 8-osobowy). Ponadto blokuje rezerwacje w tym samym czasie dla tego samego stolika i tego samego użytkownika.
* ** REST API **: `GET /api/restaurant/tables/search` zwraca optymalny wolny stolik spełniający kryteria.
* ** Bezpieczeństwo **: Kelner (`WAITER`) widzi podsumowanie rezerwacji na cały dzień, goście (`GUEST`) mogą rezerwować stoliki.

### 4. diet-tracker (Zapotrzebowanie kaloryczne)
* **Algorytm**: zaimplementowany w paradygmacie funkcyjnym (brak tradycyjnych pętli i instrukcji warunkowych `if`; użycie Stream API, lambda, `.map()`, `.filter()`, `.reduce()` oraz wyrażeń trójargumentowych). Obliczanie zapotrzebowania BMR (równanie Harrisa-Benedicta) oraz procentowa realizacja celu kalorycznego na podstawie dodanych posiłków.
* ** REST API **: `GET /api/diet/logs/summary` pobiera podsumowanie kalorii i makroskładników na wybrany dzień.

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
