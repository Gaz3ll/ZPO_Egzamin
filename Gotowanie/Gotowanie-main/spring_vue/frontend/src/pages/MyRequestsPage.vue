<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import RequestStatusBadge from '@/components/RequestStatusBadge.vue'
import { requestsApi } from '@/api/requests.api'
import { domainConfig } from '@/config/domain.config'
import { formatMoney } from '@/utils/format'
import { ApiError } from '@/types/api'
import type { RequestItem, RequestStatus } from '@/types/domain'

const requests = ref<RequestItem[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const cancelingId = ref<number | null>(null)

const showTimeColumns = computed(() => (domainConfig.timeMode as string) !== 'none')
const colSpan = computed(() => showTimeColumns.value ? 8 : 6)

const CANCELLABLE: RequestStatus[] = ['DRAFT', 'PENDING', 'CONFIRMED']

function getDayName(dateStr: string | null): string {
  if (!dateStr) return ''
  const days = ['Niedziela', 'Poniedziałek', 'Wtorek', 'Środa', 'Czwartek', 'Piątek', 'Sobota']
  return days[new Date(dateStr).getDay()]
}

function shiftTypeLabel(metadata: Record<string, unknown>): string {
  const st = metadata.shiftType
  if (st === 'MORNING') return 'Poranna'
  if (st === 'EVENING') return 'Wieczorna'
  return '—'
}

function requestInfo(r: RequestItem): string {
  if (r.metadata?.shiftType) return shiftTypeLabel(r.metadata as Record<string, unknown>)
  if (r.metadata?.meetingTitle) return String(r.metadata.meetingTitle)
  if (r.metadata?.taskName) return String(r.metadata.taskName)
  return '—'
}

function formatShiftTime(startAt: string | null, endAt: string | null): string {
  if (!startAt || !endAt) return '—'
  const s = new Date(startAt)
  const e = new Date(endAt)
  return `${String(s.getHours()).padStart(2, '0')}:${String(s.getMinutes()).padStart(2, '0')} - ${String(e.getHours()).padStart(2, '0')}:${String(e.getMinutes()).padStart(2, '0')}`
}

async function load(): Promise<void> {
  loading.value = true
  try {
    const page = await requestsApi.myRequests(0, 100)
    requests.value = page.items
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Nie udało się pobrać zgłoszeń'
  } finally {
    loading.value = false
  }
}

async function cancel(id: number): Promise<void> {
  cancelingId.value = id
  try {
    const updated = await requestsApi.cancel(id)
    const index = requests.value.findIndex((r) => r.id === id)
    if (index !== -1) requests.value[index] = updated
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Nie udało się anulować'
  } finally {
    cancelingId.value = null
  }
}

onMounted(load)
</script>

<template>
  <div class="space-y-5">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold text-gray-100">{{ domainConfig.labels.myRequests }}</h1>
      <router-link class="btn-primary" :to="{ name: 'create-request' }">
        {{ domainConfig.labels.createRequest }}
      </router-link>
    </div>

    <p v-if="error" class="rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-400">{{ error }}</p>

    <div v-if="loading" class="card h-40 animate-pulse bg-gray-800"></div>

    <div v-else class="card overflow-x-auto">
      <table class="w-full text-sm">
        <thead class="border-b border-gray-700 bg-gray-800 text-left text-xs uppercase tracking-wide text-gray-400">
          <tr>
            <th class="px-4 py-3">ID</th>
            <th v-if="showTimeColumns" class="px-4 py-3">Dzień</th>
            <th class="px-4 py-3">{{ domainConfig.request.singular }}</th>
            <th class="px-4 py-3">{{ domainConfig.resource.singular }}</th>
            <th v-if="showTimeColumns" class="px-4 py-3">Godziny</th>
            <th class="px-4 py-3 text-right">Koszt</th>
            <th class="px-4 py-3">Status</th>
            <th class="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="requests.length === 0">
            <td :colspan="colSpan" class="px-4 py-8 text-center text-gray-400">Brak wpisów.</td>
          </tr>
          <tr v-for="request in requests" :key="request.id" class="border-b border-gray-800 last:border-0">
            <td class="px-4 py-3 font-mono text-xs text-gray-400">#{{ request.id }}</td>
            <td v-if="showTimeColumns" class="px-4 py-3 text-gray-300">
              <div class="font-medium">{{ getDayName(request.startAt) }}</div>
              <div class="text-xs text-gray-500">{{ request.startAt ? new Date(request.startAt).toLocaleDateString('pl-PL') : '—' }}</div>
            </td>
            <td class="px-4 py-3">
              <span class="rounded bg-gray-800 px-2 py-0.5 text-xs font-medium text-gray-300">
                {{ requestInfo(request) }}
              </span>
            </td>
            <td class="px-4 py-3 text-gray-300">{{ request.resourceName ?? request.resourceId }}</td>
            <td v-if="showTimeColumns" class="px-4 py-3 text-xs text-gray-400">{{ formatShiftTime(request.startAt, request.endAt) }}</td>
            <td class="px-4 py-3 text-right tabular-nums text-gray-200">
              {{ formatMoney(request.calculatedValue, request.currency) }}
            </td>
            <td class="px-4 py-3"><RequestStatusBadge :status="request.status" /></td>
            <td class="px-4 py-3">
              <div class="flex items-center gap-3">
                <router-link class="text-brand-600 hover:underline" :to="{ name: 'request-details', params: { id: request.id } }">Szczegóły</router-link>
                <button
                  v-if="CANCELLABLE.includes(request.status)"
                  type="button" class="text-red-400 hover:underline disabled:opacity-50"
                  :disabled="cancelingId === request.id" @click="cancel(request.id)"
                >Anuluj</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
