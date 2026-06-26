<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import PageContainer from '@/components/layout/PageContainer.vue';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const user = useUserStore();

const enabledMenus = computed(() => {
  let count = 0;
  const walk = (items = user.menus) => {
    for (const item of items) {
      if (item.status === 'ENABLED' && item.visible !== false && !item.hidden) count += 1;
      if (item.children?.length) walk(item.children);
    }
  };
  walk();
  return count;
});

const quickLinks = [
  { title: '用户管理', path: '/system/user', description: '账号、部门、角色分配' },
  { title: '角色管理', path: '/system/role', description: '角色权限与菜单授权' },
  { title: '菜单权限', path: '/system/menu', description: '路由、按钮、权限标识' },
  { title: '系统配置', path: '/system/config', description: '运行参数与敏感配置' },
  { title: '登录日志', path: '/system/loginLog', description: '登录记录与失败原因' },
  { title: '数据库备份', path: '/system/databaseBackup', description: '备份文件与恢复操作' },
];
</script>

<template>
  <PageContainer>
    <div class="dashboard">
      <section class="summary-band">
        <div>
          <div class="eyebrow">Drip Admin</div>
          <h2>{{ user.profile?.realName || user.profile?.username || '管理员' }}</h2>
        </div>
        <a-space :size="12" class="summary-metrics">
          <a-statistic title="角色" :value="user.roles.length" />
          <a-statistic title="权限" :value="user.permissions.length" />
          <a-statistic title="菜单" :value="enabledMenus" />
        </a-space>
      </section>

      <section class="quick-section">
        <h3>快捷入口</h3>
        <a-row :gutter="[12, 12]">
          <a-col v-for="item in quickLinks" :key="item.path" :xs="24" :sm="12" :lg="8">
            <button class="quick-card" type="button" @click="router.push(item.path)">
              <span class="quick-title">{{ item.title }}</span>
              <span class="quick-desc">{{ item.description }}</span>
            </button>
          </a-col>
        </a-row>
      </section>
    </div>
  </PageContainer>
</template>

<style scoped lang="scss">
.dashboard {
  display: grid;
  gap: 16px;
}

.summary-band {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 20px 24px;
  background: #fff;
  border: 1px solid #edf0f5;
  border-radius: 6px;
}

.eyebrow {
  color: #667085;
  font-size: 13px;
  margin-bottom: 6px;
}

h2,
h3 {
  margin: 0;
  color: #101828;
}

h2 {
  font-size: 22px;
  font-weight: 600;
}

h3 {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
}

.summary-metrics {
  flex-wrap: wrap;
}

.quick-card {
  width: 100%;
  min-height: 84px;
  padding: 16px;
  text-align: left;
  background: #fff;
  border: 1px solid #edf0f5;
  border-radius: 6px;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.quick-card:hover {
  border-color: #1677ff;
  box-shadow: 0 4px 14px rgb(15 23 42 / 8%);
}

.quick-title,
.quick-desc {
  display: block;
}

.quick-title {
  color: #101828;
  font-size: 15px;
  font-weight: 600;
}

.quick-desc {
  margin-top: 8px;
  color: #667085;
  font-size: 13px;
}

@media (max-width: 768px) {
  .summary-band {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
