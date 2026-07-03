# Preset: System zgłoszeń do administracji osiedla

## Opis
Preset umożliwiający mieszkańcom osiedla zgłaszanie usterek lub problemów do administracji
oraz śledzenie ich stanu. Administrator widzi wszystkie zgłoszenia i może zmieniać ich status.

## Mapa pojęć

| Pojęcie ogólne | Odwzorowanie w domenie                   |
|----------------|------------------------------------------|
| **Resource**   | Kategoria zgłoszenia / Budynek           |
| **Request**    | Zgłoszenie usterki lub problemu          |

## Pola Resource (kategoria zgłoszenia)

| Pole              | Typ     | Opis                                                    |
|-------------------|---------|---------------------------------------------------------|
| `categoryName`    | TEXT    | Nazwa kategorii (np. AWARIA_WODY, USTERKA_WINDA)       |
| `building`        | TEXT    | Budynek / lokalizacja, której dotyczy kategoria         |
| `defaultPriority` | SELECT  | Domyślny priorytet: LOW, MEDIUM, HIGH, CRITICAL         |

## Pola Request (zgłoszenie)

| Pole          | Typ      | Opis                                               |
|---------------|----------|----------------------------------------------------|
| `title`       | TEXT     | Tytuł zgłoszenia                                   |
| `description` | TEXTAREA | Szczegółowy opis problemu                          |
| `status`      | SELECT   | Status: REPORTED, IN_PROGRESS, RESOLVED, CLOSED    |
| `location`    | TEXT     | Dokładne miejsce usterki                           |
| `tenantName`  | TEXT     | Imię i nazwisko zgłaszającego                      |

## Algorytm (DefaultDomainAlgorithm)

1. **Auto-przydział priorytetu** – na podstawie `defaultPriority` z kategorii zgłoszenia.
2. **Walidacja statusu** – akceptowane wartości: `REPORTED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`.
3. **Brak kalkulacji kosztu** – algorytm wyłącznie walidacyjny; `calculatedValue` = 0 PLN.

## Role i uprawnienia

| Rola      | Uprawnienia                                               |
|-----------|-----------------------------------------------------------|
| **USER**  | Tworzy zgłoszenia i przegląda tylko własne                |
| **ADMIN** | Przegląda wszystkie zgłoszenia i zmienia ich status       |

## ZPO (Zastosowane Praktyki Obiektowe)

- **Polimorfizm przez interfejs** – `DomainAlgorithm` wspólny dla presetów.
- **Parametryzacja generycznym profilem** – jeden profil zmienia zachowanie całej aplikacji.
- **Wzorzec Builder** – `AlgorithmBreakdownBuilder` do konstruowania opisu kroków algorytmu.
- **Kompozycja zamiast dziedziczenia** – preset nie modyfikuje klas bazowych, jedynie dostarcza konfigurację.
- **SOLID** – otwarte na rozszerzenia (nowy preset = nowy profil), zamknięte na modyfikacje.

## Użytkownicy demonstracyjni

- Admin: `admin@zpo.local` / `admin123`
- Mieszkaniec: `user@zpo.local` / `user123`

## Seeding

Dane testowe są automatycznie ładowane przy starcie aplikacji z aktywnym presetem:

- **4 kategorie**: AWARIA_WODY (HIGH), AWARIA_PRADU (CRITICAL), USTERKA_WINDA (HIGH), INNE (MEDIUM)
- **3 zgłoszenia**: wyciek wody (zgłoszony), winda (w trakcie), brak prądu (rozwiązany)
