import {
  freshnessOf,
  filterAndSortProducts,
  countByFreshness,
  countByCategory,
  isValidCategory,
  CATEGORY_IDS,
} from '../src/utils/categories';
import type {Product} from '../src/types';

const makeProduct = (over: Partial<Product>): Product => ({
  id: over.id ?? 'p1',
  name: over.name ?? 'P',
  category: over.category ?? 'dairy',
  expDate: over.expDate ?? '2030-01-01',
  quantity: '',
  createdAt: 0,
  updatedAt: 0,
  ...over,
});

describe('categories utils', () => {
  it('validates category ids', () => {
    expect(isValidCategory('dairy')).toBe(true);
    expect(isValidCategory('nope')).toBe(false);
    expect(CATEGORY_IDS).toHaveLength(8);
  });

  it('classifies freshness', () => {
    const future = makeProduct({expDate: '2099-01-01'});
    const soon = makeProduct({expDate: toISO(2)});
    const past = makeProduct({expDate: '2000-01-01'});
    expect(freshnessOf(future)).toBe('fresh');
    expect(freshnessOf(soon)).toBe('approaching');
    expect(freshnessOf(past)).toBe('expired');
  });

  it('filters by query, category and freshness, sorted by expDate', () => {
    const a = makeProduct({id: 'a', name: 'Milk', expDate: '2030-05-01'});
    const b = makeProduct({id: 'b', name: 'Cheese', category: 'dairy', expDate: '2030-01-01'});
    const c = makeProduct({id: 'c', name: 'Steak', category: 'meat', expDate: '2000-01-01'});
    const list = [a, b, c];

    expect(filterAndSortProducts(list).map(p => p.id)).toEqual(['c', 'b', 'a']);
    expect(filterAndSortProducts(list, {query: 'mil'}).map(p => p.id)).toEqual(['a']);
    expect(filterAndSortProducts(list, {category: 'meat'}).map(p => p.id)).toEqual(['c']);
    expect(filterAndSortProducts(list, {freshness: 'expired'}).map(p => p.id)).toEqual(['c']);
    expect(filterAndSortProducts(list, {query: 'zzz'})).toEqual([]);
  });

  it('counts by freshness and category', () => {
    const list = [
      makeProduct({id: 'a', expDate: '2099-01-01'}),
      makeProduct({id: 'b', expDate: '2000-01-01', category: 'meat'}),
      makeProduct({id: 'c', expDate: '2000-01-01', category: 'meat'}),
    ];
    const f = countByFreshness(list);
    expect(f.fresh).toBe(1);
    expect(f.expired).toBe(2);
    const c = countByCategory(list);
    expect(c.meat).toBe(2);
    expect(c.dairy).toBe(1);
  });
});

function toISO(daysFromNow: number): string {
  const d = new Date();
  d.setDate(d.getDate() + daysFromNow);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${dd}`;
}
