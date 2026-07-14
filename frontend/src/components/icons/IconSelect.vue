<script setup lang="ts">
import { computed, ref } from 'vue';
import IconRenderer from './IconRenderer.vue';
import { iconOptions } from './iconMap';
import { antIconNames } from './iconCatalog';
const props = defineProps<{ value?: string }>();
const emit = defineEmits<{ 'update:value': [value?: string] }>();
const keyword = ref('');
const open = ref(false);
const iconOptionValueSet = new Set(iconOptions.map((item) => item.value));
const allIconOptions = [
  ...iconOptions,
  ...antIconNames
    .filter((name) => !iconOptionValueSet.has(name))
    .map((name) => ({ label: name, value: name })),
].sort((left, right) => left.value.localeCompare(right.value));
type IconOption = (typeof allIconOptions)[number];
type IconCategoryKey =
  | 'common'
  | 'system'
  | 'master'
  | 'business'
  | 'inventory'
  | 'finance'
  | 'report'
  | 'all';

const commonIconKeys = [
  'home',
  'dashboard',
  'settings',
  'menu',
  'user',
  'users',
  'role',
  'permission',
  'database',
  'master',
  'purchase',
  'shopping-cart',
  'supplier',
  'customer',
  'warehouse',
  'inventory',
  'transaction-type',
  'unit',
  'unit-conversion',
  'number',
  'finance',
  'report',
  'config',
  'history',
];
const commonIconKeySet = new Set(commonIconKeys);
const categories: Array<{ key: IconCategoryKey; label: string; icons: string[] }> = [
  { key: 'common', label: '常用', icons: commonIconKeys },
  {
    key: 'system',
    label: '系统',
    icons: [
      'home',
      'dashboard',
      'settings',
      'menu',
      'user',
      'users',
      'role',
      'permission',
      'security',
      'dept',
      'dict',
      'job',
      'history',
      'log',
      'monitor',
      'online',
      'backup',
      'key',
    ],
  },
  {
    key: 'master',
    label: '资料',
    icons: [
      'master',
      'database',
      'number',
      'supplier',
      'customer',
      'unit',
      'unit-conversion',
      'category',
      'goods',
      'product',
      'barcode',
      'qrcode',
      'tag',
      'site',
      'factory',
    ],
  },
  {
    key: 'business',
    label: '采购',
    icons: [
      'purchase',
      'shopping-cart',
      'cart',
      'shopping',
      'supplier',
      'customer',
      'order',
      'receipt',
      'return',
      'price',
      'sales',
      'shop',
      'delivery',
      'outbound',
      'inbound',
      'invoice',
    ],
  },
  {
    key: 'inventory',
    label: '库存',
    icons: [
      'warehouse',
      'inventory',
      'stock',
      'transaction',
      'transaction-type',
      'inbound',
      'outbound',
      'swap',
      'location',
      'barcode',
      'qrcode',
      'table',
      'sync',
    ],
  },
  {
    key: 'finance',
    label: '财务',
    icons: [
      'finance',
      'account',
      'payable',
      'receivable',
      'payment',
      'wallet',
      'dollar',
      'invoice',
      'bill',
      'calculator',
      'card',
      'bank',
    ],
  },
  {
    key: 'report',
    label: '报表',
    icons: [
      'report',
      'chart',
      'LineChartOutlined',
      'BarChartOutlined',
      'PieChartOutlined',
      'AreaChartOutlined',
      'FundOutlined',
      'StockOutlined',
      'RiseOutlined',
      'FallOutlined',
    ],
  },
  { key: 'all', label: '全部', icons: [] },
];
const activeCategory = ref<IconCategoryKey>('common');
const categoryOptions = computed(() => {
  if (activeCategory.value === 'all') return allIconOptions;
  const category = categories.find((item) => item.key === activeCategory.value);
  return (category?.icons || [])
    .map((key) => allIconOptions.find((item) => item.value === key))
    .filter((item): item is IconOption => Boolean(item));
});
const filtered = computed(() => {
  const text = keyword.value.trim().toLowerCase();
  const source = text ? allIconOptions : categoryOptions.value;
  return source.filter((item) => item.label.toLowerCase().includes(text));
});
function selectIcon(value: string) {
  emit('update:value', value);
  open.value = false;
  window.setTimeout(() => {
    open.value = false;
  }, 0);
}

function clearIcon() {
  emit('update:value', undefined);
}
</script>
<template>
  <a-popover
    :open="open"
    trigger="click"
    placement="bottomLeft"
    overlay-class-name="icon-select-popover"
    @open-change="open = $event"
  >
    <button type="button" class="icon-select-trigger">
      <span v-if="props.value" class="icon-select-current">
        <IconRenderer :icon="props.value" />
        <span class="icon-select-name">{{ props.value }}</span>
      </span>
      <span v-else class="icon-select-placeholder">请选择图标</span>
      <a-button
        v-if="props.value"
        type="text"
        size="small"
        class="icon-select-clear"
        @click.stop="clearIcon"
        >清空</a-button
      >
    </button>
    <template #content>
      <div class="icon-select-panel">
        <a-input
          :value="keyword"
          allow-clear
          placeholder="搜索图标"
          class="icon-search"
          @change="keyword = $event.target.value"
        />
        <div class="icon-body">
          <div class="icon-category-list">
            <button
              v-for="category in categories"
              :key="category.key"
              type="button"
              class="icon-category"
              :class="{ active: category.key === activeCategory }"
              @click="activeCategory = category.key"
            >
              {{ category.label }}
            </button>
          </div>
          <div class="icon-results">
            <div class="icon-grid-wrap">
              <div class="icon-grid">
                <button
                  v-for="item in filtered"
                  :key="item.value"
                  type="button"
                  class="icon-cell"
                  :class="{
                    selected: item.value === props.value,
                    common: commonIconKeySet.has(item.value),
                  }"
                  :title="item.label"
                  @click="selectIcon(item.value)"
                >
                  <IconRenderer :icon="item.value" />
                </button>
              </div>
              <a-empty v-if="!filtered.length" description="暂无图标" />
            </div>
          </div>
        </div>
      </div>
    </template>
  </a-popover>
</template>
<style scoped lang="scss">
.icon-select-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 32px;
  padding: 0 8px;
  cursor: pointer;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.icon-select-trigger:hover {
  border-color: #1677ff;
}

.icon-select-current {
  display: inline-flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
}

.icon-select-name {
  overflow: hidden;
  color: #1f2937;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.icon-select-placeholder {
  color: #9ca3af;
}

.icon-select-clear {
  flex: 0 0 auto;
  padding-inline: 4px;
}

.icon-select-panel {
  width: 448px;
}

.icon-search {
  margin-bottom: 8px;
}

.icon-body {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.icon-category-list {
  display: grid;
  flex: 0 0 52px;
  gap: 4px;
}

.icon-category {
  height: 30px;
  padding: 0;
  color: #595959;
  cursor: pointer;
  background: #fff;
  border: 1px solid transparent;
  border-radius: 4px;
}

.icon-category:hover {
  color: #1677ff;
  background: #f5f7fb;
}

.icon-category.active {
  color: #1677ff;
  font-weight: 600;
  background: #e6f4ff;
  border-color: #91caff;
}

.icon-results {
  flex: 1;
  min-width: 0;
}

.icon-grid-wrap {
  height: 360px;
  overflow-y: auto;
  padding-right: 2px;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(10, 32px);
  gap: 4px;
}

.icon-cell {
  display: inline-flex;
  width: 32px;
  height: 32px;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
}

.icon-cell:hover {
  color: #1677ff;
  border-color: #1677ff;
}

.icon-cell.selected {
  color: #1677ff;
  background: #e6f4ff;
  border-color: #1677ff;
}

.icon-cell.common:not(.selected) {
  background: #fafafa;
}
</style>
