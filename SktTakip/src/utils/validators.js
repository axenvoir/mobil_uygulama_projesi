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

const isValidHexColor = (value) => typeof value === 'string' && /^#[\da-f]{6}$/i.test(value);

/** Normalize a stored product; returns null if it cannot be safely used. */
export const normalizeProduct = (product, index = 0) => {
  if (!product || typeof product !== 'object' || Array.isArray(product)) return null;

  const name = typeof product.name === 'string' ? product.name.trim() : '';
  if (!name) return null;
  if (!isValidDateString(product.expDate)) return null;

  const id = (typeof product.id === 'string' && product.id.trim())
    || (typeof product.id === 'number' && Number.isFinite(product.id) ? String(product.id) : '')
    || `recovered-product-${Date.now()}-${index}`;

  return {
    id: String(id),
    name,
    productCode: typeof product.productCode === 'string' ? product.productCode : '',
    category: typeof product.category === 'string' && product.category.trim()
      ? product.category.trim()
      : 'Diğer',
    expDate: product.expDate,
    quantity: typeof product.quantity === 'string' || typeof product.quantity === 'number'
      ? String(product.quantity)
      : '',
    note: typeof product.note === 'string' ? product.note : '',
    imageUri: typeof product.imageUri === 'string' && product.imageUri.trim()
      ? product.imageUri
      : null,
  };
};

/** Normalize a stored note; returns null if it cannot be safely used. */
export const normalizeNote = (note, index = 0) => {
  if (!note || typeof note !== 'object' || Array.isArray(note)) return null;

  const title = typeof note.title === 'string' ? note.title.trim() : '';
  const desc = typeof note.desc === 'string' ? note.desc.trim() : '';
  if (!title && !desc) return null;

  const id = (typeof note.id === 'string' && note.id.trim())
    || (typeof note.id === 'number' && Number.isFinite(note.id) ? String(note.id) : '')
    || `recovered-note-${Date.now()}-${index}`;

  return {
    id: String(id),
    title: title || 'Not',
    desc,
    pinned: Boolean(note.pinned),
    color: isValidHexColor(note.color) ? note.color : '#14B8A6',
    date: typeof note.date === 'string' ? note.date : '',
  };
};

export const normalizeProductList = (products) => {
  if (!Array.isArray(products)) return [];
  const seen = new Set();
  const normalized = [];
  products.forEach((product, index) => {
    const item = normalizeProduct(product, index);
    if (!item || seen.has(item.id)) return;
    seen.add(item.id);
    normalized.push(item);
  });
  return normalized;
};

export const normalizeNoteList = (notes) => {
  if (!Array.isArray(notes)) return [];
  const seen = new Set();
  const normalized = [];
  notes.forEach((note, index) => {
    const item = normalizeNote(note, index);
    if (!item || seen.has(item.id)) return;
    seen.add(item.id);
    normalized.push(item);
  });
  return normalized;
};