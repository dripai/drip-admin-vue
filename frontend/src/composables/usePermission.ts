import { computed } from 'vue';
import { usePermissionStore } from '@/stores/permission';
export function usePermission(code?: string) {
  const store = usePermissionStore();
  return { can: computed(() => store.can(code)), hasPermission: store.can };
}
