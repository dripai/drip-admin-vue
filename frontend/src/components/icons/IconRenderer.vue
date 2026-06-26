<script setup lang="ts">
import { computed } from 'vue';
import { QuestionCircleOutlined } from '@ant-design/icons-vue';
import { iconMap, type IconKey } from './iconMap';
const props = defineProps<{ icon?: string }>();
const component = computed(
  () => (props.icon && iconMap[props.icon as IconKey]) || QuestionCircleOutlined,
);
</script>
<template><component :is="component" /></template>
