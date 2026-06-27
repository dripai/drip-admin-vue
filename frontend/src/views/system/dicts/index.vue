<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import {
  EditOutlined,
  PlusOutlined,
  ReloadOutlined,
} from '@ant-design/icons-vue';
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
  isDefault: 0,
  sort: 0,
  status: 'ENABLED',
  builtin: 0,
});
const itemColumns: TableColumnType[] = [
  { title: '标签', dataIndex: 'label' },
  { title: '字典值', dataIndex: 'value' },
  { title: '默认值', dataIndex: 'isDefault' },
  { title: '排序', dataIndex: 'sort' },
  { title: '状态', dataIndex: 'status' },
  { title: '操作', dataIndex: 'action', width: 120 },
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
  Object.assign(typeForm, { dictName: '', dictCode: '', status: 'ENABLED', builtin: 0 });
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
  if (currentType.value.builtin === 1) {
    message.warning('内置字典类型不能删除');
    return;
  }
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
    isDefault: 0,
    sort: 0,
    status: 'ENABLED',
    builtin: 0,
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
  if (row.builtin === 1) {
    message.warning('内置字典项不能删除');
    return;
  }
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
  <PageContainer>
    <div class="dict-layout">
      <aside class="dict-types">
        <div class="panel-header">
          <div class="panel-title">字典类型</div>
          <a-space :size="8">
            <a-button type="primary" @click="addType"><PlusOutlined />新增</a-button>
            <a-button :disabled="!currentType" @click="editType"><EditOutlined />编辑</a-button>
            <ConfirmAction
              title="删除字典类型"
              danger
              :disabled="!currentType || currentType.builtin === 1"
              @confirm="removeType">
              删除
            </ConfirmAction>
          </a-space>
        </div>
        <div class="type-list">
          <button
            v-for="item in types"
            :key="item.id"
            class="type-item"
            :class="{ active: currentType?.id === item.id }"
            type="button"
            @click="selectType(item)">
            <span class="type-main">
              <span class="type-name">{{ item.dictName }}</span>
              <a-tag v-if="item.builtin === 1" class="builtin-tag">内置</a-tag>
            </span>
            <span class="type-code">{{ item.dictCode }}</span>
          </button>
          <a-empty v-if="types.length === 0" :image="undefined" description="暂无字典类型" />
        </div>
      </aside>
      <section class="dict-items">
        <div class="panel-header item-header">
          <div class="panel-title">{{ currentType?.dictName || '字典项' }}</div>
          <a-space>
            <a-button :disabled="!currentType" @click="refresh"><ReloadOutlined />刷新缓存</a-button>
            <a-button type="primary" :disabled="!currentType" @click="addItem"
              ><PlusOutlined />新增</a-button
            >
          </a-space>
        </div>
        <DataTable
          :data-source="items"
          :columns="itemColumns"
          :pagination="false"
          table-key="system-dict-item"
          @refresh="reloadItems">
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'isDefault'">
              <a-tag v-if="record.isDefault === 1" color="blue">是</a-tag>
              <span v-else>-</span>
            </template>
            <template v-else-if="column.dataIndex === 'status'">
              <StatusTag :status="record.status" />
            </template>
            <template v-else-if="column.dataIndex === 'action'">
              <a-space>
                <a-button type="link" @click="editItem(record)">编辑</a-button>
                <ConfirmAction
                  title="删除字典项？"
                  danger
                  :disabled="record.builtin === 1"
                  @confirm="removeItem(record)"
                  >删除</ConfirmAction
                >
              </a-space>
            </template>
          </template>
        </DataTable>
      </section>
    </div>
    <FormModal
      v-model:open="typeOpen"
      :title="typeForm.id ? '编辑字典类型' : '新增字典类型'"
      :submitting="submitting"
      @submit="saveType">
      <a-form layout="vertical" :model="typeForm">
        <a-form-item label="字典名称" required>
          <a-input v-model:value="typeForm.dictName" />
        </a-form-item>
        <a-form-item label="字典编码" required>
          <a-input v-model:value="typeForm.dictCode" />
        </a-form-item>
      </a-form>
    </FormModal>
    <FormModal
      v-model:open="itemOpen"
      :title="currentItem ? '编辑字典项' : '新增字典项'"
      :submitting="submitting"
      @submit="saveItem">
      <a-form layout="vertical" :model="itemForm">
        <a-form-item label="字典标签" required>
          <a-input v-model:value="itemForm.label" />
        </a-form-item>
        <a-form-item label="字典值" required>
          <a-input v-model:value="itemForm.value" />
        </a-form-item>
        <a-form-item label="是否默认值">
          <a-switch
            :checked="itemForm.isDefault === 1"
            checked-children="是"
            un-checked-children="否"
            @change="(checked: boolean) => (itemForm.isDefault = checked ? 1 : 0)" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="itemForm.sort" :min="0" class="form-number" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="itemForm.status">
            <a-select-option value="ENABLED">启用</a-select-option>
            <a-select-option value="DISABLED">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </FormModal>
  </PageContainer>
</template>
<style scoped lang="scss">
.dict-layout {
  display: grid;
  grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}
.dict-types,
.dict-items {
  min-width: 0;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
}
.dict-types {
  overflow: hidden;
}
.dict-items {
  padding: 14px 16px 16px;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.item-header {
  margin: -14px -16px 12px;
}
.panel-title {
  color: #111827;
  font-size: 15px;
  font-weight: 600;
  line-height: 22px;
}
.type-list {
  max-height: calc(100vh - 250px);
  min-height: 220px;
  overflow: auto;
  padding: 8px;
}
.type-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-height: 40px;
  padding: 6px 10px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: transparent;
  color: inherit;
  cursor: pointer;
  text-align: left;
}
.type-item:hover {
  background: #f8fafc;
}
.type-item.active {
  border-color: #91caff;
  background: #e6f4ff;
}
.type-main {
  display: inline-flex;
  align-items: center;
  min-width: 0;
  gap: 6px;
}
.type-name {
  overflow: hidden;
  color: #111827;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.type-code {
  overflow: hidden;
  max-width: 48%;
  color: #6b7280;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.builtin-tag {
  margin-inline-end: 0;
}
.form-number {
  width: 100%;
}
@media (max-width: 960px) {
  .dict-layout {
    grid-template-columns: 1fr;
  }
  .type-list {
    max-height: none;
  }
}
</style>
