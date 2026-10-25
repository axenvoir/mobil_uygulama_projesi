export type CategoryId =
  | 'dairy'
  | 'meat'
  | 'veggie'
  | 'bakery'
  | 'canned'
  | 'drink'
  | 'snack'
  | 'other';

export interface Product {
  id: string;
  name: string;
  category: CategoryId;
  expDate: string; // ISO: YYYY-MM-DD
  quantity: string;
  note?: string;
  createdAt: number;
  updatedAt: number;
}

export interface Note {
  id: string;
  title: string;
  body: string;
  createdAt: number;
  updatedAt: number;
}

export type FreshnessLevel = 'fresh' | 'approaching' | 'expired';

export interface Settings {
  dailyReminder: boolean;
  reminderHour: number;
  reminderMinute: number;
  language: 'tr' | 'en' | 'system';
  hapticsEnabled: boolean;
}

export interface AppData {
  version: 1;
  products: Product[];
  notes: Note[];
  settings: Settings;
}
