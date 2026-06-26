<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import FormModal from '@/components/form/FormModal.vue';
import ConfirmAction from '@/components/permission/ConfirmAction.vue';
import { useTable } from '@/composables/useTable';
import {
  createDatabaseBackup,
  deleteDatabaseBackup,
  downloadDatabaseBackup,
  queryDatabaseBackups,
  restoreDatabaseBackup,
} from '@/api/system/database';
import type { DatabaseBackupItem } from '@/types/system';
const fields = [
  { label: '备份名称', field: 'backupName', component: 'input' as const },
  {
    label: '状态',
    field: 'status',
    component: 'select' as const,
    options: [
      { label: '成功', value: 'SUCCESS' },
      { label: '失败', value: 'FAILED' },
      { label: '运行中', value: 'RUNNING' },
    ],
  },
  { label: '时间范围', field: 'createdRange', component: 'range' as const },
];
const columns: TableColumnType[] = [
  { title: '备份名称', dataIndex: 'backupName' },
  { title: '文件大小', dataIndex: 'fileSize' },
  { title: '状态', dataIndex: 'status' },
  { title: '创建人', dataIndex: 'createdBy' },
  { title: '创建时间', dataIndex: 'createdAt' },
  { title: '备注', dataIndex: 'remark' },
  { title: '操作', dataIndex: 'action' },
];
const table = useTable<DatabaseBackupItem, Record<string, unknown>>(
  queryDatabaseBackups as any,
  {},
  { storageKey: 'system.database.query' },
);
const open = ref(false);
const submitting = ref(false);
const form = reactive({ backupName: '', remark: '' });
async function create() {
  submitting.value = true;
  try {
    await createDatabaseBackup(form);
    message.success('操作成功');
    open.value = false;
    table.refresh();
  } finally {
    submitting.value = false;
  }
}
async function download(row: DatabaseBackupItem) {
  await downloadDatabaseBackup(row.id);
  message.success('操作成功');
}
async function restore(row: DatabaseBackupItem) {
  await restoreDatabaseBackup(row.id);
  message.success('操作成功');
}
async function remove(row: DatabaseBackupItem) {
  await deleteDatabaseBackup(row.id);
  message.success('操作成功');
  table.refresh();
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
      @reset="table.reset" />
    <DataTable
      :columns="columns"
      :data-source="table.dataSource.value"
      :loading="table.loading.value"
      :pagination="table.pagination.value"
      table-key="system-database"
      @change="table.handleTableChange"
      @refresh="table.refresh"
      ><template #toolbarLeft>
        <a-button type="primary" @click="open = true">创建备份</a-button>
      </template>
      <template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'action'"
          ><a-space
            ><ConfirmAction title="下载" @confirm="download(record)">删除</ConfirmAction
            ><ConfirmAction title="确认恢复该备份？" danger @confirm="restore(record)"
              >删除</ConfirmAction
            ><ConfirmAction title="确认删除该备份？" danger @confirm="remove(record)"
              >删除</ConfirmAction
            ></a-space
          ></template
        ></template
      ></DataTable
    ><FormModal v-model:open="open" title="创建备份" :submitting="submitting" @submit="create"
      ><a-form layout="vertical" :model="form"
        ><a-form-item label="备份名称" required
          ><a-input v-model:value="form.backupName" /></a-form-item
        ><a-form-item label="备注"
          ><a-textarea v-model:value="form.remark" /></a-form-item></a-form></FormModal
  ></PageContainer>
</template>
