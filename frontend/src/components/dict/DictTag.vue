<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useDictStore } from '@/stores/dict';
const props = defineProps<{ typeCode: string; value?: string }>();
const dict = useDictStore();
onMounted(() => dict.load(props.typeCode));
const item = computed(() =>
  dict.cache[props.typeCode]?.find((entry) => entry.value === props.value),
);
</script>
<template>
  <a-tag :color="item?.color">{{ item?.label || value || '-' }}</a-tag>
</template>
