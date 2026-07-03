<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import RequestStatusBadge from '@/components/RequestStatusBadge.vue'
import { resourcesApi } from '@/api/resources.api'
import { domainConfig } from '@/config/domain.config'
import { formatMoney } from '@/utils/format'
import { ApiError } from '@/types/api'
import type { Resource } from '@/types/domain'

const route = useRoute()
const resource = ref<Resource | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

const metaEntries = computed(() => {
  const current = resource.value
  if (!current) return []
  return domainConfig.resource.fields
    .map((field) => ({ label: field.label, value: current.metadata[field.key] }))
    .filter((entry) => entry.value !== null && entry.value !== undefined && entry.value !== '')
})

const hourlyRate = computed(() => {
  if (!resource.value) return 0
  return Number(resource.value.metadata.hourlyRate) || resource.value.baseValue || 0
})

const photoUrl = computed(() => `/photos/employee_${resource.value?.id}.jpg`)

onMounted(async () => {
  try {
    resource.value = await resourcesApi.getById(Number(route.params.id))
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Nie znaleziono zasobu'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="space-y-5">
    <router-link :to="{ name: 'resources' }" class="text-sm text-brand-600 hover:underline">
      ← {{ domainConfig.resource.plural }}
    </router-link>

    <div v-if="loading" class="card h-40 animate-pulse bg-gray-800"></div>
    <p v-else-if="error" class="rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-700">{{ error }}</p>

    <div v-else-if="resource" class="card p-6">
      <div class="flex items-start gap-4">
        <div class="h-24 w-24 flex-shrink-0 rounded-lg bg-gray-700 overflow-hidden relative">
          <img
            :src="photoUrl"
            :alt="resource.name"
            class="h-full w-full object-cover"
            @error="($event.target as HTMLImageElement).style.display = 'none'"
          />
          <div class="absolute inset-0 flex items-center justify-center text-3xl text-gray-500 pointer-events-none">👤</div>
        </div>
        <div>
          <h1 class="text-2xl font-bold text-gray-100">{{ resource.name }}</h1>
          <p v-if="resource.description" class="mt-1 text-gray-400">{{ resource.description }}</p>
        </div>
        <div class="ml-auto">
          <RequestStatusBadge :status="resource.status" kind="resource" />
        </div>
      </div>

      <dl class="mt-5 grid gap-4 sm:grid-cols-2">
        <div v-for="entry in metaEntries" :key="entry.label" class="rounded-lg bg-gray-800 p-3">
          <dt class="text-xs text-gray-400">{{ entry.label }}</dt>
          <dd class="text-lg font-semibold text-gray-100">{{ entry.value }}</dd>
        </div>
        <div class="rounded-lg bg-gray-800 p-3">
          <dt class="text-xs text-gray-400">Stawka godzinowa</dt>
          <dd class="text-lg font-semibold text-gray-100">
            {{ formatMoney(hourlyRate, domainConfig.currency) }}/h
          </dd>
        </div>
      </dl>

      <router-link
        v-if="resource.status === 'ACTIVE'"
        class="btn-primary mt-6"
        :to="{ name: 'create-request', query: { resourceId: resource.id } }"
      >
        {{ domainConfig.labels.createRequest }}
      </router-link>
    </div>
  </div>
</template>

