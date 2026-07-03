import type { DomainFieldConfig, RequestStatus, ResourceStatus } from '@/types/domain'

export const domainConfig = {
  appName: 'System rezerwacji salek konferencyjnych',
  currency: 'PLN',
  requiresTimeWindow: true,
  requiresQuantity: false,
  timeMode: 'range',
  resource: {
    singular: 'Sala',
    plural: 'Sale',
    fields: [
      { key: 'roomName', label: 'Nazwa sali', type: 'TEXT', required: true, options: [], helpText: 'Nazwa sali (np. Sala A, Sala VIP)' },
      { key: 'capacity', label: 'Pojemność', type: 'NUMBER', required: true, options: [], helpText: 'Maksymalna liczba osób' },
      { key: 'floor', label: 'Piętro', type: 'TEXT', required: false, options: [], helpText: 'Piętro (np. 1, 2, 3)' },
      { key: 'hasProjector', label: 'Projektor', type: 'BOOLEAN', required: false, options: [], helpText: 'Czy sala ma projektor' },
      { key: 'hasVideoConference', label: 'Wideokonferencja', type: 'BOOLEAN', required: false, options: [], helpText: 'Czy sala ma sprzęt do wideokonferencji' },
      { key: 'dailyRate', label: 'Stawka dzienna', type: 'NUMBER', required: true, options: [], helpText: 'Koszt wynajmu sali za dzień (PLN)' },
    ],
  },
  request: {
    singular: 'Rezerwacja',
    plural: 'Rezerwacje',
    fields: [
      { key: 'renterName', label: 'Imię i nazwisko', type: 'TEXT', required: true, options: [], helpText: 'Osoba rezerwująca salę' },
      { key: 'renterEmail', label: 'Email', type: 'TEXT', required: true, options: [], helpText: 'Adres email kontaktowy' },
      { key: 'renterPhone', label: 'Telefon', type: 'TEXT', required: true, options: [], helpText: 'Numer telefonu' },
      { key: 'meetingTitle', label: 'Tytuł spotkania', type: 'TEXT', required: true, options: [], helpText: 'Tytuł spotkania' },
      { key: 'attendeeCount', label: 'Liczba uczestników', type: 'NUMBER', required: true, options: [], helpText: 'Planowana liczba osób' },
      { key: 'notes', label: 'Uwagi', type: 'TEXTAREA', required: false, options: [], helpText: 'Dodatkowe informacje' },
    ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit',
    resources: 'Sale konferencyjne',
    myRequests: 'Moje rezerwacje',
    createRequest: 'Rezerwuj salę',
    adminPanel: 'Panel administratora',
    adminResources: 'Zarządzaj salami',
    adminRequests: 'Wszystkie rezerwacje',
    calendar: 'Kalendarz',
    availableOnly: 'Tylko dostępne sale',
    algorithmResult: 'Kalkulacja kosztu',
    calculatedValue: 'Koszt rezerwacji',
    delete: 'Usuń',
    occupied: 'Zajęte',
    free: 'Wolne',
    morning: 'Zmiana poranna',
    evening: 'Zmiana wieczorna',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-gray-800 text-gray-400' },
  PENDING: { label: 'Oczekująca', classes: 'bg-amber-500/10 text-amber-400' },
  CONFIRMED: { label: 'Potwierdzona', classes: 'bg-green-500/10 text-green-400' },
  CANCELLED: { label: 'Anulowana', classes: 'bg-gray-700 text-gray-500' },
  REJECTED: { label: 'Odrzucona', classes: 'bg-red-500/10 text-red-400' },
  COMPLETED: { label: 'Zakończona', classes: 'bg-brand-500/10 text-brand-400' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Dostępna', classes: 'bg-green-500/10 text-green-400' },
  INACTIVE: { label: 'Nieaktywna', classes: 'bg-gray-700 text-gray-500' },
  UNAVAILABLE: { label: 'Niedostępna', classes: 'bg-red-500/10 text-red-400' },
}

export const contractHoursSuggestions: Record<string, string> = {
  UOP: 'ok. 160h/miesiąc (40h/tydzień)',
  UZ: 'ok. 80h/miesiąc (20h/tydzień)',
  B2B: 'ok. 160h/miesiąc (40h/tydzień)',
}
