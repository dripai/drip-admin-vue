<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
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
import type { DeptItem } from '@/types/system';
const data = ref<DeptItem[]>([]);
const loading = ref(false);
const open = ref(false);
const submitting = ref(false);
const current = ref<DeptItem>();
const form = reactive<Partial<DeptItem>>({
  deptName: '',
  leader: '',
  phone: '',
  sort: 0,
  status: 'ENABLED',
});
const columns = [
  { title: '部门名称', dataIndex: 'deptName' },
  { title: '负责人', dataIndex: 'leader' },
  { title: '手机号', dataIndex: 'phone' },
  { title: '排序', dataIndex: 'sort' },
  { title: '状态', dataIndex: 'status' },
  { title: '操作', dataIndex: 'action' },
];
async function load() {
  loading.value = true;
  try {
    data.value = await getDeptTree();
  } finally {
    loading.value = false;
  }
}
function add() {
  current.value = undefined;
  Object.assign(form, { deptName: '', leader: '', phone: '', sort: 0, status: 'ENABLED' });
  open.value = true;
}
function edit(row: DeptItem) {
  current.value = row;
  Object.assign(form, row);
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
onMounted(load);
</script>
<template>
  <PageContainer
    ><DataTable
      :columns="columns"
      :data-source="data"
      :loading="loading"
      :pagination="false"
      table-key="system-dept"
      @refresh="load"
      ><template #toolbarLeft>
        <a-button type="primary" @click="add">新增</a-button>
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
      :title="current ? '禁用' : '启用'"
      :submitting="submitting"
      @submit="submit"
      ><a-form :model="form" layout="vertical"
        ><a-form-item label="部门名称" required
          ><a-input v-model:value="form.deptName" /></a-form-item
        ><a-form-item label="负责人"><a-input v-model:value="form.leader" /></a-form-item
        ><a-form-item label="联系电话"><a-input v-model:value="form.phone" /></a-form-item
        ><a-form-item label="排序"
          ><a-input-number
            v-model:value="form.sort"
            style="width: 100%" /></a-form-item></a-form></FormModal
  ></PageContainer>
</template>
