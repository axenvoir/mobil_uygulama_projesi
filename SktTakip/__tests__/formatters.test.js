import { formatDate, getDaysLeft } from '../src/utils/formatters';

describe('formatDate', () => {
  it('formats Turkish dates', () => {
    expect(formatDate('2026-01-15', 'tr')).toBe('15 Ocak 2026');
  });

  it('formats English dates', () => {
    expect(formatDate('2026-01-15', 'en')).toBe('January 15, 2026');
  });

  it('returns the input for invalid dates', () => {
    expect(formatDate('invalid')).toBe('invalid');
    expect(formatDate('')).toBe('');
    expect(formatDate(null)).toBe('');
  });

  it('handles leap day', () => {
    expect(formatDate('2024-02-29', 'tr')).toBe('29 Şubat 2024');
  });
});

describe('getDaysLeft', () => {
  it('returns 0 for today', () => {
    const now = new Date(2026, 0, 15, 12, 30);
    expect(getDaysLeft('2026-01-15', now)).toBe(0);
  });

  it('counts whole days regardless of time of day', () => {
    const morning = new Date(2026, 0, 15, 6, 0);
    const evening = new Date(2026, 0, 15, 23, 0);
    expect(getDaysLeft('2026-01-16', morning)).toBe(1);
    expect(getDaysLeft('2026-01-16', evening)).toBe(1);
  });

  it('returns negative values for past dates', () => {
    const now = new Date(2026, 0, 15);
    expect(getDaysLeft('2026-01-10', now)).toBe(-5);
  });

  it('handles month and year boundaries', () => {
    expect(getDaysLeft('2026-02-01', new Date(2026, 0, 31))).toBe(1);
    expect(getDaysLeft('2027-01-01', new Date(2026, 11, 31))).toBe(1);
  });

  it('returns NaN for invalid dates', () => {
    expect(Number.isNaN(getDaysLeft('invalid'))).toBe(true);
    expect(Number.isNaN(getDaysLeft(''))).toBe(true);
    expect(Number.isNaN(getDaysLeft(null))).toBe(true);
  });
});
