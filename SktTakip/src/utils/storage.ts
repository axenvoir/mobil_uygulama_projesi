import AsyncStorage from '@react-native-async-storage/async-storage';
import type {AppData} from '../types';
import {normalizeAppData} from './validators';

const DATA_KEY = '@expira/data';
const LEGACY_KEYS = ['@products', '@notes', '@settings', '@userProfile'];

export const loadData = async (): Promise<AppData> => {
  const raw = await AsyncStorage.getItem(DATA_KEY);
  if (raw) {
    try {
      const parsed = normalizeAppData(JSON.parse(raw));
      if (parsed) {
        return parsed;
      }
    } catch {
      // fall through to legacy
    }
  }
  const legacy: Record<string, unknown> = {};
  for (const key of LEGACY_KEYS) {
    const value = await AsyncStorage.getItem(key);
    if (value) {
      try {
        legacy[key.slice(1)] = JSON.parse(value);
      } catch {
        // ignore malformed legacy data
      }
    }
  }
  if (legacy.products || legacy.notes || legacy.settings) {
    return normalizeAppData(legacy) ?? {version: 1, products: [], notes: [], settings: {dailyReminder: false, reminderHour: 9, reminderMinute: 0, language: 'system', hapticsEnabled: true}};
  }
  return {version: 1, products: [], notes: [], settings: {dailyReminder: false, reminderHour: 9, reminderMinute: 0, language: 'system', hapticsEnabled: true}};
};

export const saveData = async (data: AppData): Promise<void> => {
  await AsyncStorage.setItem(DATA_KEY, JSON.stringify(data));
};

export const clearData = async (): Promise<void> => {
  await AsyncStorage.removeItem(DATA_KEY);
  await AsyncStorage.multiRemove(LEGACY_KEYS);
};

export const exportData = async (data: AppData): Promise<string> => JSON.stringify(data, null, 2);

export const importData = async (json: string): Promise<AppData> => {
  const parsed = normalizeAppData(JSON.parse(json));
  if (!parsed) {
    throw new Error('INVALID_BACKUP');
  }
  return parsed;
};
