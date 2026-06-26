import { defineConfig, loadEnv } from 'vite'
import uniPlugin from '@dcloudio/vite-plugin-uni'

const uni = typeof uniPlugin === 'function' ? uniPlugin : (uniPlugin as { default: typeof uniPlugin }).default

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [uni()],
    server: {
      host: '0.0.0.0',
      proxy: env.VITE_API_PROXY_TARGET
        ? {
            '/api': {
              target: env.VITE_API_PROXY_TARGET,
              changeOrigin: true
            }
          }
        : undefined
    }
  }
})
