# Domain presets

`domain-presets/` zawiera gotowe zestawy plików, które zmieniają temat projektu bez zmiany generycznej architektury aplikacji.

Główny projekt nadal operuje na neutralnych nazwach:
- `Resource`
- `Request`
- `DomainAlgorithm`
- `DomainProfile`
- `DomainConfig`
- `ResourceController`
- `RequestController`
- `AdminResourceController`
- `AdminRequestController`
- `ResourceEntity`
- `RequestEntity`
- `ResourceService`
- `RequestService`

Presety podmieniają tylko konfigurację profilu domeny, seed danych, implementację algorytmu i teksty frontendu. Endpointy, encje, serwisy, kontrolery i komponenty pozostają te same.

## Dostępne presety

### 15 tematów ZPO (od wykładowcy)

| Lp | Preset | Temat | Algorytm |
|----|--------|-------|----------|
| 1 | `movie-ratings` | Ocena filmów 1-5 | Średnia ocen, top po gatunku |
| 2 | `conference-room-booking` | Rezerwacja salek konferencyjnych | Kolizje czasowe, pojemność, cena za godzinę |
| 3 | `fitness-classes` | Zapisy na zajęcia fitness | Limit miejsc, lista rezerwowa, promocja |
| 4 | `employee-scheduler` | Zarządzanie pracownikami | Grafiki, godziny, zadania, nadgodziny |
| 5 | `meal-planning-catering` | Planowanie posiłków catering | Kaloryczność, dieta, skalowanie porcji |
| 6 | `animal-shelter` | Schronisko dla zwierząt | Dopasowanie adopcyjne, status |
| 7 | `online-course-platform` | Platforma kursów online | Postęp %, ukończenie lekcji |
| 8 | `player-ranking` | System rankingowy graczy | Punkty rankingowe, historia meczów |
| 9 | `habit-tracker` | Habit tracker | Streak, % realizacji |
| 10 | `library-book-rental` | Wypożyczalnia książek | Dostępność kopii, kary za opóźnienie |
| 11 | `estate-issue-reporting` | Zgłoszenia administracji osiedla | Auto-priorytet, status zgłoszenia |
| 12 | `workout-tracker` | Rejestr treningów | Objętość, czas tygodniowy |
| 13 | `exam-study-planner` | Plan nauki na egzamin | Podział materiału na dni, dni powtórek |
| 14 | `office-plant-care` | Adopcja roślin biurowych | Terminarz podlewania/nawożenia |
| 15 | `mood-diary` | Dziennik nastroju | Średnia tygodniowa, rozkład nastrojów |

### Pozostałe presety (dodatkowe)

- `barber-appointments` - rezerwacje u barbera.
- `bowling-alley` - kręgielnia.
- `car-detailing` - auto detailing.
- `car-rental` - wypożyczalnia samochodów.
- `cinema-booking` - rezerwacja biletów w kinie.
- `construction-rental` - wypożyczalnia sprzętu budowlanego.
- `coworking-reservation` - rezerwacja biurek coworkingowych.
- `diet-catering` - catering dietetyczny.
- `dog-daycare` - przedszkole dla psów.
- `escape-room` - escape room.
- `home-cleaning` - sprzątanie domów.
- `language-tutoring` - korepetycje językowe.
- `mechanic-appointments` - umawianie wizyt w warsztacie.
- `mechanic-workshop` - warsztat samochodowy.
- `music-room-booking` - rezerwacja sal muzycznych.
- `parcel-locker` - system paczkomatów.
- `photo-studio` - studio fotograficzne.
- `ski-rental` - wypożyczalnia nart.
- `tattoo-studio` - studio tatuażu.
- `tennis-court` - korty tenisowe.
- `vet-appointments` - przychodnia weterynaryjna.
- `vr-arcade` - salon VR.
- `yacht-charter` - czarter jachtów.
- `zoo-administration` - administracja zoo.

Dodatkowo w katalogu głównym jest `TOPIC_BLUEPRINTS.md`: bank 20 gotowych tematów z mapowaniem `Resource`/`Request`, metadanymi, algorytmem, security i testami. To są specyfikacje do szybkiego zrobienia kolejnego presetu, a nie aktywne katalogi dla `apply-preset.sh`.

Każdy preset zawiera:

```text
backend/DomainProfileProvider.java
backend/DataInitializer.java
backend/DefaultDomainAlgorithm.java
frontend/domain.config.ts
README_PRESET.md
TEST_CASES.md
```

## Jak zastosować preset

Najprościej użyć skryptu z katalogu głównego projektu:

```bash
./scripts/apply-preset.sh car-rental
./scripts/apply-preset.sh habit-tracker
./scripts/apply-preset.sh exam-study-planner
./scripts/apply-preset.sh office-plant-care
./scripts/apply-preset.sh employee-scheduler
./scripts/apply-preset.sh meal-planning-catering
```

Skrypt działa dla każdego katalogu z `domain-presets/` — wystarczy podać jego nazwę.
Wywołany z nieistniejącą nazwą wypisze listę dostępnych presetów.

Skrypt przed nadpisaniem zapisuje kopię aktualnych plików w `.preset-backups/<timestamp>/`.

## Ręczna podmiana plików

Jeżeli nie chcesz używać skryptu, skopiuj cztery pliki wybranego presetu:

```bash
cp domain-presets/<preset>/backend/DomainProfileProvider.java backend/src/main/java/pl/zpo/app/domain/config/DomainProfileProvider.java
cp domain-presets/<preset>/backend/DataInitializer.java backend/src/main/java/pl/zpo/app/config/DataInitializer.java
cp domain-presets/<preset>/backend/DefaultDomainAlgorithm.java backend/src/main/java/pl/zpo/app/domain/algorithm/DefaultDomainAlgorithm.java
cp domain-presets/<preset>/frontend/domain.config.ts frontend/src/config/domain.config.ts
```

## Po zmianie presetu

1. Uruchom backend i frontend od nowa.
2. Jeżeli baza zawiera dane z poprzedniego tematu, wyczyść bazę albo użyj nowej. `DataInitializer` sieje dane tylko wtedy, gdy tabela użytkowników jest pusta.
3. Przejrzyj `README_PRESET.md` i `TEST_CASES.md` w katalogu wybranego presetu.

## Testy i build

Z katalogu głównego projektu:

```bash
cd backend && ./mvnw test
cd ../frontend && npm run build
```

Albo osobno:

```bash
(cd backend && ./mvnw test)
(cd frontend && npm run build)
```
