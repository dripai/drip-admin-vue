<script setup lang="ts">
import { onMounted, ref } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import { useTable } from '@/composables/useTable';
import { formatDateTime } from '@/utils/date';
import { getLoginLog, queryLoginLogs } from '@/api/system/log';
import type { LoginLogItem } from '@/types/system';
const fields = [
  { label: '用户名', field: 'username', component: 'input' as const },
  {
    label: '登录类型',
    field: 'loginType',
    component: 'select' as const,
    options: [
      { label: '登录', value: 'LOGIN' },
      { label: '登出', value: 'LOGOUT' },
    ],
  },
  {
    label: '状态',
    field: 'status',
    component: 'select' as const,
    options: [
      { label: '成功', value: 'SUCCESS' },
      { label: '失败', value: 'FAIL' },
    ],
  },
  { label: 'IP', field: 'ip', component: 'input' as const },
  { label: '时间范围', field: 'createdRange', component: 'range' as const },
];
const columns: TableColumnType[] = [
  { title: '用户名', dataIndex: 'username' },
  { title: '登录类型', dataIndex: 'loginType', width: 100 },
  { title: '状态', dataIndex: 'status' },
  { title: 'IP', dataIndex: 'ip' },
  { title: '客户端', dataIndex: 'userAgent', ellipsis: true },
  { title: '失败原因', dataIndex: 'failureReason' },
  { title: '登录时间', dataIndex: 'loginAt' },
  { title: '操作', dataIndex: 'action' },
];
const detailOpen = ref(false);
const detail = ref<any>();
const table = useTable<LoginLogItem, Record<string, unknown>>(
  queryLoginLogs as any,
  {},
  { storageKey: 'system.loginLog.query' },
);
async function openDetail(row: LoginLogItem) {
  detail.value = await getLoginLog(row.id);
  detailOpen.value = true;
}
function statusText(status: LoginLogItem['status']) {
  if (status === 'SUCCESS') return '成功';
  return '失败';
}
function statusColor(status: LoginLogItem['status']) {
  if (status === 'SUCCESS') return 'green';
  return 'red';
}
function loginTypeText(loginType: LoginLogItem['loginType']) {
  return loginType === 'LOGOUT' ? '登出' : '登录';
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
        ><template v-if="column.dataIndex === 'status'"
          ><a-tag :color="statusColor(record.status)">{{
            statusText(record.status)
          }}</a-tag></template
        ><template v-else-if="column.dataIndex === 'loginType'">{{
          loginTypeText(record.loginType)
        }}</template
        ><template v-else-if="column.dataIndex === 'userAgent'"
          ><a-tooltip :title="record.userAgent"
            ><span class="user-agent">{{ record.userAgent }}</span></a-tooltip
          ></template
        ><template v-else-if="column.dataIndex === 'loginAt'">{{
          formatDateTime(record.loginAt)
        }}</template
        ><template v-else-if="column.dataIndex === 'action'"
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
<style scoped lang="scss">
.user-agent {
  display: inline-block;
  max-width: 360px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
}
</style>
