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
  queryRoles,
  saveRolePermissions,
  updateRole,
  updateRoleStatus,
} from '@/api/system/role';
import { getMenuTree } from '@/api/system/menu';
import type { ID } from '@/types/api';
import type { MenuNode, RoleForm, RoleItem } from '@/types/system';
const fields = [
  { label: '操作', field: 'roleName', component: 'input' as const },
  { label: '操作', field: 'roleCode', component: 'input' as const },
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
  { title: '操作', dataIndex: 'roleName' },
  { title: '操作', dataIndex: 'roleCode' },
  { title: '操作', dataIndex: 'status' },
  { title: '操作', dataIndex: 'remark' },
  { title: '操作', dataIndex: 'createdAt' },
  { title: '操作', dataIndex: 'action', width: 260 },
];
const table = useTable<RoleItem, Record<string, unknown>>(queryRoles as any, {});
const modalOpen = ref(false);
const permOpen = ref(false);
const submitting = ref(false);
const current = ref<RoleItem>();
const menus = ref<MenuNode[]>([]);
const checkedKeys = ref<ID[]>([]);
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
    message.success('操作');
    modalOpen.value = false;
    table.refresh();
  } finally {
    submitting.value = false;
  }
}
async function remove(row: RoleItem) {
  await deleteRole(row.id);
  message.success('操作');
  table.refresh();
}
async function status(row: RoleItem) {
  await updateRoleStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED');
  message.success('操作');
  table.refresh();
}
async function openPerm(row: RoleItem) {
  current.value = row;
  menus.value = await getMenuTree();
  const p = await getRolePermissions(row.id);
  checkedKeys.value = [...p.menuIds, ...p.permissionCodes];
  permOpen.value = true;
}
async function savePerm() {
  if (!current.value) return;
  submitting.value = true;
  try {
    await saveRolePermissions(current.value.id, {
      menuIds: checkedKeys.value.filter((k) => typeof k !== 'string'),
      permissionCodes: checkedKeys.value.filter((k) => typeof k === 'string') as string[],
    });
    message.success('操作');
    permOpen.value = false;
  } finally {
    submitting.value = false;
  }
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
      <PermissionButton permission="system:role:create" type="primary" @click="openCreate"
        >操作</PermissionButton
      ><a-button @click="table.refresh">操作</a-button>
    </div>
    <DataTable
      :columns="columns"
      :data-source="table.dataSource.value"
      :loading="table.loading.value"
      :pagination="table.pagination.value"
      @change="table.handleTableChange"
      ><template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'status'"
          ><StatusTag :status="record.status" /></template
        ><template v-else-if="column.dataIndex === 'action'"
          ><a-space
            ><a-button type="link" @click="openEdit(record)">操作</a-button
            ><a-button type="link" @click="openPerm(record)">操作</a-button
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
    ><FormModal
      v-model:open="modalOpen"
      :title="current ? '操作' : '操作'"
      :submitting="submitting"
      @submit="submit"
      ><a-form :model="form" layout="vertical"
        ><a-form-item label="操作" required><a-input v-model:value="form.roleName" /></a-form-item
        ><a-form-item label="操作" required><a-input v-model:value="form.roleCode" /></a-form-item
        ><a-form-item label="操作"
          ><a-select
            v-model:value="form.status"
            :options="[
              { label: '操作', value: 'ENABLED' },
              { label: '操作', value: 'DISABLED' },
            ]" /></a-form-item
        ><a-form-item label="操作"
          ><a-textarea v-model:value="form.remark" /></a-form-item></a-form></FormModal
    ><FormModal
      v-model:open="permOpen"
      title="操作"
      :submitting="submitting"
      :width="760"
      @submit="savePerm"
      ><a-tree
        v-model:checked-keys="checkedKeys"
        checkable
        check-strictly
        :tree-data="menus"
        :field-names="{ title: 'name', key: 'id', children: 'children' }" /></FormModal
  ></PageContainer>
</template>
