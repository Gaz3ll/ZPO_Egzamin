import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'list',
  appName: 'Przedszkole dla psów',
  currency: 'PLN',
  requiresTimeWindow: false,
  requiresQuantity: false,
  resource: {
    singular: 'Strefa',
    plural: 'Strefy',
    fields: [
      { key: 'zoneName', label: 'Strefa', type: 'TEXT', required: true, options: [], helpText: 'Nazwa strefy' },
      { key: 'dailyCapacityPoints', label: 'Punkty', type: 'NUMBER', required: true, options: [], helpText: 'Dzienna pojemność' },
      { key: 'acceptedDogSizes', label: 'Rozmiary', type: 'TEXT', required: true, options: [], helpText: 'SMALL,MEDIUM,LARGE' },
      { key: 'hasOutdoorRun', label: 'Wybieg', type: 'BOOLEAN', required: false, options: [], helpText: 'Dostępny wybieg' },
      { key: 'careLevel', label: 'Opieka', type: 'TEXT', required: true, options: [], helpText: 'STANDARD/ACTIVE/MEDICAL' },
      { key: 'staffCount', label: 'Opiekunowie', type: 'NUMBER', required: true, options: [], helpText: 'Liczba osób' },
    ],
  },
  request: {
    singular: 'Pobyt',
    plural: 'Pobyty',
    fields: [
      { key: 'dogName', label: 'Pies', type: 'TEXT', required: true, options: [], helpText: 'Imię psa' },
      { key: 'dogSize', label: 'Wielkość', type: 'TEXT', required: true, options: [], helpText: 'SMALL/MEDIUM/LARGE' },
      { key: 'dogWeight', label: 'Waga', type: 'NUMBER', required: false, options: [], helpText: 'kg' },
      { key: 'stayHours', label: 'Godziny', type: 'NUMBER', required: true, options: [], helpText: 'Czas pobytu' },
      { key: 'needsMedication', label: 'Leki', type: 'BOOLEAN', required: false, options: [], helpText: 'Podanie leków' },
      { key: 'extraWalk', label: 'Spacer', type: 'BOOLEAN', required: false, options: [], helpText: 'Dodatkowy spacer' },
      { key: 'feedingNotes', label: 'Karmienie', type: 'TEXTAREA', required: false, options: [], helpText: 'Instrukcje' },
    ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Panel przedszkola',
    resources: 'Strefy opieki',
    myRequests: 'Pobyty mojego psa',
    createRequest: 'Zgłoś pobyt',
    adminPanel: 'Panel opiekuna',
    adminResources: 'Zarządzaj strefami',
    adminRequests: 'Wszystkie pobyty',
    availableOnly: 'Tylko aktywne strefy',
    algorithmResult: 'Pojemność i opłaty',
    calculatedValue: 'Koszt pobytu',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Oczekuje', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'Przyjęty', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Anulowany', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucony', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Odebrany', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Aktywna', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Nieaktywna', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Niedostępna', classes: 'bg-red-100 text-red-800' },
}


