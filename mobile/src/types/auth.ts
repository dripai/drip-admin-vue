export interface LoginRequest {
  username: string
  password: string
  deviceType: string
}

export interface LoginResponse {
  token: string
  expireAt: string
  idleTimeout: number
  maxSessionDuration: number
  deviceType: string
}

export interface UserDepartment {
  id: string | number
  name: string
}

export interface UserRole {
  id: string | number
  code: string
  name: string
}

export interface MenuTreeNode {
  id: string | number
  title: string
  path?: string
  permissionCode?: string
  children?: MenuTreeNode[]
}

export interface CurrentUser {
  id: string | number
  username: string
  name: string
  avatar?: string
  department?: UserDepartment | null
}

export interface CurrentUserResponse {
  user: CurrentUser
  roles: UserRole[]
  permissionCodes: string[]
  menuTree: MenuTreeNode[]
}

export interface PersistedSession {
  token: string
  expireAt: string
  idleTimeout: number
  maxSessionDuration: number
  deviceType: string
}
