<script setup lang="ts">
import { computed, ref } from 'vue'

const props = defineProps<{
  availableDates?: string[]
  reservedDates?: string[]
  closingHours?: Record<string, string>
  multiDay?: boolean
}>()

const emit = defineEmits<{
  selected: [date: string]
  rangeSelected: [start: string, end: string]
}>()

const now = new Date()
const viewYear = ref(now.getFullYear())
const viewMonth = ref(now.getMonth())
const rangeStart = ref<string | null>(null)

const weekDays = ['Pon', 'Wt', 'Śr', 'Czw', 'Pt', 'Sob', 'Nie']
const monthNames = [
  'Styczeń', 'Luty', 'Marzec', 'Kwiecień', 'Maj', 'Czerwiec',
  'Lipiec', 'Sierpień', 'Wrzesień', 'Październik', 'Listopad', 'Grudzień',
]

function isoDay(y: number, m: number, d: number): string {
  return `${y}-${String(m + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
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

function isAvailable(date: string): boolean {
  return props.availableDates?.includes(date) ?? false
}

function isReserved(date: string): boolean {
  return props.reservedDates?.includes(date) ?? false
}

function closingFor(date: string): string | undefined {
  return props.closingHours?.[date]
}

const todayStr = computed(() => isoDay(now.getFullYear(), now.getMonth(), now.getDate()))

function isInRange(date: string): boolean {
  if (!rangeStart.value || !props.multiDay) return false
  return rangeStart.value === date
}

function selectDay(date: string): void {
  if (isReserved(date)) return
  if (props.multiDay) {
    if (!rangeStart.value) {
      rangeStart.value = date
    } else {
      const start = rangeStart.value < date ? rangeStart.value : date
      const end = rangeStart.value < date ? date : rangeStart.value
      emit('rangeSelected', start, end)
      rangeStart.value = null
    }
  } else {
    emit('selected', date)
  }
}
</script>

<template>
  <div class="card p-4">
    <div class="mb-3 flex items-center justify-between">
      <button type="button" class="btn-secondary px-3 py-1 text-sm" @click="prevMonth">&lsaquo;</button>
      <span class="text-sm font-semibold text-gray-200">
        {{ monthNames[viewMonth] }} {{ viewYear }}
      </span>
      <button type="button" class="btn-secondary px-3 py-1 text-sm" @click="nextMonth">&rsaquo;</button>
    </div>

    <div class="grid grid-cols-7 gap-0">
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
          class="relative flex flex-col items-center justify-center py-2 text-sm transition"
          :class="[
            cell.date
              ? isReserved(cell.date)
                ? 'cursor-not-allowed text-slate-300'
                : isAvailable(cell.date)
                  ? isInRange(cell.date)
                    ? 'cursor-pointer bg-brand-100 text-brand-800 font-semibold ring-2 ring-brand-400'
                    : 'cursor-pointer text-gray-200 hover:bg-brand-500/10'
                  : 'cursor-pointer text-gray-400 hover:bg-gray-800'
              : '',
            cell.date === todayStr ? 'font-bold' : '',
          ]"
          :disabled="!cell.date || isReserved(cell.date)"
          @click="cell.date && selectDay(cell.date)"
        >
          <span>{{ cell.day || '' }}</span>
          <span v-if="cell.date && isAvailable(cell.date)" class="mt-0.5 h-1.5 w-1.5 rounded-full bg-green-500/100"></span>
          <span v-else-if="cell.date && isReserved(cell.date)" class="mt-0.5 h-1.5 w-1.5 rounded-full bg-red-400"></span>
          <span
            v-else-if="cell.date && closingFor(cell.date)"
            class="mt-0.5 h-1.5 w-1.5 rounded-full bg-amber-400"
          ></span>
        </button>
      </template>
    </div>
    <p v-if="rangeStart" class="mt-2 text-xs text-brand-600">
      Wybierz drugi dzień, aby zakończyć zakres (od {{ rangeStart }})
    </p>
  </div>
</template>
