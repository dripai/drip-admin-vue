import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import PermissionButton from './PermissionButton.vue';
import { usePermissionStore } from '@/stores/permission';

describe('PermissionButton', () => {
  it('hides button without permission', () => {
    setActivePinia(createPinia());
    usePermissionStore().permissions = [];
    const wrapper = mount(PermissionButton, {
      props: { permission: 'x:y:z' },
      slots: { default: '保存' },
    });
    expect(wrapper.text()).toBe('');
  });
});
