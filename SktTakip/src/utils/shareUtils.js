import { Share } from 'react-native';
import { getDaysLeft } from './formatters';

const MONTHS_TR = ['Ocak', 'Şubat', 'Mart', 'Nisan', 'Mayıs', 'Haziran', 'Temmuz', 'Ağustos', 'Eylül', 'Ekim', 'Kasım', 'Aralık'];

export const shareProductList = async (products, userName = 'Kullanıcı') => {
  const today = new Date();
  const sortedProducts = [...products].sort((a, b) => a.expDate.localeCompare(b.expDate));

  const lines = sortedProducts.map((p, i) => {
    const days = getDaysLeft(p.expDate, today);
    const status = days < 0 ? '⚠️ Geçmiş' : days <= 7 ? '⏰ Yaklaşıyor' : '✅ Taze';
    const code = p.productCode ? ` [${p.productCode}]` : '';
    return `${i + 1}. ${p.name}${code}\n   SKT: ${p.expDate} ${status}`;
  }).join('\n\n');

  const dateStr = `${today.getDate()} ${MONTHS_TR[today.getMonth()]} ${today.getFullYear()}`;

  const message = `🧊 Expira - Buzdolabı Envanteri\n👤 ${userName}\n📅 ${dateStr}\n📦 Toplam ${products.length} ürün\n\n${lines}\n\n_Expira uygulaması ile oluşturuldu._`;

  try {
    const result = await Share.share({
      message,
      title: 'Expira Envanteri',
    });
    return { success: true, result };
  } catch (e) {
    if (__DEV__) console.error('Paylaşım hatası:', e);
    return { success: false, error: e.message };
  }
};

export const shareSingleProduct = async (product) => {
  const today = new Date();
  const days = getDaysLeft(product.expDate, today);
  const status = days < 0 ? '⚠️ Son kullanma tarihi geçmiş!' : days === 0 ? '🔴 Bugün son gün!' : days <= 7 ? `⏰ ${days} gün kaldı` : `✅ ${days} gün kaldı`;

  const message = `📦 ${product.name}\n${product.productCode ? `🏷️ Kod: ${product.productCode}\n` : ''}📁 Kategori: ${product.category || 'Belirtilmemiş'}\n📅 SKT: ${product.expDate}\n${status}\n\n_Expira ile takip ediliyor._`;

  try {
    await Share.share({ message, title: product.name });
    return { success: true };
  } catch (e) {
    return { success: false, error: e.message };
  }
};