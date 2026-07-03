export const domainConfig = {
  appName: 'Platforma e-learningowa',
  currency: '%',
  requiresTimeWindow: false,
  requiresQuantity: false,
  timeMode: 'none' as const,
  resource: {
    singular: 'Quiz',
    plural: 'Quizy',
    fields: [
      { key: 'questionCount', label: 'Liczba pytań', type: 'NUMBER' as const, required: true, options: [] as string[], helpText: 'Ile pytań w quizie' },
      { key: 'passThreshold', label: 'Próg zaliczenia (%)', type: 'NUMBER' as const, required: true, options: [] as string[], helpText: 'Minimalny % do zaliczenia (np. 50)' },
    ],
  },
  request: {
    singular: 'Podejście',
    plural: 'Podejścia',
    fields: [
      { key: 'studentName', label: 'Imię i nazwisko', type: 'TEXT' as const, required: true, options: [] as string[], helpText: 'Student podchodzący do quizu' },
      { key: 'correctAnswers', label: 'Poprawne odpowiedzi', type: 'NUMBER' as const, required: true, options: [] as string[], helpText: 'Liczba poprawnych' },
      { key: 'wrongAnswers', label: 'Błędne odpowiedzi', type: 'NUMBER' as const, required: false, options: [] as string[], helpText: 'Liczba błędnych' },
    ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit',
    resources: 'Quizy',
    myRequests: 'Moje wyniki',
    createRequest: 'Rozwiąż quiz',
    adminPanel: 'Panel nauczyciela',
    adminResources: 'Zarządzaj quizami',
    adminRequests: 'Wszystkie podejścia',
    calendar: '',
    availableOnly: 'Tylko aktywne',
    algorithmResult: 'Wynik quizu',
    calculatedValue: 'Wynik',
    delete: 'Usuń',
    occupied: 'Zaliczone',
    free: 'Dostępny',
    morning: '',
    evening: '',
  },
}

export const requestStatusMeta: Record<string, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-gray-800 text-gray-400' },
  PENDING: { label: 'W trakcie', classes: 'bg-amber-500/10 text-amber-400' },
  CONFIRMED: { label: 'Ocenione', classes: 'bg-green-500/10 text-green-400' },
  CANCELLED: { label: 'Anulowane', classes: 'bg-gray-700 text-gray-500' },
  REJECTED: { label: 'Odrzucone', classes: 'bg-red-500/10 text-red-400' },
  COMPLETED: { label: 'Zakończone', classes: 'bg-brand-500/10 text-brand-400' },
}

export const resourceStatusMeta: Record<string, { label: string; classes: string }> = {
  ACTIVE: { label: 'Aktywny', classes: 'bg-green-500/10 text-green-400' },
  INACTIVE: { label: 'Nieaktywny', classes: 'bg-gray-700 text-gray-500' },
  UNAVAILABLE: { label: 'Niedostępny', classes: 'bg-red-500/10 text-red-400' },
}

export const contractHoursSuggestions: Record<string, string> = {}
