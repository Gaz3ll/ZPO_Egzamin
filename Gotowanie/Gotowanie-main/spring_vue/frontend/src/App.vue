<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AuthLayout from '@/layouts/AuthLayout.vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import { useDomainStore } from '@/stores/domain.store'

const route = useRoute()
const domain = useDomainStore()

const layoutComponent = computed(() => (route.meta.layout === 'auth' ? AuthLayout : DashboardLayout))

onMounted(() => {
  // Load enum vocabularies / backend domain name once; failures fall back to static config.
  void domain.loadConfig()
})
</script>

<template>
  <component :is="layoutComponent">
    <router-view />
  </component>
</template>
