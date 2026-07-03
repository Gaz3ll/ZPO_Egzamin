import { http } from '@/api/httpClient'
import type { PageResponse } from '@/types/api'
import type { DomainConfigView } from '@/types/domain'
import type { CreateResourcePayload, RequestItem, RequestStatus, Resource } from '@/types/domain'

export const adminApi = {
  listResources: (page = 0, size = 50): Promise<PageResponse<Resource>> =>
    http.get<PageResponse<Resource>>('/admin/dental/dentists', { page, size }),

  createResource: (payload: CreateResourcePayload): Promise<Resource> =>
    http.post<Resource>('/admin/dental/dentists', payload),

  updateResource: (id: number, payload: CreateResourcePayload): Promise<Resource> =>
    http.put<Resource>(`/admin/dental/dentists/${id}`, payload),

  deleteResource: (id: number): Promise<void> =>
    http.del<void>(`/admin/dental/dentists/${id}`),

  listRequests: (status: RequestStatus | null, page = 0, size = 50): Promise<PageResponse<RequestItem>> =>
    http.get<PageResponse<RequestItem>>('/admin/dental/appointments', {
      status: status ?? undefined,
      page,
      size,
    }),

  updateRequestStatus: (id: number, status: RequestStatus): Promise<RequestItem> =>
    http.patch<RequestItem>(`/admin/dental/appointments/${id}/status`, { status }),

  deleteRequest: (id: number): Promise<void> =>
    http.del<void>(`/admin/dental/appointments/${id}`),
}

export const configApi = {
  get: (): Promise<DomainConfigView> => http.get<DomainConfigView>('/config'),
}
