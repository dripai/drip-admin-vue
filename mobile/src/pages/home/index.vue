<template>
  <view class="home-page">
    <view class="home-header">
      <view class="home-header__main">
        <text class="home-header__label">当前用户</text>
        <text class="home-header__name">{{ displayName }}</text>
      </view>
      <van-button size="small" plain type="primary" :loading="logoutLoading" @click="confirmLogout">
        退出
      </van-button>
    </view>

    <FeatureGrid v-if="visibleEntries.length" :entries="visibleEntries" />
    <EmptyState v-else description="暂无可用功能" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { showConfirmDialog, showToast } from 'vant'
import EmptyState from '@/components/EmptyState/index.vue'
import FeatureGrid from '@/components/FeatureGrid/index.vue'
import { useAuthGuard } from '@/composables/useAuthGuard'
import { useAuthStore } from '@/stores/auth'
import { useFeatureStore } from '@/stores/feature'
import { useUserStore } from '@/stores/user'

const authStore = useAuthStore()
const userStore = useUserStore()
const featureStore = useFeatureStore()
const logoutLoading = ref(false)

const displayName = computed(() => userStore.user?.name || userStore.user?.username || '未命名用户')
const visibleEntries = computed(() => featureStore.getVisibleEntries(userStore.permissionCodes))

onLoad(async () => {
  const { ensureAuthenticated } = useAuthGuard()

  if (!ensureAuthenticated()) {
    return
  }

  if (!userStore.user) {
    await userStore.loadCurrentUser()
  }
})

async function confirmLogout() {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message: '确认退出当前账号？',
      confirmButtonText: '退出',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  logoutLoading.value = true

  try {
    await authStore.logout()
  } catch {
    authStore.clearSession()
  } finally {
    userStore.clearUser()
    logoutLoading.value = false
    showToast('已退出登录')
    uni.reLaunch({ url: '/pages/login/index' })
  }
}
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  padding: 18px 14px;
  background: #f6f7f9;
}

.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.home-header__main {
  min-width: 0;
}

.home-header__label {
  display: block;
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.home-header__name {
  display: block;
  overflow: hidden;
  color: #1f2933;
  font-size: 20px;
  font-weight: 700;
  line-height: 28px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
