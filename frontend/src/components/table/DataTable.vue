<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { ColumnHeightOutlined, ReloadOutlined, SettingOutlined } from '@ant-design/icons-vue';
import type { TableColumnType, TablePaginationConfig } from 'ant-design-vue';
import { loadJson, saveJson } from '@/utils/storage';
import { usePreferenceStore } from '@/stores/preferences';
import type { PreferenceState } from '@/types/system';

const props = defineProps<{
  columns: TableColumnType[];
  dataSource: unknown[];
  loading?: boolean;
  pagination?: TablePaginationConfig | false;
  tableKey?: string;
  rowKey?: string;
  defaultExpandAllRows?: boolean;
  expandedRowKeys?: Array<string | number>;
  tableLayout?: 'auto' | 'fixed';
}>();
const emit = defineEmits<{
  change: [pagination: TablePaginationConfig];
  refresh: [];
  expandedRowsChange: [keys: Array<string | number>];
}>();
const preferences = usePreferenceStore();
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
  props.columns
    .filter((column) => checkedKeys.value.includes(String(column.dataIndex || column.key)))
    .map((column) =>
      column.dataIndex === 'action'
        ? { ...column, className: ['table-action-cell', column.className].filter(Boolean).join(' ') }
        : column,
    ),
);
const columnOptions = computed(() =>
  props.columns.map((column) => ({
    label: String(column.title || column.dataIndex || column.key),
    value: String(column.dataIndex || column.key),
  })),
);
const densityItems = [
  { key: 'mini', label: '迷你' },
  { key: 'small', label: '紧凑' },
  { key: 'middle', label: '默认' },
  { key: 'large', label: '宽松' },
];
const tableSize = computed(() => (preferences.tableSize === 'mini' ? 'small' : preferences.tableSize));
function updateColumns(values: Array<string | number | boolean>) {
  checkedKeys.value = values.map(String);
  if (storageKey.value) saveJson(storageKey.value, checkedKeys.value);
}
function updateDensity(info: { key: string }) {
  preferences.setTableSize(info.key as PreferenceState['tableSize']);
}
</script>
<template>
  <div v-if="tableKey" class="table-tools">
    <div class="table-tools-left"><slot name="toolbarLeft" /></div>
    <a-space :size="4">
      <a-tooltip title="刷新">
        <a-button
          class="tool-button"
          :aria-label="'刷新'"
          :loading="loading"
          @click="emit('refresh')"
        >
          <ReloadOutlined />
        </a-button>
      </a-tooltip>
      <a-tooltip title="密度">
        <a-dropdown trigger="click">
          <a-button class="tool-button" :aria-label="'密度'"><ColumnHeightOutlined /></a-button>
          <template #overlay>
            <a-menu
              :selected-keys="[preferences.tableSize]"
              :items="densityItems"
              @click="updateDensity"
            />
          </template>
        </a-dropdown>
      </a-tooltip>
      <a-tooltip title="列设置">
        <a-dropdown trigger="click">
          <a-button class="tool-button" :aria-label="'列设置'"><SettingOutlined /></a-button>
          <template #overlay>
            <div class="column-panel">
              <a-checkbox-group
                :value="checkedKeys"
                :options="columnOptions"
                @change="updateColumns"
              />
            </div>
          </template>
        </a-dropdown>
      </a-tooltip>
    </a-space>
  </div>
  <a-table
    class="data-table"
    :class="{ 'data-table-mini': preferences.tableSize === 'mini' }"
    :row-key="rowKey || 'id'"
    :columns="visibleColumns"
    :data-source="dataSource"
    :loading="loading"
    :pagination="pagination"
    :size="tableSize"
    :default-expand-all-rows="defaultExpandAllRows"
    :expanded-row-keys="expandedRowKeys"
    :table-layout="tableLayout"
    @change="(p: any) => emit('change', p)"
    @expandedRowsChange="(keys: any) => emit('expandedRowsChange', keys)"
  >
    <template #bodyCell="scope"><slot name="bodyCell" v-bind="scope" /></template>
    <template #emptyText><a-empty description="暂无数据" /></template>
  </a-table>
</template>
<style scoped lang="scss">
.table-tools {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}
.table-tools-left {
  min-width: 0;
}
.tool-button {
  width: 32px;
  padding: 0;
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
.data-table :deep(.table-action-cell .ant-space) {
  gap: 2px !important;
}
.data-table :deep(.table-action-cell .ant-btn-link) {
  padding-inline: 4px;
}
.data-table :deep(.table-action-cell .ant-space-item) {
  line-height: 1;
}
.data-table-mini :deep(.ant-table-thead > tr > th) {
  padding: 6px 8px;
  line-height: 20px;
}
.data-table-mini :deep(.ant-table-tbody > tr > td) {
  padding: 4px 8px;
  line-height: 20px;
}
.data-table-mini :deep(.ant-btn-link) {
  height: 24px;
  line-height: 22px;
}
.data-table-mini :deep(.ant-tag) {
  line-height: 18px;
}
</style>
