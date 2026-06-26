<script setup lang="ts">
import { computed, h, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import IconRenderer from '@/components/icons/IconRenderer.vue';
import { changePasswordApi } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';
import { useUserStore } from '@/stores/user';
import { usePreferenceStore } from '@/stores/preferences';
import type { MenuNode } from '@/types/system';

const auth = useAuthStore();
const user = useUserStore();
const preferences = usePreferenceStore();
const router = useRouter();
const route = useRoute();
const passwordOpen = ref(false);
const passwordSubmitting = ref(false);
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const dashboardMenuItem = {
  key: '/dashboard',
  label: '首页',
  icon: () => h(IconRenderer, { icon: 'AppstoreOutlined' }),
};
const menuItems = computed(() => [dashboardMenuItem, ...buildMenu(user.menus)]);
const activeMenuTrail = computed(() => findActiveMenuTrail(user.menus, route.path));
const selectedKeys = computed(() => {
  const activeKey = activeMenuTrail.value.keys[activeMenuTrail.value.keys.length - 1];
  return activeKey ? [activeKey] : [route.path];
});
const openKeys = ref<string[]>([]);
const breadcrumb = computed(() => {
  if (route.path === '/dashboard') return ['首页'];
  if (activeMenuTrail.value.labels.length) return activeMenuTrail.value.labels;
  return route.matched.filter((item) => item.meta.title).map((item) => String(item.meta.title));
});

function menuKey(item: MenuNode) {
  return item.path || String(item.id);
}

function normalizePath(path?: string) {
  if (!path || path === '/') return path || '';
  return path.endsWith('/') ? path.slice(0, -1) : path;
}

function buildMenu(nodes: MenuNode[]): any[] {
  return nodes
    .filter(
      (item) =>
        item.status === 'ENABLED' &&
        item.visible !== false &&
        !item.hidden &&
        item.type !== 'BUTTON',
    )
    .sort((a, b) => a.sort - b.sort)
    .map((item) => ({
      key: menuKey(item),
      label: item.name,
      icon: item.icon ? () => h(IconRenderer, { icon: item.icon }) : undefined,
      children: item.children?.length ? buildMenu(item.children) : undefined,
      type: item.type,
    }));
}

function findActiveMenuTrail(nodes: MenuNode[], routePath: string) {
  const targetPath = normalizePath(routePath);
  let bestMatch = { keys: [] as string[], labels: [] as string[] };

  function walk(
    items: MenuNode[],
    parentKeys: string[],
    parentLabels: string[],
  ): typeof bestMatch | undefined {
    for (const item of items) {
      if (
        item.status !== 'ENABLED' ||
        item.visible === false ||
        item.hidden ||
        item.type === 'BUTTON'
      ) {
        continue;
      }
      const key = menuKey(item);
      const currentKeys = [...parentKeys, key];
      const currentLabels = [...parentLabels, item.name];
      const itemPath = normalizePath(item.path);
      if (itemPath && targetPath === itemPath) return { keys: currentKeys, labels: currentLabels };
      if (
        itemPath &&
        targetPath.startsWith(`${itemPath}/`) &&
        currentKeys.length > bestMatch.keys.length
      ) {
        bestMatch = { keys: currentKeys, labels: currentLabels };
      }
      const childMatch = item.children?.length
        ? walk(item.children, currentKeys, currentLabels)
        : undefined;
      if (childMatch) return childMatch;
    }
    return undefined;
  }

  return walk(nodes, [], []) || bestMatch;
}

watch(
  activeMenuTrail,
  (trail) => {
    openKeys.value = trail.keys.slice(0, -1);
  },
  { immediate: true },
);

async function logout() {
  await auth.logout();
  message.success('已退出登录');
  router.replace('/login');
}
function openPassword() {
  Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' });
  passwordOpen.value = true;
}
async function submitPassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    message.error('请填写旧密码和新密码');
    return;
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.error('两次输入的新密码不一致');
    return;
  }
  passwordSubmitting.value = true;
  try {
    await changePasswordApi({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    });
    message.success('密码修改成功');
    passwordOpen.value = false;
  } finally {
    passwordSubmitting.value = false;
  }
}
</script>
<template>
  <a-layout class="admin-layout">
    <a-layout-sider
      :collapsed="preferences.collapsed"
      collapsible
      :trigger="null"
      width="224"
      class="sider"
    >
      <div class="brand">{{ preferences.collapsed ? 'DA' : 'Drip Admin' }}</div>
      <a-menu
        theme="dark"
        mode="inline"
        v-model:openKeys="openKeys"
        :selected-keys="selectedKeys"
        :items="menuItems"
        @click="(info: any) => router.push(String(info.key))"
      />
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <a-button type="text" @click="preferences.setCollapsed(!preferences.collapsed)">
          <MenuUnfoldOutlined v-if="preferences.collapsed" /><MenuFoldOutlined v-else />
        </a-button>
        <a-breadcrumb class="breadcrumb"
          ><a-breadcrumb-item v-for="item in breadcrumb" :key="item">{{
            item
          }}</a-breadcrumb-item></a-breadcrumb
        >
        <a-dropdown>
          <a class="user-menu" @click.prevent>{{
            user.profile?.realName || user.profile?.username || '用户'
          }}</a>
          <template #overlay
            ><a-menu
              ><a-menu-item @click="router.push('/system/user')">当前用户</a-menu-item
              ><a-menu-item @click="openPassword">修改密码</a-menu-item
              ><a-menu-item @click="logout">退出登录</a-menu-item></a-menu
            ></template
          >
        </a-dropdown>
      </a-layout-header>
      <a-layout-content class="content"><router-view /></a-layout-content>
    </a-layout>
    <a-modal
      v-model:open="passwordOpen"
      title="修改密码"
      :confirm-loading="passwordSubmitting"
      @ok="submitPassword"
    >
      <a-form :model="passwordForm" layout="vertical">
        <a-form-item label="旧密码" required>
          <a-input-password v-model:value="passwordForm.oldPassword" />
        </a-form-item>
        <a-form-item label="新密码" required>
          <a-input-password v-model:value="passwordForm.newPassword" />
        </a-form-item>
        <a-form-item label="确认新密码" required>
          <a-input-password v-model:value="passwordForm.confirmPassword" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-layout>
</template>
<style scoped lang="scss">
.admin-layout {
  min-height: 100vh;
}
.sider {
  position: sticky;
  top: 0;
  height: 100vh;
  overflow: auto;
}
.brand {
  height: 48px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}
.header {
  height: 56px;
  padding: 0 16px;
  background: #fff;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #e5e7eb;
}
.breadcrumb {
  flex: 1;
}
.content {
  padding: 16px;
}
.user-menu {
  color: #1f2937;
}
</style>
