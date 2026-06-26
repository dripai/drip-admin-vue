<script setup lang="ts">
defineProps<{ importing?: boolean; exporting?: boolean }>();
const emit = defineEmits<{ downloadTemplate: []; importFile: [file: File]; exportData: [] }>();
</script>
<template>
  <a-space>
    <a-button @click="emit('downloadTemplate')">操作</a-button>
    <a-upload
      :show-upload-list="false"
      :before-upload="
        (file: File) => {
          emit('importFile', file);
          return false;
        }
      "
      ><a-button :loading="importing">操作</a-button></a-upload
    >
    <a-button :loading="exporting" @click="emit('exportData')">操作</a-button>
  </a-space>
</template>
