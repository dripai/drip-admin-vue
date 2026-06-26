<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import FormModal from '@/components/form/FormModal.vue';
import PermissionButton from '@/components/permission/PermissionButton.vue';
import ConfirmAction from '@/components/permission/ConfirmAction.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import { useTable } from '@/composables/useTable';
import {
  assignUserRoles,
  createUser,
  deleteUser,
  listRoleOptions,
  queryUsers,
  resetUserPassword,
  updateUser,
  updateUserStatus,
} from '@/api/system/user';
import type { OptionItem, UserForm, UserItem } from '@/types/system';

const fields = [
  { label: '用户名', field: 'username', component: 'input' as const },
  { label: '姓名', field: 'realName', component: 'input' as const },
  { label: '手机号', field: 'phone', component: 'input' as const },
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
  { title: '用户名', dataIndex: 'username' },
  { title: '姓名', dataIndex: 'realName' },
  { title: '手机号', dataIndex: 'phone' },
  { title: '邮箱', dataIndex: 'email' },
  { title: '部门', dataIndex: ['dept', 'deptName'] },
  { title: '状态', dataIndex: 'status' },
  { title: '角色', dataIndex: 'roles' },
  { title: '创建时间', dataIndex: 'createdAt' },
  { title: '最后登录时间', dataIndex: 'lastLoginAt' },
  { title: '操作', dataIndex: 'action', width: 260 },
];
const table = useTable<UserItem, Record<string, unknown>>(
  queryUsers as any,
  {},
  { storageKey: 'system.users.query' },
);
const modalOpen = ref(false);
const assignOpen = ref(false);
const submitting = ref(false);
const current = ref<UserItem>();
const roleOptions = ref<OptionItem[]>([]);
const form = reactive<UserForm>({
  username: '',
  realName: '',
  phone: '',
  email: '',
  password: '',
  roleIds: [],
  status: 'ENABLED',
});
function openCreate() {
  current.value = undefined;
  Object.assign(form, {
    username: '',
    realName: '',
    phone: '',
    email: '',
    password: '',
    roleIds: [],
    status: 'ENABLED',
  });
  modalOpen.value = true;
}
function openEdit(row: UserItem) {
  current.value = row;
  Object.assign(form, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    phone: row.phone,
    email: row.email,
    password: '',
    deptId: row.dept?.id,
    roleIds: row.roles.map((r) => r.id),
    status: row.status,
  });
  modalOpen.value = true;
}
async function loadRoleOptions() {
  const roles = await listRoleOptions();
  roleOptions.value = roles.map((role) => ({ label: role.roleName, value: role.id }));
}
async function submit() {
  submitting.value = true;
  try {
    if (current.value) await updateUser(current.value.id, form);
    else await createUser(form);
    message.success('操作成功');
    modalOpen.value = false;
    table.refresh();
  } finally {
    submitting.value = false;
  }
}
async function remove(row: UserItem) {
  await deleteUser(row.id);
  message.success('操作成功');
  table.refresh();
}
async function status(row: UserItem) {
  await updateUserStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED');
  message.success('操作成功');
  table.refresh();
}
async function reset(row: UserItem) {
  await resetUserPassword(row.id);
  message.success('操作成功');
}
function openAssign(row: UserItem) {
  current.value = row;
  form.roleIds = row.roles.map((r) => r.id);
  loadRoleOptions();
  assignOpen.value = true;
}
async function saveAssign() {
  if (!current.value) return;
  submitting.value = true;
  try {
    await assignUserRoles(current.value.id, form.roleIds);
    message.success('操作成功');
    assignOpen.value = false;
    table.refresh();
  } finally {
    submitting.value = false;
  }
}
onMounted(() => {
  table.refresh();
  loadRoleOptions();
});
</script>
<template>
  <PageContainer title="用户管理">
    <SearchForm
      :model="table.query"
      :fields="fields"
      :loading="table.loading.value"
      @search="table.search"
      @reset="table.reset"
    />
    <div class="page-actions">
      <PermissionButton permission="system:user:create" type="primary" @click="openCreate"
        >新增用户</PermissionButton
      ><a-button @click="table.refresh">刷新</a-button>
    </div>
    <DataTable
      :columns="columns"
      :data-source="table.dataSource.value"
      :loading="table.loading.value"
      :pagination="table.pagination.value"
      table-key="system-users"
      @change="table.handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'"
          ><StatusTag :status="record.status"
        /></template>
        <template v-else-if="column.dataIndex === 'roles'">{{
          record.roles?.map((r: any) => r.roleName).join('，') || '-'
        }}</template>
        <template v-else-if="column.dataIndex === 'action'"
          ><a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button
            ><a-button type="link" @click="openAssign(record)">分配角色</a-button>
            <ConfirmAction
              :title="record.status === 'ENABLED' ? '禁用' : '启用'"
              @confirm="status(record)"
              >{{ record.status === 'ENABLED' ? '禁用' : '启用' }}</ConfirmAction
            >
            <ConfirmAction title="确认重置该用户密码？" @confirm="reset(record)"
              >重置密码</ConfirmAction
            ><ConfirmAction title="确认删除该用户？" danger @confirm="remove(record)"
              >删除</ConfirmAction
            >
          </a-space></template
        >
      </template>
    </DataTable>
    <FormModal
      v-model:open="modalOpen"
      :title="current ? '编辑用户' : '新增用户'"
      :submitting="submitting"
      @submit="submit"
    >
      <a-form :model="form" layout="vertical"
        ><a-form-item label="用户名" required><a-input v-model:value="form.username" /></a-form-item
        ><a-form-item label="姓名" required><a-input v-model:value="form.realName" /></a-form-item
        ><a-form-item label="手机号"><a-input v-model:value="form.phone" /></a-form-item
        ><a-form-item label="邮箱"><a-input v-model:value="form.email" /></a-form-item
        ><a-form-item v-if="!current" label="密码" required
          ><a-input-password v-model:value="form.password" /></a-form-item
      ></a-form>
    </FormModal>
    <FormModal
      v-model:open="assignOpen"
      title="分配角色"
      :submitting="submitting"
      @submit="saveAssign"
      ><a-select
        v-model:value="form.roleIds"
        mode="multiple"
        :options="roleOptions"
        style="width: 100%"
        placeholder="请选择角色"
    /></FormModal>
  </PageContainer>
</template>
