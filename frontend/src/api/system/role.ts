import { del, get, post, put } from '@/utils/request';
import type { ID, PageResult } from '@/types/api';
import type { RoleForm, RoleItem, RolePermissionForm, RoleQuery, UserItem } from '@/types/system';
import { statusValue, withNumericStatus } from './serialize';
export function queryRoles(params: RoleQuery) {
  return get<PageResult<RoleItem>>('/system/role', { params });
}
export function createRole(data: RoleForm) {
  return post<void>('/system/role', withNumericStatus(data as unknown as Record<string, unknown>));
}
export function updateRole(id: ID, data: RoleForm) {
  return put<void>(`/system/role/${id}`, withNumericStatus(data as unknown as Record<string, unknown>));
}
export function deleteRole(id: ID) {
  return del<void>(`/system/role/${id}`);
}
export function updateRoleStatus(id: ID, status: string) {
  return put<void>(`/system/role/${id}/status`, { status: statusValue(status) });
}
export function getRolePermissions(id: ID) {
  return get<RolePermissionForm>(`/system/role/${id}/permission`);
}
export function saveRolePermissions(id: ID, data: RolePermissionForm) {
  return put<void>(`/system/role/${id}/permission`, data);
}
export function getRoleUsers(id: ID) {
  return get<UserItem[]>(`/system/role/${id}/user`);
}
