<script setup lang="ts">
import { onMounted } from 'vue';
import { useAppConfigStore } from '@/stores/appConfig';

const appConfig = useAppConfigStore();

onMounted(() => {
  appConfig.load().catch(() => {});
});
</script>
<template>
  <router-view />
</template>
