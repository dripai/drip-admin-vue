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
  { label: '任务名称', field: 'jobName', component: 'input' as const },
  { label: '任务编码', field: 'jobCode', component: 'input' as const },
  {
    label: '状态',
    field: 'status',
    component: 'select' as const,
    options: [
      { label: '启用', value: 'ENABLED' },
      { label: '禁用', value: 'DISABLED' },
    ],
  },
  { label: '时间范围', field: 'createdRange', component: 'range' as const },
];
const columns: TableColumnType[] = [
  { title: '任务名称', dataIndex: 'jobName' },
  { title: '任务编码', dataIndex: 'jobCode' },
  { title: 'cron 表达式', dataIndex: 'cron' },
  { title: '状态', dataIndex: 'status' },
  { title: '最近执行时间', dataIndex: 'lastRunAt' },
  { title: '最近执行结果', dataIndex: 'lastResult' },
  { title: '下次执行时间', dataIndex: 'nextRunAt' },
  { title: '操作', dataIndex: 'action', width: 260 },
];
const table = useTable<JobItem, Record<string, unknown>>(
  queryJobs as any,
  {},
  { storageKey: 'system.jobs.query' },
);
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
    message.error('请输入 cron 表达式');
    return;
  }
  submitting.value = true;
  try {
    if (current.value) await updateJob(current.value.id, form);
    else await createJob(form);
    message.success('操作成功');
    open.value = false;
    table.refresh();
  } finally {
    submitting.value = false;
  }
}
async function remove(row: JobItem) {
  await deleteJob(row.id);
  message.success('操作成功');
  table.refresh();
}
async function status(row: JobItem) {
  await updateJobStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED');
  message.success('操作成功');
  table.refresh();
}
async function run(row: JobItem) {
  await runJob(row.id);
  message.success('操作成功');
}
async function showRecords(row: JobItem) {
  const res = await queryJobRecords(row.id, { page: 1, pageSize: 20 });
  records.value = res.list;
  recordsOpen.value = true;
}
onMounted(table.refresh);
</script>
<template>
  <PageContainer title="定时任务"
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
      table-key="system-jobs"
      @change="table.handleTableChange"
      @refresh="table.refresh"
      ><template #toolbarLeft>
        <a-button type="primary" @click="add">新增任务</a-button>
      </template>
      <template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'status'"
          ><StatusTag :status="record.status" /></template
        ><template v-else-if="column.dataIndex === 'action'"
          ><a-space
            ><a-button type="link" @click="edit(record)">编辑</a-button
            ><ConfirmAction title="确认手动执行该任务？" @confirm="run(record)"
              >手动执行</ConfirmAction
            ><a-button type="link" @click="showRecords(record)">执行记录</a-button
            ><ConfirmAction
              :title="record.status === 'ENABLED' ? '禁用' : '启用'"
              @confirm="status(record)"
              >{{ record.status === 'ENABLED' ? '禁用' : '启用' }}</ConfirmAction
            ><ConfirmAction title="确认删除该任务？" danger @confirm="remove(record)"
              >删除</ConfirmAction
            ></a-space
          ></template
        ></template
      ></DataTable
    ><FormModal
      v-model:open="open"
      :title="current ? '编辑任务' : '新增任务'"
      :submitting="submitting"
      @submit="submit"
      ><a-form layout="vertical" :model="form"
        ><a-form-item label="任务名称" required
          ><a-input v-model:value="form.jobName" /></a-form-item
        ><a-form-item label="任务编码" required
          ><a-input v-model:value="form.jobCode" /></a-form-item
        ><a-form-item label="cron 表达式" required
          ><a-input v-model:value="form.cron" /></a-form-item></a-form></FormModal
    ><a-modal v-model:open="recordsOpen" title="执行记录" :footer="null" width="760"
      ><a-table
        row-key="id"
        :data-source="records"
        :columns="[
          { title: '状态', dataIndex: 'status' },
          { title: '开始时间', dataIndex: 'startedAt' },
          { title: '结束时间', dataIndex: 'finishedAt' },
          { title: '消息', dataIndex: 'message' },
        ]" /></a-modal
  ></PageContainer>
</template>
