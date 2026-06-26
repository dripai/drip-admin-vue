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
  { label: '操作', field: 'backupName', component: 'input' as const },
  {
    label: '操作',
    field: 'status',
    component: 'select' as const,
    options: [
      { label: '操作', value: 'SUCCESS' },
      { label: '操作', value: 'FAILED' },
      { label: '操作', value: 'RUNNING' },
    ],
  },
  { label: '操作', field: 'createdRange', component: 'range' as const },
];
const columns: TableColumnType[] = [
  { title: '操作', dataIndex: 'backupName' },
  { title: '操作', dataIndex: 'fileSize' },
  { title: '操作', dataIndex: 'status' },
  { title: '操作', dataIndex: 'createdBy' },
  { title: '操作', dataIndex: 'createdAt' },
  { title: '操作', dataIndex: 'remark' },
  { title: '操作', dataIndex: 'action' },
];
const table = useTable<DatabaseBackupItem, Record<string, unknown>>(
  queryDatabaseBackups as any,
  {},
);
const open = ref(false);
const submitting = ref(false);
const form = reactive({ backupName: '', remark: '' });
async function create() {
  submitting.value = true;
  try {
    await createDatabaseBackup(form);
    message.success('操作');
    open.value = false;
    table.refresh();
  } finally {
    submitting.value = false;
  }
}
async function download(row: DatabaseBackupItem) {
  await downloadDatabaseBackup(row.id);
  message.success('操作');
}
async function restore(row: DatabaseBackupItem) {
  await restoreDatabaseBackup(row.id);
  message.success('操作');
}
async function remove(row: DatabaseBackupItem) {
  await deleteDatabaseBackup(row.id);
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
      @reset="table.reset" />
    <div class="page-actions">
      <a-button type="primary" @click="open = true">操作</a-button
      ><a-button @click="table.refresh">操作</a-button>
    </div>
    <DataTable
      :columns="columns"
      :data-source="table.dataSource.value"
      :loading="table.loading.value"
      :pagination="table.pagination.value"
      @change="table.handleTableChange"
      ><template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'action'"
          ><a-space
            ><ConfirmAction title="操作" @confirm="download(record)">操作</ConfirmAction
            ><ConfirmAction title="操作" danger @confirm="restore(record)">操作</ConfirmAction
            ><ConfirmAction title="操作" danger @confirm="remove(record)"
              >操作</ConfirmAction
            ></a-space
          ></template
        ></template
      ></DataTable
    ><FormModal v-model:open="open" title="操作" :submitting="submitting" @submit="create"
      ><a-form layout="vertical" :model="form"
        ><a-form-item label="操作" required><a-input v-model:value="form.backupName" /></a-form-item
        ><a-form-item label="操作"
          ><a-textarea v-model:value="form.remark" /></a-form-item></a-form></FormModal
  ></PageContainer>
</template>
