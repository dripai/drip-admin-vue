import { describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import DictTag from './DictTag.vue';
import { useDictStore } from '@/stores/dict';

describe('DictTag', () => {
  it('renders cached dict label', async () => {
    setActivePinia(createPinia());
    const store = useDictStore();
    store.cache.status = [
      { id: 1, typeCode: 'status', label: '启用', value: 'ENABLED', status: 'ENABLED', sort: 1 },
    ];
    vi.spyOn(store, 'load').mockResolvedValue(store.cache.status);
    const wrapper = mount(DictTag, { props: { typeCode: 'status', value: 'ENABLED' } });
    expect(wrapper.text()).toContain('启用');
  });
});
