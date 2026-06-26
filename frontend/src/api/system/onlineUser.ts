import { get, post } from '@/utils/request';
import type { ID, PageParams, PageResult } from '@/types/api';
import type { OnlineUserItem } from '@/types/system';
export function queryOnlineUsers(params: PageParams & Record<string, unknown>) {
  return get<PageResult<OnlineUserItem>>('/system/online-users', { params });
}
export function forceOffline(id: ID) {
  return post<void>(`/system/online-users/${id}/force-offline`);
}
