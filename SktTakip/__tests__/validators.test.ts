import {
  validateProduct,
  normalizeProduct,
  normalizeNote,
  normalizeSettings,
  normalizeAppData,
  createId,
} from '../src/utils/validators';

describe('validators', () => {
  it('creates unique ids', () => {
    const ids = new Set(Array.from({length: 200}, () => createId()));
    expect(ids.size).toBe(200);
  });

  it('validates required product fields', () => {
    expect(validateProduct({name: 'Milk', expDate: '2025-01-01', category: 'dairy'})).toEqual([]);
    expect(validateProduct({name: '  ', expDate: '2025-01-01', category: 'dairy'})).toContain('name');
    expect(validateProduct({name: 'Milk', expDate: 'bad', category: 'dairy'})).toContain('expDate');
    expect(validateProduct({name: 'Milk', expDate: '2025-01-01', category: ('nope' as unknown) as 'dairy'})).toContain('category');
  });

  it('normalizes valid product and rejects invalid', () => {
    const p = normalizeProduct({name: ' Milk ', expDate: '2025-01-01', category: 'dairy'});
    expect(p).not.toBeNull();
    expect(p?.name).toBe('Milk');
    expect(normalizeProduct({name: '', expDate: '2025-01-01', category: 'dairy'})).toBeNull();
    expect(normalizeProduct(null)).toBeNull();
  });

  it('fills missing product fields with defaults', () => {
    const p = normalizeProduct({name: 'X', expDate: '2025-01-01'});
    expect(p?.category).toBe('other');
    expect(p?.quantity).toBe('');
    expect(p?.id).toBeTruthy();
  });

  it('normalizes notes', () => {
    const n = normalizeNote({title: '  List ', body: 'milk, eggs'});
    expect(n?.title).toBe('List');
    expect(normalizeNote({title: '', body: ''})).toBeNull();
    expect(normalizeNote(null)).toBeNull();
  });

  it('normalizes settings with clamped values', () => {
    const s = normalizeSettings({dailyReminder: 'yes', reminderHour: 99, reminderMinute: -5, language: 'de'});
    expect(s.dailyReminder).toBe(false);
    expect(s.reminderHour).toBe(23);
    expect(s.reminderMinute).toBe(0);
    expect(s.language).toBe('system');
    expect(normalizeSettings(undefined).dailyReminder).toBe(false);
  });

  it('normalizes full app data and drops invalid entries', () => {
    const d = normalizeAppData({
      products: [
        {name: 'A', expDate: '2025-01-01', category: 'meat'},
        {name: '', expDate: '2025-01-01'},
      ],
      notes: [{title: 'n'}],
      settings: {language: 'tr'},
    });
    expect(d?.products).toHaveLength(1);
    expect(d?.notes).toHaveLength(1);
    expect(d?.settings.language).toBe('tr');
    expect(normalizeAppData(null)).toBeNull();
    expect(normalizeAppData('junk')).toBeNull();
  });
});
