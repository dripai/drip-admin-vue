<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import DataTable from '@/components/table/DataTable.vue';
import FormModal from '@/components/form/FormModal.vue';
import ConfirmAction from '@/components/permission/ConfirmAction.vue';
import PermissionButton from '@/components/permission/PermissionButton.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import { useTable } from '@/composables/useTable';
import {
  createRole,
  deleteRole,
  getRolePermissions,
  getRoleUsers,
  queryRoles,
  saveRolePermissions,
  updateRole,
  updateRoleStatus,
} from '@/api/system/role';
import { getMenuTree } from '@/api/system/menu';
import type { ID } from '@/types/api';
import type { MenuNode, RoleForm, RoleItem, UserItem } from '@/types/system';
const fields = [
  { label: '角色名称', field: 'roleName', component: 'input' as const },
  { label: '角色编码', field: 'roleCode', component: 'input' as const },
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
  { title: '角色名称', dataIndex: 'roleName' },
  { title: '角色编码', dataIndex: 'roleCode' },
  { title: '状态', dataIndex: 'status' },
  { title: '备注', dataIndex: 'remark' },
  { title: '创建时间', dataIndex: 'createdAt' },
  { title: '关联用户', dataIndex: 'relatedUsers', width: 100 },
  { title: '操作', dataIndex: 'action', width: 210 },
];
const table = useTable<RoleItem, Record<string, unknown>>(
  queryRoles as any,
  {},
  { storageKey: 'system.roles.query' },
);
const modalOpen = ref(false);
const permOpen = ref(false);
const usersOpen = ref(false);
const submitting = ref(false);
const current = ref<RoleItem>();
const menus = ref<MenuNode[]>([]);
const checkedKeys = ref<ID[]>([]);
const relatedUsers = ref<UserItem[]>([]);
const form = reactive<RoleForm>({ roleName: '', roleCode: '', status: 'ENABLED', remark: '' });
function openCreate() {
  current.value = undefined;
  Object.assign(form, { roleName: '', roleCode: '', status: 'ENABLED', remark: '' });
  modalOpen.value = true;
}
function openEdit(row: RoleItem) {
  current.value = row;
  Object.assign(form, row);
  modalOpen.value = true;
}
async function submit() {
  submitting.value = true;
  try {
    if (current.value) await updateRole(current.value.id, form);
    else await createRole(form);
    message.success('操作成功');
    modalOpen.value = false;
    table.refresh();
  } finally {
    submitting.value = false;
  }
}
async function remove(row: RoleItem) {
  await deleteRole(row.id);
  message.success('操作成功');
  table.refresh();
}
async function status(row: RoleItem) {
  await updateRoleStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED');
  message.success('操作成功');
  table.refresh();
}
async function openPerm(row: RoleItem) {
  current.value = row;
  menus.value = await getMenuTree();
  const p = await getRolePermissions(row.id);
  checkedKeys.value = p.menuIds;
  permOpen.value = true;
}
async function openUsers(row: RoleItem) {
  current.value = row;
  relatedUsers.value = await getRoleUsers(row.id);
  usersOpen.value = true;
}
async function savePerm() {
  if (!current.value) return;
  submitting.value = true;
  try {
    await saveRolePermissions(current.value.id, {
      menuIds: normalizeCheckedKeys(checkedKeys.value),
      permissionCodes: [],
    });
    message.success('操作成功');
    permOpen.value = false;
  } finally {
    submitting.value = false;
  }
}
function normalizeCheckedKeys(value: ID[]) {
  return value.filter((key): key is number => typeof key === 'number');
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
      table-key="system-roles"
      @change="table.handleTableChange"
      @refresh="table.refresh"
      ><template #toolbarLeft>
        <PermissionButton permission="system:role:create" type="primary" @click="openCreate"
          >新增</PermissionButton
        >
      </template>
      <template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'status'"
          ><StatusTag :status="record.status" /></template
        ><template v-else-if="column.dataIndex === 'relatedUsers'"
          ><a-button type="link" @click="openUsers(record)">查看</a-button></template
        ><template v-else-if="column.dataIndex === 'action'"
          ><a-space
            ><a-button type="link" @click="openEdit(record)">编辑</a-button
            ><a-button type="link" @click="openPerm(record)">分配菜单</a-button
            ><ConfirmAction
              :title="record.status === 'ENABLED' ? '禁用' : '启用'"
              @confirm="status(record)"
              >{{ record.status === 'ENABLED' ? '禁用' : '启用' }}</ConfirmAction
            ><ConfirmAction title="确认删除该角色？" danger @confirm="remove(record)"
              >删除</ConfirmAction
            ></a-space
          ></template
        ></template
      ></DataTable
    ><FormModal
      v-model:open="modalOpen"
      :title="current ? '编辑角色' : '新增角色'"
      :submitting="submitting"
      @submit="submit"
      ><a-form :model="form" layout="vertical"
        ><a-form-item label="角色名称" required
          ><a-input v-model:value="form.roleName" /></a-form-item
        ><a-form-item label="角色编码" required
          ><a-input v-model:value="form.roleCode" /></a-form-item
        ><a-form-item label="状态"
          ><a-select
            v-model:value="form.status"
            :options="[
              { label: '启用', value: 'ENABLED' },
              { label: '禁用', value: 'DISABLED' },
            ]" /></a-form-item
        ><a-form-item label="备注"
          ><a-textarea v-model:value="form.remark" /></a-form-item></a-form></FormModal
    ><FormModal
      v-model:open="permOpen"
      title="分配菜单"
      :submitting="submitting"
      :width="760"
      @submit="savePerm"
      ><a-tree
        v-model:checked-keys="checkedKeys"
        checkable
        :tree-data="menus"
        :field-names="{ title: 'name', key: 'id', children: 'children' }" /></FormModal
    ><a-modal v-model:open="usersOpen" title="关联用户" :footer="null" width="760"
      ><a-table
        row-key="id"
        size="small"
        :data-source="relatedUsers"
        :columns="[
          { title: '用户名', dataIndex: 'username' },
          { title: '姓名', dataIndex: 'realName' },
          { title: '手机号', dataIndex: 'phone' },
          { title: '状态', dataIndex: 'status' },
        ]"
        ><template #bodyCell="{ column, record }"
          ><template v-if="column.dataIndex === 'status'"
            ><StatusTag :status="record.status" /></template></template></a-table></a-modal
  ></PageContainer>
</template>
