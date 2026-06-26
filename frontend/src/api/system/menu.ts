import { del, get, post, put } from '@/utils/request';
import type { ID } from '@/types/api';
import type { MenuNode } from '@/types/system';
export function getMenuTree() {
  return get<MenuNode[]>('/system/menus');
}
export function createMenu(data: Partial<MenuNode>) {
  return post<void>('/system/menus', data);
}
export function updateMenu(id: ID, data: Partial<MenuNode>) {
  return put<void>(`/system/menus/${id}`, data);
}
export function deleteMenu(id: ID) {
  return del<void>(`/system/menus/${id}`);
}
export function updateMenuStatus(id: ID, status: string) {
  return put<void>(`/system/menus/${id}/status`, { status });
}
