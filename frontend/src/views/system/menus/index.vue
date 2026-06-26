<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
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
  { title: '操作', dataIndex: 'name' },
  { title: '操作', dataIndex: 'type' },
  { title: '操作', dataIndex: 'permissionCode' },
  { title: '操作', dataIndex: 'path' },
  { title: '操作', dataIndex: 'component' },
  { title: '操作', dataIndex: 'icon' },
  { title: '操作', dataIndex: 'sort' },
  { title: '操作', dataIndex: 'status' },
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
    message.success('操作');
    open.value = false;
    load();
  } finally {
    submitting.value = false;
  }
}
async function remove(row: MenuNode) {
  await deleteMenu(row.id);
  message.success('操作');
  load();
}
async function status(row: MenuNode) {
  await updateMenuStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED');
  message.success('操作');
  load();
}
onMounted(load);
</script>
<template>
  <PageContainer title="操作"
    ><div class="page-actions">
      <a-button type="primary" @click="openCreate('DIRECTORY')">操作</a-button
      ><a-button @click="openCreate('MENU')">操作</a-button
      ><a-button @click="openCreate('BUTTON')">操作</a-button
      ><a-button @click="load">操作</a-button>
    </div>
    <a-table
      row-key="id"
      :columns="columns"
      :data-source="data"
      :loading="loading"
      :pagination="false"
      ><template #bodyCell="{ column, record }"
        ><template v-if="column.dataIndex === 'type'"
          ><a-tag>{{ record.type }}</a-tag></template
        ><template v-else-if="column.dataIndex === 'icon'"
          ><IconRenderer :icon="record.icon" /></template
        ><template v-else-if="column.dataIndex === 'status'"
          ><StatusTag :status="record.status" /></template
        ><template v-else-if="column.dataIndex === 'action'"
          ><a-space
            ><a-button type="link" @click="openEdit(record)">操作</a-button
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
        ><a-form-item label="操作" required><a-input v-model:value="form.name" /></a-form-item
        ><a-form-item label="操作" required
          ><a-select
            v-model:value="form.type"
            :options="[
              { label: '操作', value: 'DIRECTORY' },
              { label: '操作', value: 'MENU' },
              { label: '操作', value: 'BUTTON' },
            ]" /></a-form-item
        ><a-form-item v-if="form.type !== 'BUTTON'" label="操作"
          ><IconSelect v-model:value="form.icon" /></a-form-item
        ><a-form-item v-if="form.type === 'MENU'" label="操作" required
          ><a-input v-model:value="form.path" /></a-form-item
        ><a-form-item v-if="form.type === 'MENU'" label="操作" required
          ><a-input v-model:value="form.component" placeholder="system/users/index" /></a-form-item
        ><a-form-item v-if="form.type === 'BUTTON'" label="操作" required
          ><a-input v-model:value="form.permissionCode" /></a-form-item
        ><a-form-item label="操作"
          ><a-input-number
            v-model:value="form.sort"
            style="width: 100%" /></a-form-item></a-form></FormModal
  ></PageContainer>
</template>
