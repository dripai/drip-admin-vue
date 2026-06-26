import { del, get, post, put } from '@/utils/request';
import type { ID, PageParams, PageResult } from '@/types/api';
import type { ConfigItem } from '@/types/system';
import { booleanNumber, statusValue, withNumericStatus } from './serialize';
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

function configPayload(data: Partial<ConfigItem>) {
  const payload = withNumericStatus(data as Record<string, unknown>);
  return { ...payload, isSensitive: booleanNumber(payload.isSensitive) };
}
