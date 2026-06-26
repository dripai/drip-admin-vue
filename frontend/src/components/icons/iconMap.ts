import {
  AppstoreOutlined,
  AuditOutlined,
  BookOutlined,
  ClockCircleOutlined,
  CloudServerOutlined,
  DatabaseOutlined,
  FileTextOutlined,
  FolderOutlined,
  LockOutlined,
  MenuOutlined,
  SettingOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons-vue';

export const iconMap = {
  AppstoreOutlined,
  AuditOutlined,
  BookOutlined,
  ClockCircleOutlined,
  CloudServerOutlined,
  DatabaseOutlined,
  FileTextOutlined,
  FolderOutlined,
  LockOutlined,
  MenuOutlined,
  SettingOutlined,
  TeamOutlined,
  UserOutlined,
};
export type IconKey = keyof typeof iconMap;
export const iconOptions = Object.keys(iconMap).map((key) => ({ label: key, value: key }));
