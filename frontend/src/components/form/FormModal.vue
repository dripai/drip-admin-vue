<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue';

const props = withDefaults(
  defineProps<{
    open: boolean;
    title: string;
    submitting?: boolean;
    width?: number;
    formColumns?: 'auto' | 1 | 2;
    autoColumnThreshold?: number;
    twoColumnWidth?: number;
  }>(),
  {
    formColumns: 'auto',
    autoColumnThreshold: 7,
    twoColumnWidth: 860,
  },
);
const emit = defineEmits<{ 'update:open': [value: boolean]; submit: [] }>();
const contentRef = ref<HTMLElement>();
const twoColumn = ref(false);
let observer: MutationObserver | undefined;

const modalWidth = computed(() => props.width || (twoColumn.value ? props.twoColumnWidth : 640));

watch(
  () => props.open,
  async (open) => {
    if (!open) {
      twoColumn.value = false;
      observer?.disconnect();
      observer = undefined;
      return;
    }
    await nextTick();
    updateColumns();
    observer?.disconnect();
    if (contentRef.value) {
      observer = new MutationObserver(updateColumns);
      observer.observe(contentRef.value, { childList: true, subtree: true });
    }
  },
);

onBeforeUnmount(() => observer?.disconnect());

function updateColumns() {
  if (props.formColumns === 1) {
    twoColumn.value = false;
    return;
  }
  if (props.formColumns === 2) {
    twoColumn.value = true;
    return;
  }
  const itemCount = contentRef.value?.querySelectorAll('.ant-form-item').length || 0;
  twoColumn.value = itemCount >= props.autoColumnThreshold;
}
</script>
<template>
  <a-modal
    :open="open"
    :title="title"
    :confirm-loading="submitting"
    :width="modalWidth"
    destroy-on-close
    @ok="emit('submit')"
    @cancel="emit('update:open', false)"
  >
    <div ref="contentRef" class="form-modal-content" :class="{ 'is-two-column': twoColumn }">
      <slot />
    </div>
  </a-modal>
</template>
<style scoped lang="scss">
.form-modal-content.is-two-column :deep(.ant-form-vertical) {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 16px;
}

.form-modal-content :deep(.ant-form-vertical .ant-form-item) {
  min-width: 0;
}

.form-modal-content :deep(.ant-form-vertical .ant-form-item-row) {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  column-gap: 8px;
  align-items: start;
}

.form-modal-content :deep(.ant-form-vertical .ant-form-item-label) {
  padding: 0;
  overflow: visible;
  text-align: right;
  white-space: normal;
}

.form-modal-content :deep(.ant-form-vertical .ant-form-item-label > label) {
  min-height: 32px;
  line-height: 32px;
}

.form-modal-content :deep(.ant-form-vertical .ant-form-item-control) {
  min-width: 0;
}

@media (max-width: 768px) {
  .form-modal-content.is-two-column :deep(.ant-form-vertical) {
    grid-template-columns: 1fr;
  }

  .form-modal-content :deep(.ant-form-vertical .ant-form-item-row) {
    display: block;
  }

  .form-modal-content :deep(.ant-form-vertical .ant-form-item-label) {
    text-align: left;
  }
}
</style>
