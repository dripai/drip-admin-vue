<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
import DataTable from '@/components/table/DataTable.vue';
import FormModal from '@/components/form/FormModal.vue';
import ConfirmAction from '@/components/permission/ConfirmAction.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import {
  createDept,
  deleteDept,
  getDeptTree,
  updateDept,
  updateDeptStatus,
} from '@/api/system/dept';
import type { ID, Status } from '@/types/api';
import type { DeptItem } from '@/types/system';

type DeptForm = {
  parentId: ID | null;
  deptName: string;
  deptCode: string;
  leaderUserId?: ID | null;
  sort: number;
  status: Status;
};

const data = ref<DeptItem[]>([]);
const loading = ref(false);
const open = ref(false);
const submitting = ref(false);
const current = ref<DeptItem>();
const expandedRowKeys = ref<ID[]>([]);
const form = reactive<DeptForm>({
  parentId: null,
  deptName: '',
  deptCode: '',
  leaderUserId: null,
  sort: 0,
  status: 'ENABLED',
});
const columns = [
  { title: '部门名称', dataIndex: 'deptName' },
  { title: '部门编码', dataIndex: 'deptCode', width: 160 },
  { title: '负责人ID', dataIndex: 'leaderUserId', width: 120 },
  { title: '排序', dataIndex: 'sort', width: 90 },
  { title: '状态', dataIndex: 'status', width: 90 },
  { title: '操作', dataIndex: 'action', width: 190 },
];
const parentTreeData = computed(() => markDisabled(data.value, false));
async function load() {
  loading.value = true;
  try {
    data.value = await getDeptTree();
    expandAll();
  } finally {
    loading.value = false;
  }
}
function add() {
  current.value = undefined;
  Object.assign(form, {
    parentId: null,
    deptName: '',
    deptCode: '',
    leaderUserId: null,
    sort: 0,
    status: 'ENABLED',
  });
  open.value = true;
}
function edit(row: DeptItem) {
  current.value = row;
  Object.assign(form, {
    parentId: row.parentId && row.parentId !== 0 ? row.parentId : null,
    deptName: row.deptName,
    deptCode: row.deptCode,
    leaderUserId: row.leaderUserId || null,
    sort: row.sort,
    status: row.status,
  });
  open.value = true;
}
async function submit() {
  submitting.value = true;
  try {
    if (current.value) await updateDept(current.value.id, form);
    else await createDept(form);
    message.success('操作成功');
    open.value = false;
    load();
  } finally {
    submitting.value = false;
  }
}
async function remove(row: DeptItem) {
  await deleteDept(row.id);
  message.success('操作成功');
  load();
}
async function status(row: DeptItem) {
  await updateDeptStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED');
  message.success('操作成功');
  load();
}
function markDisabled(items: DeptItem[], parentDisabled: boolean): DeptItem[] {
  return items.map((item) => ({
    ...item,
    disabled: current.value ? parentDisabled || item.id === current.value.id : false,
    children: item.children?.length
      ? markDisabled(item.children, parentDisabled || item.id === current.value?.id)
      : [],
  })) as DeptItem[];
}
function flattenDeptIds(nodes: DeptItem[]): ID[] {
  return nodes.flatMap((item) => [
    item.id,
    ...(item.children?.length ? flattenDeptIds(item.children) : []),
  ]);
}
function expandAll() {
  expandedRowKeys.value = flattenDeptIds(data.value);
}
function collapseAll() {
  expandedRowKeys.value = [];
}
function updateExpandedRows(keys: Array<string | number>) {
  expandedRowKeys.value = keys as ID[];
}
onMounted(load);
</script>
<template>
  <PageContainer
    ><DataTable
      :columns="columns"
      :data-source="data"
      :loading="loading"
      :pagination="false"
      :expanded-row-keys="expandedRowKeys"
      table-key="system-dept"
      @refresh="load"
      @expandedRowsChange="updateExpandedRows"
      ><template #toolbarLeft>
        <a-space>
          <a-button type="primary" @click="add">新增</a-button>
          <a-button @click="expandAll">展开</a-button>
          <a-button @click="collapseAll">收缩</a-button>
        </a-space>
      </template>
      <template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'status'"
          ><StatusTag :status="record.status" /></template
        ><template v-else-if="column.dataIndex === 'action'"
          ><a-space
            ><a-button type="link" @click="edit(record)">编辑</a-button
            ><ConfirmAction
              :title="record.status === 'ENABLED' ? '禁用' : '启用'"
              @confirm="status(record)"
              >{{ record.status === 'ENABLED' ? '禁用' : '启用' }}</ConfirmAction
            ><ConfirmAction title="确认删除该部门？" danger @confirm="remove(record)"
              >删除</ConfirmAction
            ></a-space
          ></template
        ></template
      ></DataTable
    ><FormModal
      v-model:open="open"
      :title="current ? '编辑部门' : '新增部门'"
      :submitting="submitting"
      @submit="submit"
      ><a-form :model="form" layout="horizontal" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }"
        ><a-form-item label="上级部门"
          ><a-tree-select
            v-model:value="form.parentId"
            :tree-data="parentTreeData"
            :field-names="{ label: 'deptName', value: 'id', children: 'children' }"
            allow-clear
            tree-default-expand-all
            placeholder="不选择表示顶级部门" /></a-form-item
        ><a-form-item label="部门名称" required
          ><a-input v-model:value="form.deptName" /></a-form-item
        ><a-form-item label="部门编码" required
          ><a-input v-model:value="form.deptCode" /></a-form-item
        ><a-form-item label="负责人ID"
          ><a-input-number
            v-model:value="form.leaderUserId"
            :min="1"
            style="width: 100%" /></a-form-item
        ><a-form-item label="排序"
          ><a-input-number
            v-model:value="form.sort"
            style="width: 100%" /></a-form-item></a-form></FormModal
  ></PageContainer>
</template>
