import * as Print from 'expo-print';
import * as Sharing from 'expo-sharing';
import { getDaysLeft } from './formatters';

const MONTHS_TR = ['Ocak', 'Şubat', 'Mart', 'Nisan', 'Mayıs', 'Haziran', 'Temmuz', 'Ağustos', 'Eylül', 'Ekim', 'Kasım', 'Aralık'];

const escapeHtml = (value) => String(value ?? '').replace(/[&<>"']/g, character => ({
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#39;',
}[character]));

export const generateInventoryPDF = async (products, userName = 'Kullanıcı') => {
  const today = new Date();
  const dateStr = `${today.getDate()} ${MONTHS_TR[today.getMonth()]} ${today.getFullYear()}`;

  const getStatusText = (daysLeft) => {
    if (daysLeft < 0) return 'Geçmiş';
    if (daysLeft <= 7) return 'Yaklaşıyor';
    return 'Taze';
  };

  const getStatusColor = (daysLeft) => {
    if (daysLeft < 0) return '#EF4444'; 
    if (daysLeft <= 7) return '#F59E0B'; 
    return '#10B981'; 
  };

  const rows = products.map((p) => {
    const days = getDaysLeft(p.expDate, today);
    return `
      <tr>
        <td>${escapeHtml(p.name)}</td>
        <td>${escapeHtml(p.productCode || '-')}</td>
        <td>${escapeHtml(p.category || '-')}</td>
        <td>${escapeHtml(p.expDate)}</td>
        <td style="color: ${getStatusColor(days)}; font-weight: bold;">${getStatusText(days)}</td>
      </tr>
    `;
  }).join('');

  const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <style>
        body { font-family: Helvetica, Arial, sans-serif; padding: 30px; color: #1E293B; }
        h1 { color: #0F172A; margin-bottom: 4px; }
        .subtitle { color: #64748B; font-size: 12px; margin-bottom: 20px; }
        .header-info { background-color: #F1F5F9; padding: 12px; border-radius: 8px; margin-bottom: 20px; }
        .header-info p { margin: 4px 0; font-size: 13px; }
        table { width: 100%; border-collapse: collapse; }
        th { background-color: #0F172A; color: white; padding: 10px; text-align: left; font-size: 13px; }
        td { padding: 10px; border-bottom: 1px solid #E2E8F0; font-size: 12px; }
        tr:nth-child(even) { background-color: #F8FAFC; }
        .footer { margin-top: 30px; text-align: center; color: #94A3B8; font-size: 11px; }
      </style>
    </head>
    <body>
      <h1>Expira - Buzdolabı Envanteri</h1>
      <p class="subtitle">Son kullanma tarihini unutma.</p>

      <div class="header-info">
        <p><strong>Kullanıcı:</strong> ${escapeHtml(userName)}</p>
        <p><strong>Tarih:</strong> ${dateStr}</p>
        <p><strong>Toplam Ürün:</strong> ${products.length}</p>
      </div>

      <table>
        <thead>
          <tr>
            <th>Ürün Adı</th>
            <th>Ürün Kodu</th>
            <th>Kategori</th>
            <th>SKT</th>
            <th>Durum</th>
          </tr>
        </thead>
        <tbody>
          ${rows}
        </tbody>
      </table>

      <div class="footer">
        Bu rapor Expira mobil uygulaması tarafından oluşturulmuştur.
      </div>
    </body>
    </html>
  `;

  try {
    const { uri } = await Print.printToFileAsync({ html });
    if (await Sharing.isAvailableAsync()) {
      await Sharing.shareAsync(uri, { mimeType: 'application/pdf', dialogTitle: 'Envanter Raporu' });
    }
    return { success: true, uri };
  } catch (e) {
    if (__DEV__) console.error('PDF oluşturma hatası:', e);
    return { success: false, error: e.message };
  }
};