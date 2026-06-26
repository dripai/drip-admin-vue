<template>
  <view class="empty-state">
    <van-empty :description="description" />
  </view>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    description?: string
  }>(),
  {
    description: '暂无可用功能'
  }
)
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  min-height: 260px;
  align-items: center;
  justify-content: center;
}
</style>
