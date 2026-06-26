<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { SettingOutlined } from '@ant-design/icons-vue';
import type { TableColumnType, TablePaginationConfig } from 'ant-design-vue';
import { loadJson, saveJson } from '@/utils/storage';

const props = defineProps<{
  columns: TableColumnType[];
  dataSource: unknown[];
  loading?: boolean;
  pagination: TablePaginationConfig;
  tableKey?: string;
}>();
const emit = defineEmits<{ change: [pagination: TablePaginationConfig] }>();
const allColumnKeys = computed(() =>
  props.columns.map((column) => String(column.dataIndex || column.key)),
);
const storageKey = computed(() => (props.tableKey ? `table-columns:${props.tableKey}` : ''));
const checkedKeys = ref<string[]>([]);

watch(
  () => props.columns,
  () => {
    checkedKeys.value = storageKey.value
      ? loadJson(storageKey.value, allColumnKeys.value)
      : allColumnKeys.value;
  },
  { immediate: true },
);

const visibleColumns = computed(() =>
  props.columns.filter((column) =>
    checkedKeys.value.includes(String(column.dataIndex || column.key)),
  ),
);
const columnOptions = computed(() =>
  props.columns.map((column) => ({
    label: String(column.title || column.dataIndex || column.key),
    value: String(column.dataIndex || column.key),
  })),
);
function updateColumns(values: Array<string | number | boolean>) {
  checkedKeys.value = values.map(String);
  if (storageKey.value) saveJson(storageKey.value, checkedKeys.value);
}
</script>
<template>
  <div v-if="tableKey" class="table-tools">
    <a-dropdown trigger="click">
      <a-button><SettingOutlined />列设置</a-button>
      <template #overlay>
        <div class="column-panel">
          <a-checkbox-group :value="checkedKeys" :options="columnOptions" @change="updateColumns" />
        </div>
      </template>
    </a-dropdown>
  </div>
  <a-table
    row-key="id"
    :columns="visibleColumns"
    :data-source="dataSource"
    :loading="loading"
    :pagination="pagination"
    size="middle"
    @change="(p: any) => emit('change', p)"
  >
    <template #bodyCell="scope"><slot name="bodyCell" v-bind="scope" /></template>
    <template #emptyText><a-empty description="暂无数据" /></template>
  </a-table>
</template>
<style scoped lang="scss">
.table-tools {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}
.column-panel {
  max-width: 220px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 12px rgb(0 0 0 / 8%);
}
.column-panel :deep(.ant-checkbox-group) {
  display: grid;
  gap: 8px;
}
</style>
