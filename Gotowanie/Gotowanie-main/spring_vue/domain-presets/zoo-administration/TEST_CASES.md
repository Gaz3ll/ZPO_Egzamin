# Test cases: zoo-administration

Te przypadki można pokazać ręcznie w UI/Swaggerze albo przenieść do testów jednostkowych algorytmu po zastosowaniu presetu.

1. Brak kolizji czasowej dla wybiegu
   - Zaplanuj zadanie na aktywnym wybiegu w wolnym przedziale czasu.
   - Oczekiwane: sukces, reguła `TIME_COLLISION_CHECK: brak kolizji`.

2. Wykrycie kolizji czasowej
   - Zaplanuj drugie zadanie dla tego samego wybiegu w nachodzącym czasie.
   - Oczekiwane: conflict, reguła `TIME_COLLISION_CHECK: kolizja`.

3. `dangerLevel=CRITICAL` zwiększa workload
   - Użyj wybiegu `Sawanna A`.
   - Oczekiwane: breakdown zawiera `dangerLevelMultiplier` z mnożnikiem większym niż `1.00`.

4. `priority=URGENT` zwiększa workload
   - Utwórz zadanie z priorytetem `URGENT`.
   - Oczekiwane: breakdown zawiera `priorityModifier` z mnożnikiem `1.60`.

5. Kwarantanna dodaje regułę bezpieczeństwa
   - Utwórz zadanie dla zasobu `Kwarantanna`.
   - Oczekiwane: breakdown zawiera `QUARANTINE_CHECK: active` oraz `quarantineRule`.

6. `cleaningDifficulty` wpływa na `calculatedValue`
   - Porównaj podobne zadanie dla sektora `LOW` i `EXTREME`.
   - Oczekiwane: `cleaningDifficultyMultiplier` zwiększa wynik dla trudniejszego sektora.

7. USER widzi tylko swoje zadania
   - Zaloguj się jako `user@zpo.local`.
   - Oczekiwane: widoczne są tylko requesty danego użytkownika.

8. ADMIN widzi wszystkie zadania
   - Zaloguj się jako `admin@zpo.local`.
   - Oczekiwane: panel admina pokazuje wszystkie zadania.

Obecne testy głównego projektu nadal pokrywają:

- generyczne działanie algorytmu,
- kolizje czasowe,
- capacity matching,
- `RequestAccessPolicy`,
- `AdminPolicy`.
