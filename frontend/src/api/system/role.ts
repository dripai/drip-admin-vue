import { del, get, post, put } from '@/utils/request';
import type { ID, PageResult } from '@/types/api';
import type { RoleForm, RoleItem, RolePermissionForm, RoleQuery, UserItem } from '@/types/system';
export function queryRoles(params: RoleQuery) {
  return get<PageResult<RoleItem>>('/system/roles', { params });
}
export function createRole(data: RoleForm) {
  return post<void>('/system/roles', data);
}
export function updateRole(id: ID, data: RoleForm) {
  return put<void>(`/system/roles/${id}`, data);
}
export function deleteRole(id: ID) {
  return del<void>(`/system/roles/${id}`);
}
export function updateRoleStatus(id: ID, status: string) {
  return put<void>(`/system/roles/${id}/status`, { status });
}
export function getRolePermissions(id: ID) {
  return get<RolePermissionForm>(`/system/roles/${id}/permissions`);
}
export function saveRolePermissions(id: ID, data: RolePermissionForm) {
  return put<void>(`/system/roles/${id}/permissions`, data);
}
export function getRoleUsers(id: ID) {
  return get<UserItem[]>(`/system/roles/${id}/users`);
}
