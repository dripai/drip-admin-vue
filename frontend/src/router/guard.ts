import router, { notFoundRoute } from './index';
import { useAuthStore } from '@/stores/auth';
import { usePermissionStore } from '@/stores/permission';
import { useUserStore } from '@/stores/user';

let dynamicAdded = false;
let routeToken = '';
let notFoundAdded = false;

function addDynamicRoutes() {
  if (dynamicAdded) return;
  const permission = usePermissionStore();
  for (const route of permission.routes) router.addRoute('root', route);
  if (!notFoundAdded) {
    router.addRoute(notFoundRoute);
    notFoundAdded = true;
  }
  dynamicAdded = true;
}

router.beforeEach(async (to) => {
  const auth = useAuthStore();
  if (to.meta.public) return auth.isLoggedIn && to.path === '/login' ? '/' : true;
  if (!auth.isLoggedIn) return { path: '/login', query: { redirect: to.fullPath } };
  if (auth.token !== routeToken) {
    dynamicAdded = false;
    routeToken = auth.token;
  }
  if (!dynamicAdded) {
    if (!useUserStore().profile) await auth.refreshCurrentUser();
    addDynamicRoutes();
    return to.fullPath;
  }
  if (!to.matched.length) return '/404';
  const code = to.meta.permissionCode as string | undefined;
  if (code && !usePermissionStore().can(code)) return '/403';
  return true;
});
