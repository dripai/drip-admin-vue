<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
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
async function loadTypes() {
  types.value = await queryDictTypes();
  if (!currentType.value && types.value[0]) selectType(types.value[0]);
}
async function selectType(row: DictTypeItem) {
  currentType.value = row;
  items.value = await queryDictItems(row.dictCode);
}
function addType() {
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
    message.success('操作');
    typeOpen.value = false;
    loadTypes();
  } finally {
    submitting.value = false;
  }
}
async function removeType() {
  if (!currentType.value) return;
  await deleteDictType(currentType.value.id);
  message.success('操作');
  currentType.value = undefined;
  loadTypes();
}
function addItem() {
  Object.assign(itemForm, {
    typeCode: currentType.value?.dictCode,
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
    message.success('操作');
    itemOpen.value = false;
    if (currentType.value) selectType(currentType.value);
  } finally {
    submitting.value = false;
  }
}
async function removeItem(row: DictItem) {
  await deleteDictItem(row.id);
  message.success('操作');
  if (currentType.value) selectType(currentType.value);
}
async function refresh() {
  await refreshDictCache(currentType.value?.dictCode);
  message.success('操作');
}
onMounted(loadTypes);
</script>
<template>
  <PageContainer title="操作"
    ><a-row :gutter="16"
      ><a-col :span="8"
        ><div class="page-actions">
          <a-button type="primary" @click="addType">操作</a-button
          ><a-button :disabled="!currentType" @click="editType">操作</a-button
          ><ConfirmAction title="操作" danger @confirm="removeType">操作</ConfirmAction>
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
        ><div class="page-actions">
          <a-button type="primary" :disabled="!currentType" @click="addItem">操作</a-button
          ><a-button :disabled="!currentType" @click="refresh">操作</a-button>
        </div>
        <a-table
          row-key="id"
          :data-source="items"
          :columns="[
            { title: '操作', dataIndex: 'label' },
            { title: '?', dataIndex: 'value' },
            { title: '操作', dataIndex: 'color' },
            { title: '操作', dataIndex: 'sort' },
            { title: '操作', dataIndex: 'status' },
            { title: '操作', dataIndex: 'action' },
          ]"
          :pagination="false"
          ><template #bodyCell="{ column, record }"
            ><template v-if="column.dataIndex === 'status'"
              ><StatusTag :status="record.status" /></template
            ><template v-else-if="column.dataIndex === 'action'"
              ><a-space
                ><a-button type="link" @click="editItem(record)">操作</a-button
                ><ConfirmAction title="操作" danger @confirm="removeItem(record)"
                  >操作</ConfirmAction
                ></a-space
              ></template
            ></template
          ></a-table
        ></a-col
      ></a-row
    ><FormModal v-model:open="typeOpen" title="操作" :submitting="submitting" @submit="saveType"
      ><a-form layout="vertical" :model="typeForm"
        ><a-form-item label="操作" required
          ><a-input v-model:value="typeForm.dictName" /></a-form-item
        ><a-form-item label="操作" required
          ><a-input v-model:value="typeForm.dictCode" /></a-form-item></a-form></FormModal
    ><FormModal v-model:open="itemOpen" title="操作" :submitting="submitting" @submit="saveItem"
      ><a-form layout="vertical" :model="itemForm"
        ><a-form-item label="操作" required><a-input v-model:value="itemForm.label" /></a-form-item
        ><a-form-item label="?" required><a-input v-model:value="itemForm.value" /></a-form-item
        ><a-form-item label="操作"
          ><a-input v-model:value="itemForm.color" /></a-form-item></a-form></FormModal
  ></PageContainer>
</template>
<style scoped>
.active {
  background: #eef4ff;
  cursor: pointer;
}
</style>
