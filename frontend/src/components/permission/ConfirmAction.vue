<script setup lang="ts">
defineProps<{ title: string; okText?: string; danger?: boolean; disabled?: boolean; block?: boolean }>();
const emit = defineEmits<{ confirm: [] }>();
</script>
<template>
  <a-popconfirm
    v-if="!disabled"
    :title="title"
    :ok-text="okText"
    @confirm="emit('confirm')"
  >
    <a-button :block="block" :danger="danger" :disabled="disabled" type="link"><slot /></a-button>
  </a-popconfirm>
  <a-button v-else :block="block" :danger="danger" disabled type="link"><slot /></a-button>
</template>
