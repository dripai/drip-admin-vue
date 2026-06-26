<script setup lang="ts">
defineProps<{ title: string; okText?: string; danger?: boolean }>();
const emit = defineEmits<{ confirm: [] }>();
</script>
<template>
  <a-popconfirm
    :title="title"
    :ok-text="okText || '确认'"
    cancel-text="取消"
    @confirm="emit('confirm')"
  >
    <a-button :danger="danger" type="link"><slot /></a-button>
  </a-popconfirm>
</template>
