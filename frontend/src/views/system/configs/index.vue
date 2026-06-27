<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import PageContainer from '@/components/layout/PageContainer.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import FormModal from '@/components/form/FormModal.vue';
import ConfirmAction from '@/components/permission/ConfirmAction.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import { useTable } from '@/composables/useTable';
import {
  createConfig,
  deleteConfig,
  queryConfigs,
  updateConfig,
  updateConfigStatus,
} from '@/api/system/config';
import type { ConfigItem } from '@/types/system';

const fields = [
  { label: '配置名称', field: 'configName', component: 'input' as const },
  { label: '配置键', field: 'configKey', component: 'input' as const },
  {
    label: '状态',
    field: 'status',
    component: 'select' as const,
    options: [
      { label: '启用', value: 'ENABLED' },
      { label: '禁用', value: 'DISABLED' },
    ],
  },
];
const columns: TableColumnType[] = [
  { title: '配置名称', dataIndex: 'configName' },
  { title: '配置键', dataIndex: 'configKey' },
  { title: '配置值', dataIndex: 'configValue' },
  { title: '内置', dataIndex: 'builtin', width: 80 },
  { title: '状态', dataIndex: 'status', width: 90 },
  { title: '备注', dataIndex: 'remark' },
  { title: '更新时间', dataIndex: 'updatedAt' },
  { title: '操作', dataIndex: 'action', width: 170 },
];
const table = useTable<ConfigItem, Record<string, unknown>>(
  queryConfigs as any,
  {},
  { storageKey: 'system.config.query' },
);
const open = ref(false);
const submitting = ref(false);
const current = ref<ConfigItem>();
const form = reactive<Partial<ConfigItem>>({
  configName: '',
  configKey: '',
  configValue: '',
  status: 'ENABLED',
  remark: '',
});

function resetForm(data: Partial<ConfigItem> = {}) {
  Object.assign(form, {
    configName: '',
    configKey: '',
    configValue: '',
    status: 'ENABLED',
    remark: '',
    ...data,
  });
}

function add() {
  current.value = undefined;
  resetForm();
  open.value = true;
}

function edit(row: ConfigItem) {
  current.value = row;
  resetForm(row);
  open.value = true;
}

async function submit() {
  submitting.value = true;
  try {
    if (current.value) await updateConfig(current.value.id, form);
    else await createConfig(form);
    message.success('操作成功');
    open.value = false;
    table.refresh();
  } finally {
    submitting.value = false;
  }
}

async function remove(row: ConfigItem) {
  await deleteConfig(row.id);
  message.success('操作成功');
  table.refresh();
}

async function status(row: ConfigItem) {
  await updateConfigStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED');
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
      @reset="table.reset" />
    <DataTable
      :columns="columns"
      :data-source="table.dataSource.value"
      :loading="table.loading.value"
      :pagination="table.pagination.value"
      table-key="system-config"
      @change="table.handleTableChange"
      @refresh="table.refresh"
      ><template #toolbarLeft>
        <a-button type="primary" @click="add">新增</a-button>
      </template>
      <template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'builtin'"
          ><a-tag>{{ record.builtin === 1 ? '是' : '否' }}</a-tag></template
        ><template v-else-if="column.dataIndex === 'status'"
          ><StatusTag :status="record.status" /></template
        ><template v-else-if="column.dataIndex === 'updatedAt'">{{
          formatTime(record.updatedAt)
        }}</template
        ><template v-else-if="column.dataIndex === 'action'"
          ><a-space
            ><a-button type="link" @click="edit(record)">编辑</a-button
            ><ConfirmAction
              v-if="record.builtin !== 1"
              :title="record.status === 'ENABLED' ? '确认禁用该配置？' : '确认启用该配置？'"
              @confirm="status(record)"
              >{{ record.status === 'ENABLED' ? '禁用' : '启用' }}</ConfirmAction
            ><ConfirmAction
              v-if="record.builtin !== 1"
              title="确认删除该配置？"
              danger
              @confirm="remove(record)"
              >删除</ConfirmAction
            ></a-space
          ></template
        ></template
      ></DataTable
    ><FormModal
      v-model:open="open"
      :title="current ? '编辑配置' : '新增配置'"
      :submitting="submitting"
      @submit="submit"
      ><a-form layout="vertical" :model="form"
        ><a-form-item label="配置名称" required
          ><a-input v-model:value="form.configName" /></a-form-item
        ><a-form-item label="配置键" required
          ><a-input v-model:value="form.configKey" :disabled="Boolean(current)" /></a-form-item
        ><a-form-item label="配置值"
          ><a-input v-model:value="form.configValue" /></a-form-item
        ><a-form-item label="备注"><a-textarea v-model:value="form.remark" :rows="3" /></a-form-item
      ></a-form></FormModal
  ></PageContainer>
</template>
