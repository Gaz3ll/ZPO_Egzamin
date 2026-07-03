import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'list',
  appName: 'Office Plant Care',
  currency: 'PKT',
  requiresTimeWindow: true,
  requiresQuantity: false,
  timeMode: 'range',
     resource: {
       singular: 'Roślina',
       plural: 'Rośliny',
       fields: [
         { key: 'species', label: 'Gatunek', type: 'TEXT', required: true, options: [], helpText: 'Gatunek rośliny' },
         { key: 'location', label: 'Pomieszczenie', type: 'TEXT', required: true, options: [], helpText: 'Pomieszczenie gdzie stoi roślina' },
         { key: 'lightRequirement', label: 'Wymagania świetlne', type: 'SELECT', required: true, options: ['Cień', 'Półcień', 'Rozproszone światło', 'Pełne słońce'], helpText: 'Wymagania świetlne' },
         { key: 'waterFrequencyDays', label: 'Podlewanie co (dni)', type: 'NUMBER', required: true, options: [], helpText: 'Jak często podlewać' },
         { key: 'fertilizeFrequencyDays', label: 'Nawożenie co (dni)', type: 'NUMBER', required: false, options: [], helpText: 'Jak często nawozić' },
         { key: 'repotFrequencyMonths', label: 'Przesadzanie co (mies.)', type: 'NUMBER', required: false, options: [], helpText: 'Jak często przesadzać' },
         { key: 'difficulty', label: 'Trudność opieki', type: 'SELECT', required: true, options: ['Łatwy', 'Średni', 'Trudny'], helpText: 'Jak wymagająca jest roślina' },
         { key: 'isAdopted', label: 'Zaadoptowana', type: 'BOOLEAN', required: false, options: [], helpText: 'Czy roślina ma już opiekuna' },
         { key: 'healthStatus', label: 'Stan zdrowia', type: 'SELECT', required: false, options: ['Dobry', 'OK', 'Zły', 'Krytyczny'], helpText: 'Kondycja rośliny' },
       ],
     },
     request: {
       singular: 'Zadanie opieki',
       plural: 'Zadania opieki',
       fields: [
         { key: 'careType', label: 'Rodzaj zabiegu', type: 'SELECT', required: true, options: ['Podlewanie', 'Nawożenie', 'Przesadzanie', 'Przycinanie', 'Zraszanie'], helpText: 'Rodzaj zabiegu' },
         { key: 'lastCareAt', label: 'Data zabiegu', type: 'DATE', required: true, options: [], helpText: 'Data wykonania zabiegu' },
         { key: 'plantCondition', label: 'Stan rośliny', type: 'TEXT', required: false, options: [], helpText: 'Obserwacje (np. żółknące liście)' },
         { key: 'notes', label: 'Notatki', type: 'TEXTAREA', required: false, options: [], helpText: 'Uwagi' },
         { key: 'isCompleted', label: 'Wykonane', type: 'BOOLEAN', required: false, options: [], helpText: 'Czy zadanie zostało wykonane' },
         { key: 'adopterName', label: 'Opiekun', type: 'TEXT', required: true, options: [], helpText: 'Osoba odpowiedzialna za zabieg' },
       ],
     },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit opieki nad roślinami',
    resources: 'Rejestr roślin',
    myRequests: 'Moje rośliny i zadania',
    createRequest: 'Dodaj zadanie opieki',
    adminPanel: 'Panel opieki nad roślinami',
    adminResources: 'Zarządzaj roślinami',
    adminRequests: 'Wszystkie zadania opieki',
    availableOnly: 'Tylko dostępne rośliny',
    algorithmResult: 'Harmonogram opieki',
    calculatedValue: 'Priorytet opieki',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Do zrobienia', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'Zaplanowane', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Anulowane', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucone', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Wykonane', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Zdrowa / dostępna', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Wycofana', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Kwarantanna', classes: 'bg-red-100 text-red-800' },
}


