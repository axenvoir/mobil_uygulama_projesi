import type {AppData, Note, Product, Settings} from '../types';
import {isValidISODate} from './date';
import {isValidCategory} from './categories';

export const createId = (): string =>
  `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`;

export const validateProduct = (input: Partial<Product>): string[] => {
  const errors: string[] = [];
  if (!input.name || !input.name.trim()) {
    errors.push('name');
  }
  if (!input.expDate || !isValidISODate(input.expDate)) {
    errors.push('expDate');
  }
  if (!input.category || !isValidCategory(input.category)) {
    errors.push('category');
  }
  return errors;
};

const asNumber = (value: unknown, fallback: number): number =>
  typeof value === 'number' && Number.isFinite(value) ? value : fallback;

export const normalizeProduct = (raw: unknown): Product | null => {
  if (!raw || typeof raw !== 'object') {
    return null;
  }
  const r = raw as Record<string, unknown>;
  const name = typeof r.name === 'string' ? r.name.trim() : '';
  const expDate = typeof r.expDate === 'string' ? r.expDate : '';
  const category = typeof r.category === 'string' ? r.category : 'other';
  if (!name || !isValidISODate(expDate) || !isValidCategory(category)) {
    return null;
  }
  const now = asNumber(r.createdAt, Date.now());
  return {
    id: typeof r.id === 'string' && r.id ? r.id : createId(),
    name,
    category,
    expDate,
    quantity: typeof r.quantity === 'string' ? r.quantity : '',
    note: typeof r.note === 'string' ? r.note : undefined,
    createdAt: now,
    updatedAt: asNumber(r.updatedAt, now),
  };
};

export const normalizeNote = (raw: unknown): Note | null => {
  if (!raw || typeof raw !== 'object') {
    return null;
  }
  const r = raw as Record<string, unknown>;
  const title = typeof r.title === 'string' ? r.title.trim() : '';
  const body = typeof r.body === 'string' ? r.body : '';
  if (!title && !body) {
    return null;
  }
  const now = asNumber(r.createdAt, Date.now());
  return {
    id: typeof r.id === 'string' && r.id ? r.id : createId(),
    title: title || body.slice(0, 40),
    body,
    createdAt: now,
    updatedAt: asNumber(r.updatedAt, now),
  };
};

export const DEFAULT_SETTINGS: Settings = {
  dailyReminder: false,
  reminderHour: 9,
  reminderMinute: 0,
  language: 'system',
  hapticsEnabled: true,
};

export const normalizeSettings = (raw: unknown): Settings => {
  if (!raw || typeof raw !== 'object') {
    return {...DEFAULT_SETTINGS};
  }
  const r = raw as Record<string, unknown>;
  const language =
    r.language === 'tr' || r.language === 'en' || r.language === 'system'
      ? r.language
      : DEFAULT_SETTINGS.language;
  const reminderHour = Math.min(23, Math.max(0, Math.trunc(asNumber(r.reminderHour, 9))));
  const reminderMinute = Math.min(59, Math.max(0, Math.trunc(asNumber(r.reminderMinute, 0))));
  return {
    dailyReminder: typeof r.dailyReminder === 'boolean' ? r.dailyReminder : false,
    reminderHour,
    reminderMinute,
    language,
    hapticsEnabled: typeof r.hapticsEnabled === 'boolean' ? r.hapticsEnabled : true,
  };
};

export const normalizeAppData = (raw: unknown): AppData | null => {
  if (!raw || typeof raw !== 'object') {
    return null;
  }
  const r = raw as Record<string, unknown>;
  const products = Array.isArray(r.products)
    ? (r.products.map(normalizeProduct).filter(Boolean) as Product[])
    : [];
  const notes = Array.isArray(r.notes)
    ? (r.notes.map(normalizeNote).filter(Boolean) as Note[])
    : [];
  return {
    version: 1,
    products,
    notes,
    settings: normalizeSettings(r.settings),
  };
};
