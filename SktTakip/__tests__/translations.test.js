import tr from '../src/translations/tr.json';
import en from '../src/translations/en.json';
import { I18n } from 'i18n-js';

const trKeys = Object.keys(tr).sort();
const enKeys = Object.keys(en).sort();

describe('translation parity', () => {
  it('has identical key sets in TR and EN', () => {
    expect(trKeys).toEqual(enKeys);
  });

  it('has no duplicate keys after JSON parsing', () => {
    const parseDuplicates = (text) => {
      const seen = new Set();
      const dupes = new Set();
      const re = /^\s{2}"([^"]+)":/gm;
      let m;
      while ((m = re.exec(text)) !== null) {
        if (seen.has(m[1])) dupes.add(m[1]);
        seen.add(m[1]);
      }
      return [...dupes];
    };
    expect(parseDuplicates(JSON.stringify(tr, null, 2))).toEqual([]);
    expect(parseDuplicates(JSON.stringify(en, null, 2))).toEqual([]);
  });

  it('has no empty values', () => {
    for (const key of trKeys) {
      expect(String(tr[key]).length).toBeGreaterThan(0);
    }
    for (const key of enKeys) {
      expect(String(en[key]).length).toBeGreaterThan(0);
    }
  });
});

describe('i18n interpolation', () => {
  const i18n = new I18n({ tr, en });

  it.each([
    ['tr', 'undo_product_deleted', { name: 'Süt' }, '"Süt" silindi'],
    ['en', 'undo_product_deleted', { name: 'Milk' }, '"Milk" deleted'],
    ['tr', 'products_deleted', { count: 3 }, '3 ürün silindi.'],
    ['en', 'products_deleted', { count: 3 }, '3 products deleted.'],
    ['tr', 'pdf_failed', { error: 'X' }, 'PDF oluşturulamadı: X'],
  ])('interpolates %s %s', (locale, key, params, expected) => {
    i18n.locale = locale;
    expect(i18n.t(key, params)).toBe(expected);
  });
});
