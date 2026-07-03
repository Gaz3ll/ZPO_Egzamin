<script setup lang="ts">
import ResourceCard from '@/components/ResourceCard.vue'
import type { Resource } from '@/types/domain'

defineProps<{
  resources: Resource[]
  loading?: boolean
  emptyText?: string
}>()
</script>

<template>
  <div>
    <div v-if="loading" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <div v-for="n in 6" :key="n" class="card h-52 animate-pulse bg-gray-800"></div>
    </div>

    <div v-else-if="resources.length === 0" class="card p-8 text-center text-sm text-gray-400">
      {{ emptyText ?? 'Brak elementów do wyświetlenia.' }}
    </div>

    <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <ResourceCard v-for="resource in resources" :key="resource.id" :resource="resource" />
    </div>
  </div>
</template>

