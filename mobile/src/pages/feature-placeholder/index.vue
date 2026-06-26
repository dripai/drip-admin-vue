<template>
  <view class="placeholder-page">
    <van-nav-bar title="功能建设中" left-arrow @click-left="goBack" />
    <view class="placeholder-page__body">
      <van-empty :description="description" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAuthGuard } from '@/composables/useAuthGuard'

const title = ref('')
const description = computed(() => (title.value ? `${title.value}功能建设中` : '功能建设中'))

onLoad((query) => {
  const { ensureAuthenticated } = useAuthGuard()

  if (!ensureAuthenticated()) {
    return
  }

  title.value = typeof query?.title === 'string' ? decodeURIComponent(query.title) : ''
})

function goBack() {
  uni.navigateBack({
    fail() {
      uni.reLaunch({ url: '/pages/home/index' })
    }
  })
}
</script>

<style scoped lang="scss">
.placeholder-page {
  min-height: 100vh;
  background: #f6f7f9;
}

.placeholder-page__body {
  display: flex;
  min-height: calc(100vh - 46px);
  align-items: center;
  justify-content: center;
}
</style>
