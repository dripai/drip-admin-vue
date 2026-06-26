import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import AdminLayout from '@/layouts/AdminLayout.vue';

export const staticRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true, title: '操作' },
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/system/users',
    children: [
      {
        path: '403',
        name: '403',
        component: () => import('@/views/error/Forbidden.vue'),
        meta: { title: '操作' },
      },
      {
        path: '404',
        name: '404',
        component: () => import('@/views/error/NotFound.vue'),
        meta: { title: '操作' },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/404' },
];

const router = createRouter({ history: createWebHistory(), routes: staticRoutes });
export default router;
