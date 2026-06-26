<script setup lang="ts">
defineProps<{ open: boolean; title: string; submitting?: boolean; width?: number }>();
const emit = defineEmits<{ 'update:open': [value: boolean]; submit: [] }>();
</script>
<template>
  <a-modal
    :open="open"
    :title="title"
    :confirm-loading="submitting"
    :width="width || 640"
    destroy-on-close
    @ok="emit('submit')"
    @cancel="emit('update:open', false)"
  >
    <slot />
  </a-modal>
</template>
