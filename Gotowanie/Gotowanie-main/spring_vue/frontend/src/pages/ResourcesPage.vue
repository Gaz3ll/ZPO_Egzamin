<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ResourceList from '@/components/ResourceList.vue'
import { resourcesApi } from '@/api/resources.api'
import { domainConfig } from '@/config/domain.config'
import { localToInstant } from '@/utils/format'
import { ApiError } from '@/types/api'
import type { Resource } from '@/types/domain'

const resources = ref<Resource[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const availableOnly = ref(false)
const startLocal = ref('')
const endLocal = ref('')
const quantity = ref<number | null>(null)

async function load(): Promise<void> {
  loading.value = true
  error.value = null
  try {
    if (availableOnly.value) {
      resources.value = await resourcesApi.available({
        start: startLocal.value ? localToInstant(startLocal.value) : null,
        end: endLocal.value ? localToInstant(endLocal.value) : null,
        quantity: quantity.value,
      })
    } else {
      const page = await resourcesApi.list(0, 100)
      resources.value = page.items
    }
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Nie udało się pobrać zasobów'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="space-y-5">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold text-gray-100">{{ domainConfig.resource.plural }}</h1>
    </div>

    <div class="card p-4">
      <label class="flex items-center gap-2 text-sm font-medium text-gray-300">
        <input v-model="availableOnly" type="checkbox" @change="load" />
        {{ domainConfig.labels.availableOnly }}
      </label>

      <div v-if="availableOnly" class="mt-4 grid gap-3 sm:grid-cols-4">
        <div>
          <label class="label" for="f-start">Początek</label>
          <input id="f-start" v-model="startLocal" type="datetime-local" class="input" />
        </div>
        <div>
          <label class="label" for="f-end">Koniec</label>
          <input id="f-end" v-model="endLocal" type="datetime-local" class="input" />
        </div>
        <div>
          <label class="label" for="f-qty">Ilość</label>
          <input id="f-qty" v-model.number="quantity" type="number" min="1" class="input" />
        </div>
        <div class="flex items-end">
          <button type="button" class="btn-primary w-full" @click="load">Filtruj</button>
        </div>
      </div>
    </div>

    <p v-if="error" class="rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-700">{{ error }}</p>

    <ResourceList :resources="resources" :loading="loading" empty-text="Brak zasobów spełniających kryteria." />
  </div>
</template>

