import { Share } from 'react-native';
import { getDaysLeft } from './formatters';

// Turkish month names kept for reference
// const MONTHS_TR = ['Ocak', 'Subat', 'Mart', 'Nisan', 'Mayis', 'Haziran', 'Temmuz', 'Agustos', 'Eylul', 'Ekim', 'Kasim', 'Aralik'];


export const shareProductList = async (products, userName = 'User') => {
  const today = new Date();
  const sortedProducts = [...products].sort((a, b) => a.expDate.localeCompare(b.expDate));

  const lines = sortedProducts.map((p, i) => {
    const days = getDaysLeft(p.expDate, today);
    const status = days < 0 ? '⚠️ Expired' : days <= 7 ? '⏰ Expiring Soon' : '✅ Fresh';
    const code = p.productCode ? ` [${p.productCode}]` : '';
    return `${i + 1}. ${p.name}${code}\n   SKT: ${p.expDate} ${status}`;
  }).join('\n\n');

  const MONTHS_EN = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
const dateStr = `${today.getDate()} ${MONTHS_EN[today.getMonth()]} ${today.getFullYear()}`;

  const message = `🧊 Expira - Fridge Inventory\n👤 ${userName}\n📅 ${dateStr}\n📦 Total ${products.length} products\n\n${lines}\n\n_Created with Expira app._`;

  try {
    const result = await Share.share({
      message,
      title: 'Expira Envanteri',
    });
    return { success: true, result };
  } catch (e) {
    if (__DEV__) console.error('Sharing error:', e);
    return { success: false, error: e.message };
  }
};

export const shareSingleProduct = async (product) => {
  const today = new Date();
  const days = getDaysLeft(product.expDate, today);
  const status = days < 0 ? '⚠️ Expired!' : days === 0 ? '🔴 Last day!' : days <= 7 ? `⏰ ${days} days left` : `✅ ${days} days left`;

  const message = `📦 ${product.name}\n${product.productCode ? `🏷️ Kod: ${product.productCode}\n` : ''}📁 Category: ${product.category || 'Not specified'}\n📅 Expiry: ${product.expDate}\n${status}\n\n_Tracked with Expira._`;

  try {
    await Share.share({ message, title: product.name });
    return { success: true };
  } catch (e) {
    return { success: false, error: e.message };
  }
};