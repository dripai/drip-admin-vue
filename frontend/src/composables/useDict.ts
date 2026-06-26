import { computed, onMounted } from 'vue';
import { useDictStore } from '@/stores/dict';
export function useDict(typeCode: string) {
  const store = useDictStore();
  onMounted(() => store.load(typeCode));
  return {
    items: computed(() => store.cache[typeCode] || []),
    refresh: () => store.refresh(typeCode),
  };
}
