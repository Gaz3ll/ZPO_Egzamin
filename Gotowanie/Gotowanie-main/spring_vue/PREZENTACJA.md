# Co pokazać na egzaminie / obronie

## Punktacja (szacowana)

| Kategoria | Pkt | Co pokazujesz |
|-----------|-----|---------------|
| UI + Algorytm | ~60% | Działająca apka, tworzenie zgłoszeń, breakdown |
| Security | 4-5 | Role (admin/user), JWT tokeny, BCrypt |
| REST API | 1-2 | Swagger, endpointy |
| Baza danych | 1-2 | Tabele, JSONB metadata |
| Testy | 1-2 | `./mvnw test` |

---

## 1. UI + Algorytm (60% - najważniejsze)

### Co pokazać:
```
1. Zaloguj się jako admin@zpo.local / admin123
2. Pokaż dashboard z nadchodzącymi zmianami
3. Przejdź do "Pracownicy" → pokaż listę
4. "Dodaj wpis do grafiku" → 
   - Wybierz pracownika
   - Wybierz datę w kalendarzu
   - Kliknij zmianę poranną (7-15) lub wieczorną (15-23)
   - Wpisz zadanie, notatki
   - Pokaż szacowany koszt
   - Kliknij "Dodaj wpis do grafiku"
5. POKAŻ BREAKDOWN ALGORYTMU (kluczowe!):
   - "Szczegóły" → zobacz rozbicie: 8h × 32 PLN = 256 PLN
   - Jeśli wieczorna → +10% = 281.60 PLN
6. Wyloguj się, zaloguj jako user@zpo.local
   - Pokaż że user widzi tylko swoje zmiany
   - Nie widzi panelu admina
7. Admin → Kalendarz → kliknij dzień → zobacz kto pracuje
```

### Gdzie to w kodzie:
- Algorytm: `backend/.../domain/algorithm/DefaultDomainAlgorithm.java`
- UI formularz: `frontend/src/components/RequestForm.vue`
- Konfiguracja: `frontend/src/config/domain.config.ts`

---

## 2. Security (4-5 pkt)

### Co pokazać:
```
1. SPRING SECURITY CONFIG:
   - Otwórz SecurityConfig.java linia 69-75
   - Pokaż: /api/admin/** → hasRole("ADMIN")
   - Pokaż: /api/admin/requests/** → hasAnyRole("ADMIN","OPERATOR")

2. JWT TOKENY:
   - F12 → Application → Local Storage → "zpo_token"
   - Skopiuj token → wklej na jwt.io → zobacz payload (id, email, role)

3. BCrypt:
   - Otwórz PasswordService.java linia 21
   - passwordEncoder.encode(rawPassword) → BCrypt hash
   - Pokaż w bazie: SELECT password_hash FROM users → $2a$10$...

4. POLICY (kto co może):
   - Otwórz RequestAccessPolicy.java
   - canRead(): ADMIN/OPERATOR wszystko, USER tylko swoje
   - canManage(): tylko ADMIN/OPERATOR
   - canCancel(): właściciel lub ADMIN

5. DEMO:
   - Zaloguj USER → spróbuj wejść na /admin/resources → 403
   - USER próbuje zmienić status cudzego zgłoszenia → 403
```

### Gdzie to w kodzie:
- `backend/.../security/SecurityConfig.java` - reguły dostępu
- `backend/.../security/JwtService.java` - tworzenie JWT
- `backend/.../security/PasswordService.java` - BCrypt
- `backend/.../domain/policy/RequestAccessPolicy.java` - uprawnienia

---

## 3. REST API + Swagger (1-2 pkt)

### Co pokazać:
```
1. Otwórz http://localhost:8080/swagger-ui/index.html
2. Pokaż listę wszystkich endpointów:
   - POST /api/auth/login
   - GET /api/resources
   - POST /api/requests
   - GET /api/admin/resources
   - PATCH /api/admin/requests/{id}/status
3. Kliknij "Authorize" → wklej token JWT
4. Wykonaj testowy request:
   - GET /api/resources → zobacz JSON z listą
   - POST /api/requests → stwórz nowe zgłoszenie przez Swagger
```

### Struktura JSON (co zwraca API):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "resourceName": "Anna Nowak",
    "status": "CONFIRMED",
    "startAt": "2026-07-03T05:00:00Z",
    "calculatedValue": 256.00,
    "metadata": { "shiftType": "MORNING", "taskName": "Obsługa kasy" },
    "algorithmBreakdown": {
      "lines": [{ "label": "baseCost", "amount": 256.00, "detail": "8h × 32.00 PLN/h" }],
      "appliedRules": ["RESOURCE_CHECK: ok", "SHIFT_COLLISION_CHECK: brak kolizji"],
      "total": 256.00
    }
  }
}
```

---

## 4. Baza danych (1-2 pkt)

### Co pokazać:
```sql
-- 1. Struktura (3 tabele):
docker exec -it zpo-app-postgres psql -U zpo_user -d zpo_app

\d users       -- id, name, email, password_hash, role
\d resources   -- id, name, type, status, base_value, metadata_json (JSONB!)
\d requests    -- id, owner_id, resource_id, status, start_at, end_at, 
               -- calculated_value, metadata_json (JSONB), algorithm_breakdown_json (JSONB)

-- 2. JSONB - elastyczne metadane (kluczowe!):
SELECT name, metadata_json FROM resources;
-- Anna Nowak | {"position": "Kasjerka", "department": "SALES", "hourlyRate": "32.00", ...}

-- 3. Algorithm breakdown zapisany jako JSON:
SELECT id, algorithm_breakdown_json FROM requests WHERE id = 1;
-- {"lines": [...], "appliedRules": [...], "total": 256.00}

-- 4. Hasła są hashowane (BCrypt):
SELECT email, password_hash FROM users;
-- admin@zpo.local | $2a$10$N9qo8uLOickgx2ZMRZoMye...
```

### Mocny punkt: "JSONB pozwala zmieniać schemat domenowy bez migracji bazy"

---

## 5. Testy (1-2 pkt)

### Lokalizacja: `backend/src/test/java/pl/zpo/app/`

| Test | Co testuje | Kategoria |
|------|-----------|-----------|
| `TimeCollisionDetectorTest` | Kolizje czasowe (nachodzenie, styk) | Algorytm |
| `CapacityMatcherTest` | Pojemność (fit/overflow) | Algorytm |
| `RequestAccessPolicyTest` | USER widzi tylko swoje, ADMIN wszystko | Security |
| `AdminPolicyTest` | ADMIN zarządza, USER nie | Security |
| `DefaultDomainAlgorithmTest` | Algorytm: kolizje, koszt, błędy | Algorytm |

### Jak odpalić (z pominięciem test-kompilacji):
```bash
cd backend

# NIE działa przez maven.test.skip=true w pom.xml
# Trzeba chwilowo usunąć lub odpalić ręcznie:

# Opcja 1: odpal tylko konkretny test
./mvnw test "-Dtest=TimeCollisionDetectorTest" "-Dmaven.test.skip=false"

# Opcja 2: usuń <maven.test.skip> z pom.xml, odpal ./mvnw test, przywróć
```

### Co pokazać:
```
1. ./mvnw test → 15+ testów przechodzi
2. Pokaż jeden test, np TimeCollisionDetectorTest:
   - "touchingDoesNotCollide" - koniec = początek → OK
   - "overlapCollides" - nachodzą → błąd
3. Pokaż RequestAccessPolicyTest:
   - "userCanReadOwn" → true
   - "userCannotReadOthers" → false
   - "adminCanReadAny" → true
```

---

## 6. Zdjęcia / avatary

```
Gdzie wrzucić zdjęcia:
  frontend/public/photos/

Nazwy plików (ID = numer pracownika z bazy):
  employee_1.jpg   → Anna Nowak (ID=1)
  employee_2.jpg   → Piotr Wiśniewski (ID=2)
  dog_1.jpg        → dla adopcji psów

Gdzie zmienić ścieżkę:
  ResourceCard.vue linia ~22:
  const photoUrl = computed(() => `/photos/employee_${props.resource.id}.jpg`)
  const photoUrl = computed(() => `/photos/dog_${props.resource.id}.jpg`)

ResourceDetailsPage.vue linia ~22: to samo
```

---

## Ściąga - co pokazać w 5 minut

| Minuta | Co |
|--------|----|
| 0-1 | Logowanie admin → dashboard |
| 1-2 | Dodaj wpis do grafiku → zobacz breakdown algorytmu |
| 2-3 | Wyloguj → login user → pokaż że nie widzi admina (403) |
| 3-4 | Swagger → pokaż endpointy → wykonaj request |
| 4-5 | Baza: `SELECT * FROM resources` → pokaż JSONB metadata |
| 5+ | Testy: `./mvnw test` → 15+ passing |
