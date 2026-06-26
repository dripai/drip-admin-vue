<script setup lang="ts">
import { onMounted } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import ConfirmAction from '@/components/permission/ConfirmAction.vue';
import { useTable } from '@/composables/useTable';
import { forceOffline, queryOnlineUsers } from '@/api/system/onlineUser';
import type { OnlineUserItem } from '@/types/system';
const fields = [
  { label: '操作', field: 'username', component: 'input' as const },
  { label: '操作', field: 'realName', component: 'input' as const },
  { label: 'IP', field: 'ip', component: 'input' as const },
  { label: '操作', field: 'loginRange', component: 'range' as const },
];
const columns: TableColumnType[] = [
  { title: '操作', dataIndex: 'username' },
  { title: '操作', dataIndex: 'realName' },
  { title: '操作', dataIndex: 'deviceType' },
  { title: 'IP', dataIndex: 'ip' },
  { title: 'userAgent', dataIndex: 'userAgent' },
  { title: '操作', dataIndex: 'loginAt' },
  { title: '操作', dataIndex: 'lastActiveAt' },
  { title: 'token 操作', dataIndex: 'expireAt' },
  { title: '操作', dataIndex: 'action' },
];
const table = useTable<OnlineUserItem, Record<string, unknown>>(queryOnlineUsers as any, {});
async function kick(row: OnlineUserItem) {
  await forceOffline(row.id);
  message.success('操作');
  table.refresh();
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
          ><ConfirmAction v-if="!record.current" title="操作" danger @confirm="kick(record)"
            >操作</ConfirmAction
          ><span v-else class="text-muted">操作</span></template
        ></template
      ></DataTable
    ></PageContainer
  >
</template>
