<script setup lang="ts">
import { computed } from 'vue'
import RequestStatusBadge from '@/components/RequestStatusBadge.vue'
import { domainConfig } from '@/config/domain.config'
import { formatMoney } from '@/utils/format'
import type { Resource } from '@/types/domain'

const props = defineProps<{ resource: Resource }>()

const metaEntries = computed(() =>
  domainConfig.resource.fields
    .map((field) => ({ label: field.label, value: props.resource.metadata[field.key] }))
    .filter((entry) => entry.value !== null && entry.value !== undefined && entry.value !== ''),
)

const hourlyRate = computed(() => {
  return Number(props.resource.metadata.hourlyRate) || props.resource.baseValue || 0
})

const contractLabel = computed(() => {
  const ct = props.resource.metadata.contractType
  if (!ct) return null
  const labels: Record<string, string> = { UOP: 'Umowa o pracę', UZ: 'Umowa zlecenie', B2B: 'Kontrakt' }
  return labels[String(ct)] ?? String(ct)
})

const photoUrl = computed(() => `/photos/employee_${props.resource.id}.jpg`)
</script>

<template>
  <div class="card group overflow-hidden transition-all hover:shadow-lg">
    <div class="aspect-square w-full bg-gray-700 relative">
      <img
        :src="photoUrl"
        :alt="resource.name"
        class="h-full w-full object-cover"
        @error="($event.target as HTMLImageElement).style.display = 'none'"
      />
      <div class="absolute inset-0 flex items-center justify-center text-gray-500 pointer-events-none">
        <span class="text-5xl">👤</span>
      </div>
      <div class="absolute top-2 right-2">
        <RequestStatusBadge :status="resource.status" kind="resource" />
      </div>
    </div>

    <div class="p-4">
      <div class="mb-2">
        <h3 class="truncate font-bold text-lg text-gray-100">{{ resource.name }}</h3>
        <p v-if="resource.metadata.position" class="text-xs text-gray-400">{{ resource.metadata.position }}</p>
      </div>

      <p v-if="resource.description" class="mb-4 line-clamp-2 text-sm text-gray-400">
        {{ resource.description }}
      </p>

      <div class="mb-4 space-y-1 text-sm">
        <div v-for="entry in metaEntries" :key="entry.label" class="flex justify-between">
          <dt class="text-gray-400">{{ entry.label }}</dt>
          <dd class="truncate font-medium text-gray-200">{{ entry.value }}</dd>
        </div>
        <div v-if="contractLabel" class="flex justify-between">
          <dt class="text-gray-400">Umowa</dt>
          <dd class="truncate font-medium text-gray-200">{{ contractLabel }}</dd>
        </div>
        <div v-if="resource.metadata.availableCopies !== undefined" class="flex justify-between border-t border-gray-700 pt-1 mt-1">
          <dt class="font-semibold text-gray-300">Dostępne</dt>
          <dd class="font-bold" :class="Number(resource.metadata.availableCopies) > 0 ? 'text-green-400' : 'text-red-400'">
            {{ resource.metadata.availableCopies }} / {{ resource.metadata.totalCopies ?? '?' }}
          </dd>
        </div>
        <div v-if="hourlyRate > 0" class="flex justify-between border-t pt-1 mt-1">
          <dt class="font-semibold text-gray-300">Stawka</dt>
          <dd class="font-bold text-brand-600">{{ formatMoney(hourlyRate, domainConfig.currency) }}/h</dd>
        </div>
      </div>

      <div class="flex gap-2">
        <router-link class="btn-secondary flex-1 text-center" :to="{ name: 'resource-details', params: { id: resource.id } }">
          Szczegóły
        </router-link>
        <router-link
          v-if="resource.status === 'ACTIVE'"
          class="btn-primary flex-1 flex items-center justify-center gap-1"
          :to="{ name: 'create-request', query: { resourceId: resource.id } }"
        >
          <span class="text-xl font-bold">+</span>
          {{ domainConfig.labels.createRequest }}
        </router-link>
      </div>
    </div>
  </div>
</template>

