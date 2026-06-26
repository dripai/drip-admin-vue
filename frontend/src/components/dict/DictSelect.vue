<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useDictStore } from '@/stores/dict';
const props = defineProps<{ typeCode: string; value?: string; placeholder?: string }>();
const emit = defineEmits<{ 'update:value': [value?: string] }>();
const dict = useDictStore();
onMounted(() => dict.load(props.typeCode));
const options = computed(() =>
  (dict.cache[props.typeCode] || []).map((item) => ({
    label: item.label,
    value: item.value,
    disabled: item.status === 'DISABLED',
  })),
);
</script>
<template>
  <a-select
    allow-clear
    :value="value"
    :options="options"
    :placeholder="placeholder"
    @change="emit('update:value', $event as string | undefined)"
  />
</template>
