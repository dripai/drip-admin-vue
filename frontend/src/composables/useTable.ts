import { computed, reactive, ref } from 'vue';
import type { PageResult } from '@/types/api';

export function useTable<T, Q extends Record<string, unknown>>(
  loader: (params: Q & { page: number; pageSize: number }) => Promise<PageResult<T>>,
  initialQuery: Q,
) {
  const query = reactive({ ...initialQuery }) as Q;
  const dataSource = ref<T[]>([]);
  const loading = ref(false);
  const page = ref(1);
  const pageSize = ref(20);
  const total = ref(0);
  const pagination = computed(() => ({
    current: page.value,
    pageSize: pageSize.value,
    total: total.value,
    showSizeChanger: true,
    showTotal: (value: number) => `? ${value} ?`,
  }));
  async function refresh() {
    loading.value = true;
    try {
      const result = await loader({ ...query, page: page.value, pageSize: pageSize.value });
      dataSource.value = result.list;
      total.value = result.total;
      page.value = result.page;
      pageSize.value = result.pageSize;
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
    page.value = 1;
    return refresh();
  }
  function handleTableChange(p: { current?: number; pageSize?: number }) {
    page.value = p.current || 1;
    pageSize.value = p.pageSize || 20;
    return refresh();
  }
  return { query, dataSource, loading, pagination, refresh, search, reset, handleTableChange };
}
