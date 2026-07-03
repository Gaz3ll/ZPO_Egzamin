import axios, { type AxiosResponse } from 'axios'
import { ApiError, type ApiResponse } from '@/types/api'

const TOKEN_KEY = 'zpo_token'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api',
  headers: { 'Content-Type': 'application/json' },
})

// Attach the bearer token to every request.
instance.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// On an expired/invalid session (401 while a token is present), clear it and bounce to login.
instance.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401 && getToken()) {
      clearToken()
      if (window.location.pathname !== '/login') {
        window.location.assign('/login')
      }
    }
    return Promise.reject(error)
  },
)

function normalizeError(err: unknown): ApiError {
  if (err instanceof ApiError) {
    return err
  }
  if (axios.isAxiosError(err)) {
    const response = err.response
    if (response) {
      const body = response.data as ApiResponse<unknown> | undefined
      const domainError = body?.error
      return new ApiError(
        domainError?.message ?? err.message,
        domainError?.code ?? 'INTERNAL_ERROR',
        response.status,
        domainError?.fieldErrors,
      )
    }
    return new ApiError('Brak połączenia z serwerem', 'NETWORK_ERROR', 0)
  }
  return new ApiError('Wystąpił nieznany błąd', 'INTERNAL_ERROR', 0)
}

async function unwrap<T>(promise: Promise<AxiosResponse<ApiResponse<T>>>): Promise<T> {
  try {
    const response = await promise
    const body = response.data
    if (body.success && body.data !== null) {
      return body.data
    }
    throw new ApiError(
      body.error?.message ?? 'Żądanie nie powiodło się',
      body.error?.code ?? 'INTERNAL_ERROR',
      response.status,
      body.error?.fieldErrors,
    )
  } catch (err) {
    throw normalizeError(err)
  }
}

export const http = {
  get: <T>(url: string, params?: Record<string, unknown>): Promise<T> =>
    unwrap<T>(instance.get<ApiResponse<T>>(url, { params })),
  post: <T>(url: string, data?: unknown): Promise<T> =>
    unwrap<T>(instance.post<ApiResponse<T>>(url, data)),
  put: <T>(url: string, data?: unknown): Promise<T> =>
    unwrap<T>(instance.put<ApiResponse<T>>(url, data)),
  patch: <T>(url: string, data?: unknown): Promise<T> =>
    unwrap<T>(instance.patch<ApiResponse<T>>(url, data)),
  del: <T>(url: string): Promise<T> => unwrap<T>(instance.delete<ApiResponse<T>>(url)),
}
