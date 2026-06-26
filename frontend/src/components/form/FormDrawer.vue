<script setup lang="ts">
defineProps<{ open: boolean; title: string; submitting?: boolean; width?: number }>();
const emit = defineEmits<{ 'update:open': [value: boolean]; submit: [] }>();
</script>

<template>
  <a-drawer
    :open="open"
    :title="title"
    :width="width || 640"
    destroy-on-close
    @close="emit('update:open', false)"
  >
    <slot />
    <template #extra>
      <a-space>
        <a-button @click="emit('update:open', false)">取消</a-button>
        <a-button type="primary" :loading="submitting" @click="emit('submit')">提交</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>
