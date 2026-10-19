const MONTHS_TR = ['Ocak', 'Şubat', 'Mart', 'Nisan', 'Mayıs', 'Haziran', 'Temmuz', 'Ağustos', 'Eylül', 'Ekim', 'Kasım', 'Aralık'];

// "2026-10-24" -> "24 Ekim 2026"
export const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const parts = dateStr.split('-');
  if (parts.length !== 3) return dateStr;
  const [year, month, day] = parts.map(Number);
  if (isNaN(year) || isNaN(month) || isNaN(day) || month < 1 || month > 12) return dateStr;
  return `${day} ${MONTHS_TR[month - 1]} ${year}`;
};

// "2026-10-24" -> "24 Eki"
export const formatDateShort = (dateStr) => {
  if (!dateStr) return '';
  const parts = dateStr.split('-');
  if (parts.length !== 3) return dateStr;
  const [, month, day] = parts.map(Number);
  if (isNaN(month) || month < 1 || month > 12) return dateStr;
  return `${day} ${MONTHS_TR[month - 1].substring(0, 3)}`;
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