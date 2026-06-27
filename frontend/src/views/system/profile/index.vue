<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import PageContainer from '@/components/layout/PageContainer.vue';
import { updateProfileApi } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';
import { useUserStore } from '@/stores/user';

const auth = useAuthStore();
const user = useUserStore();
const profileOpen = ref(false);
const submitting = ref(false);

const displayName = computed(() => user.profile?.realName || user.profile?.username || '用户');
const avatarText = computed(() => displayName.value.slice(0, 1));
const roleNames = computed(() => user.roles.map((role) => role.roleName || role.roleCode));

const profileForm = reactive({
  realName: '',
  phone: '',
  email: '',
});

function openProfile() {
  Object.assign(profileForm, {
    realName: user.profile?.realName || '',
    phone: user.profile?.phone || '',
    email: user.profile?.email || '',
  });
  profileOpen.value = true;
}

async function submitProfile() {
  if (!profileForm.realName.trim()) {
    message.error('请填写姓名');
    return;
  }
  submitting.value = true;
  try {
    await updateProfileApi({
      realName: profileForm.realName.trim(),
      phone: profileForm.phone.trim(),
      email: profileForm.email.trim(),
    });
    await auth.refreshCurrentUser();
    message.success('资料已更新');
    profileOpen.value = false;
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <PageContainer>
    <div class="profile-page">
      <section class="profile-hero">
        <div class="avatar">{{ avatarText }}</div>
        <div class="summary">
          <div class="summary-name">{{ displayName }}</div>
          <div class="summary-meta">{{ user.profile?.username || '-' }}</div>
        </div>
        <div class="summary-actions">
          <a-button type="primary" @click="openProfile">编辑资料</a-button>
        </div>
      </section>

      <section class="profile-section">
        <div class="section-title">基本资料</div>
        <dl class="info-list">
          <div>
            <dt>姓名</dt>
            <dd>{{ user.profile?.realName || '-' }}</dd>
          </div>
          <div>
            <dt>手机号</dt>
            <dd>{{ user.profile?.phone || '-' }}</dd>
          </div>
          <div>
            <dt>邮箱</dt>
            <dd>{{ user.profile?.email || '-' }}</dd>
          </div>
          <div>
            <dt>用户名</dt>
            <dd>{{ user.profile?.username || '-' }}</dd>
          </div>
          <div>
            <dt>用户ID</dt>
            <dd>{{ user.profile?.id || '-' }}</dd>
          </div>
          <div>
            <dt>部门</dt>
            <dd>{{ user.profile?.dept?.deptName || '-' }}</dd>
          </div>
          <div>
            <dt>组织角色</dt>
            <dd class="role-list">
              <a-tag v-for="role in roleNames" :key="role">{{ role }}</a-tag>
              <span v-if="!roleNames.length" class="empty-text">-</span>
            </dd>
          </div>
        </dl>
      </section>
    </div>

    <a-modal
      v-model:open="profileOpen"
      title="编辑资料"
      :confirm-loading="submitting"
      @ok="submitProfile"
    >
      <a-form :model="profileForm" layout="horizontal" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="姓名" required>
          <a-input v-model:value="profileForm.realName" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="profileForm.phone" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model:value="profileForm.email" />
        </a-form-item>
      </a-form>
    </a-modal>

  </PageContainer>
</template>

<style scoped lang="scss">
.profile-page {
  display: grid;
  gap: 14px;
}

.profile-hero,
.profile-section {
  background: #fff;
  border: 1px solid #edf0f5;
  border-radius: 6px;
}

.profile-hero {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 22px;
}

.avatar {
  width: 58px;
  height: 58px;
  display: grid;
  place-items: center;
  color: #1677ff;
  font-size: 24px;
  font-weight: 700;
  background: #e6f4ff;
  border-radius: 8px;
}

.summary {
  min-width: 0;
  flex: 1;
}

.summary-name {
  color: #101828;
  font-size: 20px;
  font-weight: 600;
  line-height: 28px;
}

.summary-meta {
  margin-top: 4px;
  color: #667085;
  font-size: 13px;
}

.summary-actions {
  display: flex;
  align-items: center;
}

.profile-section {
  padding: 18px 22px;
}

.section-title {
  margin-bottom: 16px;
  color: #101828;
  font-size: 15px;
  font-weight: 600;
}

.info-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 34px;
  margin: 0;
}

.info-list div {
  display: flex;
  align-items: flex-start;
  min-width: 0;
  min-height: 40px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f2f5;
}

.info-list dt {
  flex: 0 0 76px;
  color: #667085;
  font-size: 13px;
  line-height: 24px;
}

.info-list dd {
  flex: 1;
  min-width: 0;
  margin: 0;
  color: #101828;
  line-height: 24px;
  overflow-wrap: anywhere;
}

.role-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.empty-text {
  color: #98a2b3;
}

@media (max-width: 640px) {
  .profile-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .summary-actions {
    width: 100%;
  }

  .summary-actions :deep(.ant-btn) {
    width: 100%;
  }

  .info-list {
    grid-template-columns: 1fr;
  }
}
</style>
