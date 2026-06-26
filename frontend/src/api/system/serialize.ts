export function statusValue(status: unknown) {
  if (status === 'ENABLED') return 1;
  if (status === 'DISABLED') return 0;
  return status;
}

export function booleanNumber(value: unknown) {
  if (value === true) return 1;
  if (value === false) return 0;
  return value;
}

export function withNumericStatus<T extends Record<string, unknown>>(data: T) {
  return { ...data, status: statusValue(data.status) };
}

export function withNumericMenuFlags<T extends Record<string, unknown>>(data: T) {
  return {
    ...withNumericStatus(data),
    visible: booleanNumber(data.visible),
  };
}
