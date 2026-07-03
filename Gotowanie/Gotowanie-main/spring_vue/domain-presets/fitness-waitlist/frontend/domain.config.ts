export const domainConfig = {
  appName: 'Fitness - Listy rezerwowe',
  currency: 'PLN',
  requiresTimeWindow: false,
  requiresQuantity: false,
  timeMode: 'none' as const,
  resource: {
    singular: 'Zajęcia',
    plural: 'Zajęcia',
    fields: [
      { key: 'type', label: 'Typ zajęć', type: 'SELECT' as const, required: true, options: ['Joga','CrossFit','Zumba','Pilates','Spinning','Aerobik'] as any, helpText: 'Rodzaj zajęć' },
      { key: 'maxCapacity', label: 'Maks. uczestników', type: 'NUMBER' as const, required: true, options: [] as string[], helpText: 'Limit miejsc na liście głównej' },
    ],
  },
  request: {
    singular: 'Zapis',
    plural: 'Zapisy',
    fields: [
      { key: 'userName', label: 'Imię i nazwisko', type: 'TEXT' as const, required: true, options: [] as string[], helpText: 'Osoba zapisująca się' },
      { key: 'listType', label: 'Lista', type: 'SELECT' as const, required: false, options: ['GŁÓWNA','REZERWOWA'] as any, helpText: 'Główna lub rezerwowa' },
      { key: 'waitlistPosition', label: 'Pozycja w rezerwie', type: 'NUMBER' as const, required: false, options: [] as string[], helpText: '0=lista główna, >0=rezerwowa' },
    ],
  },
  labels: {
    login: 'Zaloguj się',
    register: 'Utwórz konto',
    logout: 'Wyloguj',
    dashboard: 'Pulpit',
    resources: 'Zajęcia fitness',
    myRequests: 'Moje zapisy',
    createRequest: 'Zapisz się',
    adminPanel: 'Panel admina',
    adminResources: 'Zarządzaj zajęciami',
    adminRequests: 'Wszystkie zapisy',
    calendar: 'Kalendarz',
    availableOnly: 'Tylko aktywne',
    algorithmResult: 'Status zapisu',
    calculatedValue: '',
    delete: 'Usuń',
    occupied: 'Pełne',
    free: 'Wolne miejsca',
    morning: '',
    evening: '',
  },
}

export const requestStatusMeta: Record<string, { label: string; classes: string }> = {
  DRAFT: { label: 'Szkic', classes: 'bg-gray-800 text-gray-400' },
  PENDING: { label: 'Rezerwowa', classes: 'bg-amber-500/10 text-amber-400' },
  CONFIRMED: { label: 'Główna', classes: 'bg-green-500/10 text-green-400' },
  CANCELLED: { label: 'Wypisany', classes: 'bg-gray-700 text-gray-500' },
  REJECTED: { label: 'Odrzucony', classes: 'bg-red-500/10 text-red-400' },
  COMPLETED: { label: 'Uczestniczył', classes: 'bg-brand-500/10 text-brand-400' },
}

export const resourceStatusMeta: Record<string, { label: string; classes: string }> = {
  ACTIVE: { label: 'Aktywne', classes: 'bg-green-500/10 text-green-400' },
  INACTIVE: { label: 'Nieaktywne', classes: 'bg-gray-700 text-gray-500' },
  UNAVAILABLE: { label: 'Odwołane', classes: 'bg-red-500/10 text-red-400' },
}

export const contractHoursSuggestions: Record<string, string> = {}
