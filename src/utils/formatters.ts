export const parseDate = (dateStr: string): Date | null => {
  if (!dateStr || !/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) return null;
  const [year, month, day] = dateStr.split('-').map(Number);
  const date = new Date(0);
  date.setUTCFullYear(year, month - 1, day);
  date.setUTCHours(0, 0, 0, 0);
  if (
    date.getUTCFullYear() !== year ||
    date.getUTCMonth() !== month - 1 ||
    date.getUTCDate() !== day
  ) {
    return null;
  }
  return date;
};

export const formatDate = (dateStr: string, locale: 'tr' | 'en' = 'en'): string => {
  const date = parseDate(dateStr);
  if (!date) return dateStr || '';
  return new Intl.DateTimeFormat(locale === 'en' ? 'en-US' : 'tr-TR', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    timeZone: 'UTC',
  }).format(date);
};

export const getDaysLeft = (dateStr: string, referenceDate = new Date()): number => {
  if (!dateStr || !/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) return Number.NaN;

  const [year, month, day] = dateStr.split('-').map(Number);
  const expiry = new Date(0);
  expiry.setUTCFullYear(year, month - 1, day);
  expiry.setUTCHours(0, 0, 0, 0);

  const today = new Date(0);
  today.setUTCFullYear(referenceDate.getFullYear(), referenceDate.getMonth(), referenceDate.getDate());
  today.setUTCHours(0, 0, 0, 0);

  return Math.round((expiry.getTime() - today.getTime()) / 86400000);
};

export const getExpiryStatus = (daysLeft: number): 'expired' | 'today' | 'approaching' | 'fresh' => {
  if (Number.isNaN(daysLeft)) return 'fresh';
  if (daysLeft < 0) return 'expired';
  if (daysLeft === 0) return 'today';
  if (daysLeft <= 7) return 'approaching';
  return 'fresh';
};

export const addDaysToToday = (days: number): string => {
  const d = new Date();
  d.setDate(d.getDate() + days);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};
