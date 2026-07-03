# Test cases: dog-daycare

| # | Scenariusz | Wejście | Oczekiwany wynik |
|---|-----------|---------|------------------|
| 1 | Mały pies | `dogSize=SMALL` | `DOG_CAPACITY_WEIGHT: 1` |
| 2 | Średni pies | `dogSize=MEDIUM` | `DOG_CAPACITY_WEIGHT: 2` |
| 3 | Duży pies | `dogSize=LARGE` | `DOG_CAPACITY_WEIGHT: 3` |
| 4 | Brak punktów | `alreadyUsedPoints + dogCapacityWeight > capacityPoints` | `success=false`, `WEIGHTED_CAPACITY_CHECK: exceeded` |
| 5 | Niedozwolony rozmiar | Rozmiar spoza `acceptedDogSizes` | `success=false`, `DOG_SIZE_CHECK: rejected` |
| 6 | Opłaty dodatkowe | `needsMedication=true`, `extraWalk=true` | linie `medicationFee`, `extraWalkFee`, notatka `totalPrice` |
