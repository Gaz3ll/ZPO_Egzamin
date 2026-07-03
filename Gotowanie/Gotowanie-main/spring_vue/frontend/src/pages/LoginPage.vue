<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { domainConfig } from '@/config/domain.config'
import { useAuthStore } from '@/stores/auth.store'
import { ApiError } from '@/types/api'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const email = ref('user@zpo.local')
const password = ref('user123')
const error = ref<string | null>(null)
const submitting = ref(false)

async function onSubmit(): Promise<void> {
  error.value = null
  submitting.value = true
  try {
    await auth.login({ email: email.value, password: password.value })
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    await router.push(redirect)
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'Logowanie nie powiodło się'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <form class="space-y-4" @submit.prevent="onSubmit">
    <h2 class="text-lg font-semibold text-gray-100">{{ domainConfig.labels.login }}</h2>

    <div>
      <label class="label" for="email">E-mail</label>
      <input id="email" v-model="email" type="email" class="input" required autocomplete="username" />
    </div>
    <div>
      <label class="label" for="password">Hasło</label>
      <input
        id="password"
        v-model="password"
        type="password"
        class="input"
        required
        autocomplete="current-password"
      />
    </div>

    <p v-if="error" class="rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-700">{{ error }}</p>

    <button type="submit" class="btn-primary w-full" :disabled="submitting">
      {{ submitting ? 'Logowanie…' : domainConfig.labels.login }}
    </button>

    <p class="text-center text-sm text-gray-400">
      Nie masz konta?
      <router-link :to="{ name: 'register' }" class="font-medium text-brand-600 hover:underline">
        {{ domainConfig.labels.register }}
      </router-link>
    </p>

    <div class="rounded-lg bg-gray-800 p-3 text-xs text-gray-400">
      <p class="font-medium text-gray-400">Konta demo:</p>
      <p>admin@zpo.local / admin123 · operator@zpo.local / operator123 · user@zpo.local / user123</p>
    </div>
  </form>
</template>

