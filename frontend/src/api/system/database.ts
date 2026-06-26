import { del, download, get, post } from '@/utils/request';
import type { ID, PageParams, PageResult } from '@/types/api';
import type { DatabaseBackupItem } from '@/types/system';
export function queryDatabaseBackups(params: PageParams & Record<string, unknown>) {
  return get<PageResult<DatabaseBackupItem>>('/system/database/backups', { params });
}
export function createDatabaseBackup(data: { backupName: string; remark?: string }) {
  return post<void>('/system/database/backups', data);
}
export function downloadDatabaseBackup(id: ID) {
  return download(`/system/database/backups/${id}/download`);
}
export function restoreDatabaseBackup(id: ID) {
  return post<void>(`/system/database/backups/${id}/restore`);
}
export function deleteDatabaseBackup(id: ID) {
  return del<void>(`/system/database/backups/${id}`);
}
