import axios, { AxiosError, type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { showToast } from 'vant'
import type { ApiResponse, RequestErrorPayload } from '@/types/api'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const service: AxiosInstance = axios.create({
  baseURL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

service.interceptors.request.use((config) => {
  const token = uni.getStorageSync('mobile_admin_token')

  if (token && typeof token === 'string') {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

service.interceptors.response.use(
  (response) => {
    const payload = response.data as ApiResponse<unknown>

    if (payload.code === 0 || payload.code === 200) {
      return payload.data as never
    }

    const message = payload.message || '请求处理失败'
    showToast(message)
    return Promise.reject({ code: payload.code, message } satisfies RequestErrorPayload)
  },
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络请求失败'

    if (status === 401) {
      const { useAuthStore } = await import('@/stores/auth')
      const { useUserStore } = await import('@/stores/user')
      useAuthStore().clearSession()
      useUserStore().clearUser()
      uni.reLaunch({ url: '/pages/login/index' })
      showToast('登录状态已失效')
      return Promise.reject({ code: status, message: '登录状态已失效' } satisfies RequestErrorPayload)
    }

    if (status === 403) {
      showToast('无权限访问')
      return Promise.reject({ code: status, message: '无权限访问' } satisfies RequestErrorPayload)
    }

    showToast(message || '系统错误，请稍后重试')
    return Promise.reject({ code: status, message } satisfies RequestErrorPayload)
  }
)

export const request = {
  get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config) as Promise<T>
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config) as Promise<T>
  }
}
