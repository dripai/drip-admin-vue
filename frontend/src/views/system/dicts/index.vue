<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
import DataTable from '@/components/table/DataTable.vue';
import FormModal from '@/components/form/FormModal.vue';
import ConfirmAction from '@/components/permission/ConfirmAction.vue';
import StatusTag from '@/components/status/StatusTag.vue';
import {
  createDictItem,
  createDictType,
  deleteDictItem,
  deleteDictType,
  queryDictItems,
  queryDictTypes,
  refreshDictCache,
  updateDictItem,
  updateDictType,
} from '@/api/system/dict';
import type { DictItem, DictTypeItem } from '@/types/system';
const types = ref<DictTypeItem[]>([]);
const items = ref<DictItem[]>([]);
const currentType = ref<DictTypeItem>();
const typeOpen = ref(false);
const itemOpen = ref(false);
const currentItem = ref<DictItem>();
const submitting = ref(false);
const typeForm = reactive<Partial<DictTypeItem>>({ dictName: '', dictCode: '', status: 'ENABLED' });
const itemForm = reactive<Partial<DictItem>>({
  label: '',
  value: '',
  color: '',
  sort: 0,
  status: 'ENABLED',
});
const itemColumns = [
  { title: '标签', dataIndex: 'label' },
  { title: '字典值', dataIndex: 'value' },
  { title: '颜色', dataIndex: 'color' },
  { title: '排序', dataIndex: 'sort' },
  { title: '状态', dataIndex: 'status' },
  { title: '操作', dataIndex: 'action' },
];
async function loadTypes() {
  types.value = await queryDictTypes();
  const selected = types.value.find((item) => currentType.value && item.id === currentType.value.id);
  if (selected) selectType(selected);
  else if (types.value[0]) selectType(types.value[0]);
  else {
    currentType.value = undefined;
    items.value = [];
  }
}
async function selectType(row: DictTypeItem) {
  currentType.value = row;
  items.value = await queryDictItems(row.id);
}
function addType() {
  delete typeForm.id;
  Object.assign(typeForm, { dictName: '', dictCode: '', status: 'ENABLED' });
  typeOpen.value = true;
}
function editType() {
  if (!currentType.value) return;
  Object.assign(typeForm, currentType.value);
  typeOpen.value = true;
}
async function saveType() {
  submitting.value = true;
  try {
    if (typeForm.id) await updateDictType(typeForm.id, typeForm);
    else await createDictType(typeForm);
    message.success('操作成功');
    typeOpen.value = false;
    loadTypes();
  } finally {
    submitting.value = false;
  }
}
async function removeType() {
  if (!currentType.value) return;
  await deleteDictType(currentType.value.id);
  message.success('操作成功');
  currentType.value = undefined;
  loadTypes();
}
function addItem() {
  currentItem.value = undefined;
  delete itemForm.id;
  Object.assign(itemForm, {
    dictTypeId: currentType.value?.id,
    label: '',
    value: '',
    color: '',
    sort: 0,
    status: 'ENABLED',
  });
  itemOpen.value = true;
}
function editItem(row: DictItem) {
  currentItem.value = row;
  Object.assign(itemForm, row);
  itemOpen.value = true;
}
async function saveItem() {
  submitting.value = true;
  try {
    if (currentItem.value) await updateDictItem(currentItem.value.id, itemForm);
    else await createDictItem(itemForm);
    message.success('操作成功');
    itemOpen.value = false;
    if (currentType.value) selectType(currentType.value);
  } finally {
    submitting.value = false;
  }
}
async function removeItem(row: DictItem) {
  await deleteDictItem(row.id);
  message.success('操作成功');
  if (currentType.value) selectType(currentType.value);
}
async function refresh() {
  await refreshDictCache(currentType.value?.dictCode);
  message.success('操作成功');
}
async function reloadItems() {
  if (!currentType.value) return;
  items.value = await queryDictItems(currentType.value.id);
}
onMounted(loadTypes);
</script>
<template>
  <PageContainer
    ><a-row :gutter="16"
      ><a-col :span="8"
        ><div class="page-actions">
          <a-button type="primary" @click="addType">新增字典类型</a-button
          ><a-button :disabled="!currentType" @click="editType">编辑字典类型</a-button
          ><ConfirmAction title="删除字典类型" danger @confirm="removeType">删除</ConfirmAction>
        </div>
        <a-list bordered :data-source="types"
          ><template #renderItem="{ item }"
            ><a-list-item :class="{ active: currentType?.id === item.id }" @click="selectType(item)"
              >{{ item.dictName
              }}<span class="text-muted"> / {{ item.dictCode }}</span></a-list-item
            ></template
          ></a-list
        ></a-col
      ><a-col :span="16"
        ><DataTable
          :data-source="items"
          :columns="itemColumns"
          :pagination="false"
          table-key="system-dict-item"
          @refresh="reloadItems"
          ><template #toolbarLeft>
            <a-space>
              <a-button type="primary" :disabled="!currentType" @click="addItem">新增</a-button
              ><a-button :disabled="!currentType" @click="refresh">刷新缓存</a-button>
            </a-space>
          </template>
          <template #bodyCell="{ column, record }"
            ><template v-if="column.dataIndex === 'status'"
              ><StatusTag :status="record.status" /></template
            ><template v-else-if="column.dataIndex === 'action'"
              ><a-space
                ><a-button type="link" @click="editItem(record)">编辑</a-button
                ><ConfirmAction title="删除字典项？" danger @confirm="removeItem(record)"
                  >删除</ConfirmAction
                ></a-space
              ></template
            ></template
          ></DataTable
        ></a-col
      ></a-row
    ><FormModal
      v-model:open="typeOpen"
      :title="typeForm.id ? '编辑字典类型' : '新增字典类型'"
      :submitting="submitting"
      @submit="saveType"
      ><a-form layout="vertical" :model="typeForm"
        ><a-form-item label="字典名称" required
          ><a-input v-model:value="typeForm.dictName" /></a-form-item
        ><a-form-item label="字典编码" required
          ><a-input v-model:value="typeForm.dictCode" /></a-form-item></a-form></FormModal
    ><FormModal
      v-model:open="itemOpen"
      :title="currentItem ? '编辑字典项' : '新增字典项'"
      :submitting="submitting"
      @submit="saveItem"
      ><a-form layout="vertical" :model="itemForm"
        ><a-form-item label="字典标签" required
          ><a-input v-model:value="itemForm.label" /></a-form-item
        ><a-form-item label="字典值" required
          ><a-input v-model:value="itemForm.value" /></a-form-item
        ><a-form-item label="颜色"
          ><a-input v-model:value="itemForm.color" /></a-form-item></a-form></FormModal
  ></PageContainer>
</template>
<style scoped>
.active {
  background: #eef4ff;
  cursor: pointer;
}
</style>
