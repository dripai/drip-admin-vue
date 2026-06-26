<script setup lang="ts">
import { onMounted, ref } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import { useTable } from '@/composables/useTable';
import { getOperationLog, queryOperationLogs } from '@/api/system/log';
const fields = [
  { label: '操作人', field: 'operator', component: 'input' as const },
  { label: '模块', field: 'module', component: 'input' as const },
  { label: '操作类型', field: 'action', component: 'input' as const },
  {
    label: '状态',
    field: 'status',
    component: 'select' as const,
    options: [
      { label: '成功', value: 'SUCCESS' },
      { label: '失败', value: 'FAILED' },
    ],
  },
  { label: '时间范围', field: 'createdRange', component: 'range' as const },
];
const columns: TableColumnType[] = [
  { title: '操作人', dataIndex: 'operator' },
  { title: '模块', dataIndex: 'module' },
  { title: '操作', dataIndex: 'action' },
  { title: '请求方法', dataIndex: 'method' },
  { title: '请求路径', dataIndex: 'path' },
  { title: '状态', dataIndex: 'status' },
  { title: '耗时', dataIndex: 'duration' },
  { title: '创建时间', dataIndex: 'createdAt' },
  { title: '操作', dataIndex: 'action' },
];
const detailOpen = ref(false);
const detail = ref<any>();
const table = useTable<any, Record<string, unknown>>(
  queryOperationLogs as any,
  {},
  { storageKey: 'system.operation-logs.query' },
);
async function openDetail(row: any) {
  detail.value = await getOperationLog(row.id);
  detailOpen.value = true;
}
onMounted(table.refresh);
</script>
<template>
  <PageContainer title="操作日志"
    ><SearchForm
      :model="table.query"
      :fields="fields"
      :loading="table.loading.value"
      @search="table.search"
      @reset="table.reset"
    /><DataTable
      :columns="columns"
      :data-source="table.dataSource.value"
      :loading="table.loading.value"
      :pagination="table.pagination.value"
      table-key="system-operation-logs"
      @change="table.handleTableChange"
      ><template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'action'"
          ><a-button type="link" @click="openDetail(record)">详情</a-button></template
        ></template
      ></DataTable
    ><a-modal v-model:open="detailOpen" title="详情" :footer="null" width="720"
      ><a-descriptions bordered :column="1"
        ><a-descriptions-item v-for="(value, key) in detail" :key="key" :label="key">{{
          value
        }}</a-descriptions-item></a-descriptions
      ></a-modal
    ></PageContainer
  >
</template>
