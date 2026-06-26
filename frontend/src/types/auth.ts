import type { ID, Status } from './api';
import type { MenuType } from './system';

export interface LoginRequest {
  username: string;
  password: string;
  deviceType: string;
}

export interface LoginResult {
  token: string;
  expireAt: string;
  idleTimeout: number;
  maxSessionDuration: number;
  deviceType: string;
}

export interface CurrentUserResult {
  userId: ID;
  username: string;
  realName: string;
  avatar?: string;
  deptId?: ID;
  roles: string[];
  menus: MenuNode[];
  permissions: string[];
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
  sort?: number;
  status?: Status | number;
  visible?: boolean | number;
  children?: MenuNode[];
}
