function getBrowserName(userAgent: string): string {
  const source = userAgent.toLowerCase()

  if (source.includes('micromessenger')) return 'wechat-webview'
  if (source.includes('android')) return 'android-webview'
  if (source.includes('iphone') || source.includes('ipad')) return 'ios-webview'
  if (source.includes('windows')) return 'windows-h5'
  return 'h5'
}

export function useDeviceType() {
  const getDeviceType = (): string => {
    const userAgent = typeof navigator === 'undefined' ? '' : navigator.userAgent
    const browserName = getBrowserName(userAgent)
    return `mobile-${browserName}`
  }

  return {
    getDeviceType
  }
}
