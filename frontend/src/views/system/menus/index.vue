<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
import DataTable from '@/components/table/DataTable.vue';
import FormModal from '@/components/form/FormModal.vue';
import ConfirmAction from '@/components/permission/ConfirmAction.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import IconSelect from '@/components/icons/IconSelect.vue';
import IconRenderer from '@/components/icons/IconRenderer.vue';
import {
  createMenu,
  deleteMenu,
  getMenuTree,
  updateMenu,
  updateMenuStatus,
} from '@/api/system/menu';
import type { ID } from '@/types/api';
import type { MenuNode, MenuType } from '@/types/system';

interface ParentOption {
  title: string;
  value: ID;
  disabled?: boolean;
  children?: ParentOption[];
}

const data = ref<MenuNode[]>([]);
const loading = ref(false);
const open = ref(false);
const submitting = ref(false);
const current = ref<MenuNode>();
const form = reactive<Partial<MenuNode>>({
  parentId: undefined,
  name: '',
  type: 'MENU',
  path: '',
  component: '',
  permissionCode: '',
  icon: '',
  sort: 0,
  visible: true,
  status: 'ENABLED',
});
const typeOptions = [
  { label: '目录', value: 'DIRECTORY' },
  { label: '菜单', value: 'MENU' },
  { label: '按钮', value: 'BUTTON' },
];
const statusOptions = [
  { label: '启用', value: 'ENABLED' },
  { label: '禁用', value: 'DISABLED' },
];
const columns = [
  { title: '名称', dataIndex: 'name', width: 180 },
  { title: '类型', dataIndex: 'type', width: 90 },
  { title: '权限标识', dataIndex: 'permissionCode', width: 180 },
  { title: '路由路径', dataIndex: 'path', width: 180 },
  { title: '组件路径', dataIndex: 'component', width: 180 },
  { title: '图标', dataIndex: 'icon', width: 80 },
  { title: '显示', dataIndex: 'visible', width: 80 },
  { title: '排序', dataIndex: 'sort', width: 80 },
  { title: '状态', dataIndex: 'status', width: 90 },
  { title: '操作', dataIndex: 'action', width: 170 },
];
const parentTreeData = computed(() => buildParentOptions(data.value));
const expandedRowKeys = computed(() => flattenMenuIds(data.value));

async function load() {
  loading.value = true;
  try {
    data.value = await getMenuTree();
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  current.value = undefined;
  Object.assign(form, {
    parentId: undefined,
    name: '',
    type: 'MENU',
    path: '',
    component: '',
    permissionCode: '',
    icon: '',
    sort: 0,
    visible: true,
    status: 'ENABLED',
  });
  open.value = true;
}

function openEdit(row: MenuNode) {
  current.value = row;
  Object.assign(form, {
    parentId: row.parentId || undefined,
    name: row.name,
    type: row.type,
    path: row.path || '',
    component: row.component || '',
    permissionCode: row.permissionCode || '',
    icon: row.icon || '',
    sort: row.sort,
    visible: row.visible !== false,
    status: row.status || 'ENABLED',
  });
  open.value = true;
}

function handleTypeChange(value: MenuType) {
  form.type = value;
  if (form.parentId && !isCompatibleParent(findMenuById(data.value, form.parentId))) {
    form.parentId = undefined;
  }
}

function menuPayload() {
  const type = form.type || 'MENU';
  return {
    parentId: form.parentId || 0,
    name: form.name,
    type,
    path: type === 'BUTTON' ? '' : form.path,
    component: type === 'MENU' ? form.component : '',
    permissionCode: form.permissionCode,
    icon: type === 'BUTTON' ? '' : form.icon,
    sort: form.sort ?? 0,
    visible: type === 'BUTTON' ? false : form.visible !== false,
    status: form.status || 'ENABLED',
  };
}

async function submit() {
  submitting.value = true;
  try {
    if (current.value) await updateMenu(current.value.id, menuPayload());
    else await createMenu(menuPayload());
    message.success('操作成功');
    open.value = false;
    load();
  } finally {
    submitting.value = false;
  }
}

async function remove(row: MenuNode) {
  await deleteMenu(row.id);
  message.success('操作成功');
  load();
}

async function status(row: MenuNode) {
  await updateMenuStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED');
  message.success('操作成功');
  load();
}

function typeLabel(type: MenuType) {
  return typeOptions.find((item) => item.value === type)?.label || type;
}

function buildParentOptions(nodes: MenuNode[]): ParentOption[] {
  return nodes.map((item) => {
    const disabled = isCurrentOrDescendant(item) || !isCompatibleParent(item);
    return {
      title: item.name,
      value: item.id,
      disabled,
      children: item.children?.length ? buildParentOptions(item.children) : undefined,
    };
  });
}

function isCompatibleParent(item?: MenuNode) {
  if (!item) return false;
  if (item.type === 'BUTTON') return false;
  if (form.type === 'BUTTON') return item.type === 'MENU';
  if (form.type === 'MENU') return item.type === 'DIRECTORY';
  return item.type === 'DIRECTORY';
}

function isCurrentOrDescendant(item: MenuNode): boolean {
  if (!current.value) return false;
  if (String(item.id) === String(current.value.id)) return true;
  return Boolean(item.children?.some(isCurrentOrDescendant));
}

function findMenuById(nodes: MenuNode[], id: ID): MenuNode | undefined {
  for (const item of nodes) {
    if (String(item.id) === String(id)) return item;
    const child = item.children?.length ? findMenuById(item.children, id) : undefined;
    if (child) return child;
  }
  return undefined;
}

function flattenMenuIds(nodes: MenuNode[]): ID[] {
  return nodes.flatMap((item) => [
    item.id,
    ...(item.children?.length ? flattenMenuIds(item.children) : []),
  ]);
}

onMounted(load);
</script>

<template>
  <PageContainer>
    <DataTable
      :columns="columns"
      :data-source="data"
      :loading="loading"
      :pagination="false"
      default-expand-all-rows
      :expanded-row-keys="expandedRowKeys"
      table-layout="fixed"
      table-key="system-menu"
      @refresh="load"
    >
      <template #toolbarLeft>
        <a-button type="primary" @click="openCreate">新增</a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'type'">
          <a-tag>{{ typeLabel(record.type) }}</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'icon'">
          <IconRenderer :icon="record.icon" />
        </template>
        <template v-else-if="column.dataIndex === 'visible'">
          <a-tag>{{ record.visible === false ? '否' : '是' }}</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'status'">
          <StatusTag :status="record.status" />
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <ConfirmAction
              :title="record.status === 'ENABLED' ? '禁用' : '启用'"
              @confirm="status(record)"
              >{{ record.status === 'ENABLED' ? '禁用' : '启用' }}</ConfirmAction
            >
            <ConfirmAction title="确认删除该菜单？" danger @confirm="remove(record)"
              >删除</ConfirmAction
            >
          </a-space>
        </template>
      </template>
    </DataTable>
    <FormModal
      v-model:open="open"
      :title="current ? '编辑' : '新增'"
      :submitting="submitting"
      @submit="submit"
    >
      <a-form :model="form" layout="vertical">
        <a-form-item label="父级菜单">
          <a-tree-select
            v-model:value="form.parentId"
            allow-clear
            tree-default-expand-all
            placeholder="不选择则为根级"
            :tree-data="parentTreeData"
          />
        </a-form-item>
        <a-form-item label="类型" required>
          <a-select :value="form.type" :options="typeOptions" @change="handleTypeChange" />
        </a-form-item>
        <a-form-item label="名称" required><a-input v-model:value="form.name" /></a-form-item>
        <a-form-item v-if="form.type !== 'BUTTON'" label="图标">
          <IconSelect v-model:value="form.icon" />
        </a-form-item>
        <a-form-item
          v-if="form.type !== 'BUTTON'"
          label="路由路径"
          :required="form.type === 'MENU'"
        >
          <a-input v-model:value="form.path" placeholder="/system/user" />
        </a-form-item>
        <a-form-item v-if="form.type === 'MENU'" label="组件路径" required>
          <a-input v-model:value="form.component" placeholder="system/user/index" />
        </a-form-item>
        <a-form-item label="权限标识" :required="form.type === 'BUTTON'">
          <a-input v-model:value="form.permissionCode" placeholder="system:user:create" />
        </a-form-item>
        <a-form-item v-if="form.type !== 'BUTTON'" label="是否显示">
          <a-switch v-model:checked="form.visible" checked-children="是" un-checked-children="否" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="form.sort" style="width: 100%" />
        </a-form-item>
      </a-form>
    </FormModal>
  </PageContainer>
</template>
