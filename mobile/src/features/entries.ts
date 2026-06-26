import type { MobileFeatureEntry } from '@/types/feature'

export const mobileFeatureEntries: MobileFeatureEntry[] = [
  {
    title: '库存盘点',
    icon: 'records-o',
    path: '/pages/feature-placeholder/index?title=%E5%BA%93%E5%AD%98%E7%9B%98%E7%82%B9',
    permissionCode: 'mobile:inventory:count',
    enabled: true,
    sort: 10
  },
  {
    title: '扫码入库',
    icon: 'scan',
    path: '/pages/feature-placeholder/index?title=%E6%89%AB%E7%A0%81%E5%85%A5%E5%BA%93',
    permissionCode: 'mobile:stock:in',
    enabled: true,
    sort: 20
  },
  {
    title: '扫码出库',
    icon: 'logistics',
    path: '/pages/feature-placeholder/index?title=%E6%89%AB%E7%A0%81%E5%87%BA%E5%BA%93',
    permissionCode: 'mobile:stock:out',
    enabled: true,
    sort: 30
  },
  {
    title: '任务处理',
    icon: 'todo-list-o',
    path: '/pages/feature-placeholder/index?title=%E4%BB%BB%E5%8A%A1%E5%A4%84%E7%90%86',
    permissionCode: 'mobile:task:handle',
    enabled: true,
    sort: 40
  }
]
