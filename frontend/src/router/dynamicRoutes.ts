import type { Component } from 'vue';

export const pageComponentMap: Record<string, () => Promise<Component>> = {
  'system/users/index': () => import('@/views/system/users/index.vue'),
  'system/roles/index': () => import('@/views/system/roles/index.vue'),
  'system/menus/index': () => import('@/views/system/menus/index.vue'),
  'system/depts/index': () => import('@/views/system/depts/index.vue'),
  'system/dicts/index': () => import('@/views/system/dicts/index.vue'),
  'system/configs/index': () => import('@/views/system/configs/index.vue'),
  'system/login-logs/index': () => import('@/views/system/logs/LoginLogs.vue'),
  'system/operation-logs/index': () => import('@/views/system/logs/OperationLogs.vue'),
  'system/online-users/index': () => import('@/views/system/online-users/index.vue'),
  'system/jobs/index': () => import('@/views/system/jobs/index.vue'),
  'system/database/index': () => import('@/views/system/database/index.vue'),
};
