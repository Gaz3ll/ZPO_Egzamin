<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { requestsApi } from '@/api/requests.api'
import { resourcesApi } from '@/api/resources.api'
import { domainConfig } from '@/config/domain.config'
import { useAuthStore } from '@/stores/auth.store'
import { useDomainStore } from '@/stores/domain.store'
import { formatMoney } from '@/utils/format'
import type { RequestItem } from '@/types/domain'

const auth = useAuthStore()
const domain = useDomainStore()

const resourceCount = ref<number | null>(null)
const myRequestCount = ref<number | null>(null)
const upcomingShifts = ref<RequestItem[]>([])
const loadingShifts = ref(false)

function getDayName(dateStr: string | null): string {
  if (!dateStr) return ''
  const days = ['Niedziela', 'Poniedziałek', 'Wtorek', 'Środa', 'Czwartek', 'Piątek', 'Sobota']
  return days[new Date(dateStr).getDay()]
}

function formatShiftTime(startAt: string | null, endAt: string | null): string {
  if (!startAt || !endAt) return ''
  const s = new Date(startAt)
  const e = new Date(endAt)
  return `${String(s.getHours()).padStart(2, '0')}:${String(s.getMinutes()).padStart(2, '0')} - ${String(e.getHours()).padStart(2, '0')}:${String(e.getMinutes()).padStart(2, '0')}`
}

function shiftLabel(metadata: Record<string, unknown>): string {
  const st = metadata.shiftType
  if (st === 'MORNING') return 'Zmiana poranna'
  if (st === 'EVENING') return 'Zmiana wieczorna'
  return 'Zmiana'
}

onMounted(async () => {
  try {
    const [resources, myReqs] = await Promise.all([
      resourcesApi.list(0, 1),
      requestsApi.myRequests(0, 1),
    ])
    resourceCount.value = resources.totalElements
    myRequestCount.value = myReqs.totalElements
  } catch {
    // Non-fatal
  }

  loadingShifts.value = true
  try {
    const page = await requestsApi.myRequests(0, 20)
    upcomingShifts.value = page.items
      .filter((r) => r.status === 'CONFIRMED' || r.status === 'PENDING')
      .sort((a, b) => (a.startAt ?? '').localeCompare(b.startAt ?? ''))
      .slice(0, 5)
  } catch {
    // Non-fatal
  } finally {
    loadingShifts.value = false
  }
})
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-100">{{ domainConfig.labels.dashboard }}</h1>
      <p class="text-gray-400">
        Witaj, <span class="font-medium text-gray-300">{{ auth.user?.name }}</span> ({{ auth.user?.role }})
      </p>
    </div>

    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <router-link :to="{ name: 'resources' }" class="card p-5 transition hover:shadow-md">
        <div class="text-sm text-gray-400">{{ domainConfig.resource.plural }}</div>
        <div class="mt-1 text-3xl font-bold text-gray-100">{{ resourceCount ?? '—' }}</div>
        <div class="mt-2 text-sm text-brand-600">Przeglądaj →</div>
      </router-link>

      <router-link :to="{ name: 'my-requests' }" class="card p-5 transition hover:shadow-md">
        <div class="text-sm text-gray-400">{{ domainConfig.labels.myRequests }}</div>
        <div class="mt-1 text-3xl font-bold text-gray-100">{{ myRequestCount ?? '—' }}</div>
        <div class="mt-2 text-sm text-brand-600">Zobacz →</div>
      </router-link>

      <router-link :to="{ name: 'create-request' }" class="card flex flex-col justify-center p-5 transition hover:shadow-md">
        <div class="text-base font-semibold text-gray-100">{{ domainConfig.labels.createRequest }}</div>
        <div class="mt-2 text-sm text-brand-600">Rozpocznij →</div>
      </router-link>

      <router-link
        v-if="auth.isAdmin"
        :to="{ name: 'calendar' }"
        class="card flex flex-col justify-center p-5 transition hover:shadow-md"
      >
        <div class="text-base font-semibold text-gray-100">{{ domainConfig.labels.calendar }}</div>
        <div class="mt-2 text-sm text-brand-600">Zobacz →</div>
      </router-link>

      <router-link
        v-if="auth.isStaff"
        :to="{ name: 'admin-requests' }"
        class="card flex flex-col justify-center p-5 transition hover:shadow-md"
      >
        <div class="text-base font-semibold text-gray-100">{{ domainConfig.labels.adminPanel }}</div>
        <div class="mt-2 text-sm text-brand-600">{{ domainConfig.labels.adminRequests }} →</div>
      </router-link>
    </div>

    <div class="card p-5">
      <h2 class="mb-3 text-base font-semibold text-gray-200">Nadchodzące {{ domainConfig.request.plural.toLowerCase() }}</h2>
      <div v-if="loadingShifts" class="h-20 animate-pulse rounded bg-gray-800"></div>
      <div v-else-if="upcomingShifts.length === 0" class="text-sm text-gray-400">
        Brak nadchodzących zmian.
      </div>
      <div v-else class="space-y-2">
        <div
          v-for="shift in upcomingShifts"
          :key="shift.id"
          class="flex items-center justify-between rounded-lg border border-gray-800 p-3"
        >
          <div>
            <div class="font-medium text-gray-200">
              {{ getDayName(shift.startAt) }}
              <span v-if="shift.startAt" class="text-xs text-gray-500">
                ({{ new Date(shift.startAt).toLocaleDateString('pl-PL') }})
              </span>
            </div>
            <div class="text-xs text-gray-400">
              {{ formatShiftTime(shift.startAt, shift.endAt) }}
              <span class="ml-1 rounded bg-brand-500/10 px-1.5 py-0.5 text-brand-400">
                {{ shiftLabel(shift.metadata) }}
              </span>
            </div>
            <div v-if="shift.resourceName" class="text-xs text-gray-500">{{ shift.resourceName }}</div>
          </div>
          <div class="text-right text-sm font-semibold text-brand-600">
            {{ formatMoney(shift.calculatedValue, shift.currency) }}
          </div>
        </div>
      </div>
    </div>

    <div class="card p-4 text-sm text-gray-400">
      <span class="font-medium text-gray-300">Profil domeny (z backendu):</span>
      {{ domain.backendDomainName ?? domainConfig.appName }}
    </div>
  </div>
</template>

