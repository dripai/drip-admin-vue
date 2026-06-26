<script setup lang="ts">
import { onMounted, ref } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import { useTable } from '@/composables/useTable';
import { getOperationLog, queryOperationLogs } from '@/api/system/log';
const fields = [
  { label: '操作', field: 'operator', component: 'input' as const },
  { label: '操作', field: 'module', component: 'input' as const },
  { label: '操作', field: 'action', component: 'input' as const },
  {
    label: '操作',
    field: 'status',
    component: 'select' as const,
    options: [
      { label: '操作', value: 'SUCCESS' },
      { label: '操作', value: 'FAILED' },
    ],
  },
  { label: '操作', field: 'createdRange', component: 'range' as const },
];
const columns: TableColumnType[] = [
  { title: '操作', dataIndex: 'operator' },
  { title: '操作', dataIndex: 'module' },
  { title: '操作', dataIndex: 'action' },
  { title: '操作', dataIndex: 'method' },
  { title: '操作', dataIndex: 'path' },
  { title: '操作', dataIndex: 'status' },
  { title: '操作', dataIndex: 'duration' },
  { title: '操作', dataIndex: 'createdAt' },
  { title: '操作', dataIndex: 'action' },
];
const detailOpen = ref(false);
const detail = ref<any>();
const table = useTable<any, Record<string, unknown>>(queryOperationLogs as any, {});
async function openDetail(row: any) {
  detail.value = await getOperationLog(row.id);
  detailOpen.value = true;
}
onMounted(table.refresh);
</script>
<template>
  <PageContainer title="操作"
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
      @change="table.handleTableChange"
      ><template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'action'"
          ><a-button type="link" @click="openDetail(record)">操作</a-button></template
        ></template
      ></DataTable
    ><a-modal v-model:open="detailOpen" title="操作" :footer="null" width="720"
      ><a-descriptions bordered :column="1"
        ><a-descriptions-item v-for="(value, key) in detail" :key="key" :label="key">{{
          value
        }}</a-descriptions-item></a-descriptions
      ></a-modal
    ></PageContainer
  >
</template>
