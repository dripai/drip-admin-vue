import { defineStore } from 'pinia'
import { getCurrentUserApi } from '@/api/auth'
import type { CurrentUser, MenuTreeNode, UserRole } from '@/types/auth'

interface UserState {
  user: CurrentUser | null
  roles: UserRole[]
  permissionCodes: string[]
  menuTree: MenuTreeNode[]
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    user: null,
    roles: [],
    permissionCodes: [],
    menuTree: []
  }),
  actions: {
    async loadCurrentUser() {
      const context = await getCurrentUserApi()

      this.$patch({
        user: context.user,
        roles: context.roles,
        permissionCodes: context.permissionCodes,
        menuTree: context.menuTree
      })

      return context
    },
    clearUser() {
      this.$patch({
        user: null,
        roles: [],
        permissionCodes: [],
        menuTree: []
      })
    }
  }
})
