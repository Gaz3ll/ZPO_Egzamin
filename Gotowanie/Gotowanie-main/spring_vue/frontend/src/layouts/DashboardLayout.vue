<script setup lang="ts">
import AppNavbar from '@/components/AppNavbar.vue'
</script>

<template>
  <div class="flex min-h-full flex-col">
    <AppNavbar />
    <div class="mx-auto flex w-full max-w-7xl flex-1 px-4 py-6">
      <main class="min-w-0 flex-1">
        <slot />
      </main>
    </div>
  </div>
</template>
