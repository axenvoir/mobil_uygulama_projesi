const parseDate = (dateStr) => {
  if (!dateStr) return '';
  if (!/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) return null;
  const [year, month, day] = dateStr.split('-').map(Number);
  const date = new Date(0);
  date.setUTCFullYear(year, month - 1, day);
  date.setUTCHours(0, 0, 0, 0);
  if (
    date.getUTCFullYear() !== year
    || date.getUTCMonth() !== month - 1
    || date.getUTCDate() !== day
  ) return null;
  return date;
};

export const formatDate = (dateStr, locale = 'tr') => {
  const date = parseDate(dateStr);
  if (!date) return dateStr || '';
  return new Intl.DateTimeFormat(locale === 'en' ? 'en-US' : 'tr-TR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC',
  }).format(date);
};

export const formatDateShort = (dateStr, locale = 'tr') => {
  const date = parseDate(dateStr);
  if (!date) return dateStr || '';
  return new Intl.DateTimeFormat(locale === 'en' ? 'en-US' : 'tr-TR', {
    day: 'numeric',
    month: 'short',
    timeZone: 'UTC',
  }).format(date);
};

// Binlik ayracı: 1500 -> "1.500"
export const formatNumber = (num) => {
  if (num === null || num === undefined) return '0';
  const n = Number(num);
  if (isNaN(n)) return String(num);
  return n.toLocaleString('tr-TR');
};

export const getDaysLeft = (dateStr, referenceDate = new Date()) => {
  if (!dateStr || !/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) return Number.NaN;

  const [year, month, day] = dateStr.split('-').map(Number);
  const expiry = new Date(0);
  expiry.setUTCFullYear(year, month - 1, day);
  expiry.setUTCHours(0, 0, 0, 0);
  if (
    expiry.getUTCFullYear() !== year
    || expiry.getUTCMonth() !== month - 1
    || expiry.getUTCDate() !== day
  ) return Number.NaN;

  const today = new Date(0);
  today.setUTCFullYear(referenceDate.getFullYear(), referenceDate.getMonth(), referenceDate.getDate());
  today.setUTCHours(0, 0, 0, 0);
  return Math.round((expiry.getTime() - today.getTime()) / 86400000);
};

// SKT göre insancıl zaman farkı: "Bugün", "Yarın", "3 gün kaldı"
export const formatDaysLeft = (dateStr) => {
  if (!dateStr) return '';
  const diff = getDaysLeft(dateStr);
  if (Number.isNaN(diff)) return dateStr;

  if (diff === 0) return 'Bugün';
  if (diff === 1) return 'Yarın';
  if (diff === -1) return 'Dün';
  if (diff < 0) return `${Math.abs(diff)} gün önce`;
  return `${diff} gün kaldı`;
};