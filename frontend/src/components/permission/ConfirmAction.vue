<script setup lang="ts">
defineProps<{ title: string; okText?: string; danger?: boolean }>();
const emit = defineEmits<{ confirm: [] }>();
</script>
<template>
  <a-popconfirm
    :title="title"
    :ok-text="okText || '操作'"
    cancel-text="操作"
    @confirm="emit('confirm')"
  >
    <a-button :danger="danger" type="link"><slot /></a-button>
  </a-popconfirm>
</template>
