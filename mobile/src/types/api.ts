export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface RequestErrorPayload {
  code?: number
  message: string
}
