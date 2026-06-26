import { describe, expect, it } from 'vitest';
import { hasPermission } from './permissions';

describe('hasPermission', () => {
  it('allows empty code and exact permission code', () => {
    expect(hasPermission([], undefined)).toBe(true);
    expect(hasPermission(['system:user:create'], 'system:user:create')).toBe(true);
    expect(hasPermission(['system:user:create'], 'system:user:delete')).toBe(false);
  });
});
