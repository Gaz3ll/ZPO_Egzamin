<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AlgorithmBreakdown from '@/components/AlgorithmBreakdown.vue'
import RequestForm from '@/components/RequestForm.vue'
import { requestsApi } from '@/api/requests.api'
import { resourcesApi } from '@/api/resources.api'
import { domainConfig } from '@/config/domain.config'
import { formatMoney } from '@/utils/format'
import { ApiError } from '@/types/api'
import type { CreateRequestPayload, RequestItem, Resource } from '@/types/domain'

const route = useRoute()

const resources = ref<Resource[]>([])
const selectedResourceId = ref<number | null>(null)
const initialDate = ref<string | null>(null)
const submitting = ref(false)
const errorMessage = ref<string | null>(null)
const fieldErrors = ref<Record<string, string>>({})
const result = ref<RequestItem | null>(null)

const selectedResource = computed<Resource | null>(
  () => resources.value.find((resource) => resource.id === selectedResourceId.value) ?? null,
)

onMounted(async () => {
  const page = await resourcesApi.list(0, 100)
  resources.value = page.items
  const queryId = Number(route.query.resourceId)
  if (!Number.isNaN(queryId) && queryId > 0) {
    selectedResourceId.value = queryId
  }
  const queryDate = route.query.date
  if (typeof queryDate === 'string' && queryDate) {
    initialDate.value = queryDate
  }
})

async function onSubmit(payload: CreateRequestPayload): Promise<void> {
  submitting.value = true
  errorMessage.value = null
  fieldErrors.value = {}
  try {
    result.value = await requestsApi.create(payload)
  } catch (err) {
    if (err instanceof ApiError) {
      errorMessage.value = err.message
      fieldErrors.value = err.fieldErrors
    } else {
      errorMessage.value = 'Nie udało się utworzyć zgłoszenia'
    }
  } finally {
    submitting.value = false
  }
}

function reset(): void {
  result.value = null
  errorMessage.value = null
  fieldErrors.value = {}
  selectedResourceId.value = null
}
</script>

<template>
  <div class="mx-auto max-w-2xl space-y-5">
    <h1 class="text-2xl font-bold text-gray-100">{{ domainConfig.labels.createRequest }}</h1>

    <div v-if="result" class="space-y-4">
      <div class="rounded-lg bg-green-500/10 p-4 text-green-400">
        <p class="font-semibold">Zgłoszenie utworzone (#{{ result.id }})</p>
        <p class="text-sm">
          {{ domainConfig.labels.calculatedValue }}:
          <span class="font-bold">{{ formatMoney(result.calculatedValue, result.currency) }}</span>
        </p>
      </div>
      <AlgorithmBreakdown :breakdown="result.algorithmBreakdown" />
      <div class="flex gap-2">
        <button type="button" class="btn-secondary" @click="reset">Utwórz kolejne</button>
        <router-link class="btn-secondary" :to="{ name: 'request-details', params: { id: result.id } }">
          Szczegóły
        </router-link>
        <router-link class="btn-primary" :to="{ name: 'my-requests' }">
          {{ domainConfig.labels.myRequests }}
        </router-link>
      </div>
    </div>

    <div v-else class="card space-y-4 p-6">
      <div>
        <label class="label" for="resource-select">{{ domainConfig.resource.singular }}</label>
        <select id="resource-select" v-model.number="selectedResourceId" class="input">
          <option :value="null">— wybierz {{ domainConfig.resource.singular.toLowerCase() }} —</option>
          <option v-for="resource in resources" :key="resource.id" :value="resource.id">
            {{ resource.name }}
          </option>
        </select>
      </div>

      <RequestForm
        :resource="selectedResource"
        :resources="resources"
        :initial-date="initialDate"
        :submitting="submitting"
        :error-message="errorMessage"
        :field-errors="fieldErrors"
        @submit="onSubmit"
      />
    </div>
  </div>
</template>

