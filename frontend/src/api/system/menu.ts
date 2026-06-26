import { del, get, post, put } from '@/utils/request';
import type { ID } from '@/types/api';
import type { MenuNode } from '@/types/system';
import { statusValue, withNumericMenuFlags } from './serialize';
export function getMenuTree() {
  return get<MenuNode[]>('/system/menu');
}
export function createMenu(data: Partial<MenuNode>) {
  return post<void>('/system/menu', withNumericMenuFlags(data as Record<string, unknown>));
}
export function updateMenu(id: ID, data: Partial<MenuNode>) {
  return put<void>(`/system/menu/${id}`, withNumericMenuFlags(data as Record<string, unknown>));
}
export function deleteMenu(id: ID) {
  return del<void>(`/system/menu/${id}`);
}
export function updateMenuStatus(id: ID, status: string) {
  return put<void>(`/system/menu/${id}/status`, { status: statusValue(status) });
}
