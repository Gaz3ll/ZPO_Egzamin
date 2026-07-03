# Test Cases — System zgłoszeń osiedlowych

## Backend — testy jednostkowe algorytmu (DefaultDomainAlgorithmTest)

### TC-ALG-01: Brak kategorii — błąd
**Given:** resource = null  
**When:** evaluate()  
**Then:** zwrócony `success = false`, błąd `"Kategoria zgłoszenia jest wymagana"`

### TC-ALG-02: Kategoria nieaktywna — błąd
**Given:** resource.status = INACTIVE  
**When:** evaluate()  
**Then:** zwrócony `success = false`, błąd `"Kategoria zgłoszenia nie jest aktywna"`

### TC-ALG-03: Auto-przydział priorytetu — HIGH
**Given:** resource metadane `{ defaultPriority: "HIGH" }`, request metadane poprawne  
**When:** evaluate()  
**Then:** `success = true`, reguły zawierają `"PRIORITY_AUTO_ASSIGN: HIGH"`

### TC-ALG-04: Auto-przydział priorytetu — CRITICAL
**Given:** resource metadane `{ defaultPriority: "CRITICAL" }`  
**When:** evaluate()  
**Then:** `success = true`, reguły zawierają `"PRIORITY_AUTO_ASSIGN: CRITICAL"`

### TC-ALG-05: Nieprawidłowy priorytet kategorii — błąd
**Given:** resource metadane `{ defaultPriority: "URGENT" }`  
**When:** evaluate()  
**Then:** `success = false`, błąd `"Nieprawidłowy lub brakujący domyślny priorytet kategorii"`

### TC-ALG-06: Brak statusu w zgłoszeniu — błąd
**Given:** requestMetadata bez klucza `status`  
**When:** evaluate()  
**Then:** `success = false`, błąd `"Status zgłoszenia jest wymagany"`

### TC-ALG-07: Nieprawidłowy status w zgłoszeniu — błąd
**Given:** requestMetadata `{ status: "UNKNOWN" }`  
**When:** evaluate()  
**Then:** `success = false`, błąd `"Nieprawidłowy status zgłoszenia: UNKNOWN"`

### TC-ALG-08: Poprawny status REPORTED — sukces
**Given:** requestMetadata `{ status: "REPORTED", tenantName: "Jan" }`  
**When:** evaluate()  
**Then:** `success = true`

### TC-ALG-09: Poprawny status IN_PROGRESS — sukces
**Given:** requestMetadata `{ status: "IN_PROGRESS", tenantName: "Jan" }`  
**When:** evaluate()  
**Then:** `success = true`

### TC-ALG-10: Poprawny status RESOLVED — sukces
**Given:** requestMetadata `{ status: "RESOLVED", tenantName: "Jan" }`  
**When:** evaluate()  
**Then:** `success = true`

### TC-ALG-11: Poprawny status CLOSED — sukces
**Given:** requestMetadata `{ status: "CLOSED", tenantName: "Jan" }`  
**When:** evaluate()  
**Then:** `success = true`

### TC-ALG-12: Brak zgłaszającego — błąd
**Given:** requestMetadata bez `tenantName`  
**When:** evaluate()  
**Then:** `success = false`, błąd `"Imię i nazwisko zgłaszającego jest wymagane"`

### TC-ALG-13: calculatedValue = 0
**Given:** Poprawne dane wejściowe  
**When:** evaluate()  
**Then:** `calculatedValue = 0`

### TC-ALG-14: Walidacja wszystkich pól jednocześnie
**Given:** Wszystkie pola poprawne  
**When:** evaluate()  
**Then:** `success = true`, breakdown zawiera reguły: RESOURCE_CHECK, CATEGORY, BUILDING, PRIORITY_AUTO_ASSIGN, STATUS_CHECK, TENANT_CHECK, VALIDATION_PASSED

## Backend — testy integracyjne DataInitializer

### TC-SEED-01: Init z pustą bazą
**Given:** Pusta baza danych, seed włączony  
**When:** DataInitializer.run()  
**Then:** Utworzono 2 użytkowników, 4 kategorie, 3 zgłoszenia

### TC-SEED-02: Init z istniejącymi danymi tego samego preset
**Given:** Istnieją już zasoby z metadanymi `categoryName`, `building`, `defaultPriority`  
**When:** DataInitializer.run()  
**Then:** Seed pominięty (log: "already matches the active profile; skipping")

### TC-SEED-03: Init z istniejącymi danymi innego preset
**Given:** Istnieją zasoby bez wymaganych metadanych  
**When:** DataInitializer.run()  
**Then:** Dane wyczyszczone i ponownie zasiane

## Backend — testy REST API

### TC-API-01: USER tworzy zgłoszenie — sukces
**Given:** Zalogowany jako USER  
**When:** POST /api/requests z `{ resourceId: 1, metadata: { title, description, status: "REPORTED", location, tenantName } }`  
**Then:** 201 Created, zgłoszenie przypisane do zalogowanego użytkownika

### TC-API-02: USER widzi tylko własne zgłoszenia
**Given:** Dwóch różnych USERów  
**When:** GET /api/requests  
**Then:** Każdy widzi tylko swoje zgłoszenia

### TC-API-03: ADMIN widzi wszystkie zgłoszenia
**Given:** Zalogowany jako ADMIN  
**When:** GET /api/requests  
**Then:** Widoczne wszystkie zgłoszenia wszystkich użytkowników

### TC-API-04: ADMIN zmienia status zgłoszenia
**Given:** Zalogowany jako ADMIN, zgłoszenie istnieje  
**When:** PUT /api/requests/{id} z `{ metadata: { ..., status: "IN_PROGRESS" } }`  
**Then:** 200 OK, status w metadanych zmieniony

### TC-API-05: USER nie może zmienić statusu cudzego zgłoszenia
**Given:** Zalogowany jako USER A, zgłoszenie należy do USER B  
**When:** PUT /api/requests/{id}  
**Then:** 403 Forbidden

### TC-API-06: Lista kategorii (resources) — sukces
**Given:** Istnieją aktywne kategorie  
**When:** GET /api/resources  
**Then:** 200 OK, lista kategorii z metadanymi

## Frontend — testy interfejsu (E2E / komponentów)

### TC-UI-01: Mieszkaniec tworzy zgłoszenie
**Given:** Zalogowany jako user@zpo.local  
**When:** Przechodzi do "Zgłoś problem", wypełnia formularz, wybiera kategorię, klika "Wyślij"  
**Then:** Zgłoszenie widoczne na liście "Moje zgłoszenia"

### TC-UI-02: Mieszkaniec widzi własne zgłoszenia
**Given:** Zalogowany jako user@zpo.local  
**When:** Przechodzi do "Moje zgłoszenia"  
**Then:** Widzi tylko 3 zgłoszenia utworzone przez siebie

### TC-UI-03: Administrator widzi panel zarządzania
**Given:** Zalogowany jako admin@zpo.local  
**When:** Przechodzi do "Panel administracji osiedla"  
**Then:** Widzi wszystkie zgłoszenia, może zmieniać ich status

### TC-UI-04: Walidacja formularza zgłoszenia
**Given:** Formularz zgłoszenia otwarty  
**When:** Próba wysłania bez wypełnienia wymaganych pól  
**Then:** Komunikaty walidacyjne, formularz nie wysłany

### TC-UI-05: Wyświetlanie statusu z kolorowaniem
**Given:** Lista zgłoszeń  
**Then:** Zgłoszony (żółty), W trakcie (niebieski), Rozwiązane (zielony), Zamknięty (szary)
