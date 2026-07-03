<script setup lang="ts">
import type { DomainFieldConfig, Metadata, MetadataValue } from '@/types/domain'

const props = defineProps<{
  fields: DomainFieldConfig[]
  errors?: Record<string, string>
}>()

// Two-way bound metadata object (Vue 3.4+ defineModel).
const model = defineModel<Metadata>({ required: true })

function setField(key: string, value: MetadataValue): void {
  model.value = { ...model.value, [key]: value }
}

function stringValue(key: string): string {
  const value = model.value[key]
  return value === null || value === undefined ? '' : String(value)
}

function boolValue(key: string): boolean {
  return model.value[key] === true
}

function onText(key: string, event: Event): void {
  const value = (event.target as HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement).value
  setField(key, value === '' ? null : value)
}

function onNumber(key: string, event: Event): void {
  const raw = (event.target as HTMLInputElement).value
  if (raw === '') {
    setField(key, null)
    return
  }
  const parsed = Number(raw)
  setField(key, Number.isNaN(parsed) ? null : parsed)
}

function onBool(key: string, event: Event): void {
  setField(key, (event.target as HTMLInputElement).checked)
}

function errorFor(key: string): string | undefined {
  return props.errors?.[key]
}
</script>

<template>
  <div class="space-y-4">
    <div v-for="field in fields" :key="field.key">
      <label class="label" :for="`field-${field.key}`">
        {{ field.label }}
        <span v-if="field.required" class="text-red-500">*</span>
      </label>

      <!-- SELECT -->
      <select
        v-if="field.type === 'SELECT'"
        :id="`field-${field.key}`"
        class="input"
        :value="stringValue(field.key)"
        @change="onText(field.key, $event)"
      >
        <option value="">— wybierz —</option>
        <option v-for="option in field.options" :key="option" :value="option">{{ option }}</option>
      </select>

      <!-- TEXTAREA -->
      <textarea
        v-else-if="field.type === 'TEXTAREA'"
        :id="`field-${field.key}`"
        class="input"
        rows="3"
        :value="stringValue(field.key)"
        @input="onText(field.key, $event)"
      ></textarea>

      <!-- NUMBER -->
      <input
        v-else-if="field.type === 'NUMBER'"
        :id="`field-${field.key}`"
        type="number"
        step="any"
        class="input"
        :value="stringValue(field.key)"
        @input="onNumber(field.key, $event)"
      />

      <!-- BOOLEAN -->
      <label v-else-if="field.type === 'BOOLEAN'" class="flex items-center gap-2 text-sm text-gray-300">
        <input type="checkbox" :checked="boolValue(field.key)" @change="onBool(field.key, $event)" />
        {{ field.helpText ?? field.label }}
      </label>

      <!-- DATE -->
      <input
        v-else-if="field.type === 'DATE'"
        :id="`field-${field.key}`"
        type="date"
        class="input"
        :value="stringValue(field.key)"
        @input="onText(field.key, $event)"
      />

      <!-- TEXT (default) -->
      <input
        v-else
        :id="`field-${field.key}`"
        type="text"
        class="input"
        :value="stringValue(field.key)"
        @input="onText(field.key, $event)"
      />

      <p v-if="field.helpText && field.type !== 'BOOLEAN'" class="mt-1 text-xs text-gray-400">
        {{ field.helpText }}
      </p>
      <p v-if="errorFor(field.key)" class="mt-1 text-xs text-red-400">{{ errorFor(field.key) }}</p>
    </div>
  </div>
</template>

