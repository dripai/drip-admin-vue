import { del, get, post, put } from '@/utils/request';
import type { ID, PageResult } from '@/types/api';
import type { DictItem, DictTypeItem } from '@/types/system';
import { statusValue, withNumericStatus } from './serialize';
export async function queryDictTypes() {
  const page = await get<PageResult<DictTypeItem>>('/system/dict/type', {
    params: { page: 1, pageSize: 100 },
  });
  return page.list;
}
export function createDictType(data: Partial<DictTypeItem>) {
  return post<void>('/system/dict/type', withNumericStatus(data as Record<string, unknown>));
}
export function updateDictType(id: ID, data: Partial<DictTypeItem>) {
  return put<void>(`/system/dict/type/${id}`, withNumericStatus(data as Record<string, unknown>));
}
export function deleteDictType(id: ID) {
  return del<void>(`/system/dict/type/${id}`);
}
export function queryDictItems(dictTypeId: ID) {
  return get<DictItem[]>(`/system/dict/type/${dictTypeId}/item`);
}
export function createDictItem(data: Partial<DictItem>) {
  return post<void>('/system/dict/item', withNumericStatus(data as Record<string, unknown>));
}
export function updateDictItem(id: ID, data: Partial<DictItem>) {
  return put<void>(`/system/dict/item/${id}`, withNumericStatus(data as Record<string, unknown>));
}
export function deleteDictItem(id: ID) {
  return del<void>(`/system/dict/item/${id}`);
}
export function refreshDictCache(typeCode?: string) {
  return post<void>('/system/dict/cache/refresh', { typeCode });
}
