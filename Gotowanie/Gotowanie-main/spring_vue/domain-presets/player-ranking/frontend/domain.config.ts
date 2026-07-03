import type { DomainFieldConfig, RequestStatus, ResourceStatus, DomainUiConfig } from '@/types/domain'
export const domainConfig: DomainUiConfig = {
  uiLayout: 'list',
  appName: 'System rankingowy graczy',
  currency: 'PTS',
  requiresTimeWindow: false,
  requiresQuantity: false,
  timeMode: 'none',
     resource: {
       singular: 'Turniej',
       plural: 'Turnieje',
       fields: [
         { key: 'gameName', label: 'Nazwa gry / turnieju', type: 'TEXT', required: true, options: [], helpText: 'Nazwa gry (np. CS2, LoL, Fortnite)' },
         { key: 'gameType', label: 'Typ gry', type: 'SELECT', required: true, options: ['BOARD_GAME', 'ESPORT'], helpText: 'BOARD_GAME-planszowa | ESPORT-komputerowa' },
         { key: 'maxPlayers', label: 'Maks. liczba graczy', type: 'NUMBER', required: true, options: [], helpText: 'Maksymalna liczba graczy' },
         { key: 'tournamentDate', label: 'Data turnieju', type: 'DATE', required: true, options: [], helpText: 'Data rozegrania turnieju' },
       ],
     },
     request: {
       singular: 'Mecz',
       plural: 'Mecze',
       fields: [
         { key: 'playerName', label: 'Nazwa gracza', type: 'TEXT', required: true, options: [], helpText: 'Pseudonim gracza' },
         { key: 'score', label: 'Punkty', type: 'NUMBER', required: true, options: [], help 때: 'Wynik punktowy' },
         { key: 'rank', label: 'Pozycja', type: 'NUMBER', required: true, options: [], helpText: 'Miejsce w rankingu' },
         { key: 'opponentName', label: 'Przeciwnik', type: 'TEXT', required: true, options: [], helpText: 'Nazwa przeciwnika' },
         { key: 'matchDate', label: 'Data meczu', type: 'DATE', required: true, options: [], helpText: 'Data meczu' },
         { key: 'result', label: 'Wynik', type: 'SELECT', required: true, options: ['Zwycięstwo', 'Porazka', 'Remis'], helpText: 'Wynik meczu' },
         { key: 'notes', label: 'Notatki', type: 'TEXTAREA', required: false, options: [], helpText: 'Notatki o meczu' },
       ],
     },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit',
    resources: 'Dostępne turnieje',
    myRequests: 'Moje mecze',
    createRequest: 'Dodaj mecz',
    adminPanel: 'Panel administratora rankingu',
    adminResources: 'Zarządzaj turniejami',
    adminRequests: 'Wszystkie mecze',
    availableOnly: 'Tylko aktywne turnieje',
    algorithmResult: 'Wynik rankingu',
    calculatedValue: 'Punkty rankingowe',
  },
}

export const requestStatusMeta: Record<RequestStatus, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-slate-100 text-slate-700' },
  PENDING: { label: 'Oczekujący', classes: 'bg-amber-100 text-amber-800' },
  CONFIRMED: { label: 'Potwierdzony', classes: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Anulowany', classes: 'bg-slate-200 text-slate-600' },
  REJECTED: { label: 'Odrzucony', classes: 'bg-red-100 text-red-800' },
  COMPLETED: { label: 'Zakończony', classes: 'bg-brand-100 text-brand-700' },
}

export const resourceStatusMeta: Record<ResourceStatus, { label: string; classes: string }> = {
  ACTIVE: { label: 'Aktywny', classes: 'bg-green-100 text-green-800' },
  INACTIVE: { label: 'Nieaktywny', classes: 'bg-slate-200 text-slate-600' },
  UNAVAILABLE: { label: 'Niedostępny', classes: 'bg-red-100 text-red-800' },
}


