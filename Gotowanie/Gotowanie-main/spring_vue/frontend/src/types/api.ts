// Mirrors the backend's common API envelope.

export interface DomainError {
  code: string
  message: string
  fieldErrors?: Record<string, string> | null
  timestamp?: string
}

export interface ApiResponse<T> {
  success: boolean
  data: T | null
  error: DomainError | null
}

export interface PageResponse<T> {
  items: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

/** Normalized error thrown by the HTTP client and consumed by stores/pages. */
export class ApiError extends Error {
  readonly code: string
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(message: string, code: string, status: number, fieldErrors?: Record<string, string> | null) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.status = status
    this.fieldErrors = fieldErrors ?? {}
  }
}
