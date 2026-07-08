import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import AppLayout from '@/layouts/AppLayout.vue';

export const staticRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/',
    name: 'root',
    component: AppLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '控制台' },
      },
      {
        path: '403',
        name: '403',
        component: () => import('@/views/error/Forbidden.vue'),
        meta: { title: '无权限' },
      },
      {
        path: '404',
        name: '404',
        component: () => import('@/views/error/NotFound.vue'),
        meta: { title: '页面不存在' },
      },
      {
        path: 'system/print-template/create',
        name: 'system-print-template-create',
        component: () => import('@/views/system/print-templates/form.vue'),
        meta: { title: '新增打印模板', permissionCode: 'system:printTemplate:create' },
      },
      {
        path: 'system/print-template/:id/design',
        name: 'system-print-template-design',
        component: () => import('@/views/system/print-templates/form.vue'),
        meta: { title: '设计打印模板', permissionCode: 'system:printTemplate:update' },
      },
      {
        path: 'system/print-template/:id',
        name: 'system-print-template-detail',
        component: () => import('@/views/system/print-templates/form.vue'),
        meta: { title: '编辑打印模板', permissionCode: 'system:printTemplate:update' },
      },
    ],
  },
];

export const notFoundRoute: RouteRecordRaw = { path: '/:pathMatch(.*)*', redirect: '/404' };

const router = createRouter({ history: createWebHistory(), routes: staticRoutes });
export default router;
