import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import './styles/index.scss'

export function createApp() {
  const app = createSSRApp(App)

  app.use(createPinia())
  app.use(Vant)

  return {
    app
  }
}
