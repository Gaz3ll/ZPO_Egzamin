# Preset: zoo-administration

Temat: administracja zoo.

## Mapowanie na generyczny model

Ten preset nie dodaje klas typu `AnimalController`, `ZooController`, `FeedingController` ani nowych komponentów Vue. Cały projekt nadal działa przez neutralne klasy i endpointy.

Mapowanie:

- `Resource` = wybieg, sektor albo zasób operacyjny zoo.
- `Request` = zadanie administracyjne dotyczące wybiegu/sektora.
- `DomainAlgorithm` = ocena ryzyka, kolizji i szacowanego nakładu pracy.

Przykłady zadań:

- karmienie zwierząt,
- sprzątanie wybiegu,
- kontrola weterynaryjna,
- przeniesienie zwierzęcia,
- przegląd techniczny sektora.

## ResourceEntity w tym presecie

`ResourceEntity` oznacza wybieg/sektor zoo.

Pola wspólne:

- `name` - nazwa, np. `Sawanna A`.
- `description` - opis sektora.
- `type` - typ sektora, np. `ENCLOSURE`, `AQUARIUM`, `AVIARY`, `TERRARIUM`.
- `status` - `ACTIVE`, `INACTIVE`, `UNAVAILABLE`.
- `baseValue` - bazowy czas albo koszt obsługi.
- `capacityValue` - maksymalna liczba zwierząt albo pojemność sektora.
- `metadataJson` - szczegóły wybiegu.

Metadane zasobu:

- `animalSpecies`
- `animalCount`
- `dangerLevel`
- `feedingType`
- `cleaningDifficulty`
- `keeperZone`
- `isQuarantine`
- `lastInspectionDate`

## RequestEntity w tym presecie

`RequestEntity` oznacza zadanie administracyjne.

Pola wspólne:

- `ownerId` - pracownik/użytkownik zgłaszający zadanie.
- `resourceId` - wybieg/sektor.
- `status` - status zadania.
- `startAt` / `endAt` - planowany czas zadania.
- `quantity` - liczba zwierząt albo poziom obciążenia zadania.
- `calculatedValue` - szacowany nakład pracy/koszt.
- `metadataJson` - szczegóły zadania.
- `algorithmBreakdownJson` - rozbicie decyzji algorytmu.

Metadane zadania:

- `taskType`
- `priority`
- `requiresVet`
- `requiresTwoKeepers`
- `animalHealthRisk`
- `notes`

## Jak działa algorytm

`DefaultDomainAlgorithm` dla zoo:

1. Sprawdza aktywność wybiegu.
2. Sprawdza zakres `startAt` / `endAt`.
3. Wykrywa kolizje czasowe na tym samym wybiegu.
4. Sprawdza pojemność sektora, jeżeli request podaje `quantity`.
5. Sprawdza kwarantannę przez `isQuarantine`.
6. Sprawdza `dangerLevel`.
7. Dla `HIGH` i `CRITICAL` wymaga `requiresTwoKeepers=true`, bo algorytm nie zna roli osoby tworzącej request.
8. Dla transferu z kwarantanny wymaga `requiresVet=true`.
9. Dla krytycznego ryzyka zdrowotnego wymaga `requiresVet=true`.
10. Liczy `calculatedValue` jako szacowany workload.

Breakdown zawiera:

- `baseWorkload`
- `animalCountFactor`
- `cleaningDifficultyMultiplier`
- `dangerLevelMultiplier`
- `taskTypeModifier`
- `priorityModifier`
- `quarantineRule`
- `requiresTwoKeepers`
- `estimatedWorkload`
- `appliedRules`

## Jak zastosować preset

Z katalogu głównego projektu:

```bash
./scripts/apply-preset.sh zoo-administration
```

Ręcznie:

```bash
cp domain-presets/zoo-administration/backend/DomainProfileProvider.java backend/src/main/java/pl/zpo/app/domain/config/DomainProfileProvider.java
cp domain-presets/zoo-administration/backend/DataInitializer.java backend/src/main/java/pl/zpo/app/config/DataInitializer.java
cp domain-presets/zoo-administration/backend/DefaultDomainAlgorithm.java backend/src/main/java/pl/zpo/app/domain/algorithm/DefaultDomainAlgorithm.java
cp domain-presets/zoo-administration/frontend/domain.config.ts frontend/src/config/domain.config.ts
```

## Jak uruchomić projekt po zastosowaniu

```bash
docker compose up -d
cd backend
./mvnw spring-boot:run
```

W drugim terminalu:

```bash
cd frontend
npm install
npm run dev
```

Adresy:

- Frontend: <http://localhost:5173>
- Swagger: <http://localhost:8080/swagger-ui/index.html>

Konta demo:

- `admin@zpo.local` / `admin123`
- `operator@zpo.local` / `operator123`
- `user@zpo.local` / `user123`

## Co pokazać prowadzącemu

- Ten sam REST API i te same klasy działają jako administracja zoo.
- `Resource` to wybieg/sektor, `Request` to zadanie administracyjne.
- Pola specyficzne dla zoo są w `metadataJson`.
- Algorytm zwraca `calculatedValue` i `algorithmBreakdownJson`.
- USER widzi swoje zadania, ADMIN widzi wszystkie zadania.
- Preset jest nakładką konfiguracyjną, a nie przebudową architektury.
