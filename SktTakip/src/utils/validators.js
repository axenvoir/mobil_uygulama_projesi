export const isValidDateString = (dateStr) => {
  if (!dateStr || !/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) return false;

  const [year, month, day] = dateStr.split('-').map(Number);
  if (month < 1 || month > 12) return false;
  if (day < 1 || day > 31) return false;

  const date = new Date(year, month - 1, day);
  return date.getFullYear() === year && date.getMonth() === month - 1 && date.getDate() === day;
};

export const isValidQuantity = (qty) => {
  if (!qty) return true; // Miktar zorunlu değilse boş geçilebilir
  const num = Number(qty);
  return !isNaN(num) && num > 0 && num < 10000; // 0'dan büyük ve makul bir sayı olmalı
};

export const sanitizeInput = (text) => {
  if (!text) return '';
  return text.trim(); // Başındaki ve sonundaki boşlukları temizle
};