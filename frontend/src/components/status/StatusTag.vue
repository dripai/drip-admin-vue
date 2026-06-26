<script setup lang="ts">
import type { Status } from '@/types/api';
defineProps<{ status?: Status }>();
</script>
<template>
  <a-tag :color="status === 'ENABLED' ? 'green' : 'default'">{{
    status === 'ENABLED' ? '启用' : '禁用'
  }}</a-tag>
</template>
