import type { Component } from 'vue';

export const pageComponentMap: Record<string, () => Promise<Component>> = {
  'system/user/index': () => import('@/views/system/users/index.vue'),
  'system/role/index': () => import('@/views/system/roles/index.vue'),
  'system/menu/index': () => import('@/views/system/menus/index.vue'),
  'system/dept/index': () => import('@/views/system/depts/index.vue'),
  'system/dict/index': () => import('@/views/system/dicts/index.vue'),
  'system/config/index': () => import('@/views/system/configs/index.vue'),
  'system/loginLog/index': () => import('@/views/system/logs/LoginLogs.vue'),
  'system/operationLog/index': () => import('@/views/system/logs/OperationLogs.vue'),
  'system/onlineUser/index': () => import('@/views/system/online-users/index.vue'),
  'system/job/index': () => import('@/views/system/jobs/index.vue'),
  'system/database/index': () => import('@/views/system/database/index.vue'),
};
