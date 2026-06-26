import { createApp } from 'vue';
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import './router/guard';
import './styles/index.scss';

createApp(App).use(createPinia()).use(router).use(Antd).mount('#app');
