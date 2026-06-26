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
    ],
  },
];

export const notFoundRoute: RouteRecordRaw = { path: '/:pathMatch(.*)*', redirect: '/404' };

const router = createRouter({ history: createWebHistory(), routes: staticRoutes });
export default router;
