const SESSION_KEY = 'mobile_admin_session'

export function readJsonStorage<T>(key: string): T | null {
  const value = uni.getStorageSync(key)

  if (!value || typeof value !== 'string') {
    return null
  }

  try {
    return JSON.parse(value) as T
  } catch {
    uni.removeStorageSync(key)
    return null
  }
}

export function writeJsonStorage<T>(key: string, value: T): void {
  uni.setStorageSync(key, JSON.stringify(value))
}

export function removeStorage(key: string): void {
  uni.removeStorageSync(key)
}

export { SESSION_KEY }
