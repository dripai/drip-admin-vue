<template>
  <view class="feature-grid">
    <button
      v-for="entry in entries"
      :key="entry.title"
      class="feature-grid__item"
      type="button"
      @click="handleNavigate(entry.path)"
    >
      <van-icon class="feature-grid__icon" :name="entry.icon" />
      <text class="feature-grid__title">{{ entry.title }}</text>
    </button>
  </view>
</template>

<script setup lang="ts">
import type { MobileFeatureEntry } from '@/types/feature'

defineProps<{
  entries: MobileFeatureEntry[]
}>()

function handleNavigate(path: string) {
  uni.navigateTo({
    url: path
  })
}
</script>

<style scoped lang="scss">
.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1px;
  overflow: hidden;
  background: #e5e7eb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.feature-grid__item {
  display: flex;
  min-width: 0;
  height: 108px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px 6px;
  color: #1f2933;
  background: #ffffff;
  border: 0;
  border-radius: 0;
  line-height: 1;
}

.feature-grid__item::after {
  border: 0;
}

.feature-grid__icon {
  color: #1677ff;
  font-size: 28px;
}

.feature-grid__title {
  max-width: 100%;
  overflow: hidden;
  font-size: 14px;
  line-height: 20px;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
