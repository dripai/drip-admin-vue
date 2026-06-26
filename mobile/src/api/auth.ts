import { request } from '@/utils/request'
import type { CurrentUserResponse, LoginRequest, LoginResponse } from '@/types/auth'

export function loginApi(data: LoginRequest) {
  return request.post<LoginResponse>('/auth/login', data)
}

export function getCurrentUserApi() {
  return request.get<CurrentUserResponse>('/auth/me')
}

export function logoutApi() {
  return request.post<null>('/auth/logout')
}
