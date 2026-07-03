# Preset: dog-daycare

Przedszkole dla psów.

`Resource` to strefa opieki, a `Request` to pobyt psa. Algorytm liczy pojemność wagowo: mały pies = 1 slot, średni = 2 sloty, duży = 3 sloty. Dodatkowo sprawdza akceptowane rozmiary psów oraz wycenia pobyt, leki i spacer.

Breakdown zawiera `capacityPoints`, `alreadyUsedPoints`, `dogCapacityWeight`, `remainingPoints`, `medicationFee`, `extraWalkFee`, `totalPrice` i `appliedRules`.

Seed dodaje 6 stref, 3 pobyty testowe i konta `admin@zpo.local`, `operator@zpo.local`, `user@zpo.local`.

```bash
./scripts/apply-preset.sh dog-daycare
```
