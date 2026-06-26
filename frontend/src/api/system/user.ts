import { del, get, post, put } from '@/utils/request';
import type { ID, PageResult } from '@/types/api';
import type { RoleSummary, UserForm, UserItem, UserQuery } from '@/types/system';
export function queryUsers(params: UserQuery) {
  return get<PageResult<UserItem>>('/system/users', { params });
}
export function createUser(data: UserForm) {
  return post<void>('/system/users', data);
}
export function updateUser(id: ID, data: UserForm) {
  return put<void>(`/system/users/${id}`, data);
}
export function deleteUser(id: ID) {
  return del<void>(`/system/users/${id}`);
}
export function updateUserStatus(id: ID, status: string) {
  return put<void>(`/system/users/${id}/status`, { status });
}
export function resetUserPassword(id: ID) {
  return post<void>(`/system/users/${id}/reset-password`);
}
export function assignUserRoles(id: ID, roleIds: ID[]) {
  return put<void>(`/system/users/${id}/roles`, { roleIds });
}
export function listRoleOptions() {
  return get<RoleSummary[]>('/system/roles/options');
}
