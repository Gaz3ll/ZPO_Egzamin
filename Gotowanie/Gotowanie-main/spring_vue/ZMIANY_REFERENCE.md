# Mapa zmian projektu - co gdzie zmienić na inny temat

## Szybkie linki
- **Swagger (API):** http://localhost:8080/swagger-ui/index.html
- **Frontend:** http://localhost:5173
- **Backend:** http://localhost:8080

## BACKEND (Java) - 4 główne pliki

### 1. `backend/src/main/java/pl/zpo/app/domain/config/DomainProfileProvider.java`
**Co zmieniasz:** Nazwy apki, nazwy zasobu/zgłoszenia, pola formularza, typ algorytmu

```java
// ZMIANA NAZW:
"Employee Scheduler"  →  "Adopcja psów"           // appName
"Pracownik"           →  "Pies"                    // resource singular
"Pracownicy"          →  "Psy"                     // resource plural
"Wpis grafiku"        →  "Adopcja"                 // request singular
"Zmiany i zadania"    →  "Adopcje"                 // request plural
"PLN"                 →  "PLN" / "EUR"             // waluta

// ZMIANA PÓL ZASOBU (resourceFields):
new DomainFieldConfig("position", "Stanowisko", FieldType.TEXT, true, ...)
→ new DomainFieldConfig("breed", "Rasa", FieldType.SELECT, true, List.of("Labrador","Owczarek"), ...)

// ZMIANA PÓL ZGŁOSZENIA (requestFields):
new DomainFieldConfig("shiftType", "Typ zmiany", FieldType.SELECT, true, ...)
→ new DomainFieldConfig("adopterEmail", "Email", FieldType.TEXT, true, ...)

// ZMIANA TRYBU:
AlgorithmMode.VALUE_CALCULATION_ONLY   // bez czasu, tylko wartość (sklep, biblioteka)
AlgorithmMode.TIME_AVAILABILITY_AND_CALCULATION  // z kolizjami czasu (grafik, sale)

// ZMIANA JEDNOSTKI:
PricingUnit.FLAT     // stała cena
PricingUnit.PER_HOUR // za godzinę
PricingUnit.PER_DAY  // za dzień
PricingUnit.PER_UNIT // za sztukę

// ZMIANA WYMAGAŃ:
requiresTimeWindow: true   // daty obowiązkowe
requiresTimeWindow: false  // bez dat (sklep, biblioteka, oceny)
```

### 2. `backend/src/main/java/pl/zpo/app/domain/algorithm/DefaultDomainAlgorithm.java`
**Co zmieniasz:** Logika walidacji i kalkulacji

```java
// DODAJ NOWY BLOK DLA SWOJEGO TEMATU:

private boolean isDogAdoption(DomainAlgorithmInput input) {
    return hasAny(input.requestMetadata(), "adopterEmail", "breed")
        || hasAny(input.resource().getMetadata(), "breed", "age", "size");
}

private DomainAlgorithmResult evaluateDogAdoption(DomainAlgorithmInput input) {
    // 1. Sprawdź czy zasób aktywny
    if (resource.getStatus() != ResourceStatus.ACTIVE) return failure(...);
    
    // 2. Twoja walidacja:
    String breed = readString(resource.getMetadata(), "breed");
    Integer age = readInteger(resource.getMetadata(), "age");
    
    // 3. Zwróć sukces lub błędy:
    if (errors.isEmpty()) return DomainAlgorithmResult.success(null, resource.getId(), breakdown);
    return DomainAlgorithmResult.failure(errors, breakdown);
}

// DODAJ WYWOŁANIE W evaluate():
if (isDogAdoption(input)) return evaluateDogAdoption(input);
```

### 3. `backend/src/main/java/pl/zpo/app/config/DataInitializer.java`
**Co zmieniasz:** Dane demo

```java
// ZMIEŃ NAZWY METOD I DANYCH:
saveEmployee(...) → saveDog("Burek", "Owczarek", 3, "Duży")
seedShift(...)    → seedAdoption(user, burek, "jan@wp.pl", "123456789")

// ZMIEŃ METADANE:
Map<String, Object> meta = new HashMap<>();
meta.put("breed", "Owczarek");     // zamiast "position"
meta.put("age", 3);                // zamiast "hourlyRate"
meta.put("size", "Duży");          // zamiast "department"
```

### 4. `backend/src/main/java/pl/zpo/app/security/SecurityConfig.java`
**Co zmieniasz:** Uprawnienia (linia 69-75)
```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")   // tylko admin
.requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "OPERATOR")  // admin+operator
.requestMatchers("/api/**").authenticated()           // każdy zalogowany
.requestMatchers("/api/**").permitAll()               // każdy (nawet niezalogowany)
```

---

## FRONTEND (Vue) - 1 główny plik + opcjonalne

### 1. `frontend/src/config/domain.config.ts`
**Co zmieniasz:** Wszystkie napisy w UI, pola formularza, statusy

```ts
// ZMIANA NAZW:
appName: 'Adopcja psów'
currency: 'PLN'
timeMode: 'none'           // 'slots' | 'range' | 'none'

// ZMIANA NAZW ZASOBU:
resource: { singular: 'Pies', plural: 'Psy' }
request:  { singular: 'Adopcja', plural: 'Adopcje' }

// ZMIANA PÓL FORMULARZA ZASOBU:
fields: [
  { key: 'breed', label: 'Rasa', type: 'SELECT', required: true, 
    options: ['Labrador','Owczarek','Buldog'], helpText: 'Rasa psa' },
  { key: 'age', label: 'Wiek', type: 'NUMBER', required: true, ... },
]

// ZMIANA PÓL FORMULARZA ZGŁOSZENIA:
fields: [
  { key: 'adopterEmail', label: 'Email', type: 'TEXT', required: true, ... },
]

// ZMIANA WSZYSTKICH LABELI W UI:
labels: {
  resources: 'Psy do adopcji',
  myRequests: 'Moje adopcje',
  createRequest: 'Adoptuj psa',
  adminResources: 'Zarządzaj psami',
  adminRequests: 'Wszystkie adopcje',
  calendar: 'Kalendarz',      // '' = ukryj kalendarz
}

// ZMIANA KOLORÓW STATUSÓW:
requestStatusMeta: {
  PENDING: { label: 'Oczekująca', classes: 'bg-amber-500/10 text-amber-400' },
  COMPLETED: { label: 'Adoptowany', classes: 'bg-brand-500/10 text-brand-400' },
}
```

### 2. `frontend/src/config/tailwind.config.js` (opcjonalnie - kolory)
```js
colors: { brand: { 600: '#059669' } }  // zielony zamiast niebieskiego
```

### 3. `frontend/src/style.css` (opcjonalnie - ciemny/jasny motyw)
```css
body { @apply bg-gray-950 text-gray-200; }  // ciemny
body { @apply bg-white text-gray-900; }      // jasny
```

### 4. Zdjęcia / avatary
```bash
# Wrzuć zdjęcia do:
frontend/public/photos/

# Nazwy plików (ID = numer z bazy danych):
employee_1.jpg    # Anna Nowak (grafik, ID=1)
employee_2.jpg    # Piotr Wiśniewski (grafik, ID=2)
dog_1.jpg         # Burek (adopcja psów, ID=1)
room_1.jpg        # Sala A (sale konferencyjne, ID=1)
product_1.jpg     # Słuchawki (sklep, ID=1)

# Gdzie zmienić ścieżkę (2 miejsca):
# 1. ResourceCard.vue ~linia 22:
const photoUrl = computed(() => `/photos/employee_${props.resource.id}.jpg`)
# Zmień na:
const photoUrl = computed(() => `/photos/dog_${props.resource.id}.jpg`)

# 2. ResourceDetailsPage.vue ~linia 22: to samo

# Jak nie ma zdjęcia → pokazuje emoji 👤 (fallback automatyczny)
```

## PRZYKŁAD: Sklep → Adopcja psów (co na co)

| Co | Sklep | Adopcja psów |
|----|-------|-------------|
| `appName` | `'Sklep internetowy'` | `'Adopcja psów'` |
| `resource.singular` | `'Produkt'` | `'Pies'` |
| `request.singular` | `'Zamówienie'` | `'Adopcja'` |
| resource field 1 | `price, NUMBER` | `breed, SELECT` |
| resource field 2 | `category, SELECT` | `age, NUMBER` |
| resource field 3 | `stock, NUMBER` | `size, SELECT` |
| request field 1 | `customerName, TEXT` | `adopterEmail, TEXT` |
| request field 2 | `quantity, NUMBER` | `adopterPhone, TEXT` |
| `labels.resources` | `'Produkty'` | `'Psy do adopcji'` |
| `labels.createRequest` | `'Zamów produkt'` | `'Adoptuj psa'` |
| `AlgorithmMode` | `VALUE_CALCULATION_ONLY` | `VALUE_CALCULATION_ONLY` |
| `PricingUnit` | `PER_UNIT` | `FLAT` |
| `requiresTimeWindow` | `false` | `false` |
| Algorytm | sprawdza stock ≥ qty | sprawdza status=ACTIVE |

---

## MINUTNIK - nowy projekt z istniejącego presetu

| # | Krok | Minuty |
|---|------|--------|
| 1 | `DomainProfileProvider.java` - nazwy + pola | 5 |
| 2 | `DefaultDomainAlgorithm.java` - logika | 5 |
| 3 | `DataInitializer.java` - dane demo | 5 |
| 4 | `domain.config.ts` - UI labele + pola | 5 |
| 5 | Zdjęcia (opcjonalnie) | 2 |
| **Razem** | | **~22 min** |
