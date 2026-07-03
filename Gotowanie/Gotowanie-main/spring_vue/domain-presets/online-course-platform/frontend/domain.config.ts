import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'cards',
  appName: 'Platforma kursów online',
  currency: 'PLN',
  requiresTimeWindow: false,
  requiresQuantity: false,
  timeMode: 'none',
  resource: {
    singular: 'Kurs',
    plural: 'Kursy',
    fields: [
      { key: 'title', label: 'Tytuł', type: 'TEXT', required: true, options: [], helpText: 'Tytuł kursu lub lekcji (np. Podstawy Javy, Wprowadzenie do Pythona)' },
      {
        key: 'category',
        label: 'Kategoria',
        type: 'SELECT',
        required: true,
        options: ['PROGRAMMING', 'MATH', 'LANGUAGE', 'BUSINESS', 'DESIGN'],
        helpText: 'Kategoria kursu',
      },
      {
        key: 'difficulty',
        label: 'Poziom trudności',
        type: 'SELECT',
        required: true,
        options: ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'],
        helpText: 'Poziom trudności kursu',
      },
      { key: 'totalLessons', label: 'Liczba lekcji', type: 'NUMBER', required: true, options: [], helpText: 'Łączna liczba lekcji w kursie (np. 20)' },
      { key: 'estimatedHours', label: 'Szacowany czas (h)', type: 'NUMBER', required: false, options: [], helpText: 'Orientacyjny czas potrzebny na ukończenie kursu w godzinach' },
    ],
  },
  request: {
    singular: 'Postęp',
    plural: 'Postępy',
    fields: [
      {
        key: 'lessonsCompleted',
        label: 'Ukończone lekcje',
        type: 'NUMBER',
        required: true,
        options: [],
        helpText: 'Liczba lekcji, które użytkownik już przerobił',
      },
      {
        key: 'progressPercent',
        label: 'Postęp (%)',
        type: 'NUMBER',
        required: false,
        options: [],
        helpText: 'Procent ukończenia kursu – wyliczany automatycznie na podstawie ukończonych lekcji',
      },
      {
        key: 'completed',
        label: 'Ukończony',
        type: 'BOOLEAN',
        required: false,
        options: [],
        helpText: 'Czy kurs został zaliczony – automatycznie ustawiane gdy postęp osiągnie 100%',
      },
    ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Mój pulpit',
    resources: 'Dostępne kursy',
    myRequests: 'Moje postępy',
    createRequest: 'Dodaj postęp',
    adminPanel: 'Panel administracyjny platformy',
    adminResources: 'Zarządzaj kursami',
    adminRequests: 'Wszystkie postępy',
    availableOnly: 'Tylko aktywne kursy',
    algorithmResult: 'Podsumowanie postępu',
    calculatedValue: 'Procent ukończenia',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Oczekujący', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'Aktywny', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Anulowany', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucony', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Zakończony', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Aktywny', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Nieaktywny', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Niedostępny', classes: 'bg-red-100 text-red-800' },
}


