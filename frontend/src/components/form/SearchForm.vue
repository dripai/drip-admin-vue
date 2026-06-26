<script setup lang="ts">
import { computed, ref } from 'vue';
export interface SearchField {
  label: string;
  field: string;
  component: 'input' | 'select' | 'range';
  placeholder?: string;
  options?: { label: string; value: string | number }[];
}
const props = defineProps<{
  model: Record<string, unknown>;
  fields: SearchField[];
  loading?: boolean;
  collapsible?: boolean;
  defaultVisibleCount?: number;
}>();
const emit = defineEmits<{ search: []; reset: [] }>();
const expanded = ref(false);
const visibleFields = computed(() => {
  if (!props.collapsible && props.collapsible !== undefined) return props.fields;
  const count = props.defaultVisibleCount || 4;
  return expanded.value ? props.fields : props.fields.slice(0, count);
});
const canToggle = computed(() => props.fields.length > (props.defaultVisibleCount || 4));
</script>
<template>
  <a-form class="search-form" layout="inline" :model="props.model">
    <a-form-item v-for="field in visibleFields" :key="field.field" :label="field.label">
      <a-input
        v-if="field.component === 'input'"
        v-model:value="props.model[field.field]"
        allow-clear
        :placeholder="field.placeholder"
      />
      <a-select
        v-else-if="field.component === 'select'"
        v-model:value="props.model[field.field]"
        allow-clear
        :options="field.options"
        :placeholder="field.placeholder"
        style="width: 168px"
      />
      <a-range-picker v-else v-model:value="props.model[field.field]" />
    </a-form-item>
    <a-form-item>
      <a-space>
        <a-button type="primary" :loading="loading" @click="emit('search')">查询</a-button>
        <a-button @click="emit('reset')">重置</a-button>
        <a-button v-if="canToggle" type="link" @click="expanded = !expanded">
          {{ expanded ? '收起' : '展开' }}
        </a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>
<style scoped lang="scss">
.search-form {
  margin-bottom: 12px;
  row-gap: 8px;
}
</style>
