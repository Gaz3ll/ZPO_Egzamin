import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'cards',
  appName: 'Escape room',
  currency: 'PLN',
  requiresTimeWindow: true,
  requiresQuantity: false,
  resource: {
    singular: 'Pokój',
    plural: 'Pokoje',
    fields: [
      { key: 'roomName', label: 'Nazwa pokoju', type: 'TEXT', required: true, options: [], helpText: 'Nazwa' },
      {
        key: 'difficulty',
        label: 'Trudność',
        type: 'SELECT',
        required: true,
        options: ['EASY', 'MEDIUM', 'HARD', 'EXPERT'],
        helpText: 'Poziom',
      },
      { key: 'theme', label: 'Motyw', type: 'TEXT', required: true, options: [], helpText: 'Tematyka' },
      { key: 'durationMinutes', label: 'Czas gry (min)', type: 'NUMBER', required: true, options: [], helpText: 'Sesja' },
      { key: 'maxPlayers', label: 'Max graczy', type: 'NUMBER', required: true, options: [], helpText: 'Limit' },
      { key: 'depositRequired', label: 'Zaliczka', type: 'BOOLEAN', required: false, options: [], helpText: 'Wymagana' },
    ],
  },
  request: {
    singular: 'Rezerwacja',
    plural: 'Rezerwacje',
    fields: [
      { key: 'teamName', label: 'Nazwa drużyny', type: 'TEXT', required: true, options: [], helpText: 'Drużyna' },
      { key: 'playersCount', label: 'Liczba graczy', type: 'NUMBER', required: true, options: [], helpText: 'Wielkość' },
      {
        key: 'preferredDifficulty',
        label: 'Preferowana trudność',
        type: 'SELECT',
        required: false,
        options: ['EASY', 'MEDIUM', 'HARD', 'EXPERT'],
        helpText: 'Opcjonalnie',
      },
      { key: 'preferredTheme', label: 'Preferowany motyw', type: 'TEXT', required: false, options: [], helpText: 'Opcjonalnie' },
      { key: 'depositPaid', label: 'Zaliczka opłacona', type: 'BOOLEAN', required: false, options: [], helpText: 'Status' },
    ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit escape roomu',
    resources: 'Pokoje',
    myRequests: 'Moje rezerwacje',
    createRequest: 'Zarezerwuj pokój',
    adminPanel: 'Panel Game Mastera',
    adminResources: 'Zarządzaj pokojami',
    adminRequests: 'Harmonogram rezerwacji',
    availableOnly: 'Tylko wolne pokoje',
    algorithmResult: 'Dobór pokoju i wycena',
    calculatedValue: 'Cena sesji',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Oczekująca', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'Potwierdzona', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Anulowana', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucona', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Rozegrana', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Dostępny', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Nieaktywny', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'W remoncie', classes: 'bg-red-100 text-red-800' },
}


