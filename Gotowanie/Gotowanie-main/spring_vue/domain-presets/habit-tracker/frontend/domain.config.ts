import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'list',
  appName: 'Habit Tracker',
  currency: 'PKT',
  requiresTimeWindow: true,
  requiresQuantity: true,
  timeMode: 'range',
  resource: {
    singular: 'Nawyk',
    plural: 'Nawyki',
    fields: [
      { key: 'habitName', label: 'Nazwa nawyku', type: 'TEXT', required: true, options: [], helpText: 'Nazwa nawyku (np. Picie wody, Czytanie książek)' },
      { key: 'category', label: 'Kategoria', type: 'SELECT', required: true, options: ['Zdrowie', 'Rozwój', 'Praca', 'Sport', 'Dom', 'Inne'], helpText: 'Kategoria nawyku' },
      { key: 'frequency', label: 'Częstotliwość', type: 'SELECT', required: true, options: ['Codziennie', 'Co drugi dzień', 'Raz w tygodniu', 'W dni powszednie'], helpText: 'Jak często należy wykonywać nawyk' },
      { key: 'targetPerDay', label: 'Cel dzienny', type: 'NUMBER', required: true, options: [], helpText: 'Docelowa dzienna liczba powtórzeń (np. 8 szklanek wody)' },
      { key: 'unit', label: 'Jednostka', type: 'TEXT', required: true, options: [], helpText: 'Jednostka (np. szklanki, minuty, strony)' },
      { key: 'colorTag', label: 'Kolor etykiety', type: 'TEXT', required: false, options: [], helpText: 'Kolor etykiety (HEX, np. #FF5733)' },
    ],
  },
  request: {
    singular: 'Wpis',
    plural: 'Wpisy',
    fields: [
      { key: 'date', label: 'Data', type: 'DATE', required: true, options: [], helpText: 'Data wpisu' },
      { key: 'value', label: 'Wartość', type: 'NUMBER', required: true, options: [], helpText: 'Liczba wykonanych powtórzeń w danym dniu' },
      { key: 'note', label: 'Notatka', type: 'TEXTAREA', required: false, options: [], helpText: 'Notatka do wpisu (np. "Dziś udało się więcej!")' },
      { key: 'skipped', label: 'Pominięty', type: 'BOOLEAN', required: false, options: [], helpText: 'Zaznacz jeśli dzień został pominięty' },
    ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit nawyków',
    resources: 'Moje nawyki',
    myRequests: 'Moje wpisy',
    createRequest: 'Dodaj dzisiejszy wpis',
    adminPanel: 'Panel administratora nawyków',
    adminResources: 'Zarządzaj nawykami',
    adminRequests: 'Wszystkie wpisy',
    availableOnly: 'Tylko aktywne nawyki',
    algorithmResult: 'Podsumowanie postępu',
    calculatedValue: 'Punkty postępu',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Do potwierdzenia', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'Zaliczony', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Pominięty', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucony', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Zarchiwizowany', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Aktywny', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Wstrzymany', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Zarchiwizowany', classes: 'bg-red-100 text-red-800' },
}


