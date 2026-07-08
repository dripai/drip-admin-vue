<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { TableColumnType } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import $ from 'jquery';
import { defaultElementTypeProvider, hiprint } from 'vue-plugin-hiprint';
import 'vue-plugin-hiprint/dist/print-lock.css';
import PageContainer from '@/components/layout/PageContainer.vue';
import DataTable from '@/components/table/DataTable.vue';
import ConfirmAction from '@/components/permission/ConfirmAction.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import { useTable } from '@/composables/useTable';
import { formatDateTime } from '@/utils/date';
import {
  copyPrintTemplate,
  deletePrintTemplate,
  queryPrintTemplates,
  updatePrintTemplateStatus,
} from '@/api/system/printTemplate';
import type { PrintTemplateItem } from '@/types/system';

const router = useRouter();

const columns: TableColumnType[] = [
  { title: '模板编码', dataIndex: 'code', width: 180 },
  { title: '模板名称', dataIndex: 'name', width: 180 },
  { title: '纸张', dataIndex: 'paperType', width: 90 },
  { title: '状态', dataIndex: 'status', width: 90 },
  { title: '更新时间', dataIndex: 'updatedAt', width: 170 },
  { title: '操作', dataIndex: 'action', width: 340 },
];

const table = useTable<PrintTemplateItem, Record<string, unknown>>(
  queryPrintTemplates as any,
  {},
  { storageKey: 'system.printTemplate.query' },
);
const copyVisible = ref(false);
const copySubmitting = ref(false);
const copySource = ref<PrintTemplateItem>();
const copyForm = reactive({
  code: '',
  name: '',
  status: 'DISABLED' as PrintTemplateItem['status'],
});

function add() {
  router.push('/system/print-template/create');
}

function edit(row: PrintTemplateItem) {
  router.push(`/system/print-template/${row.id}`);
}

function design(row: PrintTemplateItem) {
  router.push(`/system/print-template/${row.id}/design`);
}

function copy(row: PrintTemplateItem) {
  copySource.value = row;
  copyForm.code = `${row.code}_COPY`;
  copyForm.name = `${row.name} 副本`;
  copyForm.status = 'DISABLED';
  copyVisible.value = true;
}

async function submitCopy() {
  if (!copySource.value) return;
  if (!copyForm.code.trim() || !copyForm.name.trim()) {
    message.warning('请填写模板编码和模板名称');
    return;
  }
  copySubmitting.value = true;
  try {
    const id = await copyPrintTemplate(copySource.value.id, {
      code: copyForm.code.trim(),
      name: copyForm.name.trim(),
      status: copyForm.status,
    });
    message.success('复制成功');
    copyVisible.value = false;
    table.refresh();
    router.push(`/system/print-template/${id}/design`);
  } finally {
    copySubmitting.value = false;
  }
}

async function remove(row: PrintTemplateItem) {
  await deletePrintTemplate(row.id);
  message.success('操作成功');
  table.refresh();
}

async function status(row: PrintTemplateItem) {
  await updatePrintTemplateStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED');
  message.success('操作成功');
  table.refresh();
}

function initHiprint() {
  (window as any).$ = $;
  (window as any).jQuery = $;
  hiprint.init({
    providers: [new defaultElementTypeProvider()],
  });
}

function preview(row: PrintTemplateItem) {
  try {
    initHiprint();
    const printTemplate = new hiprint.PrintTemplate({ template: JSON.parse(row.templateJson || '{}') });
    printTemplate.print({});
  } catch {
    message.error('模板 JSON 格式不正确');
  }
}

onMounted(table.refresh);
</script>

<template>
  <PageContainer>
    <DataTable
      :columns="columns"
      :data-source="table.dataSource.value"
      :loading="table.loading.value"
      :pagination="table.pagination.value"
      table-key="system-print-template"
      @change="table.handleTableChange"
      @refresh="table.refresh">
      <template #toolbarLeft>
        <a-button type="primary" @click="add">新增</a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <StatusTag :status="record.status" />
        </template>
        <template v-else-if="column.dataIndex === 'updatedAt'">
          {{ formatDateTime(record.updatedAt) }}
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <a-space>
            <a-button type="link" @click="edit(record)">编辑</a-button>
            <a-button type="link" @click="design(record)">设计</a-button>
            <a-button type="link" @click="copy(record)">复制</a-button>
            <a-button type="link" @click="preview(record)">预览</a-button>
            <ConfirmAction
              :title="record.status === 1 ? '确认禁用该打印模板？' : '确认启用该打印模板？'"
              @confirm="status(record)">
              {{ record.status === 1 ? '禁用' : '启用' }}
            </ConfirmAction>
            <ConfirmAction title="确认删除该打印模板？" danger @confirm="remove(record)">
              删除
            </ConfirmAction>
          </a-space>
        </template>
      </template>
    </DataTable>
    <a-modal
      v-model:open="copyVisible"
      title="复制打印模板"
      :confirm-loading="copySubmitting"
      @ok="submitCopy">
      <a-form layout="vertical">
        <a-form-item label="模板编码" required>
          <a-input v-model:value="copyForm.code" maxlength="64" />
        </a-form-item>
        <a-form-item label="模板名称" required>
          <a-input v-model:value="copyForm.name" maxlength="100" />
        </a-form-item>
        <a-form-item label="状态" required>
          <a-radio-group v-model:value="copyForm.status">
            <a-radio value="ENABLED">启用</a-radio>
            <a-radio value="DISABLED">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>
