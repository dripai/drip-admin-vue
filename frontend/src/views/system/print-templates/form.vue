<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, shallowRef, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import $ from 'jquery';
import { defaultElementTypeProvider, hiprint } from 'vue-plugin-hiprint';
import 'vue-plugin-hiprint/dist/print-lock.css';
import PageContainer from '@/components/layout/PageContainer.vue';
import {
  createPrintTemplate,
  getPrintTemplate,
  updatePrintTemplate,
} from '@/api/system/printTemplate';
import type { PrintTemplateItem } from '@/types/system';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const current = ref<PrintTemplateItem>();
const designerView = ref<'designer' | 'json'>('designer');
const designerReady = ref(false);
const hiprintTemplate = shallowRef<any>();
const form = reactive<Partial<PrintTemplateItem>>({
  code: '',
  name: '',
  paperType: 'A4',
  templateJson: '{}',
  status: 'ENABLED',
});

const isCreate = computed(() => route.name === 'system-print-template-create');
const isDesign = computed(() => route.name === 'system-print-template-design');
const showDesignerPanel = computed(() => isCreate.value || isDesign.value);
const pageTitle = computed(() => {
  if (isCreate.value) return '新增打印模板';
  if (isDesign.value) return '设计打印模板';
  return '编辑打印模板';
});
const dragItems = [
  { title: '文本', tid: 'defaultModule.text' },
  { title: '长文本', tid: 'defaultModule.longText' },
  { title: '图片', tid: 'defaultModule.image' },
  { title: '表格', tid: 'defaultModule.table' },
  { title: '横线', tid: 'defaultModule.hline' },
  { title: '竖线', tid: 'defaultModule.vline' },
  { title: '矩形', tid: 'defaultModule.rect' },
  { title: '椭圆', tid: 'defaultModule.oval' },
  { title: '条形码', tid: 'defaultModule.barcode' },
  { title: '二维码', tid: 'defaultModule.qrcode' },
];

function resetForm(data: Partial<PrintTemplateItem> = {}) {
  Object.assign(form, {
    code: '',
    name: '',
    paperType: 'A4',
    templateJson: '{}',
    status: 'ENABLED',
    ...data,
  });
}

function validateTemplateJson() {
  try {
    JSON.parse(String(form.templateJson || ''));
    return true;
  } catch {
    message.error('模板 JSON 格式不正确');
    return false;
  }
}

function parseTemplateJson() {
  try {
    return JSON.parse(String(form.templateJson || '{}'));
  } catch {
    return {};
  }
}

function syncDesignerToJson() {
  if (!hiprintTemplate.value) return;
  form.templateJson = JSON.stringify(hiprintTemplate.value.getJson());
}

function formatTemplateJson() {
  try {
    form.templateJson = JSON.stringify(JSON.parse(String(form.templateJson || '{}')), null, 2);
  } catch {
    message.error('模板 JSON 格式不正确');
  }
}

function initHiprint() {
  (window as any).$ = $;
  (window as any).jQuery = $;
  hiprint.init({
    providers: [new defaultElementTypeProvider()],
  });
}

function initHiprintDesigner() {
  initHiprint();
  hiprint.PrintElementTypeManager.buildByHtml($('.ep-draggable-item'));
  $('#hiprint-printTemplate').empty();
  $('#PrintElementOptionSetting').empty();
  $('.hiprint-printPagination').empty();
  hiprintTemplate.value = new hiprint.PrintTemplate({
    template: parseTemplateJson(),
    settingContainer: '#PrintElementOptionSetting',
    paginationContainer: '.hiprint-printPagination',
    dataMode: 1,
    history: true,
    onDataChanged: () => syncDesignerToJson(),
  });
  hiprintTemplate.value.design('#hiprint-printTemplate');
  designerReady.value = true;
  syncDesignerToJson();
}

async function rebuildDesigner() {
  if (!showDesignerPanel.value || designerView.value !== 'designer') return;
  designerReady.value = false;
  await nextTick();
  initHiprintDesigner();
}

function switchDesignerView(value: 'designer' | 'json') {
  if (value === 'json') syncDesignerToJson();
  designerView.value = value;
  if (value === 'designer') {
    rebuildDesigner();
  }
}

function handleDesignerViewChange(value: string | number) {
  switchDesignerView(value as 'designer' | 'json');
}

function validateForm() {
  if (!form.code) {
    message.error('请输入模板编码');
    return false;
  }
  if (!form.name) {
    message.error('请输入模板名称');
    return false;
  }
  return validateTemplateJson();
}

async function save() {
  if (showDesignerPanel.value && designerView.value === 'designer') syncDesignerToJson();
  if (!validateForm()) return;
  submitting.value = true;
  try {
    if (current.value) await updatePrintTemplate(current.value.id, form);
    else await createPrintTemplate(form);
    message.success('保存成功');
    await router.push('/system/print-template');
  } finally {
    submitting.value = false;
  }
}

function preview() {
  if (showDesignerPanel.value && designerView.value === 'designer') syncDesignerToJson();
  if (!validateTemplateJson()) return;
  initHiprint();
  const printTemplate = new hiprint.PrintTemplate({ template: parseTemplateJson() });
  printTemplate.print({});
}

async function loadTemplate() {
  if (isCreate.value) {
    resetForm();
    await rebuildDesigner();
    return;
  }
  loading.value = true;
  try {
    current.value = await getPrintTemplate(route.params.id as string);
    resetForm(current.value);
    await rebuildDesigner();
  } finally {
    loading.value = false;
  }
}

onMounted(loadTemplate);

watch(
  () => form.paperType,
  () => {
    if (!showDesignerPanel.value || designerView.value !== 'designer') return;
    rebuildDesigner();
  },
);
</script>

<template>
  <PageContainer class="print-template-page" :loading="loading">
    <div class="template-toolbar">
      <div class="title-block">
        <a-button class="back-button" type="link" @click="router.push('/system/print-template')">返回列表</a-button>
        <h1>{{ pageTitle }}</h1>
        <a-tag v-if="current">{{ current.code }}</a-tag>
      </div>
      <a-space>
        <a-button v-if="showDesignerPanel" @click="preview">预览</a-button>
        <a-button type="primary" :loading="submitting" @click="save">保存</a-button>
      </a-space>
    </div>

    <a-form class="print-template-form" layout="horizontal" :model="form">
      <section class="template-panel template-meta-panel">
        <div class="meta-grid">
          <a-form-item label="模板编码" required>
            <a-input v-model:value="form.code" placeholder="PURCHASE_ORDER_A4" />
          </a-form-item>
          <a-form-item label="模板名称" required>
            <a-input v-model:value="form.name" placeholder="采购订单A4打印模板" />
          </a-form-item>
          <a-form-item label="纸张" required>
            <a-select
              v-model:value="form.paperType"
              :options="[
                { label: 'A4', value: 'A4' },
                { label: 'A5', value: 'A5' },
              ]" />
          </a-form-item>
          <a-form-item label="状态" required>
            <a-radio-group v-model:value="form.status" class="status-radio-group">
              <a-radio value="ENABLED">启用</a-radio>
              <a-radio value="DISABLED">禁用</a-radio>
            </a-radio-group>
          </a-form-item>
        </div>
      </section>

      <section v-if="showDesignerPanel" class="template-panel template-designer-panel">
        <div class="designer-toolbar">
          <a-segmented
            :value="designerView"
            :options="[
              { label: '拖拽设计', value: 'designer' },
              { label: 'JSON 源码', value: 'json' },
            ]"
            @change="handleDesignerViewChange" />
        </div>
        <div v-show="designerView === 'designer'" class="hiprint-designer">
          <aside class="designer-elements">
            <div class="designer-section-title">控件</div>
            <div
              v-for="item in dragItems"
              :key="item.tid"
              class="ep-draggable-item designer-drag-item"
              :tid="item.tid">
              {{ item.title }}
            </div>
          </aside>
          <main class="designer-canvas">
            <div id="hiprint-printTemplate" class="hiprint-print-template"></div>
            <div class="hiprint-printPagination"></div>
            <a-empty v-if="!designerReady" description="设计器加载中" />
          </main>
          <aside id="PrintElementOptionSetting" class="designer-options"></aside>
        </div>
        <div v-show="designerView === 'json'" class="template-json-panel">
          <div class="panel-header">
            <div class="panel-title">模板 JSON</div>
            <a-space>
              <a-button size="small" @click="formatTemplateJson">格式化 JSON</a-button>
              <a-button size="small" @click="switchDesignerView('designer')">返回设计</a-button>
            </a-space>
          </div>
          <a-form-item class="json-form-item" required>
            <a-textarea
              v-model:value="form.templateJson"
              class="json-editor"
              :rows="20"
              placeholder='{"panels":[{"index":0,"paperType":"A4"}]}' />
          </a-form-item>
        </div>
      </section>
    </a-form>
  </PageContainer>
</template>

<style scoped lang="scss">
.print-template-page {
  min-height: calc(100vh - 72px);
}

.template-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.title-block {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;

  h1 {
    margin: 0;
    color: #1f2937;
    font-size: 18px;
    font-weight: 600;
    line-height: 26px;
  }
}

.back-button {
  padding-left: 0;
}

.print-template-form {
  .template-panel {
    min-width: 0;
    padding: 12px 16px;
    border: 1px solid #edf0f5;
    border-radius: 8px;
    background: #fff;
  }

  .template-meta-panel {
    margin-bottom: 12px;
  }

  .meta-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 10px 24px;
    align-items: center;
  }

  .panel-title {
    margin-bottom: 14px;
    color: #1f2937;
    font-size: 15px;
    font-weight: 600;
    line-height: 22px;
  }

  .panel-header,
  .designer-toolbar {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 12px;
    margin-bottom: 8px;
  }

  .panel-header .panel-title,
  .designer-toolbar .panel-title {
    margin-bottom: 0;
  }

  .status-radio-group {
    display: flex;
    gap: 12px;
    white-space: nowrap;
  }

  .meta-grid :deep(.ant-form-item:nth-child(1)) {
    width: 250px;
  }

  .meta-grid :deep(.ant-form-item:nth-child(2)) {
    width: 250px;
  }

  .meta-grid :deep(.ant-form-item:nth-child(3)) {
    width: 120px;
  }

  .meta-grid :deep(.ant-form-item:nth-child(4)) {
    width: 190px;
  }

  .template-designer-panel {
    min-height: 660px;
  }

  .hiprint-designer {
    display: grid;
    grid-template-columns: 140px minmax(0, 1fr) 300px;
    gap: 12px;
    min-height: 620px;
  }

  .designer-elements,
  .designer-canvas,
  .designer-options {
    min-width: 0;
    border: 1px solid #edf0f5;
    border-radius: 8px;
    background: #fafafa;
  }

  .designer-elements {
    padding: 12px;
  }

  .designer-section-title {
    margin-bottom: 10px;
    color: #64748b;
    font-size: 13px;
  }

  .designer-drag-item {
    height: 34px;
    margin-bottom: 8px;
    padding: 0 10px;
    border: 1px solid #d9dfe8;
    border-radius: 6px;
    background: #fff;
    color: #1f2937;
    font-size: 13px;
    line-height: 32px;
    cursor: move;
    user-select: none;
  }

  .designer-drag-item:hover {
    border-color: #1677ff;
    color: #1677ff;
  }

  .designer-canvas {
    position: relative;
    overflow: auto;
    padding: 20px;
    background: #f3f5f8;
  }

  .hiprint-print-template {
    min-width: 760px;
  }

  .designer-options {
    overflow: auto;
    padding: 12px;
    background: #fff;
  }

  .json-form-item {
    margin-bottom: 0;
  }

  .json-editor {
    font-family: Consolas, 'Courier New', monospace;
    line-height: 1.55;
    resize: vertical;
  }

  :deep(.ant-form-item) {
    margin-bottom: 0;
  }

  .meta-grid :deep(.ant-form-item-row) {
    display: flex;
    align-items: center;
    min-width: 0;
    width: 100%;
    flex-wrap: nowrap;
  }

  .meta-grid :deep(.ant-form-item-label) {
    flex: 0 0 auto;
    padding: 0 8px 0 0;
    text-align: left;
    white-space: nowrap;
  }

  .meta-grid :deep(.ant-form-item-label > label) {
    min-height: auto;
    line-height: 22px;
  }

  .meta-grid :deep(.ant-form-item-control) {
    min-width: 0;
    flex: 1 1 auto;
  }

  .meta-grid :deep(.ant-input),
  .meta-grid :deep(.ant-select) {
    width: 100%;
  }
}

@media (max-width: 900px) {
  .template-toolbar,
  .print-template-form .meta-grid,
  .print-template-form .hiprint-designer {
    grid-template-columns: 1fr;
  }

  .template-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
