import {toISODate, isValidISODate, daysLeft, daysFromToday, formatTime} from '../src/utils/date';

describe('date utils', () => {
  it('formats ISO date correctly', () => {
    expect(toISODate(new Date(2025, 0, 5))).toBe('2025-01-05');
  });

  it('validates ISO dates', () => {
    expect(isValidISODate('2025-01-05')).toBe(true);
    expect(isValidISODate('2025-13-05')).toBe(false);
    expect(isValidISODate('not-a-date')).toBe(false);
    expect(isValidISODate('')).toBe(false);
  });

  it('computes daysLeft for future and past dates', () => {
    expect(daysLeft(daysFromToday(3))).toBe(3);
    expect(daysLeft(daysFromToday(-2))).toBe(-2);
    expect(daysLeft(daysFromToday(0))).toBe(0);
  });

  it('returns null for invalid dates', () => {
    expect(daysLeft('invalid')).toBeNull();
    expect(daysLeft('')).toBeNull();
  });

  it('formats time with zero padding', () => {
    expect(formatTime(9, 5)).toBe('09:05');
    expect(formatTime(23, 59)).toBe('23:59');
  });
});
