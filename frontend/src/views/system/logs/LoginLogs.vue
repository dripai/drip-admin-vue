<script setup lang="ts">
import { onMounted, ref } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import { useTable } from '@/composables/useTable';
import { getLoginLog, queryLoginLogs } from '@/api/system/log';
const fields = [
  { label: '用户名', field: 'username', component: 'input' as const },
  {
    label: '状态',
    field: 'status',
    component: 'select' as const,
    options: [
      { label: '成功', value: 'SUCCESS' },
      { label: '失败', value: 'FAILED' },
      { label: '登出', value: 'LOGOUT' },
    ],
  },
  { label: 'IP', field: 'ip', component: 'input' as const },
  { label: '时间范围', field: 'createdRange', component: 'range' as const },
];
const columns: TableColumnType[] = [
  { title: '用户名', dataIndex: 'username' },
  { title: '状态', dataIndex: 'status' },
  { title: 'IP', dataIndex: 'ip' },
  { title: 'userAgent', dataIndex: 'userAgent' },
  { title: '失败原因', dataIndex: 'failureReason' },
  { title: '创建时间', dataIndex: 'createdAt' },
  { title: '操作', dataIndex: 'action' },
];
const detailOpen = ref(false);
const detail = ref<any>();
const table = useTable<any, Record<string, unknown>>(
  queryLoginLogs as any,
  {},
  { storageKey: 'system.loginLog.query' },
);
async function openDetail(row: any) {
  detail.value = await getLoginLog(row.id);
  detailOpen.value = true;
}
onMounted(table.refresh);
</script>
<template>
  <PageContainer
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
      table-key="system-loginLog"
      @change="table.handleTableChange"
      @refresh="table.refresh"
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
