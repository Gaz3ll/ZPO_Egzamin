<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AdminResourceTable from '@/components/AdminResourceTable.vue'
import DynamicFieldRenderer from '@/components/DynamicFieldRenderer.vue'
import { adminApi } from '@/api/admin.api'
import { domainConfig } from '@/config/domain.config'
import { useDomainStore } from '@/stores/domain.store'
import { ApiError } from '@/types/api'
import type { CreateResourcePayload, Metadata, Resource, ResourceStatus } from '@/types/domain'

const contractHoursSuggestions: Record<string, string> = {
  UOP: 'ok. 160h/miesiąc (40h/tydzień)',
  UZ: 'ok. 80h/miesiąc (20h/tydzień)',
  B2B: 'ok. 160h/miesiąc (40h/tydzień)',
}

const domain = useDomainStore()

const resources = ref<Resource[]>([])
const loading = ref(true)
const showForm = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const error = ref<string | null>(null)
const fieldErrors = ref<Record<string, string>>({})

const form = ref({
  name: '',
  description: '',
  status: 'ACTIVE' as ResourceStatus,
})
const metadata = ref<Metadata>({})

const contractHoursHint = computed(() => {
  const ct = metadata.value.contractType
  if (!ct || typeof ct !== 'string') return null
  return contractHoursSuggestions[ct] ?? null
})

async function load(): Promise<void> {
  loading.value = true
  try {
    const page = await adminApi.listResources(0, 100)
    resources.value = page.items
  } finally {
    loading.value = false
  }
}

function resetForm(): void {
  form.value = { name: '', description: '', status: 'ACTIVE' }
  metadata.value = {}
  fieldErrors.value = {}
  error.value = null
}

function openCreate(): void {
  resetForm()
  editingId.value = null
  showForm.value = true
}

function openEdit(resource: Resource): void {
  editingId.value = resource.id
  form.value = {
    name: resource.name,
    description: resource.description ?? '',
    status: resource.status,
  }
  metadata.value = { ...resource.metadata }
  fieldErrors.value = {}
  error.value = null
  showForm.value = true
}

async function submit(): Promise<void> {
  submitting.value = true
  error.value = null
  fieldErrors.value = {}
  const payload: CreateResourcePayload = {
    name: form.value.name,
    description: form.value.description || null,
    type: null,
    status: form.value.status,
    baseValue: null,
    capacityValue: null,
    metadata: metadata.value,
  }
  try {
    if (editingId.value !== null) {
      await adminApi.updateResource(editingId.value, payload)
    } else {
      await adminApi.createResource(payload)
    }
    showForm.value = false
    await load()
  } catch (err) {
    if (err instanceof ApiError) {
      error.value = err.message
      fieldErrors.value = err.fieldErrors
    } else {
      error.value = 'Nie udało się zapisać'
    }
  } finally {
    submitting.value = false
  }
}

async function deleteResource(resource: Resource): Promise<void> {
  if (!confirm(`Usunąć pracownika "${resource.name}"? Tej operacji nie można cofnąć.`)) return
  try {
    await adminApi.deleteResource(resource.id)
    await load()
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Nie udało się usunąć'
  }
}

onMounted(load)
</script>

<template>
  <div class="space-y-5">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold text-gray-100">{{ domainConfig.labels.adminResources }}</h1>
      <button type="button" class="btn-primary" @click="openCreate">+ Nowy {{ domainConfig.resource.singular.toLowerCase() }}</button>
    </div>

    <div v-if="showForm" class="card space-y-4 p-6">
      <h2 class="text-lg font-semibold text-gray-100">
        {{ editingId ? 'Edytuj' : 'Utwórz' }} {{ domainConfig.resource.singular.toLowerCase() }}
      </h2>

      <div class="grid gap-4 sm:grid-cols-2">
        <div>
          <label class="label" for="r-name">Nazwa *</label>
          <input id="r-name" v-model="form.name" type="text" class="input" />
          <p v-if="fieldErrors.name" class="mt-1 text-xs text-red-400">{{ fieldErrors.name }}</p>
        </div>
        <div>
          <label class="label" for="r-status">Status</label>
          <select id="r-status" v-model="form.status" class="input">
            <option v-for="status in domain.resourceStatuses" :key="status" :value="status">{{ status }}</option>
          </select>
        </div>
      </div>

      <div>
        <label class="label" for="r-desc">Opis</label>
        <textarea id="r-desc" v-model="form.description" rows="2" class="input"></textarea>
      </div>

      <div>
        <h3 class="mb-2 text-sm font-semibold text-gray-300">Dane {{ domainConfig.resource.singular.toLowerCase() }}</h3>
        <DynamicFieldRenderer v-model="metadata" :fields="domainConfig.resource.fields" :errors="fieldErrors" />
        <p v-if="contractHoursHint" class="mt-1 text-xs text-brand-600 font-medium">
          Sugerowane godziny miesięcznie: {{ contractHoursHint }}
        </p>
      </div>

      <p v-if="error" class="rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-700">{{ error }}</p>

      <div class="flex gap-2">
        <button type="button" class="btn-primary" :disabled="submitting" @click="submit">
          {{ submitting ? 'Zapisywanie…' : 'Zapisz' }}
        </button>
        <button type="button" class="btn-secondary" @click="showForm = false">Anuluj</button>
      </div>
    </div>

    <div v-if="loading" class="card h-40 animate-pulse bg-gray-800"></div>
    <AdminResourceTable
      v-else
      :resources="resources"
      @edit="openEdit"
      @delete="deleteResource"
    />
  </div>
</template>

