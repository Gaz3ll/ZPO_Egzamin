import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'cards',
  appName: 'Zoo Admin',
  currency: 'PLN',
  requiresTimeWindow: true,
  requiresQuantity: false,
  resource: {
    singular: 'Wybieg',
    plural: 'Wybiegi',
    fields: [
      { key: 'animalSpecies', label: 'Gatunek zwierząt', type: 'TEXT', required: true, options: [], helpText: 'Gatunek lub grupa gatunków' },
      { key: 'animalCount', label: 'Liczba zwierząt', type: 'NUMBER', required: true, options: [], helpText: 'Aktualna liczba zwierząt' },
      {
        key: 'dangerLevel',
        label: 'Poziom zagrożenia',
        type: 'SELECT',
        required: true,
        options: ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'],
        helpText: 'Ryzyko pracy w sektorze',
      },
      {
        key: 'feedingType',
        label: 'Typ karmienia',
        type: 'SELECT',
        required: true,
        options: ['HERBIVORE', 'CARNIVORE', 'OMNIVORE', 'AQUATIC', 'SPECIAL'],
        helpText: 'Rodzaj żywienia',
      },
      {
        key: 'cleaningDifficulty',
        label: 'Trudność sprzątania',
        type: 'SELECT',
        required: true,
        options: ['LOW', 'MEDIUM', 'HIGH', 'EXTREME'],
        helpText: 'Wpływa na nakład pracy',
      },
      { key: 'keeperZone', label: 'Strefa opiekunów', type: 'TEXT', required: true, options: [], helpText: 'Strefa organizacyjna' },
      { key: 'isQuarantine', label: 'Kwarantanna', type: 'BOOLEAN', required: false, options: [], helpText: 'Sektor w kwarantannie' },
      { key: 'lastInspectionDate', label: 'Ostatnia kontrola', type: 'DATE', required: false, options: [], helpText: 'Data przeglądu' },
    ],
  },
  request: {
    singular: 'Zadanie',
    plural: 'Zadania',
    fields: [
      {
        key: 'taskType',
        label: 'Typ zadania',
        type: 'SELECT',
        required: true,
        options: ['FEEDING', 'CLEANING', 'VET_CHECK', 'TRANSFER', 'TECHNICAL_INSPECTION'],
        helpText: 'Rodzaj pracy',
      },
      {
        key: 'priority',
        label: 'Priorytet',
        type: 'SELECT',
        required: true,
        options: ['LOW', 'NORMAL', 'HIGH', 'URGENT'],
        helpText: 'Priorytet operacyjny',
      },
      { key: 'requiresVet', label: 'Wymaga weterynarza', type: 'BOOLEAN', required: false, options: [], helpText: 'Udział weterynarza' },
      { key: 'requiresTwoKeepers', label: 'Wymaga dwóch opiekunów', type: 'BOOLEAN', required: false, options: [], helpText: 'Podwójna obsada' },
      {
        key: 'animalHealthRisk',
        label: 'Ryzyko zdrowotne',
        type: 'SELECT',
        required: false,
        options: ['LOW', 'NORMAL', 'HIGH', 'CRITICAL'],
        helpText: 'Ryzyko dla zwierząt',
      },
      { key: 'notes', label: 'Notatki', type: 'TEXTAREA', required: false, options: [], helpText: 'Opis zadania' },
    ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit',
    resources: 'Wybiegi i sektory',
    myRequests: 'Moje zadania',
    createRequest: 'Zaplanuj zadanie',
    adminPanel: 'Panel administracji zoo',
    adminResources: 'Zarządzaj wybiegami',
    adminRequests: 'Wszystkie zadania',
    availableOnly: 'Tylko aktywne wybiegi',
    algorithmResult: 'Ocena zadania',
    calculatedValue: 'Szacowany nakład',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Zaplanowane', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'Potwierdzone', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Anulowane', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucone', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Wykonane', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Aktywny', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Nieaktywny', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Niedostępny', classes: 'bg-red-100 text-red-800' },
}


