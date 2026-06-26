import { defineStore } from 'pinia'
import { loginApi, logoutApi } from '@/api/auth'
import { useDeviceType } from '@/composables/useDeviceType'
import { removeStorage, SESSION_KEY, writeJsonStorage, readJsonStorage } from '@/utils/storage'
import type { LoginResponse, PersistedSession } from '@/types/auth'

interface AuthState extends PersistedSession {
  isReady: boolean
}

const emptySession: PersistedSession = {
  token: '',
  expireAt: '',
  idleTimeout: 0,
  maxSessionDuration: 0,
  deviceType: ''
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    ...emptySession,
    isReady: false
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token)
  },
  actions: {
    restoreSession() {
      const persisted = readJsonStorage<PersistedSession>(SESSION_KEY)

      if (persisted?.token) {
        this.$patch({
          ...persisted,
          isReady: true
        })
        uni.setStorageSync('mobile_admin_token', persisted.token)
        return
      }

      this.isReady = true
    },
    saveSession(session: LoginResponse) {
      this.$patch({
        token: session.token,
        expireAt: session.expireAt,
        idleTimeout: session.idleTimeout,
        maxSessionDuration: session.maxSessionDuration,
        deviceType: session.deviceType
      })
      writeJsonStorage<PersistedSession>(SESSION_KEY, {
        token: session.token,
        expireAt: session.expireAt,
        idleTimeout: session.idleTimeout,
        maxSessionDuration: session.maxSessionDuration,
        deviceType: session.deviceType
      })
      uni.setStorageSync('mobile_admin_token', session.token)
    },
    async login(username: string, password: string) {
      const { getDeviceType } = useDeviceType()
      const deviceType = getDeviceType()
      const session = await loginApi({
        username,
        password,
        deviceType
      })

      this.saveSession(session)
      return session
    },
    async logout() {
      try {
        await logoutApi()
      } finally {
        this.clearSession()
      }
    },
    clearSession() {
      this.$patch({
        ...emptySession,
        isReady: true
      })
      removeStorage(SESSION_KEY)
      uni.removeStorageSync('mobile_admin_token')
    }
  }
})
