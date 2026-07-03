<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import DynamicFieldRenderer from '@/components/DynamicFieldRenderer.vue'
import CalendarWidget from '@/components/CalendarWidget.vue'
import TimeSlotPicker from '@/components/TimeSlotPicker.vue'
import { requestsApi } from '@/api/requests.api'
import { adminApi } from '@/api/admin.api'
import { useAuthStore } from '@/stores/auth.store'
import { domainConfig } from '@/config/domain.config'
import { formatMoney, localToInstant } from '@/utils/format'
import type { CreateRequestPayload, Metadata, Resource, RequestItem } from '@/types/domain'

const props = defineProps<{
  resource: Resource | null
  resources?: Resource[]
  initialDate?: string | null
  submitting?: boolean
  errorMessage?: string | null
  fieldErrors?: Record<string, string>
}>()

const emit = defineEmits<{ submit: [payload: CreateRequestPayload] }>()

const auth = useAuthStore()
const config = domainConfig
const metadata = ref<Metadata>({})
const selectedDate = ref<string | null>(null)
const selectedShift = ref<'MORNING' | 'EVENING' | null>(null)
const rangeStart = ref('')
const rangeEnd = ref('')
const allDateShifts = ref<RequestItem[]>([])

const useShiftsMode = computed(() => !!(config as any).shifts && (config.timeMode as string) !== 'range')
const useRangeMode = computed(() => (config.timeMode as string) === 'range')
const useNoneMode = computed(() => (config.timeMode as string) === 'none')

const shifts = computed(() => {
  const s = (config as any).shifts
  if (!s) return []
  return [
    { type: 'MORNING' as const, ...s.morning },
    { type: 'EVENING' as const, ...s.evening },
  ]
})

const estimatedCost = computed(() => {
  if (!props.resource) return null
  const rate = Number(props.resource.metadata.hourlyRate || props.resource.metadata.dailyRate) || props.resource.baseValue || 0
  if (useShiftsMode.value && selectedShift.value) {
    const hours = 8
    const multiplier = selectedShift.value === 'EVENING' ? 1.1 : 1.0
    return hours * rate * multiplier
  }
  if (!useShiftsMode.value && !useRangeMode.value && selectedSlots.value.length > 0) {
    const hours = selectedSlots.value.length * 0.5
    return hours * rate
  }
  if (useRangeMode.value && rangeStart.value && rangeEnd.value) {
    const start = new Date(rangeStart.value)
    const end = new Date(rangeEnd.value)
    const days = Math.max(1, Math.ceil((end.getTime() - start.getTime()) / 86400000) + 1)
    return days * rate
  }
  return null
})

function resourceNameById(id: number): string {
  if (props.resources) {
    const found = props.resources.find((r) => r.id === id)
    if (found) return found.name
  }
  if (props.resource && props.resource.id === id) return props.resource.name
  return `#${id}`
}

const capacityInfo = computed(() => {
  if (!props.resource || !props.resource.metadata?.capacity) return null
  return `Pojemność: ${props.resource.metadata.capacity} osób`
})

// ----- SLOTS MODE -----
const selectedSlots = ref<{ start: string; end: string }[]>([])
const reservedSlots = ref<{ start: string; end: string }[]>([])

function generateSlots(): { start: string; end: string }[] {
  const slots: { start: string; end: string }[] = []
  for (let h = 8; h < 18; h++) {
    const hour = String(h).padStart(2, '0')
    const next = String(h + 1).padStart(2, '0')
    slots.push({ start: `${hour}:00`, end: `${hour}:30` })
    slots.push({ start: `${hour}:30`, end: `${next}:00` })
  }
  return slots
}

async function fetchReservedSlots(): Promise<void> {
  if (!props.resource || !selectedDate.value) {
    reservedSlots.value = []
    return
  }
  try {
    const items = await requestsApi.byResource(props.resource.id)
    reservedSlots.value = items
      .filter((r) => r.startAt && r.endAt)
      .filter((r) => {
        if (!r.startAt) return false
        return new Date(r.startAt).toISOString().slice(0, 10) === selectedDate.value
      })
      .map((r) => ({
        start: `${String(new Date(r.startAt!).getHours()).padStart(2, '0')}:${String(new Date(r.startAt!).getMinutes()).padStart(2, '0')}`,
        end: `${String(new Date(r.endAt!).getHours()).padStart(2, '0')}:${String(new Date(r.endAt!).getMinutes()).padStart(2, '0')}`,
      }))
  } catch {
    reservedSlots.value = []
  }
}

async function fetchAllForDate(): Promise<void> {
  if (!selectedDate.value || !auth.isStaff) {
    allDateShifts.value = []
    return
  }
  try {
    const page = await adminApi.listRequests(null, 0, 500)
    allDateShifts.value = page.items.filter((r) => {
      if (!r.startAt) return false
      if (r.status === 'CANCELLED' || r.status === 'REJECTED') return false
      return new Date(r.startAt).toISOString().slice(0, 10) === selectedDate.value
    })
  } catch {
    allDateShifts.value = []
  }
}

function onDateSelected(date: string): void {
  selectedDate.value = date
  selectedShift.value = null
  selectedSlots.value = []
}

function onSlotsSelected(slots: { start: string; end: string }[]): void {
  selectedSlots.value = slots
}

function isShiftOccupied(shiftType: 'MORNING' | 'EVENING'): boolean {
  return allDateShifts.value.some((r) => r.metadata?.shiftType === shiftType)
}

function selectShift(shiftType: 'MORNING' | 'EVENING'): void {
  if (isShiftOccupied(shiftType)) return
  selectedShift.value = shiftType
}

function getShiftOccupancy(shiftType: 'MORNING' | 'EVENING'): { occupied: boolean; occupantName?: string } {
  for (const shift of allDateShifts.value) {
    if (shift.metadata?.shiftType === shiftType) {
      return { occupied: true, occupantName: resourceNameById(shift.resourceId) }
    }
  }
  return { occupied: false }
}

watch([() => props.resource, selectedDate], () => {
  fetchReservedSlots()
  fetchAllForDate()
})

const eveningMultiplier = computed(() => selectedShift.value === 'EVENING' ? ' (+10% wieczorna)' : '')

function onSubmit(): void {
  if (!props.resource) return

  if (useShiftsMode.value) {
    if (!selectedDate.value || !selectedShift.value) return
    const shift = shifts.value.find((s) => s.type === selectedShift.value)
    if (!shift) return
    const startLocal = `${selectedDate.value}T${shift.start}`
    const endLocal = `${selectedDate.value}T${shift.end}`
    emit('submit', {
      resourceId: props.resource.id,
      startAt: localToInstant(startLocal),
      endAt: localToInstant(endLocal),
      quantity: null,
      metadata: { ...metadata.value, shiftType: selectedShift.value },
    })
  } else if (useRangeMode.value) {
    if (!rangeStart.value || !rangeEnd.value) return
    emit('submit', {
      resourceId: props.resource.id,
      startAt: localToInstant(rangeStart.value + 'T00:00'),
      endAt: localToInstant(rangeEnd.value + 'T23:59'),
      quantity: null,
      metadata: { ...metadata.value },
    })
  } else if (useNoneMode.value) {
    emit('submit', {
      resourceId: props.resource.id,
      startAt: new Date().toISOString(),
      endAt: null,
      quantity: null,
      metadata: { ...metadata.value },
    })
  } else {
    if (!selectedDate.value || selectedSlots.value.length === 0) return
    const startLocal = `${selectedDate.value}T${selectedSlots.value[0].start}`
    const endLocal = `${selectedDate.value}T${selectedSlots.value[selectedSlots.value.length - 1].end}`
    emit('submit', {
      resourceId: props.resource.id,
      startAt: localToInstant(startLocal),
      endAt: localToInstant(endLocal),
      quantity: null,
      metadata: { ...metadata.value },
    })
  }
}

watch(() => props.resource, () => {
  selectedDate.value = null
  selectedShift.value = null
  selectedSlots.value = []
  rangeStart.value = ''
  rangeEnd.value = ''
  metadata.value = {}
  allDateShifts.value = []
})

watch(() => props.initialDate, (date) => {
  if (date) { selectedDate.value = date; selectedShift.value = null }
}, { immediate: true })
</script>

<template>
  <form class="space-y-5" @submit.prevent="onSubmit">
    <div v-if="resource" class="rounded-lg bg-brand-500/10 p-3 text-sm">
      <div class="flex items-center justify-between">
        <div>
          <span class="text-gray-400">{{ config.resource.singular }}:</span>
          <span class="font-semibold text-gray-100"> {{ resource.name }}</span>
        </div>
        <div class="text-xs text-gray-400">
          {{ formatMoney(resource.baseValue || Number(resource.metadata.hourlyRate) || 0, config.currency) }}/h
        </div>
      </div>
      <div v-if="capacityInfo" class="mt-1 text-xs text-gray-400">{{ capacityInfo }}</div>
    </div>

    <div v-if="resource && !useRangeMode && !useNoneMode">
      <label class="label">Wybierz datę</label>
      <CalendarWidget :available-dates="[]" :reserved-dates="[]" :multi-day="false" @selected="onDateSelected" />
      <p v-if="fieldErrors?.startAt" class="mt-1 text-xs text-red-400">{{ fieldErrors.startAt }}</p>
    </div>

    <!-- SHIFTS MODE (grafik) -->
    <div v-if="useShiftsMode && selectedDate && resource" class="space-y-3">
      <p class="text-sm font-medium text-gray-300">{{ selectedDate }}</p>
      <div class="grid gap-3 sm:grid-cols-2">
        <button
          v-for="shift in shifts" :key="shift.type" type="button"
          class="rounded-lg border-2 p-4 text-left transition-all"
          :class="[
            selectedShift === shift.type ? 'border-brand-500 bg-brand-500/10 ring-2 ring-brand-200' :
            getShiftOccupancy(shift.type).occupied ? 'border-red-200 bg-red-500/10 cursor-not-allowed opacity-80' :
            'border-gray-700 bg-gray-900 hover:border-brand-300 hover:bg-brand-500/10 cursor-pointer'
          ]"
          :disabled="getShiftOccupancy(shift.type).occupied"
          @click="selectShift(shift.type)"
        >
          <div class="text-sm font-semibold text-gray-200">{{ shift.label }}</div>
          <div class="mt-1 text-lg font-bold text-gray-100">{{ shift.start }} - {{ shift.end }}</div>
          <div class="mt-1 text-xs text-gray-400">8h · {{ shift.type === 'EVENING' ? '+10%' : 'stawka bazowa' }}</div>
          <div v-if="getShiftOccupancy(shift.type).occupied" class="mt-2 rounded bg-red-500/10 px-2 py-1 text-xs">
            <span class="font-medium text-red-400">Zajęte</span>
            <span class="text-red-500"> — {{ getShiftOccupancy(shift.type).occupantName }}</span>
          </div>
          <div v-else-if="selectedShift === shift.type" class="mt-2 rounded bg-brand-500/10 px-2 py-0.5 text-xs font-medium text-brand-400">Wybrano</div>
          <div v-else class="mt-2 rounded bg-green-500/10 px-2 py-0.5 text-xs font-medium text-green-400">Wolne</div>
        </button>
      </div>
    </div>

    <!-- SLOTS MODE (rezerwacje sal, biblioteka, itp.) -->
    <div v-if="!useShiftsMode && !useRangeMode && selectedDate && resource">
      <TimeSlotPicker
        :date="selectedDate"
        :available-slots="generateSlots()"
        :reserved-slots="reservedSlots"
        opening-time="08:00"
        closing-time="18:00"
        @slots-selected="onSlotsSelected"
      />
    </div>

    <!-- RANGE MODE (rezerwacje na całe dni) -->
    <div v-if="useRangeMode && resource" class="grid gap-4 sm:grid-cols-2">
      <div>
        <label class="label" for="range-start">Data początkowa *</label>
        <input id="range-start" v-model="rangeStart" type="date" class="input" />
      </div>
      <div>
        <label class="label" for="range-end">Data końcowa *</label>
        <input id="range-end" v-model="rangeEnd" type="date" class="input" />
      </div>
      <p v-if="fieldErrors?.startAt" class="text-xs text-red-400">{{ fieldErrors.startAt }}</p>
    </div>

    <!-- NONE MODE (biblioteka - bez wyboru czasu) -->
    <div v-if="useNoneMode && resource" class="rounded-lg bg-brand-500/10 p-3 text-sm text-gray-300">
      <p>Kliknij <strong>{{ config.labels.createRequest }}</strong> aby wypożyczyć tę książkę.</p>
      <p v-if="resource.metadata?.availableCopies !== undefined" class="mt-1 text-xs text-gray-400">
        Dostępne egzemplarze: <strong class="text-green-400">{{ resource.metadata.availableCopies }}</strong>
      </p>
    </div>

    <div v-if="selectedShift" class="rounded-lg bg-green-500/10 p-3 text-sm text-green-400">
      <p class="font-medium">{{ selectedShift === 'MORNING' ? config.labels.morning : config.labels.evening }}{{ eveningMultiplier }}</p>
      <p v-if="estimatedCost !== null" class="mt-1">Szacowany koszt: <strong>{{ formatMoney(estimatedCost, config.currency) }}</strong></p>
    </div>

    <div v-if="!useShiftsMode && !useRangeMode && selectedSlots.length > 0 && selectedDate" class="rounded-lg bg-green-500/10 p-3 text-sm text-green-400">
      <p class="mb-1 font-medium">{{ selectedDate }} — {{ selectedSlots.length }} slotów ({{ selectedSlots.length * 30 }} min)</p>
      <p v-if="estimatedCost !== null">Szacowany koszt: <strong>{{ formatMoney(estimatedCost, config.currency) }}</strong></p>
    </div>

    <div v-if="useRangeMode && rangeStart && rangeEnd" class="rounded-lg bg-green-500/10 p-3 text-sm text-green-400">
      <p class="font-medium">{{ rangeStart }} – {{ rangeEnd }}</p>
      <p v-if="estimatedCost !== null">Szacowany koszt: <strong>{{ formatMoney(estimatedCost, config.currency) }}</strong></p>
    </div>

    <DynamicFieldRenderer v-model="metadata" :fields="config.request.fields" :errors="fieldErrors" />

    <p v-if="errorMessage" class="rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-400">{{ errorMessage }}</p>

    <button
      type="submit" class="btn-primary w-full"
      :disabled="submitting || !resource || (useShiftsMode ? !selectedShift : useRangeMode ? !rangeStart || !rangeEnd : useNoneMode ? false : selectedSlots.length === 0)"
    >
      {{ submitting ? 'Przetwarzanie…' : config.labels.createRequest }}
    </button>
  </form>
</template>
