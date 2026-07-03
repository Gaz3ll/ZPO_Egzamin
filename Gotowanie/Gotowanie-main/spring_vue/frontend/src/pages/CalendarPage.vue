<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin.api'
import { resourcesApi } from '@/api/resources.api'
import { domainConfig } from '@/config/domain.config'
import { formatMoney } from '@/utils/format'
import type { RequestItem, Resource } from '@/types/domain'

const now = new Date()
const viewYear = ref(now.getFullYear())
const viewMonth = ref(now.getMonth())
const selectedDate = ref<string | null>(null)
const allRequests = ref<RequestItem[]>([])
const allResources = ref<Resource[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const isShiftPreset = computed(() => !!(domainConfig as any).shifts)

const weekDays = ['Pon', 'Wt', 'Śr', 'Czw', 'Pt', 'Sob', 'Nie']
const monthNames = [
  'Styczeń', 'Luty', 'Marzec', 'Kwiecień', 'Maj', 'Czerwiec',
  'Lipiec', 'Sierpień', 'Wrzesień', 'Październik', 'Listopad', 'Grudzień',
]

function isoDay(y: number, m: number, d: number): string {
  return `${y}-${String(m + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
}

function requestDate(r: RequestItem): string | null {
  if (!r.startAt) return null
  return new Date(r.startAt).toISOString().slice(0, 10)
}

const calendarDays = computed(() => {
  const year = viewYear.value
  const month = viewMonth.value
  const firstOfMonth = new Date(year, month, 1)
  const startDow = (firstOfMonth.getDay() + 6) % 7
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const rows: { date: string | null; day: number }[][] = []
  let row: { date: string | null; day: number }[] = []

  for (let i = 0; i < startDow; i++) {
    row.push({ date: null, day: 0 })
  }

  for (let d = 1; d <= daysInMonth; d++) {
    row.push({ date: isoDay(year, month, d), day: d })
    if (row.length === 7) {
      rows.push(row)
      row = []
    }
  }

  while (row.length > 0 && row.length < 7) {
    row.push({ date: null, day: 0 })
  }
  if (row.length > 0) rows.push(row)

  while (rows.length < 6) {
    rows.push(Array.from({ length: 7 }, () => ({ date: null, day: 0 })))
  }

  return rows
})

const todayStr = computed(() => isoDay(now.getFullYear(), now.getMonth(), now.getDate()))

function shiftsForDate(date: string): RequestItem[] {
  return allRequests.value.filter((r) => {
    if (r.status === 'CANCELLED' || r.status === 'REJECTED') return false
    return requestDate(r) === date
  })
}

const selectedDateShifts = computed(() => {
  if (!selectedDate.value) return []
  return shiftsForDate(selectedDate.value)
})

function hasMorningShift(date: string): boolean {
  return shiftsForDate(date).some((r) => r.metadata?.shiftType === 'MORNING')
}

function hasEveningShift(date: string): boolean {
  return shiftsForDate(date).some((r) => r.metadata?.shiftType === 'EVENING')
}

function shiftCount(date: string): number {
  return shiftsForDate(date).length
}

function resourceName(resourceId: number): string {
  const res = allResources.value.find((r) => r.id === resourceId)
  return res?.name ?? `#${resourceId}`
}

function shiftTypeLabel(r: RequestItem): string {
  const m = r.metadata as Record<string, unknown> | undefined
  if (m?.shiftType === 'MORNING') return 'Poranna 7-15'
  if (m?.shiftType === 'EVENING') return 'Wieczorna 15-23'
  if (m?.renterName) return String(m.renterName)
  if (m?.meetingTitle) return String(m.meetingTitle)
  return domainConfig.request.singular
}

function prevMonth(): void {
  if (viewMonth.value === 0) {
    viewYear.value--
    viewMonth.value = 11
  } else {
    viewMonth.value--
  }
}

function nextMonth(): void {
  if (viewMonth.value === 11) {
    viewYear.value++
    viewMonth.value = 0
  } else {
    viewMonth.value++
  }
}

function selectDay(date: string): void {
  selectedDate.value = date
}

async function loadData(): Promise<void> {
  loading.value = true
  error.value = null
  try {
    const [reqPage, resPage] = await Promise.all([
      adminApi.listRequests(null, 0, 500),
      resourcesApi.list(0, 100),
    ])
    allRequests.value = reqPage.items
    allResources.value = resPage.items
  } catch (err) {
    error.value = 'Nie udało się pobrać danych'
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="space-y-5">
    <h1 class="text-2xl font-bold text-gray-100">{{ domainConfig.labels.calendar }}</h1>

    <p v-if="error" class="rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-700">{{ error }}</p>

    <div class="grid gap-5 lg:grid-cols-3">
      <div class="lg:col-span-2">
        <div class="card p-4">
          <div class="mb-3 flex items-center justify-between">
            <button type="button" class="btn-secondary px-3 py-1 text-sm" @click="prevMonth">&lsaquo;</button>
            <span class="text-sm font-semibold text-gray-200">
              {{ monthNames[viewMonth] }} {{ viewYear }}
            </span>
            <button type="button" class="btn-secondary px-3 py-1 text-sm" @click="nextMonth">&rsaquo;</button>
          </div>

          <div v-if="loading" class="h-80 animate-pulse bg-gray-800 rounded"></div>

          <div v-else class="grid grid-cols-7 gap-0">
            <div
              v-for="wd in weekDays"
              :key="wd"
              class="py-1 text-center text-xs font-semibold uppercase text-gray-500"
            >
              {{ wd }}
            </div>

            <template v-for="(row, ri) in calendarDays" :key="ri">
              <button
                v-for="(cell, ci) in row"
                :key="`${ri}-${ci}`"
                type="button"
                class="relative flex flex-col items-center justify-center py-2 text-sm transition hover:bg-brand-500/10"
                :class="[
                  cell.date
                    ? selectedDate === cell.date
                      ? 'bg-brand-100 ring-2 ring-brand-400'
                      : 'text-gray-300'
                    : '',
                  cell.date === todayStr ? 'font-bold' : '',
                ]"
                :disabled="!cell.date"
                @click="cell.date && selectDay(cell.date)"
              >
                <span>{{ cell.day || '' }}</span>
                <div v-if="cell.date && shiftCount(cell.date) > 0" class="flex gap-0.5 mt-0.5">
                  <template v-if="isShiftPreset">
                    <span v-if="hasMorningShift(cell.date)" class="h-1.5 w-1.5 rounded-full bg-amber-400" title="Poranna"></span>
                    <span v-if="hasEveningShift(cell.date)" class="h-1.5 w-1.5 rounded-full bg-indigo-400" title="Wieczorna"></span>
                  </template>
                  <span v-else class="h-1.5 w-1.5 rounded-full bg-amber-400" title="Zajęte"></span>
                </div>
              </button>
            </template>
          </div>
        </div>
      </div>

      <div class="space-y-4">
        <div v-if="selectedDate" class="card p-4">
          <h2 class="mb-3 text-base font-semibold text-gray-200">
            {{ selectedDate }}
            <span class="text-sm font-normal text-gray-500">
              ({{ ['Niedziela', 'Poniedziałek', 'Wtorek', 'Środa', 'Czwartek', 'Piątek', 'Sobota'][new Date(selectedDate).getDay()] }})
            </span>
          </h2>

          <div v-if="selectedDateShifts.length === 0" class="text-sm text-gray-400">
            Brak zmian w tym dniu.
          </div>

          <div v-else class="space-y-2">
            <div
              v-for="shift in selectedDateShifts"
              :key="shift.id"
              class="rounded-lg border border-gray-700 p-3"
            >
              <div class="flex items-center justify-between">
                <span class="text-sm font-medium text-gray-300">{{ shiftTypeLabel(shift) }}</span>
                <span
                  class="rounded-full px-2 py-0.5 text-xs"
                  :class="shift.status === 'CONFIRMED' ? 'bg-green-500/10 text-green-700' : 'bg-amber-500/10 text-amber-700'"
                >
                  {{ shift.status }}
                </span>
              </div>
              <div class="mt-1 text-sm font-semibold text-gray-100">
                {{ resourceName(shift.resourceId) }}
              </div>
              <div v-if="shift.metadata?.taskName" class="mt-0.5 text-xs text-gray-400">
                {{ shift.metadata.taskName }}
              </div>
              <div class="mt-1 text-xs text-brand-600">
                {{ formatMoney(shift.calculatedValue, shift.currency) }}
              </div>
            </div>
          </div>

          <div class="mt-4 pt-3 border-t border-gray-700">
            <router-link
              class="btn-primary w-full text-center"
              :to="{ name: 'create-request', query: { date: selectedDate } }"
            >
              + Dodaj wpis na {{ selectedDate }}
            </router-link>
          </div>
        </div>

        <div v-else class="card p-4 text-center text-sm text-gray-400">
          Kliknij dzień w kalendarzu, aby zobaczyć {{ domainConfig.request.plural.toLowerCase() }}.
        </div>

        <div class="card p-4">
          <h3 class="mb-2 text-sm font-semibold text-gray-300">Legenda</h3>
          <div class="space-y-1 text-xs">
            <div v-if="isShiftPreset" class="flex items-center gap-2">
              <span class="h-2 w-2 rounded-full bg-amber-400"></span>
              <span class="text-gray-400">Zmiana poranna (7-15)</span>
            </div>
            <div v-if="isShiftPreset" class="flex items-center gap-2">
              <span class="h-2 w-2 rounded-full bg-indigo-400"></span>
              <span class="text-gray-400">Zmiana wieczorna (15-23)</span>
            </div>
            <div v-if="!isShiftPreset" class="flex items-center gap-2">
              <span class="h-2 w-2 rounded-full bg-amber-400"></span>
              <span class="text-gray-400">Zajęte</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

