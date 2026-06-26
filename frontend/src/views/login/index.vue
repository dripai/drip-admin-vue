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
    message.success('操作');
    router.replace((route.query.redirect as string) || '/');
  } finally {
    loading.value = false;
  }
}
</script>
<template>
  <main class="login-page">
    <a-form class="login-form" :model="form" layout="vertical" @finish="submit">
      <h1>操作</h1>
      <a-form-item label="操作" name="username" :rules="[{ required: true, message: '操作' }]"
        ><a-input v-model:value="form.username" autocomplete="username"
      /></a-form-item>
      <a-form-item label="操作" name="password" :rules="[{ required: true, message: '操作' }]"
        ><a-input-password v-model:value="form.password" autocomplete="current-password"
      /></a-form-item>
      <a-button type="primary" html-type="submit" block :loading="loading">操作</a-button>
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
