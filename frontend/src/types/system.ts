import type { ID, Status } from './api';

export type MenuType = 'DIRECTORY' | 'MENU' | 'BUTTON';

export interface RoleSummary {
  id: ID;
  roleName: string;
  roleCode: string;
}
export interface DeptSummary {
  id: ID;
  deptName: string;
}
export interface UserProfile {
  id: ID;
  username: string;
  realName: string;
  avatar?: string;
  dept?: DeptSummary;
}

export interface MenuNode {
  id: ID;
  parentId?: ID | null;
  name: string;
  type: MenuType;
  path?: string;
  component?: string;
  permissionCode?: string;
  icon?: string;
  sort: number;
  status: Status;
  visible?: boolean;
  hidden?: boolean;
  children?: MenuNode[];
}

export interface UserItem extends UserProfile {
  phone?: string;
  email?: string;
  status: Status;
  roles: RoleSummary[];
  createdAt: string;
  lastLoginAt?: string;
}
export interface UserQuery {
  username?: string;
  realName?: string;
  phone?: string;
  status?: Status;
  roleId?: ID;
  deptId?: ID;
  createdRange?: [string, string];
  page: number;
  pageSize: number;
}
export interface UserForm {
  id?: ID;
  username: string;
  realName: string;
  phone?: string;
  email?: string;
  password?: string;
  deptId?: ID;
  roleIds: ID[];
  status: Status;
}

export interface RoleItem extends RoleSummary {
  status: Status;
  remark?: string;
  createdAt: string;
}
export interface RoleQuery {
  roleName?: string;
  roleCode?: string;
  status?: Status;
  createdRange?: [string, string];
  page: number;
  pageSize: number;
}
export interface RoleForm {
  id?: ID;
  roleName: string;
  roleCode: string;
  status: Status;
  remark?: string;
}
export interface RolePermissionForm {
  menuIds: ID[];
  permissionCodes: string[];
}

export interface DeptItem {
  id: ID;
  parentId?: ID | null;
  deptName: string;
  leader?: string;
  phone?: string;
  sort: number;
  status: Status;
  children?: DeptItem[];
}
export interface DictTypeItem {
  id: ID;
  dictName: string;
  dictCode: string;
  status: Status;
  remark?: string;
}
export interface DictItem {
  id: ID;
  typeCode: string;
  label: string;
  value: string;
  color?: string;
  sort: number;
  status: Status;
}
export interface ConfigItem {
  id: ID;
  configName: string;
  configKey: string;
  configValue: string;
  groupName: string;
  sensitive: boolean;
  status: Status;
  updatedAt: string;
}
export interface LoginLogItem {
  id: ID;
  username: string;
  status: 'SUCCESS' | 'FAILED' | 'LOGOUT';
  ip: string;
  userAgent: string;
  failureReason?: string;
  loginAt: string;
}
export interface OperationLogItem {
  id: ID;
  operator: string;
  module: string;
  action: string;
  method: string;
  path: string;
  status: 'SUCCESS' | 'FAILED';
  duration: number;
  createdAt: string;
  detail?: string;
}
export interface OnlineUserItem {
  id: ID;
  username: string;
  realName: string;
  deviceType: string;
  ip: string;
  userAgent: string;
  loginAt: string;
  lastActiveAt: string;
  expireAt: string;
  current?: boolean;
}
export interface JobItem {
  id: ID;
  jobName: string;
  jobCode: string;
  cron: string;
  status: Status;
  lastRunAt?: string;
  lastResult?: string;
  nextRunAt?: string;
}
export interface JobRecordItem {
  id: ID;
  jobId: ID;
  status: 'SUCCESS' | 'FAILED';
  startedAt: string;
  finishedAt?: string;
  message?: string;
}
export interface DatabaseBackupItem {
  id: ID;
  backupName: string;
  fileSize: number;
  status: 'SUCCESS' | 'FAILED' | 'RUNNING';
  createdBy: string;
  createdAt: string;
  remark?: string;
}
export interface PreferenceState {
  collapsed: boolean;
  tableSize: 'small' | 'middle' | 'large';
}
export interface OptionItem {
  label: string;
  value: string | number;
  disabled?: boolean;
  color?: string;
}
