import type { MenuNode, RoleSummary, UserProfile } from './system';

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
  user: UserProfile;
  roles: RoleSummary[];
  menus: MenuNode[];
  permissions: string[];
}
