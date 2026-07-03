import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'list',
  appName: 'Adopcje zwierząt',
  currency: 'PLN',
  requiresTimeWindow: false,
  requiresQuantity: false,
  timeMode: 'none',
  resource: {
    singular: 'Zwierzę',
    plural: 'Zwierzęta',
    fields: [
      { key: 'animalName', label: 'Imię', type: 'TEXT', required: true, options: [], helpText: 'Imię nadane zwierzęciu w schronisku' },
      { key: 'species', label: 'Gatunek', type: 'SELECT', required: true, options: ['Pies', 'Kot', 'Królik', 'Chomik', 'Świnka morska', 'Papuga', 'Inne'], helpText: 'Gatunek zwierzęcia' },
      { key: 'size', label: 'Wielkość', type: 'SELECT', required: true, options: ['Mały', 'Średni', 'Duży'], helpText: 'Wielkość zwierzęcia' },
      { key: 'temperament', label: 'Charakter', type: 'SELECT', required: true, options: ['Spokojny', 'Aktywny', 'Nieśmiały', 'Towarzyski', 'Niezależny'], helpText: 'Charakter zwierzęcia' },
      { key: 'needsGarden', label: 'Ogród', type: 'BOOLEAN', required: false, options: [], helpText: 'Wymagany ogród' },
      { key: 'monthlyCost', label: 'Koszt/mies.', type: 'NUMBER', required: true, options: [], helpText: 'Szacunkowy miesięczny koszt utrzymania (PLN)' },
    ],
  },
  request: {
    singular: 'Wniosek',
    plural: 'Wnioski',
    fields: [
      { key: 'adopterName', label: 'Adoptujący', type: 'TEXT', required: true, options: [], helpText: 'Imię i nazwisko osoby adoptującej' },
      { key: 'preferredSpecies', label: 'Gatunek', type: 'SELECT', required: true, options: ['Pies', 'Kot', 'Królik', 'Chomik', 'Świnka morska', 'Papuga', 'Inne', 'Dowolny'], helpText: 'Preferowany gatunek zwierzęcia' },
      { key: 'homeType', label: 'Dom', type: 'SELECT', required: true, options: ['Mieszkanie', 'Dom z ogrodem', 'Dom bez ogrodu', 'Gospodarstwo'], helpText: 'Typ miejsca zamieszkania' },
      { key: 'hasGarden', label: 'Ogród', type: 'BOOLEAN', required: false, options: [], helpText: 'Czy posiadasz ogród?' },
      { key: 'experienceLevel', label: 'Doświadczenie', type: 'SELECT', required: true, options: ['Brak', 'Początkujący', 'Średniozaawansowany', 'Zaawansowany'], helpText: 'Doświadczenie w opiece nad zwierzętami' },
      { key: 'budgetMonthly', label: 'Budżet', type: 'NUMBER', required: true, options: [], helpText: 'Miesięczny budżet na utrzymanie zwierzęcia (PLN)' },
    ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Panel adopcji',
    resources: 'Zwierzęta',
    myRequests: 'Moje wnioski',
    createRequest: 'Złóż wniosek',
    adminPanel: 'Panel schroniska',
    adminResources: 'Zarządzaj zwierzętami',
    adminRequests: 'Wnioski adopcyjne',
    availableOnly: 'Tylko do adopcji',
    algorithmResult: 'Ocena dopasowania',
    calculatedValue: 'Koszt adopcji',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'W analizie', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'Zaakceptowany', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Wycofany', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucony', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Zakończony', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Do adopcji', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Ukryte', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Niedostępne', classes: 'bg-red-100 text-red-800' },
}


