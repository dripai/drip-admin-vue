<template>
  <view class="login-page">
    <view class="login-panel">
      <view class="login-panel__title">移动端后台入口</view>
      <van-form class="login-form" @submit="handleSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            autocomplete="username"
            :rules="[{ required: true, message: '请输入用户名' }]"
          />
          <van-field
            v-model="form.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            autocomplete="current-password"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
        </van-cell-group>
        <view v-if="errorMessage" class="login-form__error">{{ errorMessage }}</view>
        <view class="login-form__actions">
          <van-button block type="primary" native-type="submit" :loading="loading">
            登录
          </van-button>
        </view>
      </van-form>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'

const authStore = useAuthStore()
const userStore = useUserStore()
const loading = ref(false)
const errorMessage = ref('')
const form = reactive({
  username: '',
  password: ''
})

onLoad(() => {
  authStore.restoreSession()

  if (authStore.isLoggedIn) {
    uni.reLaunch({ url: '/pages/home/index' })
  }
})

async function handleSubmit() {
  loading.value = true
  errorMessage.value = ''

  try {
    await authStore.login(form.username, form.password)
    await userStore.loadCurrentUser()
    uni.reLaunch({ url: '/pages/home/index' })
  } catch (error) {
    const message = error instanceof Error ? error.message : (error as { message?: string }).message || '登录失败'
    errorMessage.value = message
    form.password = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  padding: 20px 16px;
  background: #f6f7f9;
}

.login-panel {
  width: 100%;
  max-width: 420px;
}

.login-panel__title {
  margin-bottom: 28px;
  color: #1f2933;
  font-size: 24px;
  font-weight: 700;
  line-height: 32px;
  text-align: center;
}

.login-form__error {
  margin: 12px 16px 0;
  color: #d92d20;
  font-size: 13px;
  line-height: 20px;
}

.login-form__actions {
  margin: 24px 16px 0;
}
</style>
