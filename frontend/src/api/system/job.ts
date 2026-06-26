import { del, get, post, put } from '@/utils/request';
import type { ID, PageParams, PageResult } from '@/types/api';
import type { JobItem, JobRecordItem } from '@/types/system';
export function queryJobs(params: PageParams & Record<string, unknown>) {
  return get<PageResult<JobItem>>('/system/jobs', { params });
}
export function createJob(data: Partial<JobItem>) {
  return post<void>('/system/jobs', data);
}
export function updateJob(id: ID, data: Partial<JobItem>) {
  return put<void>(`/system/jobs/${id}`, data);
}
export function deleteJob(id: ID) {
  return del<void>(`/system/jobs/${id}`);
}
export function updateJobStatus(id: ID, status: string) {
  return put<void>(`/system/jobs/${id}/status`, { status });
}
export function runJob(id: ID) {
  return post<void>(`/system/jobs/${id}/run`);
}
export function queryJobRecords(id: ID, params: PageParams) {
  return get<PageResult<JobRecordItem>>(`/system/jobs/${id}/records`, { params });
}
