import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'list',
  appName: 'System zgłoszeń osiedlowych',
  currency: 'PLN',
  requiresTimeWindow: false,
  requiresQuantity: false,
  timeMode: 'none',
     resource: {
       singular: 'Kategoria',
       plural: 'Kategorie',
       fields: [
         { key: 'categoryName', label: 'Nazwa kategorii', type: 'TEXT', required: true, options: [], helpText: 'Nazwa kategorii zgłoszenia (np. AWARIA_WODY)' },
         { key: 'building', label: 'Budynek', type: 'TEXT', required: true, options: [], helpText: 'Budynek/lokalizacja, której dotyczy kategoria' },
         { key: 'defaultPriority', label: 'Priorytet domyślny', type: 'SELECT', required: true, options: ['Niski', 'Średni', 'Wysoki', 'Krytyczny'], helpText: 'Priorytet domyślny' },
       ],
     },
     request: {
       singular: 'Zgłoszenie',
       plural: 'Zgłoszenia',
       fields: [
         { key: 'title', label: 'Tytuł zgłoszenia', type: 'TEXT', required: true, options: [], helpText: 'Krótki tytuł opisujący problem' },
         { key: 'description', label: 'Opis', type: 'TEXTAREA', required: true, options: [], helpText: 'Szczegółowy opis problemu' },
         { key: 'status', label: 'Status', type: 'SELECT', required: true, options: ['Zgłoszony', 'W trakcie', 'Rozwiązany', 'Zamknięty'], helpText: 'Status zgłoszenia' },
         { key: 'location', label: 'Lokalizacja', type: 'TEXT', required: true, options: [], helpText: 'Dokładne miejsce usterki' },
         { key: 'tenantName', label: 'Lokator', type: 'TEXT', required: true, options: [], helpText: 'Imię i nazwisko lokatora' },
         { key: 'reportedBy', label: 'Zgłaszający', type: 'TEXT', required: true, options: [], helpText: 'Imię i nazwisko zgłaszającego' },
         { key: 'contactPhone', label: 'Telefon', type: 'TEXT', required: true, options: [], helpText: 'Telefon kontaktowy' },
         { key: 'urgency', label: 'Pilność', type: 'SELECT', required: true, options: ['Niski', 'Średni', 'Wysoki', 'Natychmiastowy'], helpText: 'Jak pilna jest sprawa?' },
         { key: 'additionalInfo', label: 'Dodatkowe info', type: 'TEXTAREA', required: false, options: [], helpText: 'Dodatkowe informacje lub zdjęcia' },
       ],
     },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit osiedla',
    resources: 'Kategorie zgłoszeń',
    myRequests: 'Moje zgłoszenia',
    createRequest: 'Zgłoś problem',
    adminPanel: 'Panel administracji osiedla',
    adminResources: 'Zarządzaj kategoriami',
    adminRequests: 'Wszystkie zgłoszenia',
    availableOnly: 'Tylko aktywne kategorie',
    algorithmResult: 'Weryfikacja zgłoszenia',
    calculatedValue: 'Koszt (0 PLN)',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Zgłoszony', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'W trakcie', classes: 'bg-blue-100 text-blue-800' },
  CANCELLED: { label: 'Anulowane', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucone', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Rozwiązane', classes: 'bg-green-100 text-green-800' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Aktywna', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Nieaktywna', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Niedostępna', classes: 'bg-red-100 text-red-800' },
}


