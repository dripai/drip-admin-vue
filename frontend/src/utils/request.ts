import axios, { AxiosError, type AxiosRequestConfig } from 'axios';
import { message } from 'ant-design-vue';
import type { ApiResponse } from '@/types/api';
import { useAuthStore } from '@/stores/auth';

export const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

request.interceptors.request.use((config) => {
  const auth = useAuthStore();
  if (auth.token) {
    config.headers.Authorization = auth.token;
  }
  return config;
});

request.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>;
    if (typeof body?.code === 'number' && body.code !== 0) {
      message.error(body.message || '请求处理失败');
      return Promise.reject(new Error(body.message || '业务错误'));
    }
    return normalizeResponseData(body?.data ?? response.data) as any;
  },
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const status = error.response?.status;
    const requestUrl = error.config?.url || '';
    const backendMessage = error.response?.data?.message;
    if (status === 401 && requestUrl === '/system/login') {
      message.error(backendMessage || '用户名或密码错误');
      return Promise.reject(error);
    }
    if (status === 401) {
      const auth = useAuthStore();
      auth.clearSession();
      message.error('登录状态已失效，请重新登录');
      if (window.location.pathname !== '/login') window.location.href = '/login';
      return Promise.reject(error);
    }
    if (status === 403) {
      message.error('没有权限执行该操作');
      return Promise.reject(error);
    }
    message.error(backendMessage || '网络或服务异常，请稍后重试');
    return Promise.reject(error);
  },
);

function normalizeResponseData(value: unknown): unknown {
  if (Array.isArray(value)) return value.map(normalizeResponseData);
  if (!value || typeof value !== 'object' || value instanceof Blob) return value;
  const source = value as Record<string, unknown>;
  const normalized: Record<string, unknown> = {};
  for (const [key, item] of Object.entries(source)) {
    normalized[key] =
      key === 'status'
        ? normalizeStatus(item)
        : key === 'visible'
          ? normalizeBoolean(item)
          : normalizeResponseData(item);
  }
  return normalized;
}

function normalizeStatus(value: unknown) {
  if (value === 1) return 'ENABLED';
  if (value === 0) return 'DISABLED';
  return value;
}

function normalizeBoolean(value: unknown) {
  if (value === 1) return true;
  if (value === 0) return false;
  return value;
}

export function createRequestController() {
  return new AbortController();
}

export function get<T>(url: string, config?: AxiosRequestConfig) {
  return request.get<unknown, T>(url, config);
}
export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.post<unknown, T>(url, data, config);
}
export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.put<unknown, T>(url, data, config);
}
export function del<T>(url: string, config?: AxiosRequestConfig) {
  return request.delete<unknown, T>(url, config);
}
export function download(url: string, params?: unknown) {
  return request.get<unknown, Blob>(url, { params, responseType: 'blob' });
}
