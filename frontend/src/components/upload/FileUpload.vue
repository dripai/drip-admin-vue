<script setup lang="ts">
import { message, type UploadProps } from 'ant-design-vue';
const props = defineProps<{
  action: string;
  accept?: string;
  maxCount?: number;
  maxSizeMb?: number;
  fileList?: UploadProps['fileList'];
}>();
const emit = defineEmits<{ 'update:fileList': [value: UploadProps['fileList']] }>();
function beforeUpload(file: File) {
  if (props.maxSizeMb && file.size / 1024 / 1024 > props.maxSizeMb) {
    message.error(`文件大小不能超过 ${props.maxSizeMb}MB`);
    return false;
  }
  return true;
}
</script>
<template>
  <a-upload
    :action="action"
    :accept="accept"
    :max-count="maxCount"
    :file-list="fileList"
    :before-upload="beforeUpload"
    @change="emit('update:fileList', $event.fileList)"
  >
    <a-button>上传文件</a-button>
  </a-upload>
</template>
