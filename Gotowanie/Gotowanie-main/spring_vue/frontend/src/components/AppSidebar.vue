<script setup lang="ts">
import { computed } from 'vue'
import type { RouteLocationRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useDomainStore } from '@/stores/domain.store'

interface NavItem {
  label: string
  to: RouteLocationRaw
  show: boolean
}

const auth = useAuthStore()
const domain = useDomainStore()
const labels = domain.config.labels

const items = computed<NavItem[]>(() => [
  { label: labels.dashboard, to: { name: 'dashboard' }, show: true },
  { label: labels.resources, to: { name: 'resources' }, show: true },
  { label: labels.createRequest, to: { name: 'create-request' }, show: true },
  { label: labels.myRequests, to: { name: 'my-requests' }, show: true },
  { label: labels.calendar, to: { name: 'calendar' }, show: auth.isAdmin },
  { label: labels.adminResources, to: { name: 'admin-resources' }, show: auth.isAdmin },
  { label: labels.adminRequests, to: { name: 'admin-requests' }, show: auth.isStaff },
])

const visibleItems = computed(() => items.value.filter((item) => item.show))
</script>

<template>
  <aside>
    <nav class="card p-2">
      <ul class="space-y-1">
        <li v-for="item in visibleItems" :key="item.label">
          <router-link
            :to="item.to"
            class="block rounded-lg px-3 py-2 text-sm font-medium text-gray-400 hover:bg-gray-800"
            active-class="bg-brand-500/10 text-brand-400"
          >
            {{ item.label }}
          </router-link>
        </li>
      </ul>
    </nav>
  </aside>
</template>

