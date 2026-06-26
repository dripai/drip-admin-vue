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
  { label: '分组', field: 'groupName', component: 'input' as const },
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
  { title: '分组', dataIndex: 'groupName' },
  { title: '是否敏感', dataIndex: 'sensitive' },
  { title: '状态', dataIndex: 'status' },
  { title: '更新时间', dataIndex: 'updatedAt' },
  { title: '操作', dataIndex: 'action' },
];
const table = useTable<ConfigItem, Record<string, unknown>>(
  queryConfigs as any,
  {},
  { storageKey: 'system.configs.query' },
);
const open = ref(false);
const submitting = ref(false);
const current = ref<ConfigItem>();
const form = reactive<Partial<ConfigItem>>({
  configName: '',
  configKey: '',
  configValue: '',
  groupName: 'default',
  sensitive: false,
  status: 'ENABLED',
});
function add() {
  current.value = undefined;
  Object.assign(form, {
    configName: '',
    configKey: '',
    configValue: '',
    groupName: 'default',
    sensitive: false,
    status: 'ENABLED',
  });
  open.value = true;
}
function edit(row: ConfigItem) {
  current.value = row;
  Object.assign(form, row, { configValue: row.sensitive ? '' : row.configValue });
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
      table-key="system-configs"
      @change="table.handleTableChange"
      @refresh="table.refresh"
      ><template #toolbarLeft>
        <a-button type="primary" @click="add">新增</a-button>
      </template>
      <template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'configValue'">{{
          record.sensitive ? '******' : record.configValue
        }}</template
        ><template v-else-if="column.dataIndex === 'sensitive'"
          ><a-tag>{{ record.sensitive ? '是' : '否' }}</a-tag></template
        ><template v-else-if="column.dataIndex === 'status'"
          ><StatusTag :status="record.status" /></template
        ><template v-else-if="column.dataIndex === 'action'"
          ><a-space
            ><a-button type="link" @click="edit(record)">编辑</a-button
            ><ConfirmAction
              :title="record.status === 'ENABLED' ? '禁用' : '启用'"
              @confirm="status(record)"
              >{{ record.status === 'ENABLED' ? '禁用' : '启用' }}</ConfirmAction
            ><ConfirmAction title="确认删除该配置？" danger @confirm="remove(record)"
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
          ><a-input v-model:value="form.configKey" /></a-form-item
        ><a-form-item label="配置值" required
          ><a-input v-model:value="form.configValue" /></a-form-item
        ><a-form-item label="分组"><a-input v-model:value="form.groupName" /></a-form-item
        ><a-form-item label="敏感配置"
          ><a-switch v-model:checked="form.sensitive" /></a-form-item></a-form></FormModal
  ></PageContainer>
</template>
