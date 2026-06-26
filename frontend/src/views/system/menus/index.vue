<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
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
import type { MenuNode } from '@/types/system';
const data = ref<MenuNode[]>([]);
const loading = ref(false);
const open = ref(false);
const submitting = ref(false);
const current = ref<MenuNode>();
const form = reactive<Partial<MenuNode>>({
  name: '',
  type: 'MENU',
  path: '',
  component: '',
  permissionCode: '',
  icon: '',
  sort: 0,
  status: 'ENABLED',
});
const columns = [
  { title: '名称', dataIndex: 'name' },
  { title: '类型', dataIndex: 'type' },
  { title: '权限码', dataIndex: 'permissionCode' },
  { title: '请求路径', dataIndex: 'path' },
  { title: '组件路径', dataIndex: 'component' },
  { title: '图标', dataIndex: 'icon' },
  { title: '排序', dataIndex: 'sort' },
  { title: '状态', dataIndex: 'status' },
  { title: '操作', dataIndex: 'action', width: 220 },
];
async function load() {
  loading.value = true;
  try {
    data.value = await getMenuTree();
  } finally {
    loading.value = false;
  }
}
function openCreate(type = 'MENU') {
  current.value = undefined;
  Object.assign(form, {
    name: '',
    type,
    path: '',
    component: '',
    permissionCode: '',
    icon: '',
    sort: 0,
    status: 'ENABLED',
  });
  open.value = true;
}
function openEdit(row: MenuNode) {
  current.value = row;
  Object.assign(form, row);
  open.value = true;
}
async function submit() {
  submitting.value = true;
  try {
    if (current.value) await updateMenu(current.value.id, form);
    else await createMenu(form);
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
onMounted(load);
</script>
<template>
  <PageContainer title="菜单权限"
    ><DataTable
      :columns="columns"
      :data-source="data"
      :loading="loading"
      :pagination="false"
      table-key="system-menu"
      @refresh="load"
      ><template #toolbarLeft>
        <a-space>
          <a-button type="primary" @click="openCreate('DIRECTORY')">新增目录</a-button
          ><a-button @click="openCreate('MENU')">新增菜单</a-button
          ><a-button @click="openCreate('BUTTON')">新增按钮</a-button>
        </a-space>
      </template>
      <template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'type'"
          ><a-tag>{{ record.type }}</a-tag></template
        ><template v-else-if="column.dataIndex === 'icon'"
          ><IconRenderer :icon="record.icon" /></template
        ><template v-else-if="column.dataIndex === 'status'"
          ><StatusTag :status="record.status" /></template
        ><template v-else-if="column.dataIndex === 'action'"
          ><a-space
            ><a-button type="link" @click="openEdit(record)">编辑</a-button
            ><ConfirmAction
              :title="record.status === 'ENABLED' ? '禁用' : '启用'"
              @confirm="status(record)"
              >{{ record.status === 'ENABLED' ? '禁用' : '启用' }}</ConfirmAction
            ><ConfirmAction title="确认删除该菜单？" danger @confirm="remove(record)"
              >删除</ConfirmAction
            ></a-space
          ></template
        ></template
      ></DataTable
    ><FormModal
      v-model:open="open"
      :title="current ? '编辑菜单' : '新增菜单'"
      :submitting="submitting"
      @submit="submit"
      ><a-form :model="form" layout="vertical"
        ><a-form-item label="名称" required><a-input v-model:value="form.name" /></a-form-item
        ><a-form-item label="类型" required
          ><a-select
            v-model:value="form.type"
            :options="[
              { label: '目录', value: 'DIRECTORY' },
              { label: '菜单', value: 'MENU' },
              { label: '按钮', value: 'BUTTON' },
            ]" /></a-form-item
        ><a-form-item v-if="form.type !== 'BUTTON'" label="图标"
          ><IconSelect v-model:value="form.icon" /></a-form-item
        ><a-form-item v-if="form.type === 'MENU'" label="路由路径" required
          ><a-input v-model:value="form.path" /></a-form-item
        ><a-form-item v-if="form.type === 'MENU'" label="组件路径" required
          ><a-input v-model:value="form.component" placeholder="system/user/index" /></a-form-item
        ><a-form-item v-if="form.type === 'BUTTON'" label="权限标识" required
          ><a-input v-model:value="form.permissionCode" /></a-form-item
        ><a-form-item label="排序"
          ><a-input-number
            v-model:value="form.sort"
            style="width: 100%" /></a-form-item></a-form></FormModal
  ></PageContainer>
</template>
