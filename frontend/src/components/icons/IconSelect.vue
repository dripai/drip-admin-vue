<script setup lang="ts">
import { computed, ref } from 'vue';
import IconRenderer from './IconRenderer.vue';
import { iconOptions } from './iconMap';
const props = defineProps<{ value?: string }>();
const emit = defineEmits<{ 'update:value': [value?: string] }>();
const keyword = ref('');
const open = ref(false);
const filtered = computed(() =>
  iconOptions.filter((item) => item.label.toLowerCase().includes(keyword.value.toLowerCase())),
);

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
        <div class="icon-grid">
          <button
            v-for="item in filtered"
            :key="item.value"
            type="button"
            class="icon-cell"
            :class="{ selected: item.value === props.value }"
            :title="item.label"
            @click="selectIcon(item.value)"
          >
            <IconRenderer :icon="item.value" />
          </button>
        </div>
        <a-empty v-if="!filtered.length" description="暂无图标" />
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
  width: 376px;
}

.icon-search {
  margin-bottom: 10px;
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
</style>
