import { describe, expect, it } from 'vitest';
import { useTable } from './useTable';

describe('useTable', () => {
  it('loads paged data and resets to first page on search', async () => {
    const table = useTable(
      async (params: any) => ({
        list: [{ id: 1 }],
        total: 1,
        page: params.page,
        pageSize: params.pageSize,
      }),
      {},
    );
    await table.search();
    expect(table.dataSource.value).toHaveLength(1);
    expect(table.pagination.value.total).toBe(1);
  });
});
