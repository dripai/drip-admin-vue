import { del, get, post, put } from '@/utils/request';
import type { ID, PageParams, PageResult } from '@/types/api';
import type { ConfigItem } from '@/types/system';
import { statusValue, withNumericStatus } from './serialize';
export function queryConfigs(params: PageParams & Record<string, unknown>) {
  return get<PageResult<ConfigItem>>('/system/config', { params });
}
export function createConfig(data: Partial<ConfigItem>) {
  return post<void>('/system/config', configPayload(data));
}
export function updateConfig(id: ID, data: Partial<ConfigItem>) {
  return put<void>(`/system/config/${id}`, configPayload(data));
}
export function deleteConfig(id: ID) {
  return del<void>(`/system/config/${id}`);
}
export function updateConfigStatus(id: ID, status: string) {
  return put<void>(`/system/config/${id}/status`, { status: statusValue(status) });
}
export function getPublicConfig() {
  return get<{ systemName: string; logoUrl: string }>('/system/publicConfig');
}

function configPayload(data: Partial<ConfigItem>) {
  const { builtin, ...rest } = data;
  void builtin;
  return withNumericStatus(rest as Record<string, unknown>);
}
