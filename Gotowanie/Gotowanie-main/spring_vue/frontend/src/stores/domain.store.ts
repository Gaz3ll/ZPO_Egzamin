import { defineStore } from 'pinia'
import { ref } from 'vue'
import { configApi } from '@/api/admin.api'
import { resourcesApi } from '@/api/resources.api'
import { domainConfig } from '@/config/domain.config'
import type { RequestStatus, Resource, ResourceStatus } from '@/types/domain'

/**
 * Holds the static UI config (labels/fields) plus enum vocabularies fetched from the backend
 * (GET /api/config) and a cache of resources. Demonstrates the backend as source of truth while
 * the UI text stays editable in domain.config.ts.
 */
export const useDomainStore = defineStore('domain', () => {
  const config = domainConfig
  const backendDomainName = ref<string | null>(null)
  const requestStatuses = ref<RequestStatus[]>([
    'DRAFT',
    'PENDING',
    'CONFIRMED',
    'CANCELLED',
    'REJECTED',
    'COMPLETED',
  ])
  const resourceStatuses = ref<ResourceStatus[]>(['ACTIVE', 'INACTIVE', 'UNAVAILABLE'])
  const resources = ref<Resource[]>([])
  const resourcesLoading = ref(false)

  async function loadConfig(): Promise<void> {
    try {
      const view = await configApi.get()
      backendDomainName.value = view.profile.domainName
      requestStatuses.value = view.requestStatuses
      resourceStatuses.value = view.resourceStatuses
    } catch {
      // Fall back to the static defaults if the endpoint is unreachable.
    }
  }

  async function loadResources(): Promise<Resource[]> {
    resourcesLoading.value = true
    try {
      const page = await resourcesApi.list(0, 100)
      resources.value = page.items
      return resources.value
    } finally {
      resourcesLoading.value = false
    }
  }

  return {
    config,
    backendDomainName,
    requestStatuses,
    resourceStatuses,
    resources,
    resourcesLoading,
    loadConfig,
    loadResources,
  }
})
