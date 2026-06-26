<script setup lang="ts">
defineProps<{ importing?: boolean; exporting?: boolean }>();
const emit = defineEmits<{ downloadTemplate: []; importFile: [file: File]; exportData: [] }>();
</script>
<template>
  <a-space>
    <a-button @click="emit('downloadTemplate')">下载模板</a-button>
    <a-upload
      :show-upload-list="false"
      :before-upload="
        (file: File) => {
          emit('importFile', file);
          return false;
        }
      "
    >
      <a-button :loading="importing">导入</a-button>
    </a-upload>
    <a-button :loading="exporting" @click="emit('exportData')">导出</a-button>
  </a-space>
</template>
