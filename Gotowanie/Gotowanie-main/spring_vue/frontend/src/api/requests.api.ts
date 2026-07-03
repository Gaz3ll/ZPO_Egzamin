import { http } from '@/api/httpClient'
import type { PageResponse } from '@/types/api'
import type { CreateRequestPayload, RequestItem } from '@/types/domain'

export const requestsApi = {
  create: (payload: CreateRequestPayload): Promise<RequestItem> =>
    http.post<RequestItem>('/requests', payload),

  myRequests: (page = 0, size = 50): Promise<PageResponse<RequestItem>> =>
    http.get<PageResponse<RequestItem>>('/requests/my', { page, size }),

  getById: (id: number): Promise<RequestItem> => http.get<RequestItem>(`/requests/${id}`),

  cancel: (id: number): Promise<RequestItem> => http.del<RequestItem>(`/requests/${id}`),

  returnRequest: (id: number): Promise<RequestItem> => http.patch<RequestItem>(`/requests/${id}/return`),

  byResource: (resourceId: number): Promise<RequestItem[]> =>
    http.get<RequestItem[]>(`/requests/by-resource/${resourceId}`),
}
