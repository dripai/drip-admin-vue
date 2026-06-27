import { get } from '@/utils/request';
import type { ID, PageParams, PageResult } from '@/types/api';
import type { LoginLogItem, OperationLogItem } from '@/types/system';
type RangeDateLike = {
  startOf: (unit: string) => RangeDateLike;
  endOf: (unit: string) => RangeDateLike;
  format: (template: string) => string;
};

export function queryLoginLogs(params: PageParams & Record<string, unknown>) {
  return get<PageResult<LoginLogItem>>('/system/loginLog', { params: loginLogParams(params) });
}
export function getLoginLog(id: ID) {
  return get<LoginLogItem>(`/system/loginLog/${id}`);
}
export function queryOperationLogs(params: PageParams & Record<string, unknown>) {
  return get<PageResult<OperationLogItem>>('/system/operationLog', { params: operationLogParams(params) });
}
export function getOperationLog(id: ID) {
  return get<OperationLogItem>(`/system/operationLog/${id}`);
}

function loginLogParams(params: PageParams & Record<string, unknown>) {
  const { createdRange, ...rest } = params;
  if (!Array.isArray(createdRange) || createdRange.length !== 2) return rest;
  return {
    ...rest,
    loginFrom: formatRangeDate(createdRange[0], 'start'),
    loginTo: formatRangeDate(createdRange[1], 'end'),
  };
}

function operationLogParams(params: PageParams & Record<string, unknown>) {
  const { createdRange, ...rest } = params;
  if (!Array.isArray(createdRange) || createdRange.length !== 2) return rest;
  return {
    ...rest,
    createdFrom: formatRangeDate(createdRange[0], 'start'),
    createdTo: formatRangeDate(createdRange[1], 'end'),
  };
}

function formatRangeDate(value: unknown, boundary: 'start' | 'end') {
  if (value && typeof value === 'object' && 'format' in value) {
    const date = value as RangeDateLike;
    return (boundary === 'start' ? date.startOf('day') : date.endOf('day')).format('YYYY-MM-DDTHH:mm:ss');
  }
  return value;
}
