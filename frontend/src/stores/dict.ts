import { defineStore } from 'pinia';
import { queryDictItems, refreshDictCache } from '@/api/system/dict';
import type { DictItem } from '@/types/system';

export const useDictStore = defineStore('dict', {
  state: () => ({ cache: {} as Record<string, DictItem[]> }),
  actions: {
    async load(typeCode: string) {
      if (!this.cache[typeCode]) this.cache[typeCode] = await queryDictItems(typeCode);
      return this.cache[typeCode];
    },
    async refresh(typeCode?: string) {
      await refreshDictCache(typeCode);
      if (typeCode) delete this.cache[typeCode];
      else this.cache = {};
    },
  },
});
