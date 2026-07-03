import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '@/api/auth.api'
import { clearToken, getToken, setToken } from '@/api/httpClient'
import type { LoginRequest, RegisterRequest, Role, User } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const user = ref<User | null>(null)
  const initialized = ref(false)

  const isAuthenticated = computed<boolean>(() => token.value !== null && user.value !== null)
  const role = computed<Role | null>(() => user.value?.role ?? null)
  const isAdmin = computed<boolean>(() => role.value === 'ADMIN')
  const isOperator = computed<boolean>(() => role.value === 'OPERATOR')
  const isStaff = computed<boolean>(() => isAdmin.value || isOperator.value)

  function applyAuth(newToken: string, newUser: User): void {
    token.value = newToken
    user.value = newUser
    setToken(newToken)
  }

  async function login(payload: LoginRequest): Promise<void> {
    const response = await authApi.login(payload)
    applyAuth(response.token, response.user)
  }

  async function register(payload: RegisterRequest): Promise<void> {
    const response = await authApi.register(payload)
    applyAuth(response.token, response.user)
  }

  async function fetchMe(): Promise<void> {
    user.value = await authApi.me()
  }

  function logout(): void {
    token.value = null
    user.value = null
    clearToken()
  }

  /** Runs once on app start: if a token is stored, hydrate the current user. */
  async function init(): Promise<void> {
    if (initialized.value) {
      return
    }
    if (token.value !== null) {
      try {
        await fetchMe()
      } catch {
        logout()
      }
    }
    initialized.value = true
  }

  return {
    token,
    user,
    initialized,
    isAuthenticated,
    role,
    isAdmin,
    isOperator,
    isStaff,
    login,
    register,
    fetchMe,
    logout,
    init,
  }
})
