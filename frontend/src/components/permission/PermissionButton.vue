<script setup lang="ts">
import { computed } from 'vue';
import { usePermissionStore } from '@/stores/permission';
const props = withDefaults(
  defineProps<{ permission?: string; disabledMode?: boolean; type?: string; danger?: boolean }>(),
  { type: 'default', permission: undefined },
);
const can = computed(() => usePermissionStore().can(props.permission));
</script>
<template>
  <a-button
    v-if="can || disabledMode"
    :type="type as any"
    :danger="danger"
    :disabled="disabledMode && !can"
    ><slot
  /></a-button>
</template>
