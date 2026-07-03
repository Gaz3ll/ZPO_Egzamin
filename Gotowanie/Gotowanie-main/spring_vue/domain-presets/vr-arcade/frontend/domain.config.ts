import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'cards',
  appName: 'Strefa VR – rezerwacja gogli',
  currency: 'PLN',
  requiresTimeWindow: false,
  requiresQuantity: true,
  resource: {
    singular: 'Strefa',
    plural: 'Strefy',
    fields: [
      { key: 'zoneName', label: 'Nazwa strefy', type: 'TEXT', required: true, options: [], helpText: 'Nazwa' },
      { key: 'totalHeadsets', label: 'Liczba gogli', type: 'NUMBER', required: true, options: [], helpText: 'Pula' },
      { key: 'gameTypes', label: 'Gry', type: 'TEXT', required: true, options: [], helpText: 'Po przecinku' },
      { key: 'maxPlayers', label: 'Max graczy', type: 'NUMBER', required: true, options: [], helpText: 'Limit' },
      { key: 'hasMultiplayer', label: 'Multiplayer', type: 'BOOLEAN', required: false, options: [], helpText: 'Wieloosobowa' },
    ],
  },
  request: {
    singular: 'Rezerwacja',
    plural: 'Rezerwacje',
    fields: [
      { key: 'playersCount', label: 'Liczba graczy', type: 'NUMBER', required: true, options: [], helpText: 'Gogle' },
      { key: 'gameType', label: 'Gra', type: 'TEXT', required: true, options: [], helpText: 'Wybrana' },
      { key: 'customerName', label: 'Rezerwujący', type: 'TEXT', required: true, options: [], helpText: 'Imię i nazwisko' },
      { key: 'qrCode', label: 'Kod QR', type: 'TEXT', required: false, options: [], helpText: 'Odbiór' },
    ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit strefy VR',
    resources: 'Strefy VR',
    myRequests: 'Moje rezerwacje',
    createRequest: 'Zarezerwuj gogle',
    adminPanel: 'Panel obsługi VR',
    adminResources: 'Zarządzaj strefami',
    adminRequests: 'Wszystkie rezerwacje',
    availableOnly: 'Tylko wolne strefy',
    algorithmResult: 'Pula gogli i wycena',
    calculatedValue: 'Koszt rezerwacji',
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
  ACTIVE: { label: 'Dostępna', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Nieaktywna', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'W serwisie', classes: 'bg-red-100 text-red-800' },
}


