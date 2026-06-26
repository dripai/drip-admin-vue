export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageParams {
  page: number;
  pageSize: number;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

export type Status = 'ENABLED' | 'DISABLED';
export type ID = string | number;
