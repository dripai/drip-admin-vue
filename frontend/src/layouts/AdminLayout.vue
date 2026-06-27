<script setup lang="ts">
import { computed, h, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  AppstoreOutlined,
  LayoutOutlined,
  MenuOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  PartitionOutlined,
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import IconRenderer from '@/components/icons/IconRenderer.vue';
import { changePasswordApi } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';
import { useUserStore } from '@/stores/user';
import { usePreferenceStore } from '@/stores/preferences';
import { useAppConfigStore } from '@/stores/appConfig';
import type { MenuNode, PreferenceState } from '@/types/system';

type MenuItem = {
  key: string;
  label: string;
  icon?: () => unknown;
  children?: MenuItem[];
  type?: MenuNode['type'];
};

type TopNavItem = {
  key: string;
  label: string;
  icon?: string;
};

const auth = useAuthStore();
const user = useUserStore();
const preferences = usePreferenceStore();
const appConfig = useAppConfigStore();
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
const dashboardTopNavItem: TopNavItem = {
  key: '/dashboard',
  label: '首页',
  icon: 'AppstoreOutlined',
};
const rootMenuNodes = computed(() => enabledMenus(user.menus));
const rootMenuItems = computed(() => [dashboardMenuItem, ...rootMenuNodes.value.map(toRootMenuItem)]);
const topNavItems = computed(() => [dashboardTopNavItem, ...rootMenuNodes.value.map(toTopNavItem)]);
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
    preferences.layoutMode === 'mix' ||
    (preferences.layoutMode === 'doubleSide' && childMenuItems.value.length > 0),
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
  { key: 'doubleSide', label: '左侧双列', icon: () => h(PartitionOutlined) },
  { key: 'side', label: '左侧单列', icon: () => h(MenuOutlined) },
  { key: 'mix', label: '顶部+左侧', icon: () => h(LayoutOutlined) },
];
const activeLayout = computed(
  () => layoutOptions.find((item) => item.key === preferences.layoutMode) || layoutOptions[1],
);
const headerUserText = computed(() =>
  (user.profile?.realName || user.profile?.username || '用户').slice(0, 1),
);
const watermarkContent = computed(() =>
  [user.profile?.realName || user.profile?.username || '', user.profile?.email || ''].filter(
    Boolean,
  ),
);
const watermarkEnabled = computed(
  () => appConfig.watermarkEnabled && auth.isLoggedIn && watermarkContent.value.length > 0,
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

function toTopNavItem(item: MenuNode): TopNavItem {
  return {
    key: menuKey(item),
    label: item.name,
    icon: item.icon,
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
  <a-watermark
    class="layout-watermark"
    :content="watermarkEnabled ? watermarkContent : []"
    :font="{ color: 'rgba(0, 0, 0, 0.08)', fontSize: 15 }"
    :gap="[160, 120]"
    :z-index="8"
  >
    <a-layout class="admin-layout" :class="`layout-${preferences.layoutMode}`">
    <a-layout-sider
      v-if="preferences.layoutMode !== 'mix'"
      :collapsed="preferences.layoutMode === 'side' && preferences.collapsed"
      collapsible
      :trigger="null"
      :width="preferences.layoutMode === 'doubleSide' ? 84 : 224"
      :collapsed-width="72"
      class="sider root-sider"
    >
      <div class="brand">
        <img
          v-if="appConfig.logoUrl"
          class="brand-logo"
          :src="appConfig.logoUrl"
          :alt="appConfig.systemName"
        />
        <span v-else-if="preferences.layoutMode === 'doubleSide' || preferences.collapsed">
          {{ appConfig.systemName.slice(0, 2).toUpperCase() }}
        </span>
        <span v-else class="brand-text">{{ appConfig.systemName }}</span>
        <span
          v-if="appConfig.logoUrl && preferences.layoutMode !== 'doubleSide' && !preferences.collapsed"
          class="brand-text"
          >{{ appConfig.systemName }}</span
        >
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
      <div class="child-title">
        {{ preferences.layoutMode === 'mix' ? appConfig.systemName : activeMenuTrail.labels[0] || '菜单' }}
      </div>
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
        <nav v-if="preferences.layoutMode === 'mix'" class="top-menu" aria-label="一级菜单">
          <button
            v-for="item in topNavItems"
            :key="item.key"
            type="button"
            class="top-menu-item"
            :class="{ active: rootSelectedKeys.includes(item.key) }"
            @click="handleRootClick({ key: item.key })"
          >
            <IconRenderer v-if="item.icon" :icon="item.icon" />
            <span>{{ item.label }}</span>
          </button>
        </nav>
        <a-breadcrumb v-if="preferences.layoutMode !== 'mix'" class="breadcrumb"
          ><a-breadcrumb-item v-for="item in breadcrumb" :key="item">{{
            item
          }}</a-breadcrumb-item></a-breadcrumb
        >
        <div class="header-actions">
          <a-popover v-model:open="layoutOpen" trigger="click" placement="bottomRight">
            <button type="button" class="header-action-icon" :title="activeLayout.label">
              <component :is="activeLayout.icon" />
            </button>
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
            <button type="button" class="user-menu" @click.prevent>{{ headerUserText }}</button>
            <template #overlay
              ><a-menu
                ><a-menu-item @click="router.push('/system/profile')">个人中心</a-menu-item
                ><a-menu-item @click="openPassword">修改密码</a-menu-item
                ><a-menu-item @click="logout">退出登录</a-menu-item></a-menu
              ></template
            >
          </a-dropdown>
        </div>
      </a-layout-header>
      <div v-if="preferences.layoutMode === 'mix'" class="breadcrumb-bar">
        <a-breadcrumb class="breadcrumb"
          ><a-breadcrumb-item v-for="item in breadcrumb" :key="item">{{
            item
          }}</a-breadcrumb-item></a-breadcrumb
        >
      </div>
      <a-layout-content class="content"><router-view /></a-layout-content>
    </a-layout>
    <a-modal
      v-model:open="passwordOpen"
      title="修改密码"
      :confirm-loading="passwordSubmitting"
      @ok="submitPassword"
    >
      <a-form
        :model="passwordForm"
        layout="horizontal"
        :label-col="{ style: { width: '84px' } }"
        :wrapper-col="{ flex: 1 }"
      >
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
  </a-watermark>
</template>
<style scoped lang="scss">
.layout-watermark {
  min-height: 100vh;
  display: block;
}

.admin-layout {
  --layout-header-height: 48px;

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

.layout-doubleSide .root-sider {
  background: #001529;
}

.layout-doubleSide .root-sider :deep(.ant-menu) {
  padding: 4px 6px;
  background: transparent;
}

.layout-doubleSide .root-sider :deep(.ant-menu-title-content) {
  display: block;
  flex: 0 0 16px;
  width: 100%;
  height: 16px;
  overflow: hidden;
  font-size: 12px;
  line-height: 16px !important;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap !important;
}

.layout-doubleSide .root-sider :deep(.ant-menu-item),
.layout-doubleSide .root-sider :deep(.ant-menu-submenu-title) {
  width: 72px;
  height: 58px;
  margin: 4px 0;
  padding-inline: 0 !important;
  display: flex !important;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  line-height: 16px;
  border-radius: 6px;
}

.layout-doubleSide .root-sider :deep(.ant-menu-item-icon) {
  margin-inline-end: 0;
  font-size: 18px;
  line-height: 18px;
}

.layout-doubleSide .root-sider :deep(.ant-menu-item-selected) {
  background: #1677ff;
}

.brand,
.child-title {
  height: var(--layout-header-height);
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-size: 16px;
  font-weight: 600;
}

.brand {
  color: #fff;
  gap: 8px;
  min-width: 0;
}

.layout-doubleSide .brand {
  justify-content: center;
  padding: 0;
}

.brand-logo {
  width: 28px;
  height: 28px;
  flex: 0 0 auto;
  object-fit: contain;
}

.brand-text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.child-title {
  color: #1f2937;
}

.child-sider {
  border-right: 1px solid #e5e7eb;
  background: #fff;
}

.child-title {
  padding: 0 18px;
  font-weight: 700;
  border-bottom: 1px solid #eef2f7;
}

.child-sider :deep(.ant-menu) {
  padding: 8px;
  border-inline-end: 0 !important;
}

.child-sider :deep(.ant-menu-item) {
  height: 40px;
  margin: 4px 0;
  padding-inline: 14px !important;
  border-radius: 6px;
}

.child-sider :deep(.ant-menu-item .ant-menu-item-icon) {
  margin-inline-end: 10px;
}

.child-sider :deep(.ant-menu-item-selected) {
  background: #e6f4ff;
}

.header {
  height: var(--layout-header-height);
  padding: 0 16px;
  background: #fff;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.top-menu {
  height: var(--layout-header-height);
  min-width: 0;
  flex: 0 1 auto;
  display: flex;
  align-items: center;
  gap: 4px;
  overflow: hidden;
}

.top-menu-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 34px;
  padding: 0 12px;
  color: #1f2937;
  cursor: pointer;
  background: transparent;
  border: 0;
  border-radius: 6px;
  white-space: nowrap;
}

.top-menu-item:hover,
.top-menu-item.active {
  color: #1677ff;
  background: #e6f4ff;
}

.top-menu-item :deep(.anticon),
.top-menu-item :deep(svg) {
  flex: 0 0 auto;
}

.breadcrumb {
  flex: 1;
  min-width: 120px;
}

.breadcrumb-bar {
  min-height: 40px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #eef2f7;
}

.content {
  padding: 16px;
}

.header-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-action-icon,
.user-menu {
  height: 32px;
  color: #1f2937;
  cursor: pointer;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.header-action-icon {
  width: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.user-menu {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  padding: 0;
  overflow: hidden;
  color: #1677ff;
  font-weight: 600;
  line-height: 1;
  text-overflow: ellipsis;
  white-space: nowrap;
  background: #e6f4ff;
  border-color: #bae0ff;
}

.header-action-icon:hover,
.user-menu:hover {
  color: #1677ff;
  background: #d9ecff;
  border-color: #91caff;
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

</style>
