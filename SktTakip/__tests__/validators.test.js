import {
  isValidDateString,
  isValidQuantity,
  sanitizeInput,
  normalizeProduct,
  normalizeNote,
  normalizeProductList,
  normalizeNoteList,
} from '../src/utils/validators';

describe('isValidDateString', () => {
  it.each([
    ['2026-01-01'],
    ['2024-02-29'],
    ['2000-12-31'],
  ])('accepts valid dates (%s)', (value) => {
    expect(isValidDateString(value)).toBe(true);
  });

  it.each([
    ['2023-02-29'],
    ['2026-13-01'],
    ['2026-00-10'],
    ['2026-01-32'],
    ['2026-1-1'],
    ['26-01-01'],
    ['2026/01/01'],
    [''],
    [null],
    [undefined],
    ['2026-01-00'],
  ])('rejects invalid dates (%s)', (value) => {
    expect(isValidDateString(value)).toBe(false);
  });
});

describe('isValidQuantity', () => {
  it('accepts empty values', () => {
    expect(isValidQuantity(null)).toBe(true);
    expect(isValidQuantity(undefined)).toBe(true);
    expect(isValidQuantity('')).toBe(true);
    expect(isValidQuantity('   ')).toBe(true);
  });

  it.each([
    ['1'], ['1.5'], ['0.5'], ['.5'], [3], ['2,5'], ['10.25'],
  ])('accepts valid quantities (%s)', (value) => {
    expect(isValidQuantity(value)).toBe(true);
  });

  it.each([
    ['0'], ['-1'], ['abc'], ['1.2.3'], [10000], [{}], [true],
  ])('rejects invalid quantities (%s)', (value) => {
    expect(isValidQuantity(value)).toBe(false);
  });
});

describe('sanitizeInput', () => {
  it('trims whitespace', () => {
    expect(sanitizeInput('  hello  ')).toBe('hello');
  });

  it('returns empty string for falsy input', () => {
    expect(sanitizeInput(null)).toBe('');
    expect(sanitizeInput(undefined)).toBe('');
  });
});

describe('normalizeProduct', () => {
  it('returns null for invalid input', () => {
    expect(normalizeProduct(null)).toBeNull();
    expect(normalizeProduct('string')).toBeNull();
    expect(normalizeProduct([])).toBeNull();
    expect(normalizeProduct({ name: 'Milk' })).toBeNull();
    expect(normalizeProduct({ name: '', expDate: '2026-01-01' })).toBeNull();
    expect(normalizeProduct({ name: 'Milk', expDate: 'invalid' })).toBeNull();
  });

  it('normalizes a complete product', () => {
    const result = normalizeProduct({
      id: 'p1', name: '  Milk  ', expDate: '2026-01-01',
      productCode: '123', category: 'Dairy', quantity: 2, note: 'x',
    });
    expect(result).toEqual({
      id: 'p1', name: 'Milk', expDate: '2026-01-01',
      productCode: '123', category: 'Dairy', quantity: '2', note: 'x',
      imageUri: null,
    });
  });

  it('recovers a missing id', () => {
    const result = normalizeProduct({ name: 'Milk', expDate: '2026-01-01' });
    expect(result.id).toMatch(/^recovered-product-/);
  });

  it('coerces numeric ids to strings and fills defaults', () => {
    const result = normalizeProduct({ id: 42, name: 'Milk', expDate: '2026-01-01' });
    expect(result.id).toBe('42');
    expect(result.category).toBe('Other');
    expect(result.quantity).toBe('');
    expect(result.note).toBe('');
    expect(result.imageUri).toBeNull();
  });
});

describe('normalizeNote', () => {
  it('returns null for invalid input', () => {
    expect(normalizeNote(null)).toBeNull();
    expect(normalizeNote({})).toBeNull();
    expect(normalizeNote({ title: '', desc: '' })).toBeNull();
  });

  it('fills defaults and validates color', () => {
    const result = normalizeNote({ title: 'T', desc: '' });
    expect(result.title).toBe('T');
    expect(result.pinned).toBe(false);
    expect(result.color).toBe('#14B8A6');
    expect(result.date).toBe('');
  });

  it('falls back to a default title when only desc exists', () => {
    expect(normalizeNote({ desc: 'hello' }).title).toBe('Not');
  });

  it('rejects invalid hex colors', () => {
    expect(normalizeNote({ title: 'T', color: 'red' }).color).toBe('#14B8A6');
    expect(normalizeNote({ title: 'T', color: '#12345' }).color).toBe('#14B8A6');
    expect(normalizeNote({ title: 'T', color: '#ABCDEF' }).color).toBe('#ABCDEF');
  });
});

describe('normalizeProductList', () => {
  it('drops invalid entries and dedupes ids', () => {
    const result = normalizeProductList([
      { id: 'a', name: 'Milk', expDate: '2026-01-01' },
      { id: 'a', name: 'Duplicate', expDate: '2026-01-02' },
      { name: 'NoId', expDate: '2026-01-01' },
      null,
      { name: '', expDate: '2026-01-01' },
    ]);
    expect(result).toHaveLength(2);
    expect(result[0].name).toBe('Milk');
    expect(result[1].name).toBe('NoId');
  });

  it('returns empty array for non-arrays', () => {
    expect(normalizeProductList(null)).toEqual([]);
    expect(normalizeProductList('x')).toEqual([]);
  });
});

describe('normalizeNoteList', () => {
  it('dedupes ids and drops invalid entries', () => {
    const result = normalizeNoteList([
      { id: 'n1', title: 'A', desc: '' },
      { id: 'n1', title: 'Dup', desc: '' },
      { title: '', desc: '' },
    ]);
    expect(result).toHaveLength(1);
    expect(result[0].title).toBe('A');
  });
});
