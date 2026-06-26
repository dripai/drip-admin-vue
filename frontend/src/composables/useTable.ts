import { computed, reactive, ref, toRaw } from 'vue';
import type { PageResult } from '@/types/api';
import { loadJson, removeStorage, saveJson } from '@/utils/storage';

interface UseTableOptions {
  storageKey?: string;
}

const PAGE_SIZE_OPTIONS = ['10', '15', '20', '50', '100'];
const DEFAULT_PAGE_SIZE = 10;

export function useTable<T, Q extends Record<string, unknown>>(
  loader: (params: Q & { page: number; pageSize: number }) => Promise<PageResult<T>>,
  initialQuery: Q,
  options: UseTableOptions = {},
) {
  const persisted = options.storageKey
    ? loadJson<{ query: Partial<Q>; page: number; pageSize: number } | null>(
        options.storageKey,
        null,
      )
    : null;
  const query = reactive({ ...initialQuery, ...(persisted?.query || {}) }) as Q;
  const dataSource = ref<T[]>([]);
  const loading = ref(false);
  const page = ref(persisted?.page || 1);
  const pageSize = ref(normalizePageSize(persisted?.pageSize));
  const total = ref(0);
  const pagination = computed(() => ({
    current: page.value,
    pageSize: pageSize.value,
    pageSizeOptions: PAGE_SIZE_OPTIONS,
    total: total.value,
    showSizeChanger: true,
    showTotal: (value: number, range: [number, number]) => `共 ${value} 条，本页 ${range[0]}-${range[1]}`,
  }));

  function persist() {
    if (!options.storageKey) return;
    saveJson(options.storageKey, {
      query: toRaw(query),
      page: page.value,
      pageSize: pageSize.value,
    });
  }

  async function refresh() {
    loading.value = true;
    try {
      const result = await loader({ ...toRaw(query), page: page.value, pageSize: pageSize.value });
      dataSource.value = result.list;
      total.value = result.total;
      page.value = result.page;
      pageSize.value = result.pageSize;
      persist();
    } finally {
      loading.value = false;
    }
  }
  function search() {
    page.value = 1;
    return refresh();
  }
  function reset() {
    Object.keys(query).forEach((key) => ((query as Record<string, unknown>)[key] = undefined));
    Object.assign(query, initialQuery);
    page.value = 1;
    if (options.storageKey) removeStorage(options.storageKey);
    return refresh();
  }
  function handleTableChange(p: { current?: number; pageSize?: number }) {
    page.value = p.current || 1;
    pageSize.value = normalizePageSize(p.pageSize);
    return refresh();
  }
  return { query, dataSource, loading, pagination, refresh, search, reset, handleTableChange };
}

function normalizePageSize(value?: number) {
  return PAGE_SIZE_OPTIONS.includes(String(value)) ? Number(value) : DEFAULT_PAGE_SIZE;
}
