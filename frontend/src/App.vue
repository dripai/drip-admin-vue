<script setup lang="ts">
import { onMounted } from 'vue';
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import { useAppConfigStore } from '@/stores/appConfig';

const appConfig = useAppConfigStore();
dayjs.locale('zh-cn');

onMounted(() => {
  appConfig.load().catch(() => {});
});
</script>
<template>
  <a-config-provider :locale="zhCN">
    <router-view />
  </a-config-provider>
</template>
