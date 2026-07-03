<script setup lang="ts">
import { computed, ref } from 'vue'

const props = defineProps<{
  date: string
  availableSlots: { start: string; end: string }[]
  reservedSlots: { start: string; end: string }[]
  openingTime: string
  closingTime: string
}>()

const emit = defineEmits<{
  slotsSelected: [slots: { start: string; end: string }[]]
}>()

const selected = ref<Set<string>>(new Set())

function parseMins(t: string): number {
  const [h, m] = t.split(':').map(Number)
  return h * 60 + m
}

function fmt(minutes: number): string {
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
}

function overlaps(a: { start: string; end: string }, b: { start: string; end: string }): boolean {
  const aS = parseMins(a.start), aE = parseMins(a.end)
  const bS = parseMins(b.start), bE = parseMins(b.end)
  return aS < bE && bS < aE
}

const slots = computed(() => {
  const open = parseMins(props.openingTime)
  const close = parseMins(props.closingTime)
  const list: { key: string; start: string; end: string; reserved: boolean }[] = []

  for (let m = open; m + 30 <= close; m += 30) {
    const start = fmt(m)
    const end = fmt(m + 30)
    const key = `${start}-${end}`
    const reserved = props.reservedSlots.some((r) => overlaps({ start, end }, r))
    list.push({ key, start, end, reserved })
  }
  return list
})

function toggle(slot: { key: string; start: string; end: string; reserved: boolean }): void {
  if (slot.reserved) return
  const next = new Set(selected.value)
  if (next.has(slot.key)) {
    next.delete(slot.key)
  } else {
    next.add(slot.key)
  }
  selected.value = next
  emitSlots()
}

function emitSlots(): void {
  const result = slots.value
    .filter((s) => selected.value.has(s.key))
    .map((s) => ({ start: s.start, end: s.end }))
  emit('slotsSelected', result)
}

function slotClass(slot: { key: string; reserved: boolean }): string {
  if (slot.reserved) return 'border-red-200 bg-red-500/10 text-red-400 cursor-not-allowed'
  if (selected.value.has(slot.key)) return 'border-brand-400 bg-brand-100 text-brand-800 font-semibold'
  return 'border-green-300 bg-green-500/10 text-green-400 hover:bg-green-500/10 cursor-pointer'
}

const selectedList = computed(() =>
  slots.value.filter((s) => selected.value.has(s.key)),
)

function clearSelection(): void {
  selected.value = new Set()
  emit('slotsSelected', [])
}
</script>

<template>
  <div class="card p-4">
    <div class="mb-3 flex items-center justify-between">
      <h3 class="text-sm font-semibold text-gray-300">
        {{ date }} — godziny
      </h3>
      <button
        v-if="selectedList.length > 0"
        type="button"
        class="text-xs text-red-400 hover:text-red-400"
        @click="clearSelection"
      >Wyczyść</button>
    </div>

    <p class="mb-2 text-xs text-gray-400">Klikaj aby zaznaczyć/odznaczyć:</p>

    <ul class="max-h-60 space-y-1 overflow-y-auto">
      <li v-for="(slot, idx) in slots" :key="idx">
        <button
          type="button"
          class="w-full rounded-lg border px-3 py-2 text-left text-sm transition"
          :class="slotClass(slot)"
          :disabled="slot.reserved"
          @click="toggle(slot)"
        >
          <span class="font-medium">{{ slot.start }} – {{ slot.end }}</span>
          <span v-if="slot.reserved" class="ml-2 text-xs text-red-500">zarezerwowane</span>
          <span v-if="selected.has(slot.key)" class="ml-2 text-xs text-brand-600">✓</span>
        </button>
      </li>
    </ul>

    <div v-if="selectedList.length > 0" class="mt-3 rounded-lg bg-brand-500/10 p-3 text-xs text-brand-800">
      <p class="mb-1 font-medium">Zaznaczono {{ selectedList.length }} slotów:</p>
      <ul class="list-inside list-disc space-y-0.5">
        <li v-for="s in selectedList" :key="s.key">
          <strong>{{ s.start }}</strong> – <strong>{{ s.end }}</strong>
        </li>
      </ul>
    </div>

    <div v-if="slots.length === 0" class="py-4 text-center text-sm text-gray-500">
      Brak dostępnych godzin w tym dniu
    </div>
  </div>
</template>
