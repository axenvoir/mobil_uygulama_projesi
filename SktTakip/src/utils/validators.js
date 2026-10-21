export const isValidDateString = (dateStr) => {
  if (!dateStr || !/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) return false;

  const [year, month, day] = dateStr.split('-').map(Number);
  if (month < 1 || month > 12) return false;
  if (day < 1 || day > 31) return false;

  const date = new Date(0);
  date.setUTCFullYear(year, month - 1, day);
  date.setUTCHours(0, 0, 0, 0);
  return date.getUTCFullYear() === year
    && date.getUTCMonth() === month - 1
    && date.getUTCDate() === day;
};

export const isValidQuantity = (qty) => {
  if (qty === null || qty === undefined || (typeof qty === 'string' && !qty.trim())) return true;
  if (typeof qty !== 'string' && typeof qty !== 'number') return false;

  const normalized = String(qty).trim().replace(',', '.');
  if (!/^(?:\d+(?:\.\d*)?|\.\d+)$/.test(normalized)) return false;

  const num = Number(normalized);
  return Number.isFinite(num) && num > 0 && num < 10000;
};

export const sanitizeInput = (text) => {
  if (!text) return '';
  return text.trim();
};