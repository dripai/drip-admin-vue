<script setup lang="ts">
import { computed, ref } from 'vue';
import { Select } from 'ant-design-vue';
import IconRenderer from './IconRenderer.vue';
import { iconOptions } from './iconMap';
const props = defineProps<{ value?: string }>();
const emit = defineEmits<{ 'update:value': [value?: string] }>();
const keyword = ref('');
const filtered = computed(() =>
  iconOptions.filter((item) => item.label.toLowerCase().includes(keyword.value.toLowerCase())),
);
</script>
<template>
  <Select
    allow-clear
    show-search
    :value="props.value"
    :options="filtered"
    placeholder="操作"
    @search="keyword = $event"
    @change="emit('update:value', $event as string | undefined)"
  >
    <template #option="option"
      ><IconRenderer :icon="option.value" /> <span>{{ option.label }}</span></template
    >
  </Select>
</template>
