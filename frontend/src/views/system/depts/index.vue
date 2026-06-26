<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
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
  { title: '操作', dataIndex: 'deptName' },
  { title: '操作', dataIndex: 'leader' },
  { title: '操作', dataIndex: 'phone' },
  { title: '操作', dataIndex: 'sort' },
  { title: '操作', dataIndex: 'status' },
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
    message.success('操作');
    open.value = false;
    load();
  } finally {
    submitting.value = false;
  }
}
async function remove(row: DeptItem) {
  await deleteDept(row.id);
  message.success('操作');
  load();
}
async function status(row: DeptItem) {
  await updateDeptStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED');
  message.success('操作');
  load();
}
onMounted(load);
</script>
<template>
  <PageContainer title="操作"
    ><div class="page-actions">
      <a-button type="primary" @click="add">操作</a-button><a-button @click="load">操作</a-button>
    </div>
    <a-table
      row-key="id"
      :columns="columns"
      :data-source="data"
      :loading="loading"
      :pagination="false"
      ><template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'status'"
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
      ></a-table
    ><FormModal
      v-model:open="open"
      :title="current ? '操作' : '操作'"
      :submitting="submitting"
      @submit="submit"
      ><a-form :model="form" layout="vertical"
        ><a-form-item label="操作" required><a-input v-model:value="form.deptName" /></a-form-item
        ><a-form-item label="操作"><a-input v-model:value="form.leader" /></a-form-item
        ><a-form-item label="操作"><a-input v-model:value="form.phone" /></a-form-item
        ><a-form-item label="操作"
          ><a-input-number
            v-model:value="form.sort"
            style="width: 100%" /></a-form-item></a-form></FormModal
  ></PageContainer>
</template>
