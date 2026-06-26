<script setup lang="ts">
defineProps<{ title?: string; loading?: boolean; error?: string }>();
</script>
<template>
  <section class="page-container">
    <div v-if="title" class="page-header">
      <h1>{{ title }}</h1>
      <slot name="extra" />
    </div>
    <a-alert v-if="error" type="error" :message="error" show-icon class="page-error" />
    <a-spin :spinning="Boolean(loading)"><slot /></a-spin>
  </section>
</template>
<style scoped lang="scss">
.page-container {
  padding: 16px;
  background: #fff;
  min-height: calc(100vh - 112px);
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}
.page-error {
  margin-bottom: 12px;
}
</style>
