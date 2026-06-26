import { defineStore } from 'pinia';
import { loginApi, logoutApi, getCurrentUserApi } from '@/api/auth';
import { detectDeviceType } from '@/utils/device';
import { loadJson, removeStorage, saveJson } from '@/utils/storage';
import { useUserStore } from './user';
import { usePermissionStore } from './permission';

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
      const result = await getCurrentUserApi();
      const userStore = useUserStore();
      const permissionStore = usePermissionStore();
      userStore.setCurrent({
        profile: result.user,
        roles: result.roles,
        menus: result.menus,
        permissions: result.permissions,
      });
      permissionStore.buildRoutes(result.menus, result.permissions);
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
      removeStorage(AUTH_KEY);
      useUserStore().clear();
      usePermissionStore().clear();
    },
  },
});
