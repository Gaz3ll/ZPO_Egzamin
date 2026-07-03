# TOPIC_BLUEPRINTS

Bank tematów awaryjnych do szybkiego przerobienia projektu na inny scenariusz ZPO.

Każdy temat trzyma tę samą architekturę:

- `ResourceEntity` = główny zasób, który można rezerwować / wykorzystywać / dopasowywać.
- `RequestEntity` = rezerwacja, zamówienie, zadanie albo zgłoszenie użytkownika.
- `metadata_json` = pola specyficzne dla tematu.
- `DefaultDomainAlgorithm` = logika tematu.
- REST API, security, encje, kontrolery i komponenty zostają neutralne.

Żeby z blueprintu zrobić pełny preset, utwórz katalog:

```text
domain-presets/<nazwa-tematu>/
  backend/DomainProfileProvider.java
  backend/DataInitializer.java
  backend/DefaultDomainAlgorithm.java
  frontend/domain.config.ts
  README_PRESET.md
  TEST_CASES.md
```

Najłatwiej skopiować istniejący preset, np. `domain-presets/mechanic-workshop`, zmienić pola, seed i algorytm.

---

## 1. Rezerwacja stanowiska w coworkingu

**Resource** = stanowisko pracy / gabinet / sala w coworkingu.  
**Request** = rezerwacja miejsca na zakres dat.

Resource metadata:
- `deskType`: `OPEN_SPACE`, `PRIVATE_OFFICE`, `MEETING_ROOM`
- `floor`
- `seatNumber`
- `hasMonitor`
- `hasWindow`
- `zone`

Request metadata:
- `clientName`
- `deskType`
- `companyName`
- `needsInvoice`
- `discountCode`

Algorytm:
- Sprawdza kolizje `startAt` / `endAt`.
- Filtruje dostępne stanowiska po `deskType`.
- Liczy cenę przez dni z zakresu.
- Weekend może mieć zniżkę.
- Rezerwacja powyżej 5 dni dostaje rabat.
- Do sumowania kosztu użyć logiki typu `stream().map(...).reduce(...)`.

Breakdown:
- `baseDailyPrice`
- `daysCount`
- `weekendDiscount`
- `longStayDiscount`
- `deskTypeMultiplier`
- `totalPrice`
- `appliedRules`

Security:
- Freelancer widzi swoje rezerwacje i faktury.
- Menedżer biura widzi zajętość całego obiektu i może anulować rezerwacje.

Testy:
- brak kolizji,
- kolizja dat,
- rabat weekendowy,
- rabat powyżej 5 dni,
- USER widzi swoje rezerwacje,
- ADMIN/OPERATOR widzi wszystkie.

---

## 2. Barber shop / salon fryzjerski

**Resource** = barber / fryzjer albo stanowisko fryzjerskie.  
**Request** = wizyta na konkretną usługę.

Resource metadata:
- `staffName`
- `specialization`
- `workdayStart`
- `workdayEnd`
- `services`
- `chairNumber`

Request metadata:
- `serviceType`: `HAIRCUT`, `BEARD`, `HAIRCUT_BEARD`, `COLORING`
- `serviceDurationMinutes`
- `customerName`
- `customerPhone`
- `preferredStaff`

Algorytm:
- Pobiera wizyty danego dnia.
- Mapuje je na zajęte bloki czasowe.
- Szuka przerwy dłuższej lub równej `serviceDurationMinutes`.
- Zwraca pierwszą wolną godzinę jako sugestię w breakdownie.
- Przy rezerwacji sprawdza, czy wybrany slot nie koliduje.

Breakdown:
- `serviceDuration`
- `selectedStaff`
- `firstAvailableSlot`
- `basePrice`
- `serviceModifier`
- `totalPrice`
- `appliedRules`

Security:
- Klient widzi swoje wizyty.
- Barber widzi swój kalendarz.
- Admin/właściciel widzi wszystko.

Testy:
- znajduje wolne 90 minut,
- ignoruje zbyt krótkie przerwy,
- kolizja wizyty,
- barber widzi tylko swój kalendarz,
- admin widzi wszystko.

---

## 3. Wynajem sprzętu narciarskiego / snowboardowego

**Resource** = element sprzętu: narty, deska, buty, kask.  
**Request** = zamówienie zestawu na termin wyjazdu.

Resource metadata:
- `equipmentType`: `SKIS`, `SNOWBOARD`, `BOOTS`, `HELMET`
- `lengthCm`
- `bootSize`
- `skillLevel`
- `condition`
- `warehouseCode`

Request metadata:
- `customerHeightCm`
- `customerWeightKg`
- `bootSize`
- `rideType`: `SKI`, `SNOWBOARD`
- `skillLevel`

Algorytm:
- Sprawdza dostępność w terminie.
- Dla nart liczy oczekiwaną długość: `height - 15`.
- Filtruje sprzęt niedopasowany.
- Wybiera najlepsze dopasowanie przez minimalną różnicę długości.
- Sprawdza buty po rozmiarze.

Breakdown:
- `targetLength`
- `selectedEquipment`
- `lengthDifference`
- `availabilityCheck`
- `baseRentalCost`
- `durationMultiplier`
- `totalPrice`

Security:
- Klient zarządza swoim zamówieniem.
- Serwisant widzi listę sprzętu do przygotowania.

Testy:
- wybór najlepiej dopasowanych nart,
- odrzucenie niedostępnego sprzętu,
- dopasowanie butów,
- koszt wynajmu za dni,
- serwisant widzi przygotowania.

---

## 4. Rezerwacja toru w kręgielni

**Resource** = tor bowlingowy.  
**Request** = rezerwacja gry dla grupy.

Resource metadata:
- `laneNumber`
- `hasKidsBumpers`
- `zone`
- `maxPlayers`
- `isVipLane`

Request metadata:
- `groupSize`
- `needsKidsBumpers`
- `customerName`
- `shoeRentalCount`
- `eventType`

Algorytm:
- Sprawdza dostępność torów w czasie.
- Jeżeli `groupSize <= 6`, wybiera jeden wolny tor.
- Jeżeli `groupSize > 6`, szuka dwóch sąsiadujących wolnych torów.
- Dla dzieci filtruje tory z bandami.
- Zwraca wybrane tory w breakdownie.

Breakdown:
- `groupSize`
- `requiredLanes`
- `selectedLanes`
- `kidsBumpers`
- `baseLanePrice`
- `shoeRentalFee`
- `totalPrice`

Security:
- Gracz widzi swoje rezerwacje.
- Obsługa widzi obłożenie torów na żywo.

Testy:
- grupa 5 osób dostaje 1 tor,
- grupa 10 osób dostaje 2 sąsiadujące tory,
- brak sąsiadujących torów daje conflict,
- bandy dla dzieci filtrują tory,
- obsługa widzi obłożenie.

---

## 5. Catering dietetyczny

**Resource** = plan/menu dzienne albo zestaw posiłków.  
**Request** = zamówienie boxów na zakres dni.

Resource metadata:
- `mealType`
- `calories`
- `allergens`
- `dietType`
- `deliveryDay`
- `protein`
- `carbs`
- `fat`

Request metadata:
- `targetCalories`
- `excludedAllergens`
- `dietType`
- `deliveryAddress`
- `deliveryDays`

Algorytm:
- Filtruje posiłki po alergenach.
- Filtruje po typie diety.
- Składa dzienny zestaw do celu kalorycznego.
- Sumuje kalorie przez redukcję.
- Jeśli brak zestawu, zwraca conflict albo alternatywę.

Breakdown:
- `targetCalories`
- `selectedMeals`
- `caloriesTotal`
- `excludedAllergens`
- `dietCompatibility`
- `dailyPrice`
- `totalPrice`

Security:
- Klient zarządza adresem i boxami.
- Dietetyk zarządza menu.
- Kurier widzi tylko listy przewozowe na dany dzień.

Testy:
- odrzuca alergeny,
- składa zestaw około 2000 kcal,
- brak posiłków zwraca conflict,
- kurier widzi tylko dostawy dnia,
- klient widzi swoje boxy.

---

## 6. Wypożyczalnia sprzętu budowlanego

**Resource** = sprzęt budowlany.  
**Request** = wynajem jednego albo kilku sprzętów.

Resource metadata:
- `equipmentCode`
- `equipmentType`
- `dailyRate`
- `deposit`
- `failureRisk`
- `requiresTransport`
- `weightKg`

Request metadata:
- `equipmentTypes`
- `rentalDays`
- `constructionAddress`
- `withTransport`
- `operatorNeeded`

Algorytm:
- Sprawdza dostępność sprzętu w terminie.
- Sumuje ceny dzienne wybranego sprzętu.
- Wybiera najwyższą kaucję przez logikę `max()`.
- Jeżeli transport zamówiony, dolicza albo obniża koszt według reguły.
- Zwraca listę sprzętu i kaucję.

Breakdown:
- `selectedEquipment`
- `rentalDays`
- `baseRentalCost`
- `transportModifier`
- `maxDeposit`
- `totalPrice`

Security:
- Klient widzi aktywne wynajmy.
- Magazynier zatwierdza zwroty i odblokowuje kaucje.

Testy:
- koszt za 3 dni,
- najwyższa kaucja ustawiona jako kaucja zamówienia,
- niedostępny sprzęt daje conflict,
- transport modyfikuje cenę,
- magazynier widzi zwroty.

---

## 7. Escape room

**Resource** = pokój escape room.  
**Request** = rezerwacja pokoju na godzinę.

Resource metadata:
- `roomName`
- `difficulty`
- `theme`
- `durationMinutes`
- `maxPlayers`
- `depositRequired`

Request metadata:
- `teamName`
- `playersCount`
- `preferredDifficulty`
- `preferredTheme`
- `depositPaid`

Algorytm:
- Sprawdza kolizję terminu dla pokoju.
- Sprawdza liczbę graczy.
- Jeżeli pokój zajęty, szuka alternatywy o tym samym poziomie trudności i wolnej godzinie.
- Alternatywę zapisuje jako notatkę w breakdownie.

Breakdown:
- `selectedRoom`
- `difficulty`
- `theme`
- `alternativeRoom`
- `deposit`
- `totalPrice`
- `appliedRules`

Security:
- Gracz widzi swoje rezerwacje.
- Game Master widzi harmonogram i zaliczki.

Testy:
- wolny pokój rezerwuje się poprawnie,
- zajęty pokój daje alternatywę,
- za dużo graczy daje conflict,
- Game Master widzi harmonogram dnia.

---

## 8. Myjnia samochodowa / detailing

**Resource** = stanowisko detailingu albo pracownik.  
**Request** = usługa myjni/detailingu dla auta.

Resource metadata:
- `workerName`
- `stationType`
- `services`
- `workdayStart`
- `workdayEnd`
- `zone`

Request metadata:
- `vehicleType`
- `selectedPackages`
- `plateNumber`
- `customerPhone`
- `requiresPickup`

Algorytm:
- Mapuje pakiety na czas trwania.
- Sumuje czas przez `mapToInt().sum()`.
- Filtruje pracowników/stanowiska z wystarczająco długim wolnym slotem.
- Sprawdza kolizję terminu.
- Wylicza cenę pakietów.

Breakdown:
- `selectedPackages`
- `totalDuration`
- `selectedWorker`
- `basePackagePrice`
- `vehicleTypeModifier`
- `totalPrice`

Security:
- Klient widzi status usługi.
- Pracownik widzi checklistę auta.

Testy:
- suma czasu pakietów,
- wolne okienko wystarcza,
- za krótki slot daje conflict,
- status usługi widoczny dla klienta.

---

## 9. Korepetycje językowe

**Resource** = lektor / nauczyciel.  
**Request** = rezerwacja lekcji albo pakietu lekcji.

Resource metadata:
- `teacherName`
- `languages`
- `levels`
- `tags`
- `hourlyRate`
- `workdayStart`
- `workdayEnd`

Request metadata:
- `language`
- `level`
- `goal`
- `lessonPackageSize`
- `preferredTags`

Algorytm:
- Filtruje lektorów po języku i poziomie.
- Liczy score dopasowania tagów.
- Sortuje lektorów komparatorem po score.
- Sprawdza wolny termin.
- Wycenia pakiet lekcji.

Breakdown:
- `language`
- `level`
- `matchingScore`
- `selectedTeacher`
- `packageSize`
- `totalPrice`

Security:
- Uczeń widzi swoje lekcje.
- Lektor zarządza swoim kalendarzem i stawkami.

Testy:
- filtr po języku,
- sortowanie po score,
- pakiet 5 lekcji,
- lektor widzi swój kalendarz.

---

## 10. Rezerwacja stanowisk VR

**Resource** = strefa VR / pula gogli / stanowisko VR.  
**Request** = rezerwacja gogli dla grupy.

Resource metadata:
- `zoneName`
- `totalHeadsets`
- `gameTypes`
- `maxPlayers`
- `hasMultiplayer`

Request metadata:
- `playersCount`
- `gameType`
- `customerName`
- `qrCode`

Algorytm:
- Pobiera rezerwacje dla godziny.
- Mapuje rezerwacje na liczbę gogli.
- Sumuje przez reduce.
- Sprawdza, czy `used + requested <= totalHeadsets`.
- Zwraca kod QR jako metadata albo notatkę.

Breakdown:
- `totalHeadsets`
- `currentlyReserved`
- `requestedHeadsets`
- `remainingHeadsets`
- `qrCode`
- `totalPrice`

Security:
- Klient widzi rezerwację i QR.
- Obsługa skanuje QR i wydaje sprzęt.

Testy:
- mieści się w puli,
- przekroczenie puli daje conflict,
- QR istnieje,
- obsługa widzi wydania.

---

## 11. Usługi sprzątania do domu

**Resource** = ekipa sprzątająca / rejon miasta.  
**Request** = zlecenie sprzątania mieszkania.

Resource metadata:
- `teamName`
- `cityZone`
- `workdayStart`
- `workdayEnd`
- `maxAreaM2`
- `baseHourlyRate`

Request metadata:
- `areaM2`
- `bathroomsCount`
- `extraOptions`
- `addressZone`
- `customerNotes`

Algorytm:
- Liczy bazowy czas z metrażu.
- Dodaje czas za łazienki.
- Opcje dodatkowe obsługuje przez Optional.
- `windowCleaning` mnoży czas przez wskaźnik trudności.
- Sprawdza wolny slot ekipy.

Breakdown:
- `baseTime`
- `bathroomTime`
- `extraOptionsTime`
- `difficultyMultiplier`
- `estimatedDuration`
- `totalPrice`

Security:
- Klient widzi wizyty i uwagi.
- Firma przypisuje pracowników do rejonów.
- Pracownik widzi tylko adresy zleceń przypisanych.

Testy:
- metraż wpływa na czas,
- opcje dodatkowe wpływają na koszt,
- rejon filtruje ekipy,
- pracownik widzi tylko swoje adresy.

---

## 12. Zajęcia grupowe fitness

**Resource** = zajęcia / trening w harmonogramie.  
**Request** = zapis uczestnika na zajęcia albo lista rezerwowa.

Resource metadata:
- `className`
- `trainerName`
- `roomName`
- `capacity`
- `difficulty`
- `startTime`

Request metadata:
- `participantName`
- `membershipType`
- `waitlistAccepted`
- `signupTime`

Algorytm:
- Liczy zajęte miejsca.
- Jeśli wolne miejsce istnieje, zapisuje jako aktywne.
- Jeśli pełno, zapisuje na listę rezerwową.
- Po anulowaniu wybiera pierwszą osobę z listy rezerwowej przez `min(signupTime)`.

Breakdown:
- `capacity`
- `occupiedSeats`
- `waitlistPosition`
- `promotedFromWaitlist`
- `totalPrice`

Security:
- Klubowicz widzi swoje treningi.
- Trener widzi listę obecności.
- Admin dodaje harmonogram.

Testy:
- wolne miejsce,
- pełna grupa -> lista rezerwowa,
- promocja pierwszego z listy,
- trener widzi obecność.

---

## 13. Schronisko - adopcje i spacery zapoznawcze

**Resource** = podopieczny / pies albo slot spotkania.  
**Request** = wniosek adopcyjny i termin spaceru.

Resource metadata:
- `animalName`
- `size`
- `age`
- `temperament`
- `goodWithKids`
- `goodForApartment`
- `volunteerId`

Request metadata:
- `housingType`
- `hasChildren`
- `experienceLevel`
- `preferredSize`
- `meetingNotes`

Algorytm:
- Filtruje psy po ankiecie.
- Odrzuca ryzykowne dopasowania.
- Jeśli lista pusta, zwraca Optional empty i sugestię kontaktu z behawiorystą.
- Rezerwuje termin spaceru.

Breakdown:
- `matchingAnimals`
- `rejectedReasons`
- `selectedAnimal`
- `behavioristSuggestion`
- `meetingSlot`

Security:
- Użytkownik widzi status adopcji.
- Wolontariusz widzi spacery ze swoimi podopiecznymi.

Testy:
- filtr blok + małe dzieci,
- brak dopasowań,
- wolontariusz widzi swoje spacery,
- użytkownik widzi swój wniosek.

---

## 14. Warsztat samochodowy - umawianie naprawy

Ten temat jest już zaimplementowany jako aktywny preset `mechanic-workshop`.

**Resource** = stanowisko serwisowe.  
**Request** = zlecenie naprawy.

Algorytm wersji rozszerzonej:
- Dostaje listę części.
- Filtruje części dostępne na magazynie.
- Dla niedostępnych wybiera najpóźniejszą datę dostawy przez `max()`.
- Wylicza najwcześniejszą datę oddania auta.
- Generuje kosztorys.

Security:
- Klient widzi historię napraw swojego auta.
- Mechanik zmienia statusy: diagnoza, czekam na części, zrobione.

Testy:
- części dostępne od razu,
- brak części przesuwa datę oddania,
- kosztorys wymaga akceptacji,
- mechanik zmienia status.

---

## 15. Sale prób dla zespołów muzycznych

**Resource** = sala prób.  
**Request** = rezerwacja sali i sprzętu.

Resource metadata:
- `roomType`
- `hasPiano`
- `hasDrums`
- `availableEquipment`
- `doorCode`
- `hourlyRate`

Request metadata:
- `bandName`
- `requestedEquipment`
- `membersCount`
- `needsDoorCode`

Algorytm:
- Sprawdza kolizję sali.
- Zbiera sprzęt zajęty w tym samym czasie.
- Porównuje z `requestedEquipment`.
- Jeśli zbiory nachodzą na siebie, zwraca conflict.
- W przeciwnym razie rezerwuje salę i sprzęt.

Breakdown:
- `requestedEquipment`
- `occupiedEquipment`
- `equipmentConflicts`
- `roomPrice`
- `equipmentFee`
- `totalPrice`

Security:
- Muzyk widzi kod do drzwi.
- Właściciel studia widzi obłożenie i sprzęt.

Testy:
- sala wolna,
- sprzęt zajęty daje conflict,
- kod drzwi widoczny po rezerwacji,
- właściciel widzi obłożenie.

---

## 16. Studio tatuażu

**Resource** = tatuażysta.  
**Request** = konsultacja albo sesja tatuażu.

Resource metadata:
- `artistName`
- `styles`
- `hourlyRate`
- `workdayStart`
- `workdayEnd`
- `portfolioTags`

Request metadata:
- `tattooStyle`
- `sizeCm2`
- `bodyPlacement`
- `referenceImageUrl`
- `sessionType`

Algorytm:
- Styl daje mnożnik czasu.
- Rozmiar przelicza się na bazowy czas.
- Szuka najbliższego dnia z ciągłym wolnym slotem.
- Przy dużych projektach rezerwuje cały dzień.

Breakdown:
- `baseDuration`
- `styleMultiplier`
- `sizeModifier`
- `selectedArtist`
- `firstAvailableSlot`
- `deposit`
- `totalPrice`

Security:
- Klient widzi projekty i wiadomości.
- Artysta widzi tylko swoje sesje i referencje.

Testy:
- rozmiar wpływa na czas,
- styl realizm zwiększa czas,
- najbliższy wolny slot,
- artysta widzi swoje sesje.

---

## 17. Korty tenisowe

**Resource** = kort tenisowy.  
**Request** = rezerwacja kortu.

Resource metadata:
- `courtNumber`
- `surfaceType`
- `isIndoor`
- `hasLighting`
- `baseHourlyRate`

Request metadata:
- `surfaceType`
- `needsLighting`
- `membershipNumber`
- `paymentStatus`

Algorytm:
- Sprawdza kolizję terminu.
- Filtruje po nawierzchni.
- Po godzinie 18:00 dodaje opłatę za światło.
- W zimie dodaje opłatę za ogrzewanie balonu.
- Anuluje nieopłacone rezerwacje po czasie.

Breakdown:
- `basePrice`
- `lightingFee`
- `winterHeatingFee`
- `membershipDiscount`
- `totalPrice`

Security:
- Gracz widzi karnety i rezerwacje.
- Rejestracja kortów anuluje nieopłacone rezerwacje.

Testy:
- dopłata po 18:00,
- dopłata zimowa,
- brak płatności -> anulowanie,
- gracz widzi swoje rezerwacje.

---

## 18. Wynajem jachtów / żaglówek

**Resource** = jednostka pływająca.  
**Request** = czarter na termin.

Resource metadata:
- `boatName`
- `boatType`
- `requiresLicense`
- `homePort`
- `deposit`
- `dailyRate`

Request metadata:
- `pickupPort`
- `returnPort`
- `licenseNumber`
- `skipperName`
- `depositPaid`

Algorytm:
- Sprawdza dostępność jednostki.
- Jeśli `requiresLicense=true`, używa Optional do sprawdzenia licencji.
- Brak licencji daje conflict.
- Liczy cenę za dni i kaucję.

Breakdown:
- `requiresLicense`
- `licenseCheck`
- `rentalDays`
- `deposit`
- `portFee`
- `totalPrice`

Security:
- Sternik widzi czarter i kaucję.
- Bosmanat zatwierdza odbiór/oddanie.

Testy:
- jednostka bez patentu,
- wymagana licencja i brak licencji,
- koszt weekendu,
- bosmanat widzi odbiory.

---

## 19. Sesje zdjęciowe / studio fotograficzne

**Resource** = studio / sala fotograficzna.  
**Request** = rezerwacja przestrzeni i sprzętu.

Resource metadata:
- `roomName`
- `cycloramaColor`
- `availableEquipment`
- `hourlyRate`
- `cleaningBufferMinutes`

Request metadata:
- `photographerName`
- `neededEquipment`
- `shootType`
- `invoiceRequired`

Algorytm:
- Sprawdza kolizję sali.
- Sprawdza dostępność lamp i akcesoriów.
- Generuje raport sprzętu na godzinę przez grupowanie po sali.
- Dodaje bufor sprzątania.

Breakdown:
- `room`
- `neededEquipment`
- `equipmentReport`
- `cleaningBuffer`
- `basePrice`
- `totalPrice`

Security:
- Fotograf rezerwuje i płaci.
- Obsługa widzi sprzęt do przygotowania i sprzątanie.

Testy:
- konflikt sali,
- konflikt sprzętu,
- raport grupowany po sali,
- bufor sprzątania.

---

## 20. Przedszkole dla psów

**Resource** = strefa dzienna / grupa opieki.  
**Request** = pobyt psa w daycare.

Resource metadata:
- `zoneName`
- `dailySlotLimit`
- `acceptedDogSizes`
- `hasMedicationSupport`
- `outdoorWalks`

Request metadata:
- `dogName`
- `dogSize`
- `dogWeightKg`
- `needsMedication`
- `feedingNotes`
- `extraOptions`

Algorytm:
- Mapuje rozmiar psa na wagę slotu: mały = 1, duży = 2.
- Sumuje obciążenie aktualnych rezerwacji.
- Sprawdza, czy nie przekracza limitu dziennego.
- Opcje dodatkowe doliczają cenę.

Breakdown:
- `dogSlotWeight`
- `currentLoad`
- `dailySlotLimit`
- `extraOptions`
- `basePrice`
- `totalPrice`

Security:
- Właściciel psa widzi harmonogram dnia.
- Pracownik widzi uwagi żywieniowe i medyczne.

Testy:
- mały pies zajmuje 1 slot,
- duży pies zajmuje 2 sloty,
- przekroczenie limitu daje conflict,
- leki widoczne dla pracownika,
- właściciel widzi swoje rezerwacje.

---

## Szybki wzór obrony hermetyzacji danych

Na backendzie zawsze sprawdzaj właściciela danych:

```java
requestRepository.findById(id)
        .filter(request -> request.getOwnerId().equals(currentUser.id()) || currentUser.isAdmin())
        .orElseThrow(() -> new ForbiddenException("Brak dostępu"));
```

W projekcie tę odpowiedzialność pełnią klasy policy:

- `RequestAccessPolicy`
- `AdminPolicy`

To jest dobry argument na prezentacji: UI nie jest jedyną blokadą, autoryzacja jest wymuszana w backendzie.
