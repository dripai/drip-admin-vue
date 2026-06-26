<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import FormModal from '@/components/form/FormModal.vue';
import ConfirmAction from '@/components/permission/ConfirmAction.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import { useTable } from '@/composables/useTable';
import {
  createJob,
  deleteJob,
  queryJobRecords,
  queryJobs,
  runJob,
  updateJob,
  updateJobStatus,
} from '@/api/system/job';
import type { JobItem, JobRecordItem } from '@/types/system';
const fields = [
  { label: '操作', field: 'jobName', component: 'input' as const },
  { label: '操作', field: 'jobCode', component: 'input' as const },
  {
    label: '操作',
    field: 'status',
    component: 'select' as const,
    options: [
      { label: '操作', value: 'ENABLED' },
      { label: '操作', value: 'DISABLED' },
    ],
  },
  { label: '操作', field: 'createdRange', component: 'range' as const },
];
const columns: TableColumnType[] = [
  { title: '操作', dataIndex: 'jobName' },
  { title: '操作', dataIndex: 'jobCode' },
  { title: 'cron 操作', dataIndex: 'cron' },
  { title: '操作', dataIndex: 'status' },
  { title: '操作', dataIndex: 'lastRunAt' },
  { title: '操作', dataIndex: 'lastResult' },
  { title: '操作', dataIndex: 'nextRunAt' },
  { title: '操作', dataIndex: 'action', width: 260 },
];
const table = useTable<JobItem, Record<string, unknown>>(queryJobs as any, {});
const open = ref(false);
const recordsOpen = ref(false);
const records = ref<JobRecordItem[]>([]);
const current = ref<JobItem>();
const submitting = ref(false);
const form = reactive<Partial<JobItem>>({ jobName: '', jobCode: '', cron: '', status: 'ENABLED' });
function add() {
  current.value = undefined;
  Object.assign(form, { jobName: '', jobCode: '', cron: '', status: 'ENABLED' });
  open.value = true;
}
function edit(row: JobItem) {
  current.value = row;
  Object.assign(form, row);
  open.value = true;
}
async function submit() {
  if (!form.cron?.trim()) {
    message.error('操作 cron 操作');
    return;
  }
  submitting.value = true;
  try {
    if (current.value) await updateJob(current.value.id, form);
    else await createJob(form);
    message.success('操作');
    open.value = false;
    table.refresh();
  } finally {
    submitting.value = false;
  }
}
async function remove(row: JobItem) {
  await deleteJob(row.id);
  message.success('操作');
  table.refresh();
}
async function status(row: JobItem) {
  await updateJobStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED');
  message.success('操作');
  table.refresh();
}
async function run(row: JobItem) {
  await runJob(row.id);
  message.success('操作');
}
async function showRecords(row: JobItem) {
  const res = await queryJobRecords(row.id, { page: 1, pageSize: 20 });
  records.value = res.list;
  recordsOpen.value = true;
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
      <a-button type="primary" @click="add">操作</a-button
      ><a-button @click="table.refresh">操作</a-button>
    </div>
    <DataTable
      :columns="columns"
      :data-source="table.dataSource.value"
      :loading="table.loading.value"
      :pagination="table.pagination.value"
      @change="table.handleTableChange"
      ><template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'status'"
          ><StatusTag :status="record.status" /></template
        ><template v-else-if="column.dataIndex === 'action'"
          ><a-space
            ><a-button type="link" @click="edit(record)">操作</a-button
            ><ConfirmAction title="操作" @confirm="run(record)">操作</ConfirmAction
            ><a-button type="link" @click="showRecords(record)">操作</a-button
            ><ConfirmAction
              :title="record.status === 'ENABLED' ? '操作' : '操作'"
              @confirm="status(record)"
              >{{ record.status === 'ENABLED' ? '操作' : '操作' }}</ConfirmAction
            ><ConfirmAction title="操作" danger @confirm="remove(record)"
              >操作</ConfirmAction
            ></a-space
          ></template
        ></template
      ></DataTable
    ><FormModal v-model:open="open" title="操作" :submitting="submitting" @submit="submit"
      ><a-form layout="vertical" :model="form"
        ><a-form-item label="操作" required><a-input v-model:value="form.jobName" /></a-form-item
        ><a-form-item label="操作" required><a-input v-model:value="form.jobCode" /></a-form-item
        ><a-form-item label="cron 操作" required
          ><a-input v-model:value="form.cron" /></a-form-item></a-form></FormModal
    ><a-modal v-model:open="recordsOpen" title="操作" :footer="null" width="760"
      ><a-table
        row-key="id"
        :data-source="records"
        :columns="[
          { title: '操作', dataIndex: 'status' },
          { title: '操作', dataIndex: 'startedAt' },
          { title: '操作', dataIndex: 'finishedAt' },
          { title: '操作', dataIndex: 'message' },
        ]" /></a-modal
  ></PageContainer>
</template>
