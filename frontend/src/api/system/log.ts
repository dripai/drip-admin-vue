import { get } from '@/utils/request';
import type { ID, PageParams, PageResult } from '@/types/api';
import type { LoginLogItem, OperationLogItem } from '@/types/system';
export function queryLoginLogs(params: PageParams & Record<string, unknown>) {
  return get<PageResult<LoginLogItem>>('/system/loginLog', { params });
}
export function getLoginLog(id: ID) {
  return get<LoginLogItem>(`/system/loginLog/${id}`);
}
export function queryOperationLogs(params: PageParams & Record<string, unknown>) {
  return get<PageResult<OperationLogItem>>('/system/operationLog', { params });
}
export function getOperationLog(id: ID) {
  return get<OperationLogItem>(`/system/operationLog/${id}`);
}
