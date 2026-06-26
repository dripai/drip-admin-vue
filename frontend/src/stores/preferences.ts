import { defineStore } from 'pinia';
import { loadJson, saveJson } from '@/utils/storage';
import type { PreferenceState } from '@/types/system';

const KEY = 'drip-admin-preferences';
const defaults: PreferenceState = { collapsed: false, tableSize: 'middle', layoutMode: 'side' };

export const usePreferenceStore = defineStore('preferences', {
  state: (): PreferenceState => ({ ...defaults, ...loadJson<Partial<PreferenceState>>(KEY, {}) }),
  actions: {
    setCollapsed(collapsed: boolean) {
      this.collapsed = collapsed;
      saveJson(KEY, this.$state);
    },
    setTableSize(tableSize: PreferenceState['tableSize']) {
      this.tableSize = tableSize;
      saveJson(KEY, this.$state);
    },
    setLayoutMode(layoutMode: PreferenceState['layoutMode']) {
      this.layoutMode = layoutMode;
      saveJson(KEY, this.$state);
    },
  },
});
