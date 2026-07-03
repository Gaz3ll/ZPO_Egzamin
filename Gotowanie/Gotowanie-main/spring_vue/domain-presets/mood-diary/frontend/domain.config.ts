import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'list',
  appName: 'Dziennik nastroju',
  currency: 'PCT',
  requiresTimeWindow: false,
  requiresQuantity: false,
  timeMode: 'none',
     resource: {
       singular: 'Dzień',
       plural: 'Dni',
       fields: [
         { key: 'entryDate', label: 'Data dnia', type: 'DATE', required: true, options: [], helpText: 'Data reprezentująca konkretny dzień' },
       ],
     },
     request: {
       singular: 'Wpis',
       plural: 'Wpisy',
       fields: [
         { key: 'moodScore', label: 'Intensywność (1-10)', type: 'NUMBER', required: true, options: [], helpText: 'Punktowa ocena nastroju: 1 (najgorzej) - 10 (najlepiej)' },
         { key: 'moodLabel', label: 'Etykieta nastroju', type: 'SELECT', required: true, options: ['Świetnie', 'Dobrze', 'Neutralnie', 'Źle', 'Bardzo źle'], helpText: 'Opis nastroju' },
         { key: 'activities', label: 'Aktywności', type: 'TEXT', required: false, options: [], helpText: 'Wypisz aktywności dnia (np. spacer, czytanie)' },
         { key: 'sleepHours', label: 'Godziny snu', type: 'NUMBER', required: false, options: [], helpText: 'Liczba godzin snu' },
         { key: 'trigger', label: 'Przyczyna', type: 'TEXT', required: false, options: [], helpText: 'Co wywołało ten nastrój?' },
         { key: 'notes', label: 'Notatka', type: 'TEXTAREA', required: false, options: [], helpText: 'Opis wydarzeń lub myśli' },
       ],
     },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Mój dziennik',
    resources: 'Dostępne dni',
    myRequests: 'Moje wpisy',
    createRequest: 'Dodaj wpis nastroju',
    adminPanel: 'Panel terapeuty / administratora',
    adminResources: 'Zarządzaj dniami',
    adminRequests: 'Wszystkie wpisy pacjentów',
    availableOnly: 'Tylko dni bez wpisu',
    algorithmResult: 'Podsumowanie wpisu',
    calculatedValue: 'Ocena nastroju',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Oczekujący', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'Potwierdzony', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Anulowany', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucony', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Zakończony', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Dostępny', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Nieaktywny', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Niedostępny', classes: 'bg-red-100 text-red-800' },
}


