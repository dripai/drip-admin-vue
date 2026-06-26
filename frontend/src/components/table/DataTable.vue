<script setup lang="ts">
import type { TableColumnType } from 'ant-design-vue';
import type { TablePaginationConfig } from 'ant-design-vue';
defineProps<{
  columns: TableColumnType[];
  dataSource: unknown[];
  loading?: boolean;
  pagination: TablePaginationConfig;
}>();
const emit = defineEmits<{ change: [pagination: TablePaginationConfig] }>();
</script>
<template>
  <a-table
    row-key="id"
    :columns="columns"
    :data-source="dataSource"
    :loading="loading"
    :pagination="pagination"
    size="middle"
    @change="(p: any) => emit('change', p)"
  >
    <template #bodyCell="scope"><slot name="bodyCell" v-bind="scope" /></template>
    <template #emptyText><a-empty description="操作" /></template>
  </a-table>
</template>
