<script setup lang="ts">
import { onMounted } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import { useTable } from '@/composables/useTable';
import { queryJobRunLogs } from '@/api/system/job';
import type { JobRecordItem } from '@/types/system';
import { formatDateTime } from '@/utils/date';

const fields = [
  { label: '任务名称', field: 'jobName', component: 'input' as const },
  {
    label: '执行结果',
    field: 'status',
    component: 'select' as const,
    options: [
      { label: '执行中', value: 'RUNNING' },
      { label: '成功', value: 'SUCCESS' },
      { label: '失败', value: 'FAIL' },
    ],
  },
  { label: '开始时间', field: 'startedRange', component: 'range' as const },
];

const columns: TableColumnType[] = [
  { title: '任务名称', dataIndex: 'jobName' },
  { title: '状态', dataIndex: 'status' },
  { title: '开始时间', dataIndex: 'startedAt' },
  { title: '结束时间', dataIndex: 'finishedAt' },
  { title: '耗时(ms)', dataIndex: 'costMs' },
  { title: '错误信息', dataIndex: 'errorMessage' },
];

const table = useTable<JobRecordItem, Record<string, unknown>>(
  queryJobRunLogs as any,
  {},
  { storageKey: 'system.jobHistory.query' },
);

onMounted(table.refresh);
</script>

<template>
  <PageContainer>
    <SearchForm
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
      table-key="system-job-history"
      @change="table.handleTableChange"
      @refresh="table.refresh"
      ><template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'startedAt'">
          {{ formatDateTime(record.startedAt) }}
        </template>
        <template v-else-if="column.dataIndex === 'finishedAt'">
          {{ formatDateTime(record.finishedAt) }}
        </template>
      </template></DataTable
    >
  </PageContainer>
</template>
