import { http } from '@/api/httpClient'
import type { PageResponse } from '@/types/api'
import type { Resource } from '@/types/domain'

export interface AvailabilityQuery {
  start?: string | null
  end?: string | null
  quantity?: number | null
}

export const resourcesApi = {
  list: (page = 0, size = 50): Promise<PageResponse<Resource>> =>
    http.get<PageResponse<Resource>>('/dental-appointments/dentists', { page, size }),

  available: (query: AvailabilityQuery): Promise<Resource[]> =>
    http.get<Resource[]>('/resources/available', {
      start: query.start ?? undefined,
      end: query.end ?? undefined,
      quantity: query.quantity ?? undefined,
    }),

  getById: (id: number): Promise<Resource> => http.get<Resource>(`/resources/${id}`),
}
