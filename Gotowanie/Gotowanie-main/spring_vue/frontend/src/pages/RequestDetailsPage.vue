<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AlgorithmBreakdown from '@/components/AlgorithmBreakdown.vue'
import RequestStatusBadge from '@/components/RequestStatusBadge.vue'
import { requestsApi } from '@/api/requests.api'
import { domainConfig } from '@/config/domain.config'
import { formatMoney } from '@/utils/format'
import { ApiError } from '@/types/api'
import type { RequestItem, RequestStatus } from '@/types/domain'

const route = useRoute()
const request = ref<RequestItem | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const canceling = ref(false)

const CANCELLABLE: RequestStatus[] = ['DRAFT', 'PENDING', 'CONFIRMED']

const metaEntries = computed(() => {
  const current = request.value
  if (!current) return []
  return domainConfig.request.fields
    .map((field) => ({ label: field.label, value: current.metadata[field.key] }))
    .filter((entry) => entry.value !== null && entry.value !== undefined && entry.value !== '')
})

function shiftTypeLabel(): string {
  const st = request.value?.metadata?.shiftType
  if (st === 'MORNING') return 'Zmiana poranna (7:00-15:00)'
  if (st === 'EVENING') return 'Zmiana wieczorna (15:00-23:00, +10%)'
  return 'Zmiana'
}

function getDayName(dateStr: string | null): string {
  if (!dateStr) return ''
  const days = ['Niedziela', 'Poniedziałek', 'Wtorek', 'Środa', 'Czwartek', 'Piątek', 'Sobota']
  return days[new Date(dateStr).getDay()]
}

function formatDate(dateStr: string | null): string {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('pl-PL')
}

function formatTime(dateStr: string | null): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function load(): Promise<void> {
  loading.value = true
  try {
    request.value = await requestsApi.getById(Number(route.params.id))
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Nie znaleziono zgłoszenia'
  } finally {
    loading.value = false
  }
}

async function cancel(): Promise<void> {
  if (!request.value) return
  canceling.value = true
  try {
    request.value = await requestsApi.cancel(request.value.id)
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Nie udało się anulować zgłoszenia'
  } finally {
    canceling.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="mx-auto max-w-2xl space-y-5">
    <router-link :to="{ name: 'my-requests' }" class="text-sm text-brand-600 hover:underline">
      ← {{ domainConfig.labels.myRequests }}
    </router-link>

    <div v-if="loading" class="card h-40 animate-pulse bg-gray-800"></div>
    <p v-else-if="error" class="rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-700">{{ error }}</p>

    <template v-else-if="request">
      <div class="card p-6">
        <div class="flex items-start justify-between">
          <div>
            <h1 class="text-xl font-bold text-gray-100">
              {{ domainConfig.request.singular }} #{{ request.id }}
            </h1>
            <p class="text-sm text-gray-400">{{ request.resourceName ?? request.resourceId }}</p>
          </div>
          <RequestStatusBadge :status="request.status" />
        </div>

        <dl class="mt-5 grid gap-4 sm:grid-cols-2">
          <div class="rounded-lg bg-gray-800 p-3">
            <dt class="text-xs text-gray-400">Dzień</dt>
            <dd class="text-sm font-medium text-gray-100">
              {{ getDayName(request.startAt) }}, {{ formatDate(request.startAt) }}
            </dd>
          </div>
          <div class="rounded-lg bg-gray-800 p-3">
            <dt class="text-xs text-gray-400">Zmiana</dt>
            <dd class="text-sm font-medium text-gray-100">{{ shiftTypeLabel() }}</dd>
          </div>
          <div class="rounded-lg bg-gray-800 p-3">
            <dt class="text-xs text-gray-400">Godziny</dt>
            <dd class="text-sm font-medium text-gray-100">
              {{ formatTime(request.startAt) }} – {{ formatTime(request.endAt) }}
            </dd>
          </div>
          <div class="rounded-lg bg-gray-800 p-3">
            <dt class="text-xs text-gray-400">{{ domainConfig.labels.calculatedValue }}</dt>
            <dd class="text-sm font-semibold text-gray-100">
              {{ formatMoney(request.calculatedValue, request.currency) }}
            </dd>
          </div>
          <div v-for="entry in metaEntries" :key="entry.label" class="rounded-lg bg-gray-800 p-3">
            <dt class="text-xs text-gray-400">{{ entry.label }}</dt>
            <dd class="text-sm font-medium text-gray-100">{{ entry.value }}</dd>
          </div>
        </dl>

        <button
          v-if="CANCELLABLE.includes(request.status)"
          type="button"
          class="btn-danger mt-5"
          :disabled="canceling"
          @click="cancel"
        >
          {{ canceling ? 'Anulowanie…' : 'Anuluj' }}
        </button>
      </div>

      <AlgorithmBreakdown :breakdown="request.algorithmBreakdown" />
    </template>
  </div>
</template>

