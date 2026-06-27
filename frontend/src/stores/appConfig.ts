import { defineStore } from 'pinia';
import { getPublicConfig } from '@/api/system/config';

interface AppConfigState {
  systemName: string;
  logoUrl: string;
}

export const useAppConfigStore = defineStore('appConfig', {
  state: (): AppConfigState => ({
    systemName: 'Drip Admin',
    logoUrl: '',
  }),
  actions: {
    async load() {
      const config = await getPublicConfig();
      this.systemName = config.systemName || 'Drip Admin';
      this.logoUrl = config.logoUrl || '';
    },
  },
});
