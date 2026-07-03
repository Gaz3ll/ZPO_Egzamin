import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'list',
  appName: 'Planer Nauki',
  currency: 'MIN',
  requiresTimeWindow: true,
  requiresQuantity: false,
  timeMode: 'range',
     resource: {
       singular: 'Egzamin',
       plural: 'Egzaminy',
       fields: [
         { key: 'examDate', label: 'Data egzaminu', type: 'DATE', required: true, options: [], helpText: 'Dzień egzaminu' },
         { key: 'subject', label: 'Przedmiot', type: 'TEXT', required: true, options: [], helpText: 'Nazwa przedmiotu' },
         { key: 'difficulty', label: 'Trudność', type: 'SELECT', required: true, options: ['Łatwy', 'Średni', 'Trudny'], helpText: 'Poziom trudności' },
         { key: 'materialCount', label: 'Ilość materiału', type: 'NUMBER', required: true, options: [], helpText: 'Liczba tematów / stron' },
         { key: 'materialUnit', label: 'Jednostka materiału', type: 'SELECT', required: true, options: ['Tematy', 'Rozdziały', 'Strony'], helpText: 'Czym liczony jest materiał' },
         { key: 'topics', label: 'Lista tematów', type: 'TEXTAREA', required: false, options: [], helpText: 'Tematy do przerobienia' },
         { key: 'dailyStudyLimitMinutes', label: 'Limit nauki dziennie', type: 'NUMBER', required: false, options: [], helpText: 'Maksymalny czas nauki na dzień' },
         { key: 'importance', label: 'Priorytet', type: 'SELECT', required: true, options: ['Niski', 'Średni', 'Wysoki', 'Krytyczny'], helpText: 'Ważność przedmiotu' },
       ],
     },
     request: {
       singular: 'Plan nauki',
       plural: 'Plany nauki',
       fields: [
         { key: 'studyDate', label: 'Data nauki', type: 'DATE', required: true, options: [], helpText: 'Data sesji nauki' },
         { key: 'studyMinutes', label: 'Czas nauki (min)', type: 'NUMBER', required: true, options: [], helpText: 'Ile czasu planujesz poświęcić' },
         { key: 'materialDone', label: 'Przerobiony materiał', type: 'NUMBER', required: true, options: [], helpText: 'Ilość przerobionego materiału' },
         { key: 'selectedTopics', label: 'Wybrane tematy', type: 'TEXTAREA', required: false, options: [], helpText: 'Podzbiór materiału' },
         { key: 'priority', label: 'Priorytet', type: 'SELECT', required: false, options: ['Niski', 'Normalny', 'Wysoki'], helpText: 'Priorytet sesji' },
         { key: 'isRevision', label: 'Sesja powtórkowa', type: 'BOOLEAN', required: false, options: [], helpText: 'Czy to powtórka' },
         { key: 'completed', label: 'Zrealizowany', type: 'BOOLEAN', required: false, options: [], helpText: 'Czy plan został zrealizowany' },
         { key: 'notes', label: 'Notatki', type: 'TEXTAREA', required: false, options: [], helpText: 'Uwagi do sesji' },
       ],
     },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit nauki',
    resources: 'Kalendarz egzaminów',
    myRequests: 'Moje plany',
    createRequest: 'Wygeneruj plan nauki',
    adminPanel: 'Panel administratora egzaminów',
    adminResources: 'Zarządzaj egzaminami',
    adminRequests: 'Wszystkie plany nauki',
    availableOnly: 'Tylko nadchodzące egzaminy',
    algorithmResult: 'Rozkład materiału',
    calculatedValue: 'Łączny czas nauki (min)',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Oczekujący', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'Zaplanowany', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Anulowany', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucony', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Zrealizowany', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Nadchodzący', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Zakończony', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Odwołany', classes: 'bg-red-100 text-red-800' },
}


