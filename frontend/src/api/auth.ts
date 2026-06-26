import { get, post } from '@/utils/request';
import type { CurrentUserResult, LoginRequest, LoginResult } from '@/types/auth';

export function loginApi(data: LoginRequest) {
  return post<LoginResult>('/auth/login', data);
}
export function logoutApi() {
  return post<void>('/auth/logout');
}
export function getCurrentUserApi() {
  return get<CurrentUserResult>('/auth/me');
}
export function changePasswordApi(data: { oldPassword: string; newPassword: string }) {
  return post<void>('/auth/password', data);
}
