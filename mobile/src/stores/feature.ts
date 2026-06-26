import { defineStore } from 'pinia'
import { mobileFeatureEntries } from '@/features/entries'
import { filterFeatureEntries } from '@/utils/permissions'
import type { MobileFeatureEntry } from '@/types/feature'

interface FeatureState {
  entries: MobileFeatureEntry[]
}

export const useFeatureStore = defineStore('feature', {
  state: (): FeatureState => ({
    entries: mobileFeatureEntries
  }),
  actions: {
    getVisibleEntries(permissionCodes: string[]) {
      return filterFeatureEntries(this.entries, permissionCodes)
    }
  }
})
