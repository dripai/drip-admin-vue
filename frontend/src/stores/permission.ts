import { defineStore } from 'pinia';
import type { RouteRecordRaw } from 'vue-router';
import type { MenuNode } from '@/types/system';
import { hasPermission } from '@/utils/permissions';
import { pageComponentMap } from '@/router/dynamicRoutes';

function toRoute(menu: MenuNode): RouteRecordRaw | null {
  if (
    menu.type !== 'MENU' ||
    !menu.path ||
    menu.hidden ||
    menu.visible === false ||
    menu.status !== 'ENABLED'
  )
    return null;
  const component = menu.component ? pageComponentMap[menu.component] : undefined;
  if (!component) return null;
  return {
    path: menu.path,
    name: String(menu.id),
    component,
    meta: { title: menu.name, permissionCode: menu.permissionCode },
  };
}

export const usePermissionStore = defineStore('permission', {
  state: () => ({ routes: [] as RouteRecordRaw[], permissions: [] as string[] }),
  actions: {
    buildRoutes(menus: MenuNode[], permissions: string[]) {
      this.permissions = permissions;
      const routes: RouteRecordRaw[] = [];
      const walk = (items: MenuNode[]) => {
        items
          .sort((a, b) => a.sort - b.sort)
          .forEach((item) => {
            const route = toRoute(item);
            if (route && hasPermission(permissions, item.permissionCode)) routes.push(route);
            if (item.children?.length) walk(item.children);
          });
      };
      walk(menus);
      this.routes = routes;
    },
    can(code?: string) {
      return hasPermission(this.permissions, code);
    },
    clear() {
      this.routes = [];
      this.permissions = [];
    },
  },
});
