import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'list',
  appName: 'System paczkomatów',
  currency: 'PLN',
  requiresTimeWindow: false,
  requiresQuantity: false,
  resource: {
    singular: 'Skrytka',
    plural: 'Skrytki',
    fields: [
      { key: 'lockerCode', label: 'Kod skrytki', type: 'TEXT', required: true, options: [], helpText: 'Np. WAW-01-S1, KRA-05-M3 - unikalny identyfikator skrytki w systemie' },
      { key: 'location', label: 'Lokalizacja', type: 'TEXT', required: true, options: [], helpText: 'Np. Warszawa, ul. Prosta 1 - adres paczkomatu' },
      {
        key: 'lockerSize',
        label: 'Rozmiar skrytki',
        type: 'SELECT',
        required: true,
        options: ['S', 'M', 'L', 'XL'],
        helpText: 'S - mała (do 5kg) | M - średnia (do 12kg) | L - duża (do 20kg) | XL - największa (do 30kg)',
      },
      { key: 'maxWeight', label: 'Maksymalna waga (kg)', type: 'NUMBER', required: true, options: [], helpText: 'Maksymalna waga paczki jaką może pomieścić skrytka (w kg)' },
      {
        key: 'isOccupied',
        label: 'Zajęta operacyjnie',
        type: 'BOOLEAN',
        required: false,
        options: [],
        helpText: 'Skrytka zajęta operacyjnie (np. uszkodzona, zablokowana)',
      },
    ],
  },
  request: {
    singular: 'Paczka',
    plural: 'Paczki',
    fields: [
      { key: 'receiverName', label: 'Odbiorca', type: 'TEXT', required: true, options: [], helpText: 'Imię i nazwisko odbiorcy przesyłki' },
      { key: 'receiverEmail', label: 'Email odbiorcy', type: 'TEXT', required: true, options: [], helpText: 'Adres e-mail do powiadomienia o dostarczeniu' },
      {
        key: 'parcelSize',
        label: 'Rozmiar paczki',
        type: 'SELECT',
        required: true,
        options: ['S', 'M', 'L', 'XL'],
        helpText: 'S - koperta/gabaryt A (do 5kg) | M - pudełko średnie (do 12kg) | L - duże pudło (do 20kg) | XL - maksymalny gabaryt (do 30kg). Algorytm dobierze najmniejszą pasującą skrytkę',
      },
      { key: 'weight', label: 'Waga paczki (kg)', type: 'NUMBER', required: true, options: [], helpText: 'Waga w kilogramach. Nadwaga powyżej 5kg: +1.50zł/kg dopłaty' },
      { key: 'pickupCode', label: 'Kod odbioru', type: 'TEXT', required: false, options: [], helpText: 'Opcjonalny 6-cyfrowy kod do odbioru paczki (generowany automatycznie jeśli pusty)' },
    ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit',
    resources: 'Dostępne skrytki',
    myRequests: 'Moje paczki',
    createRequest: 'Nadaj paczkę',
    adminPanel: 'Panel operatora',
    adminResources: 'Zarządzaj skrytkami',
    adminRequests: 'Wszystkie paczki',
    availableOnly: 'Tylko dostępne skrytki',
    algorithmResult: 'Dobór skrytki',
    calculatedValue: 'Koszt nadania',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Nadana', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'W skrytce', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Anulowana', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucona', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Odebrana', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Aktywna', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Nieaktywna', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Niedostępna', classes: 'bg-red-100 text-red-800' },
}


