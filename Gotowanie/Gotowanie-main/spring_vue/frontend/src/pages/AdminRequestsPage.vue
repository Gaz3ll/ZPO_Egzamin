<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AdminRequestTable from '@/components/AdminRequestTable.vue'
import { adminApi } from '@/api/admin.api'
import { domainConfig } from '@/config/domain.config'
import { useDomainStore } from '@/stores/domain.store'
import { ApiError } from '@/types/api'
import type { RequestItem, RequestStatus } from '@/types/domain'

const domain = useDomainStore()

const requests = ref<RequestItem[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const statusFilter = ref<RequestStatus | ''>('')
const updatingId = ref<number | null>(null)

async function load(): Promise<void> {
  loading.value = true
  error.value = null
  try {
    const page = await adminApi.listRequests(statusFilter.value === '' ? null : statusFilter.value, 0, 100)
    requests.value = page.items
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Nie udało się pobrać zgłoszeń'
  } finally {
    loading.value = false
  }
}

async function updateStatus(id: number, status: RequestStatus): Promise<void> {
  updatingId.value = id
  error.value = null
  try {
    const updated = await adminApi.updateRequestStatus(id, status)
    const index = requests.value.findIndex((request) => request.id === id)
    if (index !== -1) {
      requests.value[index] = updated
    }
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Nie udało się zmienić statusu'
    await load()
  } finally {
    updatingId.value = null
  }
}

async function deleteRequest(id: number): Promise<void> {
  if (!confirm('Usunąć ten wpis grafiku? Tej operacji nie można cofnąć.')) return
  try {
    await adminApi.deleteRequest(id)
    requests.value = requests.value.filter((r) => r.id !== id)
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Nie udało się usunąć'
  }
}

onMounted(load)
</script>

<template>
  <div class="space-y-5">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold text-gray-100">{{ domainConfig.labels.adminRequests }}</h1>
      <div class="flex items-center gap-2">
        <label class="text-sm text-gray-400" for="status-filter">Status:</label>
        <select id="status-filter" v-model="statusFilter" class="input w-40" @change="load">
          <option value="">Wszystkie</option>
          <option v-for="status in domain.requestStatuses" :key="status" :value="status">{{ status }}</option>
        </select>
      </div>
    </div>

    <p v-if="error" class="rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-700">{{ error }}</p>

    <div v-if="loading" class="card h-40 animate-pulse bg-gray-800"></div>
    <AdminRequestTable
      v-else
      :requests="requests"
      :statuses="domain.requestStatuses"
      :updating-id="updatingId"
      @update-status="updateStatus"
      @delete="deleteRequest"
    />
  </div>
</template>

