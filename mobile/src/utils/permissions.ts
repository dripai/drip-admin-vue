import type { MobileFeatureEntry } from '@/types/feature'

export function hasPermission(permissionCode: string, permissionCodes: string[]): boolean {
  return !permissionCode || permissionCodes.includes(permissionCode)
}

export function filterFeatureEntries(
  entries: MobileFeatureEntry[],
  permissionCodes: string[]
): MobileFeatureEntry[] {
  return entries
    .filter((entry) => entry.enabled)
    .filter((entry) => hasPermission(entry.permissionCode, permissionCodes))
    .sort((left, right) => left.sort - right.sort)
}
