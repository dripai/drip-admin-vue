<script setup lang="ts">
import { onMounted } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import PageContainer from '@/components/layout/PageContainer.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import ConfirmAction from '@/components/permission/ConfirmAction.vue';
import { useTable } from '@/composables/useTable';
import { forceOffline, queryOnlineUsers } from '@/api/system/onlineUser';
import type { OnlineUserItem } from '@/types/system';

const fields = [
  { label: '用户名', field: 'username', component: 'input' as const },
  { label: '姓名', field: 'realName', component: 'input' as const },
  { label: 'IP', field: 'ip', component: 'input' as const },
  { label: '登录时间范围', field: 'loginRange', component: 'range' as const },
];
const columns: TableColumnType[] = [
  { title: '用户名', dataIndex: 'username' },
  { title: '姓名', dataIndex: 'realName' },
  { title: '设备类型', dataIndex: 'deviceType' },
  { title: 'IP', dataIndex: 'ip' },
  { title: 'userAgent', dataIndex: 'userAgent', ellipsis: true },
  { title: '登录时间', dataIndex: 'loginAt' },
  { title: '最后活跃时间', dataIndex: 'lastActiveAt' },
  { title: '会话过期时间', dataIndex: 'expireAt' },
  { title: '操作', dataIndex: 'action' },
];
const table = useTable<OnlineUserItem, Record<string, unknown>>(
  queryOnlineUsers as any,
  {},
  { storageKey: 'system.onlineUser.query' },
);

async function kick(row: OnlineUserItem) {
  await forceOffline(row.tokenId);
  message.success('操作成功');
  table.refresh();
}

function formatTime(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '';
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
      table-key="system-onlineUser"
      row-key="tokenId"
      @change="table.handleTableChange"
      @refresh="table.refresh"
      ><template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'userAgent'"
          ><a-tooltip :title="record.userAgent"
            ><span class="user-agent">{{ record.userAgent }}</span></a-tooltip
          ></template
        ><template
          v-else-if="
            ['loginAt', 'lastActiveAt', 'expireAt'].includes(String(column.dataIndex))
          "
          >{{ formatTime(record[column.dataIndex as keyof OnlineUserItem] as string) }}</template
        ><template v-else-if="column.dataIndex === 'action'"
          ><ConfirmAction
            v-if="!record.current"
            title="确认强制该用户下线？"
            danger
            @confirm="kick(record)"
            >强制下线</ConfirmAction
          ><span v-else class="text-muted">当前会话</span></template
        ></template
      ></DataTable
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
