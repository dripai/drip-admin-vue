import { defineStore } from 'pinia';
import { loadJson, saveJson } from '@/utils/storage';
import type { PreferenceState } from '@/types/system';

const KEY = 'drip-admin-preferences';
const defaults: PreferenceState = { collapsed: false, tableSize: 'middle' };

export const usePreferenceStore = defineStore('preferences', {
  state: (): PreferenceState => loadJson(KEY, defaults),
  actions: {
    setCollapsed(collapsed: boolean) {
      this.collapsed = collapsed;
      saveJson(KEY, this.$state);
    },
    setTableSize(tableSize: PreferenceState['tableSize']) {
      this.tableSize = tableSize;
      saveJson(KEY, this.$state);
    },
  },
});
