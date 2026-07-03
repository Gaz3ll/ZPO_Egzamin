<script setup lang="ts">
import RequestStatusBadge from '@/components/RequestStatusBadge.vue'
import { domainConfig } from '@/config/domain.config'
import type { Resource } from '@/types/domain'

defineProps<{ resources: Resource[] }>()
const emit = defineEmits<{
  edit: [resource: Resource]
  delete: [resource: Resource]
}>()

function metaField(resource: Resource, key: string): string {
  const v = resource.metadata[key]
  if (v === null || v === undefined || v === '') return '—'
  if (typeof v === 'boolean') return v ? 'Tak' : 'Nie'
  return String(v)
}

const visibleFields = domainConfig.resource.fields.slice(0, 5)
</script>

<template>
  <div class="card overflow-x-auto">
    <table class="w-full text-sm">
      <thead class="border-b border-gray-700 bg-gray-800 text-left text-xs uppercase tracking-wide text-gray-400">
        <tr>
          <th class="px-4 py-3">ID</th>
          <th class="px-4 py-3">{{ domainConfig.resource.singular }}</th>
          <th v-for="field in visibleFields" :key="field.key" class="px-4 py-3">{{ field.label }}</th>
          <th class="px-4 py-3">Status</th>
          <th class="px-4 py-3"></th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="resources.length === 0">
          <td :colspan="4 + visibleFields.length" class="px-4 py-8 text-center text-gray-400">
            Brak {{ domainConfig.resource.plural.toLowerCase() }}.
          </td>
        </tr>
        <tr v-for="resource in resources" :key="resource.id" class="border-b border-gray-800 last:border-0">
          <td class="px-4 py-3 font-mono text-xs text-gray-400">#{{ resource.id }}</td>
          <td class="px-4 py-3 font-medium text-gray-200 whitespace-nowrap">{{ resource.name }}</td>
          <td v-for="field in visibleFields" :key="field.key" class="px-4 py-3 text-gray-400 whitespace-nowrap">
            {{ metaField(resource, field.key) }}
          </td>
          <td class="px-4 py-3"><RequestStatusBadge :status="resource.status" kind="resource" /></td>
          <td class="px-4 py-3">
            <div class="flex items-center gap-3">
              <button type="button" class="text-brand-600 hover:underline" @click="emit('edit', resource)">
                Edytuj
              </button>
              <button type="button" class="text-red-400 hover:underline" @click="emit('delete', resource)">
                Usuń
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
