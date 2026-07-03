import type { DomainFieldConfig, RequestStatus, ResourceStatus } from '@/types/domain'

export const domainConfig = {
  appName: 'Biblioteka',
  currency: 'PLN',
  requiresTimeWindow: false,
  requiresQuantity: false,
  timeMode: 'none' as const,
  resource: {
    singular: 'Książka',
    plural: 'Książki',
    fields: [
      { key: 'author', label: 'Autor', type: 'TEXT' as const, required: true, options: [] as string[], helpText: 'Autor książki' },
      { key: 'isbn', label: 'ISBN', type: 'TEXT' as const, required: false, options: [] as string[], helpText: 'Numer ISBN' },
      { key: 'totalCopies', label: 'Egzemplarze łącznie', type: 'NUMBER' as const, required: true, options: [] as string[], helpText: 'Całkowita liczba egzemplarzy' },
      { key: 'availableCopies', label: 'Dostępne', type: 'NUMBER' as const, required: true, options: [] as string[], helpText: 'Aktualnie dostępne egzemplarze' },
    ],
  },
  request: {
    singular: 'Wypożyczenie',
    plural: 'Wypożyczenia',
    fields: [] as DomainFieldConfig[],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit',
    resources: 'Książki',
    myRequests: 'Moje wypożyczenia',
    createRequest: 'Wypożycz książkę',
    adminPanel: 'Panel administratora',
    adminResources: 'Zarządzaj książkami',
    adminRequests: 'Wszystkie wypożyczenia',
    availableOnly: 'Tylko dostępne',
    algorithmResult: 'Status wypożyczenia',
    calculatedValue: '',
    delete: 'Usuń',
    occupied: 'Wypożyczona',
    free: 'Dostępna',
    calendar: 'Kalendarz',
    morning: '',
    evening: '',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-gray-800 text-gray-400' },
  PENDING: { label: 'Wypożyczona', classes: 'bg-amber-500/10 text-amber-400' },
  CONFIRMED: { label: 'Wypożyczona', classes: 'bg-green-500/10 text-green-400' },
  CANCELLED: { label: 'Anulowana', classes: 'bg-gray-700 text-gray-500' },
  REJECTED: { label: 'Odrzucona', classes: 'bg-red-500/10 text-red-400' },
  COMPLETED: { label: 'Zwrócona', classes: 'bg-brand-500/10 text-brand-400' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Dostępna', classes: 'bg-green-500/10 text-green-400' },
  INACTIVE: { label: 'Niedostępna', classes: 'bg-gray-700 text-gray-500' },
  UNAVAILABLE: { label: 'Brak', classes: 'bg-red-500/10 text-red-400' },
}

export const contractHoursSuggestions: Record<string, string> = {}
