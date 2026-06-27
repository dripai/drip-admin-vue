import { get, post, put } from '@/utils/request';
import type { CurrentUserResult, LoginRequest, LoginResult } from '@/types/auth';

export function loginApi(data: LoginRequest) {
  return post<LoginResult>('/system/login', data);
}
export function logoutApi() {
  return post<void>('/system/logout');
}
export function getCurrentUserApi() {
  return get<CurrentUserResult>('/system/me');
}
export function changePasswordApi(data: { oldPassword: string; newPassword: string }) {
  return put<void>('/system/password', data);
}
export function updateProfileApi(data: { realName: string; phone?: string; email?: string }) {
  return put<void>('/system/profile', data);
}
