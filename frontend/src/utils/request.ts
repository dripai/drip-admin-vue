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
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

request.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>;
    if (typeof body?.code === 'number' && body.code !== 0) {
      message.error(body.message || '操作');
      return Promise.reject(new Error(body.message || '操作'));
    }
    return body?.data ?? response.data;
  },
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const status = error.response?.status;
    if (status === 401) {
      const auth = useAuthStore();
      auth.clearSession();
      message.error('操作');
      if (window.location.pathname !== '/login') window.location.href = '/login';
      return Promise.reject(error);
    }
    if (status === 403) {
      message.error('操作');
      return Promise.reject(error);
    }
    const backendMessage = error.response?.data?.message;
    message.error(backendMessage || '操作');
    return Promise.reject(error);
  },
);

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
