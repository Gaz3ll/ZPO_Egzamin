# Swagger - co pokazać na egzaminie

Otwórz: http://localhost:8080/swagger-ui/index.html

---

## 1. Login przez Swagger

```
POST /api/auth/login → "Try it out" → wpisz:
{
  "email": "admin@zpo.local",
  "password": "admin123"
}
→ Execute → skopiuj token z odpowiedzi

Odpowiedź:
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": { "id": 1, "name": "Administrator", "email": "admin@zpo.local", "role": "ADMIN" }
  }
}
```

## 2. Authorize (kluczowe!)

```
Kliknij zielony przycisk "Authorize" na górze strony
Wklej: Bearer eyJhbGciOiJIUzI1NiJ9... (token z kroku 1)
→ Authorize → Close
```

Teraz kłódki przy endpointach są zamknięte = autoryzacja działa.

## 3. Pobierz listę zasobów

```
GET /api/resources → Execute
→ JSON z listą pracowników/sal/produktów
```

## 4. Stwórz zgłoszenie przez API

```
POST /api/requests → Try it out → wpisz:
{
  "resourceId": 1,
  "startAt": "2026-07-03T05:00:00Z",
  "endAt": "2026-07-03T13:00:00Z",
  "metadata": {
    "shiftType": "MORNING",
    "taskName": "Test ze Swaggera"
  }
}
→ Execute → zobacz breakdown algorytmu:

{
  "success": true,
  "data": {
    "id": 25,
    "status": "PENDING",
    "calculatedValue": 256.00,
    "algorithmBreakdown": {
      "lines": [
        { "label": "baseCost", "amount": 256.00, "detail": "8h × 32.00 PLN/h" }
      ],
      "appliedRules": [
        "RESOURCE_CHECK: ok",
        "SHIFT_COLLISION_CHECK: brak kolizji",
        "BASE_COST: 8h × 32.00 PLN/h",
        "TOTAL_COST: 256.00"
      ],
      "total": 256.00,
      "currency": "PLN"
    }
  }
}
```

## 5. Admin - zmień status (opcjonalnie)

```
PATCH /api/admin/requests/{id}/status → Try it out → wpisz id i:
{ "status": "CONFIRMED" }
→ Execute → status zmieniony
```

## 6. Admin - usuń zgłoszenie (opcjonalnie)

```
DELETE /api/admin/dental/appointments/{id} → Try it out → wpisz id
→ Execute → usunięte
```

---

## Token JWT - jak pokazać

1. W apce: F12 → Application → Local Storage → "zpo_token"
2. Skopiuj token
3. Otwórz https://jwt.io
4. Wklej → zobacz rozkodowane:
```json
{
  "sub": "1",
  "email": "admin@zpo.local",
  "role": "ADMIN",
  "iss": "zpo-app",
  "iat": ...,
  "exp": ...
}
```
