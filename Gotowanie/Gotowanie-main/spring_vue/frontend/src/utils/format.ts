/** Presentation helpers. All monetary values come from the backend algorithm — never computed here. */

export function formatMoney(value: number | null | undefined, currency: string): string {
  if (value === null || value === undefined) {
    return '—'
  }
  try {
    return new Intl.NumberFormat('pl-PL', { style: 'currency', currency }).format(value)
  } catch {
    return `${value.toFixed(2)} ${currency}`
  }
}

export function formatDateTime(iso: string | null | undefined): string {
  if (!iso) {
    return '—'
  }
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) {
    return '—'
  }
  return new Intl.DateTimeFormat('pl-PL', { dateStyle: 'medium', timeStyle: 'short' }).format(date)
}

/** Convert a <input type="datetime-local"> value (local, no timezone) to an ISO-8601 instant. */
export function localToInstant(localDateTime: string): string {
  return new Date(localDateTime).toISOString()
}
