import { defineStore } from 'pinia';
import { getPublicConfig } from '@/api/system/config';

interface AppConfigState {
  systemName: string;
  companyFullName: string;
  logoUrl: string;
  watermarkEnabled: boolean;
  silentPrintEnabled: boolean;
}

export const useAppConfigStore = defineStore('appConfig', {
  state: (): AppConfigState => ({
    systemName: 'Drip Admin',
    companyFullName: '',
    logoUrl: '',
    watermarkEnabled: false,
    silentPrintEnabled: false,
  }),
  actions: {
    async load() {
      const config = await getPublicConfig();
      this.systemName = config.systemName || 'Drip Admin';
      this.companyFullName = config.companyFullName || '';
      this.logoUrl = config.logoUrl || '';
      this.watermarkEnabled = config.watermarkEnabled === 'true';
      this.silentPrintEnabled = config.silentPrintEnabled === 'true';
    },
  },
});
