<script setup lang="ts">
import type { Status } from '@/types/api';
defineProps<{ value: Status; loading?: boolean }>();
const emit = defineEmits<{ change: [value: Status] }>();
</script>
<template>
  <a-switch
    :checked="value === 'ENABLED'"
    :loading="loading"
    checked-children="操作"
    un-checked-children="操作"
    @change="emit('change', $event ? 'ENABLED' : 'DISABLED')"
  />
</template>
