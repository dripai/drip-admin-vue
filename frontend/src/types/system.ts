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
  phone?: string;
  email?: string;
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
  deptCode: string;
  leaderUserId?: ID | null;
  sort: number;
  status: Status;
  children?: DeptItem[];
}
export interface DictTypeItem {
  id: ID;
  dictName: string;
  dictCode: string;
  status: Status;
  builtin?: number;
  remark?: string;
}
export interface DictItem {
  id: ID;
  dictTypeId?: ID;
  typeCode: string;
  label: string;
  value: string;
  isDefault?: number;
  sort: number;
  status: Status;
  builtin?: number;
}
export interface ConfigItem {
  id: ID;
  configName: string;
  configKey: string;
  configValue: string;
  valueType: 'string' | 'boolean' | 'number';
  builtin: number;
  status: Status;
  remark?: string;
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
  operatorId?: ID;
  operator?: string;
  module: string;
  action: string;
  method: string;
  path: string;
  requestParams?: string;
  status: 'RUNNING' | 'SUCCESS' | 'FAIL';
  errorMessage?: string;
  duration: number;
  createdAt: string;
}
export interface OnlineUserItem {
  tokenId: string;
  userId: ID;
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
  cronExpression: string;
  executorType: 'shell' | 'bat' | 'powershell' | 'python' | 'java';
  scriptFile?: string;
  scriptArgs?: string;
  className?: string;
  methodName: string;
  status: Status;
  remark?: string;
  createdAt?: string;
  updatedAt?: string;
}
export interface JobRecordItem {
  id: ID;
  jobId: ID;
  jobName: string;
  status: 'RUNNING' | 'SUCCESS' | 'FAIL';
  startedAt: string;
  finishedAt?: string;
  costMs?: number;
  errorMessage?: string;
}
export interface PreferenceState {
  collapsed: boolean;
  tableSize: 'mini' | 'small' | 'middle' | 'large';
  layoutMode: 'side' | 'doubleSide' | 'mix';
}
export interface OptionItem {
  label: string;
  value: string | number;
  disabled?: boolean;
  color?: string;
}
