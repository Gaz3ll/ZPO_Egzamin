import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'list',
  appName: 'Tracker treningów',
  currency: 'KG',
  requiresTimeWindow: true,
  requiresQuantity: false,
  timeMode: 'range',
  resource: {
    singular: 'Ćwiczenie',
    plural: 'Ćwiczenia',
    fields: [
      { key: 'exerciseName', label: 'Nazwa ćwiczenia', type: 'TEXT', required: true, options: [], helpText: 'Nazwa ćwiczenia (np. Przysiad, Wyciskanie, Bieganie)' },
      { key: 'muscleGroup', label: 'Grupa mięśniowa', type: 'SELECT', required: true, options: ['Nogi', 'Plecy', 'Klatka piersiowa', 'Barki', 'Ramiona', 'Brzuch', 'Całe ciało'], helpText: 'Grupa mięśniowa' },
      { key: 'exerciseType', label: 'Rodzaj ćwiczenia', type: 'SELECT', required: true, options: ['Siłowe', 'Cardio', 'Rozciąganie', 'Kalistenika'], helpText: 'Rodzaj ćwiczenia' },
      { key: 'difficulty', label: 'Poziom trudności', type: 'SELECT', required: true, options: ['Początkujący', 'Średniozaawansowany', 'Zaawansowany'], helpText: 'Poziom trudności' },
      { key: 'equipment', label: 'Sprzęt', type: 'TEXT', required: false, options: [], helpText: 'Sprzęt potrzebny do ćwiczenia' },
    ],
  },
     request: {
       singular: 'Trening',
       plural: 'Treningi',
       fields: [
         { key: 'workoutDate', label: 'Data treningu', type: 'DATE', required: true, options: [], helpText: 'Data treningu' },
         { key: 'durationMinutes', label: 'Czas trwania (min)', type: 'NUMBER', required: true, options: [], helpText: 'Czas trwania ćwiczenia w minutach' },
         { key: 'sets', label: 'Liczba serii', type: 'NUMBER', required: true, options: [], helpText: 'Liczba serii' },
         { key: 'reps', label: 'Liczba powtórzeń', type: 'NUMBER', required: true, options: [], helpText: 'Liczba powtórzeń w serii' },
         { key: 'weight', label: 'Ciężar (kg)', type: 'NUMBER', required: false, options: [], helpText: 'Ciężar (kg)' },
         { key: 'notes', label: 'Notatki', type: 'TEXTAREA', required: false, options: [], helpText: 'Notatki (np. samopoczucie, trudności)' },
       ],
     },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Mój panel treningowy',
    resources: 'Baza ćwiczeń',
    myRequests: 'Moje treningi',
    createRequest: 'Dodaj trening',
    adminPanel: 'Panel administratora',
    adminResources: 'Zarządzaj ćwiczeniami',
    adminRequests: 'Wszystkie treningi',
    availableOnly: 'Tylko aktywne ćwiczenia',
    algorithmResult: 'Wynik treningu',
    calculatedValue: 'Całkowita objętość (kg)',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Zaplanowany', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'Wykonany', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Anulowany', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucony', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Zakończony', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Aktywne', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Nieaktywne', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Niedostępne', classes: 'bg-red-100 text-red-800' },
}


