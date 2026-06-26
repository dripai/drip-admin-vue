import { del, get, post, put } from '@/utils/request';
import type { ID } from '@/types/api';
import type { DeptItem } from '@/types/system';
import { statusValue, withNumericStatus } from './serialize';
export function getDeptTree() {
  return get<DeptItem[]>('/system/dept');
}
export function createDept(data: Partial<DeptItem>) {
  return post<void>('/system/dept', withNumericStatus(data as Record<string, unknown>));
}
export function updateDept(id: ID, data: Partial<DeptItem>) {
  return put<void>(`/system/dept/${id}`, withNumericStatus(data as Record<string, unknown>));
}
export function deleteDept(id: ID) {
  return del<void>(`/system/dept/${id}`);
}
export function updateDeptStatus(id: ID, status: string) {
  return put<void>(`/system/dept/${id}/status`, { status: statusValue(status) });
}
