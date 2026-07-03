import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'list',
  appName: 'Ocena filmów',
  currency: 'PLN',
  requiresTimeWindow: false,
  requiresQuantity: false,
  timeMode: 'none',
  resource: {
    singular: 'Film',
    plural: 'Filmy',
     fields: [
       { key: 'director', label: 'Reżyser', type: 'TEXT', required: true, options: [], helpText: 'Reżyser filmu' },
       { key: 'releaseYear', label: 'Rok produkcji', type: 'NUMBER', required: true, options: [], helpText: 'Rok produkcji' },
       { key: 'genre', label: 'Gatunek', type: 'SELECT', required: true, options: ['Akcja', 'Komedia', 'Dramat', 'Horror', 'Sci-Fi', 'Romans', 'Thriller', 'Animacja', 'Dokumentalny', 'Inny'], helpText: 'Gatunek filmu' },
       { key: 'durationMinutes', label: 'Czas trwania (min)', type: 'NUMBER', required: false, options: [], helpText: 'Długość filmu w minutach' },
     ],
  },
  request: {
    singular: 'Ocena',
    plural: 'Oceny',
    fields: [
      { key: 'rating', label: 'Ocena', type: 'NUMBER', required: true, options: [], helpText: 'Ocena w skali 1-5' },
      { key: 'review', label: 'Recenzja', type: 'TEXTAREA', required: false, options: [], helpText: 'Twoja recenzja filmu (opcjonalnie)' },
      { key: 'watchDate', label: 'Data obejrzenia', type: 'DATE', required: true, options: [], helpText: 'Data obejrzenia' },
      { key: 'platform', label: 'Platforma', type: 'TEXT', required: false, options: [], helpText: 'Platforma / kino gdzie obejrzano' },
    ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit',
    resources: 'Dostępne filmy',
    myRequests: 'Moje oceny',
    createRequest: 'Oceń film',
    adminPanel: 'Panel administratora filmów',
    adminResources: 'Zarządzaj filmami',
    adminRequests: 'Wszystkie oceny',
    availableOnly: 'Tylko aktywne filmy',
    algorithmResult: 'Podsumowanie oceny',
    calculatedValue: 'Średnia ocena',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Oczekująca', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'Potwierdzona', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Anulowana', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucona', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Zakończona', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Dostępny', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Nieaktywny', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Niedostępny', classes: 'bg-red-100 text-red-800' },
}


