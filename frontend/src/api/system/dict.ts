import { del, get, post, put } from '@/utils/request';
import type { ID } from '@/types/api';
import type { DictItem, DictTypeItem } from '@/types/system';
export function queryDictTypes() {
  return get<DictTypeItem[]>('/system/dicts/types');
}
export function createDictType(data: Partial<DictTypeItem>) {
  return post<void>('/system/dicts/types', data);
}
export function updateDictType(id: ID, data: Partial<DictTypeItem>) {
  return put<void>(`/system/dicts/types/${id}`, data);
}
export function deleteDictType(id: ID) {
  return del<void>(`/system/dicts/types/${id}`);
}
export function queryDictItems(typeCode: string) {
  return get<DictItem[]>(`/system/dicts/types/${typeCode}/items`);
}
export function createDictItem(data: Partial<DictItem>) {
  return post<void>('/system/dicts/items', data);
}
export function updateDictItem(id: ID, data: Partial<DictItem>) {
  return put<void>(`/system/dicts/items/${id}`, data);
}
export function deleteDictItem(id: ID) {
  return del<void>(`/system/dicts/items/${id}`);
}
export function refreshDictCache(typeCode?: string) {
  return post<void>('/system/dicts/cache/refresh', { typeCode });
}
