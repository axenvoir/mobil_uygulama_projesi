import type {CategoryId, FreshnessLevel, Product} from '../types';
import type {TranslationKey} from '../i18n/tr';
import {daysLeft, APPROACHING_DAYS} from './date';

export const CATEGORY_IDS: CategoryId[] = [
  'dairy',
  'meat',
  'veggie',
  'bakery',
  'canned',
  'drink',
  'snack',
  'other',
];

export const CATEGORY_LABEL_KEYS: Record<CategoryId, TranslationKey> = {
  dairy: 'catDairy',
  meat: 'catMeat',
  veggie: 'catVeggie',
  bakery: 'catBakery',
  canned: 'catCanned',
  drink: 'catDrink',
  snack: 'catSnack',
  other: 'catOther',
};

export const CATEGORY_COLORS: Record<CategoryId, string> = {
  dairy: '#3B82F6',
  meat: '#EF4444',
  veggie: '#10B981',
  bakery: '#F59E0B',
  canned: '#8B5CF6',
  drink: '#06B984',
  snack: '#EC4899',
  other: '#6B7280',
};

export const isValidCategory = (value: string): value is CategoryId =>
  (CATEGORY_IDS as string[]).includes(value);

export const freshnessOf = (product: Product): FreshnessLevel => {
  const d = daysLeft(product.expDate);
  if (d === null) {
    return 'fresh';
  }
  if (d < 0) {
    return 'expired';
  }
  return d <= APPROACHING_DAYS ? 'approaching' : 'fresh';
};

export const filterAndSortProducts = (
  products: Product[],
  opts: {query?: string; category?: CategoryId | 'all'; freshness?: FreshnessLevel | 'all'} = {},
): Product[] => {
  const {query, category = 'all', freshness = 'all'} = opts;
  const q = (query ?? '').trim().toLocaleLowerCase('tr');
  return products
    .filter(p => {
      if (q && !p.name.toLocaleLowerCase('tr').includes(q)) {
        return false;
      }
      if (category !== 'all' && p.category !== category) {
        return false;
      }
      if (freshness !== 'all' && freshnessOf(p) !== freshness) {
        return false;
      }
      return true;
    })
    .sort((a, b) => a.expDate.localeCompare(b.expDate));
};

export const countByFreshness = (products: Product[]): Record<FreshnessLevel, number> => {
  const counts = {fresh: 0, approaching: 0, expired: 0};
  for (const p of products) {
    counts[freshnessOf(p)] += 1;
  }
  return counts;
};

export const countByCategory = (products: Product[]): Partial<Record<CategoryId, number>> => {
  const counts: Partial<Record<CategoryId, number>> = {};
  for (const p of products) {
    counts[p.category] = (counts[p.category] ?? 0) + 1;
  }
  return counts;
};
