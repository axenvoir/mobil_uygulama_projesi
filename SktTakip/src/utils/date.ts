const DAY_MS = 24 * 60 * 60 * 1000;
const APPROACHING_DAYS = 3;

export const toISODate = (date: Date): string => {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
};

export const todayISO = (): string => toISODate(new Date());

export const daysFromToday = (days: number): string =>
  toISODate(new Date(Date.now() + days * DAY_MS));

export const isValidISODate = (value: string): boolean =>
  /^\d{4}-\d{2}-\d{2}$/.test(value) && !Number.isNaN(new Date(`${value}T00:00:00`).getTime());

/** Days until expiry. Negative = past. null = invalid date. */
export const daysLeft = (isoDate: string): number | null => {
  if (!isValidISODate(isoDate)) {
    return null;
  }
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const target = new Date(`${isoDate}T00:00:00`);
  return Math.round((target.getTime() - today.getTime()) / DAY_MS);
};

export const formatDate = (isoDate: string, locale: 'tr' | 'en'): string => {
  if (!isValidISODate(isoDate)) {
    return isoDate;
  }
  const date = new Date(`${isoDate}T00:00:00`);
  return date.toLocaleDateString(locale === 'tr' ? 'tr-TR' : 'en-US', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });
};

export const formatTime = (hour: number, minute: number): string =>
  `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;

export {APPROACHING_DAYS};
