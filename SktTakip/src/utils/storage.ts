import AsyncStorage from '@react-native-async-storage/async-storage';
import type {AppData} from '../types';
import {normalizeAppData} from './validators';
import {DEFAULT_SETTINGS} from './validators';

const DATA_KEY = '@expira/data';

export const DEFAULT_APP_DATA: AppData = {
  version: 1,
  products: [],
  notes: [],
  settings: {...DEFAULT_SETTINGS},
};
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
    return normalizeAppData(legacy) ?? DEFAULT_APP_DATA;
  }
  return DEFAULT_APP_DATA;
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
