<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
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
const router = useRouter()
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

function logout(): void {
  auth.logout()
  void router.push({ name: 'login' })
}
</script>

<template>
  <header class="border-b border-gray-800 bg-gray-900">
    <div class="mx-auto flex h-14 w-full max-w-7xl items-center justify-between px-4">
      <div class="flex items-center gap-1">
        <router-link :to="{ name: 'dashboard' }" class="flex items-center gap-2 font-semibold text-gray-100 shrink-0">
          <span class="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-xs font-bold text-white">ZPO</span>
          {{ domain.config.appName }}
        </router-link>
        <div class="ml-6 hidden items-center gap-1 md:flex">
          <router-link
            v-for="item in visibleItems"
            :key="item.label"
            :to="item.to"
            class="rounded-lg px-3 py-1.5 text-sm font-medium text-gray-400 transition hover:bg-gray-800 hover:text-gray-200"
            active-class="bg-gray-800 text-brand-400"
          >
            {{ item.label }}
          </router-link>
        </div>
      </div>

      <div v-if="auth.user" class="flex items-center gap-4">
        <span class="hidden text-xs text-gray-500 sm:block">{{ auth.user.email }}</span>
        <button class="btn-secondary text-xs" type="button" @click="logout">
          {{ labels.logout }}
        </button>
      </div>
    </div>
  </header>
</template>
