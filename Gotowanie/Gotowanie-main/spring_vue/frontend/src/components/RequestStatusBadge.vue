<script setup lang="ts">
import { computed } from 'vue'
import { requestStatusMeta, resourceStatusMeta } from '@/config/domain.config'
import type { RequestStatus, ResourceStatus } from '@/types/domain'

const props = defineProps<{
  status: RequestStatus | ResourceStatus
  kind?: 'request' | 'resource'
}>()

const meta = computed(() => {
  if (props.kind === 'resource') {
    return resourceStatusMeta[props.status as ResourceStatus]
  }
  const requestMeta = requestStatusMeta[props.status as RequestStatus]
  return requestMeta ?? resourceStatusMeta[props.status as ResourceStatus]
})
</script>

<template>
  <span
    class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
    :class="meta?.classes ?? 'bg-gray-800 text-gray-300'"
  >
    {{ meta?.label ?? status }}
  </span>
</template>

