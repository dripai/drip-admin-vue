import { del, download, get, post } from '@/utils/request';
import type { ID, PageParams, PageResult } from '@/types/api';
import type { DatabaseBackupItem } from '@/types/system';
export function queryDatabaseBackups(params: PageParams & Record<string, unknown>) {
  return get<PageResult<DatabaseBackupItem>>('/system/databaseBackup', { params });
}
export function createDatabaseBackup(data: { backupName: string; remark?: string }) {
  return post<void>('/system/databaseBackup', data);
}
export function downloadDatabaseBackup(id: ID) {
  return download(`/system/databaseBackup/${id}/download`);
}
export function restoreDatabaseBackup(id: ID) {
  return post<void>(`/system/databaseBackup/${id}/restore`, { confirmed: true });
}
export function deleteDatabaseBackup(id: ID) {
  return del<void>(`/system/databaseBackup/${id}`);
}
