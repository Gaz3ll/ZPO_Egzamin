import type { DomainFieldConfig, RequestStatus, ResourceStatus } from '@/types/domain'

export const domainConfig = {
  appName: 'Sklep internetowy',
  currency: 'PLN',
  requiresTimeWindow: false,
  requiresQuantity: false,
  timeMode: 'none' as const,
  resource: {
    singular: 'Produkt',
    plural: 'Produkty',
    fields: [
      { key: 'price', label: 'Cena', type: 'NUMBER' as const, required: true, options: [] as string[], helpText: 'Cena produktu w PLN' },
      { key: 'category', label: 'Kategoria', type: 'SELECT' as const, required: true, options: ['Elektronika', 'Spożywcze', 'Odzież', 'Dom', 'Sport', 'Inne'] as any, helpText: 'Kategoria produktu' },
      { key: 'stock', label: 'Stan magazynowy', type: 'NUMBER' as const, required: true, options: [] as string[], helpText: 'Liczba sztuk na stanie' },
    ],
  },
  request: {
    singular: 'Zamówienie',
    plural: 'Zamówienia',
    fields: [
      { key: 'customerName', label: 'Imię i nazwisko', type: 'TEXT' as const, required: true, options: [] as string[], helpText: 'Dane klienta' },
      { key: 'customerPhone', label: 'Telefon', type: 'TEXT' as const, required: true, options: [] as string[], helpText: 'Numer telefonu' },
      { key: 'customerAddress', label: 'Adres wysyłki', type: 'TEXT' as const, required: true, options: [] as string[], helpText: 'Ulica, kod pocztowy, miasto' },
      { key: 'quantity', label: 'Ilość sztuk', type: 'NUMBER' as const, required: true, options: [] as string[], helpText: 'Zamawiana ilość' },
    ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit',
    resources: 'Produkty',
    myRequests: 'Moje zamówienia',
    createRequest: 'Zamów produkt',
    adminPanel: 'Panel administratora',
    adminResources: 'Zarządzaj produktami',
    adminRequests: 'Wszystkie zamówienia',
    calendar: 'Kalendarz',
    availableOnly: 'Tylko dostępne',
    algorithmResult: 'Szczegóły zamówienia',
    calculatedValue: 'Do zapłaty',
    delete: 'Usuń',
    occupied: 'Brak',
    free: 'Dostępny',
    morning: '',
    evening: '',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-gray-800 text-gray-400' },
  PENDING: { label: 'Oczekujące', classes: 'bg-amber-500/10 text-amber-400' },
  CONFIRMED: { label: 'Potwierdzone', classes: 'bg-green-500/10 text-green-400' },
  CANCELLED: { label: 'Anulowane', classes: 'bg-gray-700 text-gray-500' },
  REJECTED: { label: 'Odrzucone', classes: 'bg-red-500/10 text-red-400' },
  COMPLETED: { label: 'Zrealizowane', classes: 'bg-brand-500/10 text-brand-400' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Dostępny', classes: 'bg-green-500/10 text-green-400' },
  INACTIVE: { label: 'Niedostępny', classes: 'bg-gray-700 text-gray-500' },
  UNAVAILABLE: { label: 'Brak', classes: 'bg-red-500/10 text-red-400' },
}

export const contractHoursSuggestions: Record<string, string> = {}
