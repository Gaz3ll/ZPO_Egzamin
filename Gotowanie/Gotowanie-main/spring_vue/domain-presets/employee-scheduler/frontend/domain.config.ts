import type { DomainFieldConfig, RequestStatus, ResourceStatus } from '@/types/domain'

export interface DomainUiConfig {
  appName: string
  currency: string
  requiresTimeWindow: boolean
  requiresQuantity: boolean
  timeMode: 'slots' | 'range' | 'none'
  resource: {
    singular: string
    plural: string
    fields: DomainFieldConfig[]
  }
  request: {
    singular: string
    plural: string
    fields: DomainFieldConfig[]
  }
  shifts: {
    morning: { label: string; start: string; end: string }
    evening: { label: string; start: string; end: string }
  }
  labels: Record<string, string>
}

export const domainConfig: DomainUiConfig = {
  appName: 'Employee Scheduler',
  currency: 'PLN',
  requiresTimeWindow: true,
  requiresQuantity: false,
  timeMode: 'slots',
  resource: {
    singular: 'Pracownik',
    plural: 'Pracownicy',
    fields: [
      { key: 'position', label: 'Stanowisko', type: 'TEXT', required: true, options: [], helpText: 'Stanowisko pracy (np. Kasjer, Magazynier)' },
      {
        key: 'department',
        label: 'Dział',
        type: 'SELECT',
        required: true,
        options: ['SALES', 'LOGISTICS', 'IT', 'HR', 'SUPPORT'],
        helpText: 'Dział pracownika',
      },
      {
        key: 'contractType',
        label: 'Typ umowy',
        type: 'SELECT',
        required: true,
        options: ['UOP', 'UZ', 'B2B'],
        helpText: 'UOP - umowa o pracę | UZ - umowa zlecenie | B2B - kontrakt',
      },
      {
        key: 'maxHoursPerWeek',
        label: 'Limit godzin/tydzień',
        type: 'NUMBER',
        required: true,
        options: [],
        helpText: 'Maksymalna tygodniowa liczba godzin',
      },
      { key: 'hourlyRate', label: 'Stawka godzinowa', type: 'NUMBER', required: true, options: [], helpText: 'Stawka za godzinę pracy (PLN)' },
    ],
  },
  request: {
    singular: 'Wpis grafiku',
    plural: 'Zmiany i zadania',
    fields: [
      { key: 'taskName', label: 'Zadanie', type: 'TEXT', required: false, options: [], helpText: 'Nazwa zadania do wykonania (np. Inwentaryzacja, Sprzątanie)' },
      { key: 'notes', label: 'Notatki', type: 'TEXTAREA', required: false, options: [], helpText: 'Dodatkowe uwagi do zmiany lub zadania' },
    ],
  },
  shifts: {
    morning: { label: 'Zmiana poranna', start: '07:00', end: '15:00' },
    evening: { label: 'Zmiana wieczorna', start: '15:00', end: '23:00' },
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit grafiku',
    resources: 'Pracownicy',
    myRequests: 'Moje zmiany',
    createRequest: 'Dodaj wpis do grafiku',
    adminPanel: 'Panel zarządzania',
    adminResources: 'Zarządzaj pracownikami',
    adminRequests: 'Wszystkie zmiany i zadania',
    calendar: 'Kalendarz',
    availableOnly: 'Tylko dostępni pracownicy',
    algorithmResult: 'Analiza grafiku',
    calculatedValue: 'Koszt zmiany',
    morning: 'Zmiana poranna',
    evening: 'Zmiana wieczorna',
    delete: 'Usuń',
    occupied: 'Zajęte',
    free: 'Wolne',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Do zatwierdzenia', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'W grafiku', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Anulowany', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucony', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Przepracowany', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Dostępny', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Nieobecny', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Niedostępny', classes: 'bg-red-100 text-red-800' },
}

export const contractHoursSuggestions: Record<string, string> = {
  UOP: 'ok. 160h/miesiąc (40h/tydzień)',
  UZ: 'ok. 80h/miesiąc (20h/tydzień)',
  B2B: 'ok. 160h/miesiąc (40h/tydzień)',
}
