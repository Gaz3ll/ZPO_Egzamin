<script setup lang="ts">
import { domainConfig } from '@/config/domain.config'
</script>

<template>
  <div class="flex min-h-full items-center justify-center px-4 py-12">
    <div class="w-full max-w-md">
      <div class="mb-6 text-center">
        <div class="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-brand-600 text-lg font-bold text-white">
          ZPO
        </div>
        <h1 class="text-2xl font-bold text-gray-100">{{ domainConfig.appName }}</h1>
        <p class="text-sm text-gray-500">Generic Resource &amp; Request Management System</p>
      </div>
      <div class="card p-6">
        <slot />
      </div>
    </div>
  </div>
</template>
