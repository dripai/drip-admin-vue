import { del, get, post, put } from '@/utils/request';
import type { ID, PageParams, PageResult } from '@/types/api';
import type { JobItem, JobRecordItem } from '@/types/system';
import { statusValue, withNumericStatus } from './serialize';
export function queryJobs(params: PageParams & Record<string, unknown>) {
  return get<PageResult<JobItem>>('/system/job', { params });
}
export function createJob(data: Partial<JobItem>) {
  return post<void>('/system/job', withNumericStatus(data as Record<string, unknown>));
}
export function updateJob(id: ID, data: Partial<JobItem>) {
  return put<void>(`/system/job/${id}`, withNumericStatus(data as Record<string, unknown>));
}
export function deleteJob(id: ID) {
  return del<void>(`/system/job/${id}`);
}
export function updateJobStatus(id: ID, status: string) {
  return put<void>(`/system/job/${id}/status`, { status: statusValue(status) });
}
export function runJob(id: ID) {
  return post<void>(`/system/job/${id}/run`);
}
export function queryJobRecords(id: ID, params: PageParams) {
  return get<PageResult<JobRecordItem>>(`/system/job/${id}/runLog`, { params });
}
