export function hasPermission(permissions: string[], code?: string) {
  if (!code) return true;
  return permissions.includes(code);
}
