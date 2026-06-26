import { defineStore } from 'pinia';
import type { MenuNode, RoleSummary, UserProfile } from '@/types/system';

interface UserState {
  profile?: UserProfile;
  roles: RoleSummary[];
  menus: MenuNode[];
  permissions: string[];
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({ roles: [], menus: [], permissions: [] }),
  actions: {
    setCurrent(data: UserState) {
      this.profile = data.profile;
      this.roles = data.roles;
      this.menus = data.menus;
      this.permissions = data.permissions;
    },
    clear() {
      this.profile = undefined;
      this.roles = [];
      this.menus = [];
      this.permissions = [];
    },
  },
});
