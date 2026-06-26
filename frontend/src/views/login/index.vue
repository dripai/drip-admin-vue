<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();
const loading = ref(false);
const form = reactive({ username: '', password: '' });
async function submit() {
  loading.value = true;
  try {
    await auth.login(form.username, form.password);
    message.success('登录成功');
    router.replace((route.query.redirect as string) || '/');
  } catch {
    // 登录失败提示由请求层使用后端 message 统一展示。
  } finally {
    loading.value = false;
  }
}
</script>
<template>
  <main class="login-page">
    <a-form class="login-form" :model="form" layout="vertical" @finish="submit">
      <h1>后台管理系统</h1>
      <a-form-item
        label="用户名"
        name="username"
        :rules="[{ required: true, message: '请填写必填项' }]"
        ><a-input v-model:value="form.username" autocomplete="username"
      /></a-form-item>
      <a-form-item
        label="密码"
        name="password"
        :rules="[{ required: true, message: '请填写必填项' }]"
        ><a-input-password v-model:value="form.password" autocomplete="current-password"
      /></a-form-item>
      <a-button type="primary" html-type="submit" block :loading="loading">登录</a-button>
    </a-form>
  </main>
</template>
<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: #f5f7fb;
}
.login-form {
  width: 360px;
  padding: 24px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}
h1 {
  margin: 0 0 20px;
  font-size: 20px;
}
</style>
