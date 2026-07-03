import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'cards',
  appName: 'Catering Planner',
  currency: 'PLN',
  requiresTimeWindow: false,
  requiresQuantity: true,
  timeMode: 'none',
  resource: {
    singular: 'Posiłek',
    plural: 'Posiłki',
     fields: [
       { key: 'mealType', label: 'Rodzaj posiłku', type: 'SELECT', required: true, options: ['Śniadanie', 'Drugie śniadanie', 'Obiad', 'Podwieczorek', 'Kolacja', 'Deser'], helpText: 'Rodzaj posiłku' },
       { key: 'dietType', label: 'Dieta', type: 'SELECT', required: true, options: ['Standardowa', 'Wegetariańska', 'Wegańska', 'Bezglutenowa', 'Niskokaloryczna', 'Keto'], helpText: 'Typ diety' },
       { key: 'calories', label: 'Kalorie na porcję', type: 'NUMBER', required: true, options: [], helpText: 'Liczba kalorii na porcję' },
       { key: 'protein', label: 'Białko (g)', type: 'NUMBER', required: false, options: [], helpText: 'Gramy białka' },
       { key: 'carbs', label: 'Węglowodany (g)', type: 'NUMBER', required: false, options: [], helpText: 'Gramy węglowodanów' },
       { key: 'fat', label: 'Tłuszcz (g)', type: 'NUMBER', required: false, options: [], helpText: 'Gramy tłuszczu' },
       { key: 'allergens', label: 'Alergeny', type: 'TEXTAREA', required: true, options: [], helpText: 'Lista alergenów (oddzielone przecinkami)' },
     ],
  },
  request: {
    singular: 'Plan diety',
    plural: 'Plany diety',
     fields: [
       { key: 'daysCount', label: 'Liczba dni', type: 'NUMBER', required: true, options: [], helpText: 'Liczba dni planowania' },
       { key: 'mealsPerDay', label: 'Posiłki dziennie', type: 'NUMBER', required: true, options: [], helpText: 'Liczba posiłków dziennie' },
       { key: 'dietType', label: 'Dieta', type: 'SELECT', required: true, options: ['Standardowa', 'Wegetariańska', 'Wegańska', 'Bezglutenowa', 'Niskokaloryczna', 'Keto'], helpText: 'Wybrany rodzaj diety' },
       { key: 'targetCalories', label: 'Cel kaloryczny', type: 'NUMBER', required: true, options: [], helpText: 'Docelowe kcal na dzień' },
       { key: 'portionMultiplier', label: 'Wielkość porcji', type: 'SELECT', required: true, options: ['0.5', '1.0', '2.0'], helpText: 'Połowa / normalna / podwójna' },
       { key: 'excludedAllergens', label: 'Wykluczone alergeny', type: 'TEXTAREA', required: false, options: [], helpText: 'Alergie i nietolerancje pokarmowe' },
       { key: 'preferredMealTypes', label: 'Preferowane posiłki', type: 'TEXT', required: false, options: [], helpText: 'Np. BREAKFAST,LUNCH,DINNER' },
     ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit cateringu',
    resources: 'Baza posiłków',
    myRequests: 'Moje plany',
    createRequest: 'Wygeneruj plan diety',
    adminPanel: 'Panel cateringu',
    adminResources: 'Zarządzaj posiłkami',
    adminRequests: 'Wszystkie plany diety',
    availableOnly: 'Tylko dostępne posiłki',
    algorithmResult: 'Podsumowanie planu',
    calculatedValue: 'Cena planu',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Oczekujący', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'Potwierdzony', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Anulowany', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucony', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Dostarczony', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'W menu', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Poza sezonem', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Wyprzedany', classes: 'bg-red-100 text-red-800' },
}


