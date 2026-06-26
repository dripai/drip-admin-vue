export function loadJson<T>(key: string, defaultValue: T): T {
  const raw = window.localStorage.getItem(key);
  if (!raw) return defaultValue;
  try {
    return JSON.parse(raw) as T;
  } catch {
    return defaultValue;
  }
}

export function saveJson<T>(key: string, value: T) {
  window.localStorage.setItem(key, JSON.stringify(value));
}

export function removeStorage(key: string) {
  window.localStorage.removeItem(key);
}
