import { useAuthStore } from '@/stores/auth'

export function useAuthGuard() {
  const ensureAuthenticated = (): boolean => {
    const authStore = useAuthStore()

    if (!authStore.isReady) {
      authStore.restoreSession()
    }

    if (!authStore.isLoggedIn) {
      uni.reLaunch({ url: '/pages/login/index' })
      return false
    }

    return true
  }

  return {
    ensureAuthenticated
  }
}
