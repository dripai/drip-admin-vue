import { del, get, post, put } from '@/utils/request';
import type { ID, PageParams, PageResult } from '@/types/api';
import type { ConfigItem } from '@/types/system';
export function queryConfigs(params: PageParams & Record<string, unknown>) {
  return get<PageResult<ConfigItem>>('/system/configs', { params });
}
export function createConfig(data: Partial<ConfigItem>) {
  return post<void>('/system/configs', data);
}
export function updateConfig(id: ID, data: Partial<ConfigItem>) {
  return put<void>(`/system/configs/${id}`, data);
}
export function deleteConfig(id: ID) {
  return del<void>(`/system/configs/${id}`);
}
export function updateConfigStatus(id: ID, status: string) {
  return put<void>(`/system/configs/${id}/status`, { status });
}
