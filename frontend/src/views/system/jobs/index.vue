<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
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
  queryJobScripts,
  queryJobs,
  runJob,
  updateJob,
  updateJobStatus,
} from '@/api/system/job';
import type { JobItem } from '@/types/system';
const fields = [
  { label: '任务名称', field: 'jobName', component: 'input' as const },
  { label: '备注', field: 'remark', component: 'input' as const },
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
  { title: '备注', dataIndex: 'remark' },
  { title: 'cron 表达式', dataIndex: 'cronExpression' },
  { title: '执行类型', dataIndex: 'executorType' },
  { title: '状态', dataIndex: 'status' },
  { title: '操作', dataIndex: 'action', width: 260 },
];
const table = useTable<JobItem, Record<string, unknown>>(
  queryJobs as any,
  {},
  { storageKey: 'system.jobs.query' },
);
const open = ref(false);
const current = ref<JobItem>();
const submitting = ref(false);
const scriptFiles = ref<string[]>([]);
const form = reactive<Partial<JobItem>>({
  jobName: '',
  remark: '',
  cronExpression: '',
  executorType: 'python',
  scriptFile: 'mysql-backup.py',
  scriptArgs: '',
  status: 'ENABLED',
});
function add() {
  current.value = undefined;
  Object.assign(form, {
    jobName: '',
    remark: '',
    cronExpression: '',
    executorType: 'python',
    scriptFile: 'mysql-backup.py',
    scriptArgs: '',
    status: 'ENABLED',
  });
  open.value = true;
  loadScripts();
}
function edit(row: JobItem) {
  current.value = row;
  Object.assign(form, {
    jobName: row.jobName,
    remark: row.remark,
    cronExpression: row.cronExpression,
    executorType: row.executorType,
    scriptFile: row.scriptFile,
    scriptArgs: row.scriptArgs,
    status: row.status,
  });
  open.value = true;
  loadScripts();
}
async function submit() {
  if (!form.cronExpression?.trim()) {
    message.error('请输入 cron 表达式');
    return;
  }
  if (!form.executorType?.trim()) {
    message.error('请选择执行类型');
    return;
  }
  if (!form.scriptFile?.trim()) {
    message.error('请选择脚本文件');
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
async function loadScripts() {
  if (!form.executorType) {
    scriptFiles.value = [];
    return;
  }
  scriptFiles.value = await queryJobScripts(form.executorType);
  if (form.scriptFile && !scriptFiles.value.includes(form.scriptFile)) {
    form.scriptFile = undefined;
  }
}
watch(() => form.executorType, loadScripts);
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
      table-key="system-jobs"
      @change="table.handleTableChange"
      @refresh="table.refresh"
      ><template #toolbarLeft>
        <a-button type="primary" @click="add">新增</a-button>
      </template>
      <template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'status'"
          ><StatusTag :status="record.status" /></template
        ><template v-else-if="column.dataIndex === 'action'"
          ><a-space
            ><a-button type="link" @click="edit(record)">编辑</a-button
            ><ConfirmAction title="确认手动执行该任务？" @confirm="run(record)"
              >手动执行</ConfirmAction
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
        ><a-form-item label="cron 表达式" required
          ><a-input v-model:value="form.cronExpression" /></a-form-item
        ><a-form-item label="执行类型" required
          ><a-select v-model:value="form.executorType">
            <a-select-option value="shell">Shell</a-select-option>
            <a-select-option value="bat">Bat/Cmd</a-select-option>
            <a-select-option value="powershell">PowerShell</a-select-option>
            <a-select-option value="python">Python</a-select-option>
          </a-select></a-form-item
        ><a-form-item label="脚本文件" required
          ><a-select v-model:value="form.scriptFile" @focus="loadScripts">
            <a-select-option v-for="file in scriptFiles" :key="file" :value="file">{{
              file
            }}</a-select-option>
          </a-select></a-form-item
        ><a-form-item label="脚本参数"
          ><a-input v-model:value="form.scriptArgs" /></a-form-item
        ><a-form-item label="备注"
          ><a-input v-model:value="form.remark" /></a-form-item
        ></a-form></FormModal
  ></PageContainer>
</template>
