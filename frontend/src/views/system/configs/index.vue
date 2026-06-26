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
  { label: '操作', field: 'configName', component: 'input' as const },
  { label: '操作', field: 'configKey', component: 'input' as const },
  { label: '操作', field: 'groupName', component: 'input' as const },
  {
    label: '操作',
    field: 'status',
    component: 'select' as const,
    options: [
      { label: '操作', value: 'ENABLED' },
      { label: '操作', value: 'DISABLED' },
    ],
  },
];
const columns: TableColumnType[] = [
  { title: '操作', dataIndex: 'configName' },
  { title: '操作', dataIndex: 'configKey' },
  { title: '操作', dataIndex: 'configValue' },
  { title: '操作', dataIndex: 'groupName' },
  { title: '操作', dataIndex: 'sensitive' },
  { title: '操作', dataIndex: 'status' },
  { title: '操作', dataIndex: 'updatedAt' },
  { title: '操作', dataIndex: 'action' },
];
const table = useTable<ConfigItem, Record<string, unknown>>(queryConfigs as any, {});
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
    message.success('操作');
    open.value = false;
    table.refresh();
  } finally {
    submitting.value = false;
  }
}
async function remove(row: ConfigItem) {
  await deleteConfig(row.id);
  message.success('操作');
  table.refresh();
}
async function status(row: ConfigItem) {
  await updateConfigStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED');
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
        ><template v-if="column.dataIndex === 'configValue'">{{
          record.sensitive ? '******' : record.configValue
        }}</template
        ><template v-else-if="column.dataIndex === 'sensitive'"
          ><a-tag>{{ record.sensitive ? '?' : '?' }}</a-tag></template
        ><template v-else-if="column.dataIndex === 'status'"
          ><StatusTag :status="record.status" /></template
        ><template v-else-if="column.dataIndex === 'action'"
          ><a-space
            ><a-button type="link" @click="edit(record)">操作</a-button
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
        ><a-form-item label="操作" required><a-input v-model:value="form.configName" /></a-form-item
        ><a-form-item label="操作" required><a-input v-model:value="form.configKey" /></a-form-item
        ><a-form-item label="操作" required
          ><a-input v-model:value="form.configValue" /></a-form-item
        ><a-form-item label="操作"><a-input v-model:value="form.groupName" /></a-form-item
        ><a-form-item label="操作"
          ><a-switch v-model:checked="form.sensitive" /></a-form-item></a-form></FormModal
  ></PageContainer>
</template>
