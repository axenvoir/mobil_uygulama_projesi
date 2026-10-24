export interface Product {
  id: string;
  name: string;
  productCode?: string;
  category: string;
  expDate: string; // YYYY-MM-DD
  quantity?: string | number;
  note?: string;
  imageUri?: string | null;
}

export interface Note {
  id: string;
  title: string;
  desc: string;
  pinned: boolean;
  color: string;
  date: string;
}

export interface Settings {
  dailyReminder: boolean;
  reminderTime: string; // '09:00'
  instantNotifications: boolean;
  hapticsEnabled: boolean;
  notificationMessage?: string;
}

export interface UserProfile {
  name: string;
  avatarColor: string;
  language: 'system' | 'tr' | 'en';
  biometricsEnabled: boolean;
  theme?: 'system' | 'light' | 'dark';
}

export interface ToastInfo {
  id: string;
  message: string;
  type: 'success' | 'info' | 'warning' | 'error';
  action?: {
    label: string;
    onPress: () => void;
  } | null;
}

export type TabType = 'home' | 'products' | 'add' | 'notes' | 'alerts' | 'stats' | 'profile' | 'categories' | 'backup' | 'help' | 'detail';
