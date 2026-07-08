import { del, get, post, put } from '@/utils/request';
import type { ID, PageParams, PageResult } from '@/types/api';
import type { PrintTemplateItem } from '@/types/system';
import { statusValue, withNumericStatus } from './serialize';

export function queryPrintTemplates(params: PageParams & Record<string, unknown>) {
  return get<PageResult<PrintTemplateItem>>('/system/print-template', { params });
}

export function getPrintTemplate(id: ID) {
  return get<PrintTemplateItem>(`/system/print-template/${id}`);
}

export function createPrintTemplate(data: Partial<PrintTemplateItem>) {
  return post<ID>('/system/print-template', printTemplatePayload(data));
}

export function copyPrintTemplate(id: ID, data: Pick<PrintTemplateItem, 'code' | 'name' | 'status'>) {
  return post<ID>(`/system/print-template/${id}/copy`, withNumericStatus(data as Record<string, unknown>));
}

export function updatePrintTemplate(id: ID, data: Partial<PrintTemplateItem>) {
  return put<void>(`/system/print-template/${id}`, printTemplatePayload(data));
}

export function deletePrintTemplate(id: ID) {
  return del<void>(`/system/print-template/${id}`);
}

export function updatePrintTemplateStatus(id: ID, status: string) {
  return put<void>(`/system/print-template/${id}/status`, { status: statusValue(status) });
}

function printTemplatePayload(data: Partial<PrintTemplateItem>) {
  const { id, createdAt, updatedAt, deleted, ...rest } = data;
  void id;
  void createdAt;
  void updatedAt;
  void deleted;
  return withNumericStatus(rest as Record<string, unknown>);
}
