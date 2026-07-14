<script setup lang="ts">
import { computed, defineAsyncComponent, type Component } from 'vue';
import QuestionCircleOutlined from '@ant-design/icons-vue/es/icons/QuestionCircleOutlined.js';
import { iconMap, type IconKey } from './iconMap';
import { antIconLoaders, isAntIconName } from './iconCatalog';
const props = defineProps<{ icon?: string }>();
const asyncIconCache = new Map<string, Component>();

function getAsyncIcon(icon: string) {
  const cached = asyncIconCache.get(icon);
  if (cached) return cached;
  if (!isAntIconName(icon)) return QuestionCircleOutlined;
  const component = defineAsyncComponent({
    loader: () => antIconLoaders[icon]().then((module) => module.default),
    delay: 0,
    errorComponent: QuestionCircleOutlined,
  });
  asyncIconCache.set(icon, component);
  return component;
}

const component = computed(() => {
  if (!props.icon) return QuestionCircleOutlined;
  return iconMap[props.icon as IconKey] || getAsyncIcon(props.icon);
});
</script>
<template><component :is="component" /></template>
