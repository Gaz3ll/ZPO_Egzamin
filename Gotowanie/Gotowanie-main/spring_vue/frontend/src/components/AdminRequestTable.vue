<script setup lang="ts">
import RequestStatusBadge from '@/components/RequestStatusBadge.vue'
import { domainConfig } from '@/config/domain.config'
import { formatMoney } from '@/utils/format'
import type { RequestItem, RequestStatus } from '@/types/domain'

defineProps<{
  requests: RequestItem[]
  statuses: RequestStatus[]
  updatingId?: number | null
}>()

const emit = defineEmits<{
  updateStatus: [id: number, status: RequestStatus]
  delete: [id: number]
}>()

const DELETABLE: RequestStatus[] = ['CANCELLED', 'REJECTED']

function onChange(id: number, event: Event): void {
  const value = (event.target as HTMLSelectElement).value as RequestStatus
  emit('updateStatus', id, value)
}

function getDayName(dateStr: string | null): string {
  if (!dateStr) return ''
  const days = ['Nd', 'Pon', 'Wt', 'Śr', 'Czw', 'Pt', 'Sob']
  return days[new Date(dateStr).getDay()]
}

function requestLabel(r: RequestItem): string {
  const m = r.metadata as Record<string, unknown> | undefined
  if (m?.renterName) return String(m.renterName)
  if (m?.shiftType === 'MORNING') return 'Poranna'
  if (m?.shiftType === 'EVENING') return 'Wieczorna'
  if (m?.meetingTitle) return String(m.meetingTitle)
  return '—'
}

function formatShiftTime(startAt: string | null, endAt: string | null): string {
  if (!startAt || !endAt) return '—'
  const s = new Date(startAt)
  const e = new Date(endAt)
  return `${String(s.getHours()).padStart(2, '0')}:${String(s.getMinutes()).padStart(2, '0')}-${String(e.getHours()).padStart(2, '0')}:${String(e.getMinutes()).padStart(2, '0')}`
}
</script>

<template>
  <div class="card overflow-x-auto">
    <table class="w-full text-sm">
      <thead class="border-b border-gray-700 bg-gray-800 text-left text-xs uppercase tracking-wide text-gray-400">
        <tr>
          <th class="px-4 py-3">ID</th>
          <th class="px-4 py-3">{{ domainConfig.resource.singular }}</th>
          <th class="px-4 py-3">Rezerwujący</th>
          <th class="px-4 py-3">Dzień</th>
          <th class="px-4 py-3">Godziny</th>
          <th class="px-4 py-3 text-right">Koszt</th>
          <th class="px-4 py-3">Status</th>
          <th class="px-4 py-3">Zmień</th>
          <th class="px-4 py-3"></th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="requests.length === 0">
          <td colspan="9" class="px-4 py-8 text-center text-gray-400">Brak zgłoszeń.</td>
        </tr>
        <tr v-for="request in requests" :key="request.id" class="border-b border-gray-800 last:border-0">
          <td class="px-4 py-3 font-mono text-xs text-gray-400">#{{ request.id }}</td>
          <td class="px-4 py-3 font-medium text-gray-200">{{ request.resourceName ?? request.resourceId }}</td>
          <td class="px-4 py-3 text-gray-300">
            {{ requestLabel(request) }}
          </td>
          <td class="px-4 py-3 text-gray-300">
            <div>{{ getDayName(request.startAt) }}</div>
            <div class="text-xs text-gray-500">{{ request.startAt ? new Date(request.startAt).toLocaleDateString('pl-PL') : '—' }}</div>
          </td>
          <td class="px-4 py-3 text-xs text-gray-400">
            {{ formatShiftTime(request.startAt, request.endAt) }}
          </td>
          <td class="px-4 py-3 text-right tabular-nums text-gray-200">
            {{ formatMoney(request.calculatedValue, request.currency) }}
          </td>
          <td class="px-4 py-3"><RequestStatusBadge :status="request.status" /></td>
          <td class="px-4 py-3">
            <select
              class="input py-1 text-xs" :value="request.status"
              :disabled="updatingId === request.id"
              @change="onChange(request.id, $event)"
            >
              <option v-for="status in statuses" :key="status" :value="status">{{ status }}</option>
            </select>
          </td>
          <td class="px-4 py-3">
            <div class="flex items-center gap-2">
              <router-link class="text-brand-600 hover:underline" :to="{ name: 'request-details', params: { id: request.id } }">Szczegóły</router-link>
              <button v-if="DELETABLE.includes(request.status)" type="button" class="text-red-400 hover:underline" @click="emit('delete', request.id)">Usuń</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
