<script setup lang="ts">
import { computed, h, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  AppstoreOutlined,
  BorderOutlined,
  ColumnWidthOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  PicCenterOutlined,
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import IconRenderer from '@/components/icons/IconRenderer.vue';
import { changePasswordApi } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';
import { useUserStore } from '@/stores/user';
import { usePreferenceStore } from '@/stores/preferences';
import type { MenuNode, PreferenceState } from '@/types/system';

type MenuItem = {
  key: string;
  label: string;
  icon?: () => unknown;
  children?: MenuItem[];
  type?: MenuNode['type'];
};

const auth = useAuthStore();
const user = useUserStore();
const preferences = usePreferenceStore();
const router = useRouter();
const route = useRoute();
const passwordOpen = ref(false);
const layoutOpen = ref(false);
const passwordSubmitting = ref(false);
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const dashboardMenuItem: MenuItem = {
  key: '/dashboard',
  label: '首页',
  icon: () => h(IconRenderer, { icon: 'AppstoreOutlined' }),
};
const rootMenuNodes = computed(() => enabledMenus(user.menus));
const rootMenuItems = computed(() => [dashboardMenuItem, ...rootMenuNodes.value.map(toRootMenuItem)]);
const sideMenuItems = computed(() => [dashboardMenuItem, ...buildMenu(rootMenuNodes.value)]);
const activeMenuTrail = computed(() => findActiveMenuTrail(user.menus, route.path));
const activeRootKey = computed(() => activeMenuTrail.value.keys[0] || '/dashboard');
const selectedKeys = computed(() => {
  const activeKey = activeMenuTrail.value.keys[activeMenuTrail.value.keys.length - 1];
  return activeKey ? [activeKey] : [route.path];
});
const rootSelectedKeys = computed(() => [activeRootKey.value]);
const childMenuItems = computed(() => {
  if (activeRootKey.value === '/dashboard') return [];
  const root = rootMenuNodes.value.find((item) => menuKey(item) === activeRootKey.value);
  return root?.children?.length ? buildMenu(root.children) : [];
});
const childSelectedKeys = computed(() => {
  if (activeRootKey.value === '/dashboard') return ['/dashboard'];
  return selectedKeys.value;
});
const showChildSider = computed(
  () =>
    (preferences.layoutMode === 'doubleSide' || preferences.layoutMode === 'mix') &&
    childMenuItems.value.length > 0,
);
const openKeys = ref<string[]>([]);
const childOpenKeys = ref<string[]>([]);
const breadcrumb = computed(() => {
  if (route.path === '/dashboard') return ['首页'];
  if (activeMenuTrail.value.labels.length) return activeMenuTrail.value.labels;
  return route.matched.filter((item) => item.meta.title).map((item) => String(item.meta.title));
});
const layoutOptions: Array<{
  key: PreferenceState['layoutMode'];
  label: string;
  icon: () => unknown;
}> = [
  { key: 'doubleSide', label: '左侧双列', icon: () => h(ColumnWidthOutlined) },
  { key: 'side', label: '左侧单列', icon: () => h(BorderOutlined) },
  { key: 'mix', label: '顶部+左侧', icon: () => h(PicCenterOutlined) },
];
const activeLayout = computed(
  () => layoutOptions.find((item) => item.key === preferences.layoutMode) || layoutOptions[1],
);

function menuKey(item: MenuNode) {
  return item.path || String(item.id);
}

function normalizePath(path?: string) {
  if (!path || path === '/') return path || '';
  return path.endsWith('/') ? path.slice(0, -1) : path;
}

function enabledMenus(nodes: MenuNode[]) {
  return nodes
    .filter(
      (item) =>
        item.status === 'ENABLED' &&
        item.visible !== false &&
        !item.hidden &&
        item.type !== 'BUTTON',
    )
    .sort((a, b) => a.sort - b.sort);
}

function buildMenu(nodes: MenuNode[]): MenuItem[] {
  return enabledMenus(nodes).map((item) => ({
    key: menuKey(item),
    label: item.name,
    icon: item.icon ? () => h(IconRenderer, { icon: item.icon }) : undefined,
    children: item.children?.length ? buildMenu(item.children) : undefined,
    type: item.type,
  }));
}

function toRootMenuItem(item: MenuNode): MenuItem {
  return {
    key: menuKey(item),
    label: item.name,
    icon: item.icon ? () => h(IconRenderer, { icon: item.icon }) : undefined,
  };
}

function findActiveMenuTrail(nodes: MenuNode[], routePath: string) {
  const targetPath = normalizePath(routePath);
  let bestMatch = { keys: [] as string[], labels: [] as string[] };

  function walk(
    items: MenuNode[],
    parentKeys: string[],
    parentLabels: string[],
  ): typeof bestMatch | undefined {
    for (const item of enabledMenus(items)) {
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

function firstNavigablePath(item?: MenuNode): string | undefined {
  if (!item) return undefined;
  if (item.type === 'MENU' && item.path) return item.path;
  for (const child of enabledMenus(item.children || [])) {
    const path = firstNavigablePath(child);
    if (path) return path;
  }
  return undefined;
}

function handleMenuClick(info: { key: string }) {
  const key = String(info.key);
  if (!key || key === route.path) return;
  router.push(key);
}

function handleRootClick(info: { key: string }) {
  const key = String(info.key);
  if (key === '/dashboard') {
    router.push('/dashboard');
    return;
  }
  const root = rootMenuNodes.value.find((item) => menuKey(item) === key);
  const target = firstNavigablePath(root);
  if (target) router.push(target);
}

function setLayoutMode(layoutMode: PreferenceState['layoutMode']) {
  preferences.setLayoutMode(layoutMode);
  layoutOpen.value = false;
}

watch(
  activeMenuTrail,
  (trail) => {
    openKeys.value = trail.keys.slice(0, -1);
    childOpenKeys.value = trail.keys.slice(1, -1);
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
  <a-layout class="admin-layout" :class="`layout-${preferences.layoutMode}`">
    <a-layout-sider
      v-if="preferences.layoutMode !== 'mix'"
      :collapsed="preferences.layoutMode === 'side' && preferences.collapsed"
      collapsible
      :trigger="null"
      :width="preferences.layoutMode === 'doubleSide' ? 88 : 224"
      :collapsed-width="72"
      class="sider root-sider"
    >
      <div class="brand">
        {{ preferences.layoutMode === 'doubleSide' || preferences.collapsed ? 'DA' : 'Drip Admin' }}
      </div>
      <a-menu
        theme="dark"
        mode="inline"
        v-model:openKeys="openKeys"
        :selected-keys="preferences.layoutMode === 'doubleSide' ? rootSelectedKeys : selectedKeys"
        :items="preferences.layoutMode === 'doubleSide' ? rootMenuItems : sideMenuItems"
        @click="preferences.layoutMode === 'doubleSide' ? handleRootClick($event as any) : handleMenuClick($event as any)"
      />
    </a-layout-sider>
    <a-layout-sider
      v-if="showChildSider"
      collapsible
      :trigger="null"
      width="188"
      class="child-sider"
      theme="light"
    >
      <div class="child-title">{{ activeMenuTrail.labels[0] || '菜单' }}</div>
      <a-menu
        mode="inline"
        v-model:openKeys="childOpenKeys"
        :selected-keys="childSelectedKeys"
        :items="childMenuItems"
        @click="handleMenuClick($event as any)"
      />
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <a-button
          v-if="preferences.layoutMode === 'side'"
          type="text"
          @click="preferences.setCollapsed(!preferences.collapsed)"
        >
          <MenuUnfoldOutlined v-if="preferences.collapsed" /><MenuFoldOutlined v-else />
        </a-button>
        <div v-if="preferences.layoutMode === 'mix'" class="top-brand">Drip Admin</div>
        <a-menu
          v-if="preferences.layoutMode === 'mix'"
          class="top-menu"
          mode="horizontal"
          :selected-keys="rootSelectedKeys"
          :items="rootMenuItems"
          @click="handleRootClick($event as any)"
        />
        <a-breadcrumb class="breadcrumb"
          ><a-breadcrumb-item v-for="item in breadcrumb" :key="item">{{
            item
          }}</a-breadcrumb-item></a-breadcrumb
        >
        <a-popover v-model:open="layoutOpen" trigger="click" placement="bottomRight">
          <a-button type="text" class="layout-switch" :title="activeLayout.label">
            <component :is="activeLayout.icon" />
          </a-button>
          <template #content>
            <div class="layout-panel">
              <button
                v-for="item in layoutOptions"
                :key="item.key"
                type="button"
                class="layout-option"
                :class="{ active: item.key === preferences.layoutMode }"
                @click="setLayoutMode(item.key)"
              >
                <component :is="item.icon" />
                <span>{{ item.label }}</span>
              </button>
            </div>
          </template>
        </a-popover>
        <a-dropdown :trigger="['click']">
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

.sider,
.child-sider {
  position: sticky;
  top: 0;
  height: 100vh;
  overflow: auto;
}

.root-sider :deep(.ant-menu-item),
.root-sider :deep(.ant-menu-submenu-title) {
  min-width: 0;
}

.layout-doubleSide .root-sider :deep(.ant-menu-title-content) {
  display: block;
  overflow: hidden;
  font-size: 12px;
  line-height: 18px;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.layout-doubleSide .root-sider :deep(.ant-menu-item),
.layout-doubleSide .root-sider :deep(.ant-menu-submenu-title) {
  height: 58px;
  padding-inline: 8px !important;
  flex-direction: column;
  gap: 4px;
  line-height: 18px;
}

.layout-doubleSide .root-sider :deep(.ant-menu-item-icon) {
  margin-inline-end: 0;
  font-size: 18px;
}

.brand,
.top-brand,
.child-title {
  height: 48px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-size: 16px;
  font-weight: 600;
}

.brand {
  color: #fff;
}

.top-brand,
.child-title {
  color: #1f2937;
}

.child-sider {
  border-right: 1px solid #e5e7eb;
  background: #fff;
}

.child-title {
  border-bottom: 1px solid #eef2f7;
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

.top-menu {
  min-width: 0;
  flex: 0 1 auto;
  border-bottom: 0;
}

.breadcrumb {
  flex: 1;
  min-width: 120px;
}

.content {
  padding: 16px;
}

.layout-switch {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.layout-panel {
  display: grid;
  gap: 4px;
  min-width: 132px;
}

.layout-option {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  height: 32px;
  padding: 0 8px;
  color: #1f2937;
  cursor: pointer;
  background: transparent;
  border: 0;
  border-radius: 4px;
  text-align: left;
}

.layout-option:hover,
.layout-option.active {
  color: #1677ff;
  background: #e6f4ff;
}

.user-menu {
  color: #1f2937;
  white-space: nowrap;
}
</style>
