import { http } from '@/api/httpClient'
import type { AuthResponse, LoginRequest, RegisterRequest, User } from '@/types/auth'

export const authApi = {
  login: (payload: LoginRequest): Promise<AuthResponse> => http.post<AuthResponse>('/auth/login', payload),
  register: (payload: RegisterRequest): Promise<AuthResponse> =>
    http.post<AuthResponse>('/auth/register', payload),
  me: (): Promise<User> => http.get<User>('/auth/me'),
}
