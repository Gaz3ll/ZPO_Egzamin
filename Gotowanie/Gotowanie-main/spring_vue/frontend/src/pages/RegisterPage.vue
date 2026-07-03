<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { domainConfig } from '@/config/domain.config'
import { useAuthStore } from '@/stores/auth.store'
import { ApiError } from '@/types/api'

const auth = useAuthStore()
const router = useRouter()

const name = ref('')
const email = ref('')
const password = ref('')
const error = ref<string | null>(null)
const fieldErrors = ref<Record<string, string>>({})
const submitting = ref(false)

async function onSubmit(): Promise<void> {
  error.value = null
  fieldErrors.value = {}
  submitting.value = true
  try {
    await auth.register({ name: name.value, email: email.value, password: password.value })
    await router.push('/dashboard')
  } catch (err) {
    if (err instanceof ApiError) {
      error.value = err.message
      fieldErrors.value = err.fieldErrors
    } else {
      error.value = 'Rejestracja nie powiodła się'
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <form class="space-y-4" @submit.prevent="onSubmit">
    <h2 class="text-lg font-semibold text-gray-100">{{ domainConfig.labels.register }}</h2>

    <div>
      <label class="label" for="name">Imię i nazwisko</label>
      <input id="name" v-model="name" type="text" class="input" required />
      <p v-if="fieldErrors.name" class="mt-1 text-xs text-red-400">{{ fieldErrors.name }}</p>
    </div>
    <div>
      <label class="label" for="email">E-mail</label>
      <input id="email" v-model="email" type="email" class="input" required autocomplete="username" />
      <p v-if="fieldErrors.email" class="mt-1 text-xs text-red-400">{{ fieldErrors.email }}</p>
    </div>
    <div>
      <label class="label" for="password">Hasło</label>
      <input id="password" v-model="password" type="password" class="input" required minlength="6" autocomplete="new-password" />
      <p v-if="fieldErrors.password" class="mt-1 text-xs text-red-400">{{ fieldErrors.password }}</p>
    </div>

    <p v-if="error" class="rounded-lg bg-red-500/10 px-3 py-2 text-sm text-red-700">{{ error }}</p>

    <button type="submit" class="btn-primary w-full" :disabled="submitting">
      {{ submitting ? 'Tworzenie konta…' : domainConfig.labels.register }}
    </button>

    <p class="text-center text-sm text-gray-400">
      Masz już konto?
      <router-link :to="{ name: 'login' }" class="font-medium text-brand-600 hover:underline">
        {{ domainConfig.labels.login }}
      </router-link>
    </p>
  </form>
</template>

