<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import IconRenderer from '@/components/icons/IconRenderer.vue';
import { useAuthStore } from '@/stores/auth';
import { useUserStore } from '@/stores/user';
import { usePreferenceStore } from '@/stores/preferences';
import type { MenuNode } from '@/types/system';

const auth = useAuthStore();
const user = useUserStore();
const preferences = usePreferenceStore();
const router = useRouter();
const route = useRoute();

const menuItems = computed(() => buildMenu(user.menus));
const selectedKeys = computed(() => [route.path]);
const breadcrumb = computed(() =>
  route.matched.filter((item) => item.meta.title).map((item) => String(item.meta.title)),
);

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
      key: item.path || String(item.id),
      label: item.name,
      icon: item.icon,
      children: item.children?.length ? buildMenu(item.children) : undefined,
      type: item.type,
    }));
}

async function logout() {
  await auth.logout();
  message.success('操作');
  router.replace('/login');
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
        :selected-keys="selectedKeys"
        :items="menuItems"
        @click="(info: any) => router.push(String(info.key))"
      >
        <template #icon="item"><IconRenderer :icon="item.icon" /></template>
      </a-menu>
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
            user.profile?.realName || user.profile?.username || '操作'
          }}</a>
          <template #overlay
            ><a-menu
              ><a-menu-item @click="router.push('/system/users')">操作</a-menu-item
              ><a-menu-item @click="logout">操作</a-menu-item></a-menu
            ></template
          >
        </a-dropdown>
      </a-layout-header>
      <a-layout-content class="content"><router-view /></a-layout-content>
    </a-layout>
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
