export type Role = 'USER' | 'OPERATOR' | 'ADMIN'

export interface User {
  id: number
  name: string
  email: string
  role: Role
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  tokenType: string
  expiresIn: number
  user: User
}
