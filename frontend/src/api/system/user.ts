import { del, get, post, put } from '@/utils/request';
import type { ID, PageResult } from '@/types/api';
import type { RoleSummary, UserForm, UserItem, UserQuery } from '@/types/system';
import { statusValue, withNumericStatus } from './serialize';
export function queryUsers(params: UserQuery) {
  return get<PageResult<UserItem>>('/system/user', { params });
}
export function createUser(data: UserForm) {
  return post<void>('/system/user', withNumericStatus(data as unknown as Record<string, unknown>));
}
export function updateUser(id: ID, data: UserForm) {
  return put<void>(`/system/user/${id}`, userUpdatePayload(data));
}
export function deleteUser(id: ID) {
  return del<void>(`/system/user/${id}`);
}
export function updateUserStatus(id: ID, status: string) {
  return put<void>(`/system/user/${id}/status`, { status: statusValue(status) });
}
export function resetUserPassword(id: ID) {
  return post<void>(`/system/user/${id}/resetPassword`, {});
}
export function assignUserRoles(id: ID, roleIds: ID[]) {
  return put<void>(`/system/user/${id}/role`, { roleIds });
}
export function listRoleOptions() {
  return get<RoleSummary[]>('/system/role/option');
}

function userUpdatePayload(data: UserForm) {
  const payload = withNumericStatus(data as unknown as Record<string, unknown>);
  if (!payload.password) delete payload.password;
  return payload;
}
