import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'list',
  appName: 'Zapisy na zajęcia fitness',
  currency: 'PLN',
  requiresTimeWindow: true,
  requiresQuantity: true,
  timeMode: 'slots',
  resource: {
    singular: 'Zajęcia',
    plural: 'Zajęcia',
    fields: [
      { key: 'className', label: 'Zajęcia', type: 'TEXT', required: true, options: [], helpText: 'Nazwa zajęć (np. Joga, CrossFit, Zumba)' },
      { key: 'trainerName', label: 'Trener', type: 'TEXT', required: true, options: [], helpText: 'Imię i nazwisko trenera prowadzącego' },
      { key: 'difficultyLevel', label: 'Poziom', type: 'SELECT', required: true, options: ['Początkujący', 'Średniozaawansowany', 'Zaawansowany'], helpText: 'Poziom trudności zajęć' },
      { key: 'capacity', label: 'Miejsca', type: 'NUMBER', required: true, options: [], helpText: 'Maksymalna liczba uczestników' },
      { key: 'equipmentRequired', label: 'Sprzęt', type: 'TEXT', required: false, options: [], helpText: 'Wymagany sprzęt (wypisz po przecinku)' },
      { key: 'dropInPrice', label: 'Cena wejścia', type: 'NUMBER', required: true, options: [], helpText: 'Cena za pojedyncze wejście (PLN)' },
    ],
  },
  request: {
    singular: 'Zapis',
    plural: 'Zapisy',
    fields: [
      { key: 'memberName', label: 'Uczestnik', type: 'TEXT', required: true, options: [], helpText: 'Imię i nazwisko uczestnika' },
      { key: 'preferredDifficulty', label: 'Poziom', type: 'SELECT', required: true, options: ['Początkujący', 'Średniozaawansowany', 'Zaawansowany'], helpText: 'Preferowany poziom trudności' },
      { key: 'passType', label: 'Karnet', type: 'SELECT', required: true, options: ['Wejście jednorazowe', 'Karnet 4 wejścia', 'Karnet miesięczny', 'Multisport'], helpText: 'Rodzaj karnetu' },
      { key: 'needsEquipment', label: 'Sprzęt', type: 'BOOLEAN', required: false, options: [], helpText: 'Czy potrzebujesz wypożyczyć sprzęt?' },
      { key: 'healthNotes', label: 'Uwagi', type: 'TEXTAREA', required: false, options: [], helpText: 'Informacje zdrowotne lub uwagi dla trenera' },
    ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Grafik fitness',
    resources: 'Zajęcia',
    myRequests: 'Moje zapisy',
    createRequest: 'Zapisz się',
    adminPanel: 'Panel recepcji',
    adminResources: 'Zarządzaj zajęciami',
    adminRequests: 'Wszystkie zapisy',
    availableOnly: 'Tylko aktywne zajęcia',
    algorithmResult: 'Miejsca i cena',
    calculatedValue: 'Koszt zapisu',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Oczekuje', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'Zapisany', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Anulowany', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucony', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Odbyty', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Aktywne', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Nieaktywne', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Pełne', classes: 'bg-red-100 text-red-800' },
}


