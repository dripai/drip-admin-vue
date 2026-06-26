import router from './index';
import { useAuthStore } from '@/stores/auth';
import { usePermissionStore } from '@/stores/permission';

let dynamicAdded = false;

function addDynamicRoutes() {
  if (dynamicAdded) return;
  const permission = usePermissionStore();
  for (const route of permission.routes) router.addRoute('/', route);
  dynamicAdded = true;
}

router.beforeEach(async (to) => {
  const auth = useAuthStore();
  if (to.meta.public) return auth.isLoggedIn && to.path === '/login' ? '/' : true;
  if (!auth.isLoggedIn) return { path: '/login', query: { redirect: to.fullPath } };
  if (!dynamicAdded) {
    await auth.refreshCurrentUser();
    addDynamicRoutes();
    return to.fullPath;
  }
  const code = to.meta.permissionCode as string | undefined;
  if (code && !usePermissionStore().can(code)) return '/403';
  return true;
});
