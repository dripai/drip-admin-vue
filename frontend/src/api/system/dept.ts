import { del, get, post, put } from '@/utils/request';
import type { ID } from '@/types/api';
import type { DeptItem } from '@/types/system';
export function getDeptTree() {
  return get<DeptItem[]>('/system/depts');
}
export function createDept(data: Partial<DeptItem>) {
  return post<void>('/system/depts', data);
}
export function updateDept(id: ID, data: Partial<DeptItem>) {
  return put<void>(`/system/depts/${id}`, data);
}
export function deleteDept(id: ID) {
  return del<void>(`/system/depts/${id}`);
}
export function updateDeptStatus(id: ID, status: string) {
  return put<void>(`/system/depts/${id}/status`, { status });
}
