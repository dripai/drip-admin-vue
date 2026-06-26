import { defineStore } from 'pinia';
import { loginApi, logoutApi, getCurrentUserApi } from '@/api/auth';
import { detectDeviceType } from '@/utils/device';
import { loadJson, removeStorage, saveJson } from '@/utils/storage';
import { useUserStore } from './user';
import { usePermissionStore } from './permission';
import type { MenuNode, RoleSummary, UserProfile } from '@/types/system';
import type { MenuNode as AuthMenuNode } from '@/types/auth';

const AUTH_KEY = 'drip-admin-auth';

interface AuthPersistedState {
  token: string;
  expireAt: string;
  idleTimeout: number;
  maxSessionDuration: number;
  deviceType: string;
}

const emptyAuth: AuthPersistedState = {
  token: '',
  expireAt: '',
  idleTimeout: 0,
  maxSessionDuration: 0,
  deviceType: detectDeviceType(),
};

let currentUserRequest: Promise<void> | null = null;

function normalizeMenus(items: AuthMenuNode[]): MenuNode[] {
  return items.map((item) => ({
    id: item.id,
    parentId: item.parentId,
    name: item.name,
    type: item.type,
    path: item.path,
    component: item.component,
    permissionCode: item.permissionCode,
    icon: item.icon,
    sort: item.sort ?? 0,
    status: item.status ?? 'ENABLED',
    visible: typeof item.visible === 'number' ? item.visible === 1 : item.visible,
    children: item.children?.length ? normalizeMenus(item.children) : [],
  }));
}

function normalizeRoles(roles: string[]): RoleSummary[] {
  return roles.map((roleCode) => ({ id: roleCode, roleCode, roleName: roleCode }));
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthPersistedState => loadJson(AUTH_KEY, emptyAuth),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
  },
  actions: {
    persist() {
      saveJson(AUTH_KEY, {
        token: this.token,
        expireAt: this.expireAt,
        idleTimeout: this.idleTimeout,
        maxSessionDuration: this.maxSessionDuration,
        deviceType: this.deviceType,
      });
    },
    async login(username: string, password: string) {
      const deviceType = detectDeviceType();
      const result = await loginApi({ username, password, deviceType });
      this.token = result.token;
      this.expireAt = result.expireAt;
      this.idleTimeout = result.idleTimeout;
      this.maxSessionDuration = result.maxSessionDuration;
      this.deviceType = result.deviceType || deviceType;
      this.persist();
      await this.refreshCurrentUser();
    },
    async refreshCurrentUser() {
      if (currentUserRequest) return currentUserRequest;
      currentUserRequest = (async () => {
        const result = await getCurrentUserApi();
        const userStore = useUserStore();
        const permissionStore = usePermissionStore();
        const profile: UserProfile = {
          id: result.userId,
          username: result.username,
          realName: result.realName,
          avatar: result.avatar,
          dept: result.deptId ? { id: result.deptId, deptName: '' } : undefined,
        };
        const roles = normalizeRoles(result.roles);
        const menus = normalizeMenus(result.menus);
        userStore.setCurrent({
          profile,
          roles,
          menus,
          permissions: result.permissions,
        });
        permissionStore.buildRoutes(menus, result.permissions);
      })();
      try {
        await currentUserRequest;
      } finally {
        currentUserRequest = null;
      }
    },
    async logout() {
      try {
        await logoutApi();
      } finally {
        this.clearSession();
      }
    },
    clearSession() {
      this.token = '';
      this.expireAt = '';
      this.idleTimeout = 0;
      this.maxSessionDuration = 0;
      this.deviceType = detectDeviceType();
      currentUserRequest = null;
      removeStorage(AUTH_KEY);
      useUserStore().clear();
      usePermissionStore().clear();
    },
  },
});
