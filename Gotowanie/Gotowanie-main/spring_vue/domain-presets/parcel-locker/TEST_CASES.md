# Test cases: parcel-locker (System paczkomatów)

Zgodność z punktacją ZPO:
- **Algorytm (60% punktów)**: testy 1-5 pokrywają logikę doboru skrytki i wyceny
- **Testy jednostkowe (1-2 pkt)**: 7 testów pokrywających algorytm i security
- **Security (4-5 pkt)**: testy 6-7 weryfikują polityki dostępu

## Testy algorytmu

### 1. Dobór najmniejszej skrytki
- Paczka rozmiar `S`, waga 1kg.
- Dostępne skrytki: S(pusta), M(pusta), L(pusta).
- Oczekiwane: algorytm wybiera skrytkę rozmiaru `S`.
- **Cel**: weryfikacja doboru najmniejszej pasującej skrytki

### 2. Pominięcie zajętej skrytki
- Paczka rozmiar `M`, waga 5kg.
- Skrytka `S` zajęta (ma status `isOccuped=true`), skrytka `M` wolna.
- Oczekiwane: algorytm wybiera skrytkę rozmiaru `M`, ignoruje zajętą `S`.
- **Cel**: sprawdzenie filtrowania skrytek zajętych operacyjnie

### 3. Przekroczenie wagi
- Paczka rozmiar `S`, waga 10kg.
- Skrytka `S` ma `maxWeight=5kg`.
- Oczekiwane: conflict, brak dostępnej skrytki.
- **Cel**: weryfikacja limitu wagowego skrytki

### 4. Opłata za wagę
- Paczka rozmiar `S`, waga 7kg (powyżej 5kg).
- Oczekiwane: `weightFee = (7-5) × 1.50 = 3.00zł`.
- **Cel**: sprawdzenie naliczania opłaty za nadwagę

### 5. Opłata za rozmiar
- Paczka rozmiar `L`.
- Oczekiwane: `sizeFee = 8.00zł`.
- **Cel**: weryfikacja dopłaty za duży gabaryt

## Testy security

### 6. USER widzi tylko swoje paczki
- Zalogowany jako Jan Kowalski (`user@zpo.local`).
- Żądanie listy paczek zwraca tylko paczki należące do tego użytkownika.
- **Cel**: weryfikacja `RequestAccessPolicy`

### 7. ADMIN/OPERATOR widzi wszystkie paczki
- Zalogowany jako Administrator (`admin@zpo.local`).
- Żądanie listy paczek zwraca wszystkie paczki w systemie.
- **Cel**: weryfikacja `AdminPolicy`

## Uruchomienie

```bash
cd backend && ./mvnw test
```
