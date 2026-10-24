import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { formatDate } from '../utils/formatters';
import {
  User,
  Globe,
  Sun,
  Moon,
  Download,
  Upload,
  Trash2,
  FileText,
  HelpCircle,
  ChevronDown,
  ChevronUp,
  Check,
  Share2,
} from 'lucide-react';

const AVATAR_COLORS = ['#267968', '#14B8A6', '#F59E0B', '#EF4444', '#3B82F6', '#8B5CF6', '#EC4899'];

export const ProfileScreen: React.FC = () => {
  const {
    userProfile,
    updateProfile,
    changeLanguage,
    toggleTheme,
    isDark,
    products,
    notes,
    locale,
    t,
    resetAppData,
    exportBackupData,
    importBackupData,
    showToast,
  } = useApp();

  const [name, setName] = useState(userProfile.name);
  const [openFaq, setOpenFaq] = useState<number | null>(null);

  const handleSaveName = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    updateProfile({ name: name.trim() });
  };

  const handleExportBackup = () => {
    const jsonStr = exportBackupData();
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `expira-backup-${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
    showToast(t('backup_created'), 'success');
  };

  const handleImportBackup = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      const result = importBackupData(reader.result as string);
      if (!result.success) {
        showToast(result.error || t('backup_restore_failed'), 'error');
      }
    };
    reader.readAsText(file);
  };

  const handleResetData = () => {
    if (!window.confirm(t('backup_reset_confirm_message'))) return;
    resetAppData();
  };

  const handlePrintInventory = () => {
    const printWindow = window.open('', '_blank');
    if (!printWindow) {
      showToast('Could not open print window', 'error');
      return;
    }

    const itemsHtml = products
      .map(
        (p) =>
          `<tr>
            <td style="padding: 8px; border-bottom: 1px solid #ddd;">${p.name}</td>
            <td style="padding: 8px; border-bottom: 1px solid #ddd;">${p.category}</td>
            <td style="padding: 8px; border-bottom: 1px solid #ddd;">${formatDate(p.expDate, locale)}</td>
            <td style="padding: 8px; border-bottom: 1px solid #ddd;">${p.quantity || '1'}</td>
            <td style="padding: 8px; border-bottom: 1px solid #ddd;">${p.productCode || '-'}</td>
          </tr>`
      )
      .join('');

    printWindow.document.write(`
      <html>
        <head>
          <title>Expira Inventory Report</title>
          <style>
            body { font-family: system-ui, sans-serif; padding: 24px; color: #19332D; }
            h1 { color: #267968; margin-bottom: 4px; }
            table { width: 100%; border-collapse: collapse; margin-top: 16px; font-size: 14px; }
            th { text-align: left; padding: 8px; background: #EDF2EE; }
          </style>
        </head>
        <body>
          <h1>Expira - Inventory Report</h1>
          <p>Generated: ${new Date().toLocaleDateString()} | Total Products: ${products.length}</p>
          <table>
            <thead>
              <tr>
                <th>Product</th>
                <th>Category</th>
                <th>Expiry Date</th>
                <th>Qty</th>
                <th>Code</th>
              </tr>
            </thead>
            <tbody>
              ${itemsHtml}
            </tbody>
          </table>
          <script>window.print();</script>
        </body>
      </html>
    `);
    printWindow.document.close();
  };

  const FAQS = [
    { q: t('help_faq_add_q'), a: t('help_faq_add_a') },
    { q: t('help_faq_date_q'), a: t('help_faq_date_a') },
    { q: t('help_faq_notifications_q'), a: t('help_faq_notifications_a') },
    { q: t('help_faq_data_q'), a: t('help_faq_data_a') },
    { q: t('help_faq_backup_q'), a: t('help_faq_backup_a') },
    { q: t('help_faq_notes_q'), a: t('help_faq_notes_a') },
  ];

  return (
    <div className="space-y-5 pb-28 max-w-lg mx-auto">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-extrabold text-[#19332D] dark:text-[#F0F4EC]">
          {t('profile_title')}
        </h2>
      </div>

      {/* Profile Card */}
      <div className="bg-white dark:bg-[#1A2B26] p-5 rounded-3xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm space-y-4">
        <div className="flex items-center gap-4">
          <div
            className="w-16 h-16 rounded-full flex items-center justify-center text-white text-2xl font-black shrink-0 shadow-md"
            style={{ backgroundColor: userProfile.avatarColor || '#267968' }}
          >
            {userProfile.name ? userProfile.name.charAt(0).toUpperCase() : 'U'}
          </div>

          <div className="flex-1 min-w-0">
            <form onSubmit={handleSaveName} className="flex items-center gap-2">
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder={t('profile_name_placeholder')}
                className="flex-1 px-3 py-1.5 bg-[#F5F7F4] dark:bg-[#101C19] text-[#19332D] dark:text-[#F0F4EC] rounded-xl border border-[#E1E8E3] dark:border-[#344A40] text-sm font-bold focus:outline-none"
              />
              <button
                type="submit"
                className="px-3 py-1.5 bg-[#267968] dark:bg-[#70CDB0] text-white dark:text-[#0D2623] rounded-xl text-xs font-bold"
              >
                {t('save')}
              </button>
            </form>
            <p className="text-xs text-[#5A6B63] dark:text-[#B5C5BC] mt-1">
              {t('profile_member')} • {products.length} {t('products').toLowerCase()}
            </p>
          </div>
        </div>

        {/* Avatar Color Picker */}
        <div className="pt-2 border-t border-[#E1E8E3] dark:border-[#344A40] flex items-center justify-between">
          <span className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
            {t('profile_avatar_color')}
          </span>
          <div className="flex items-center gap-1.5">
            {AVATAR_COLORS.map((col) => (
              <button
                key={col}
                type="button"
                onClick={() => updateProfile({ avatarColor: col })}
                className="w-6 h-6 rounded-full flex items-center justify-center transition-transform active:scale-95 shadow-sm"
                style={{ backgroundColor: col }}
              >
                {userProfile.avatarColor === col && <Check className="w-3.5 h-3.5 text-white" />}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Preferences: Language & Appearance */}
      <div className="bg-white dark:bg-[#1A2B26] p-5 rounded-3xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm space-y-4">
        <h3 className="text-sm font-bold text-[#19332D] dark:text-[#F0F4EC]">
          {t('settings')}
        </h3>

        {/* Language selector */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <Globe className="w-4 h-4 text-[#267968] dark:text-[#70CDB0]" />
            <div>
              <div className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
                {t('language')}
              </div>
              <div className="text-[11px] text-[#5A6B63] dark:text-[#B5C5BC]">
                {t('language_desc')}
              </div>
            </div>
          </div>

          <div className="flex bg-[#EDF2EE] dark:bg-[#253A33] p-1 rounded-xl text-xs font-bold">
            <button
              onClick={() => changeLanguage('tr')}
              className={`px-3 py-1 rounded-lg transition-colors ${
                userProfile.language === 'tr'
                  ? 'bg-white dark:bg-[#1A2B26] text-[#267968] dark:text-[#70CDB0] shadow-sm'
                  : 'text-[#5A6B63] dark:text-[#B5C5BC]'
              }`}
            >
              Türkçe
            </button>
            <button
              onClick={() => changeLanguage('en')}
              className={`px-3 py-1 rounded-lg transition-colors ${
                userProfile.language === 'en'
                  ? 'bg-white dark:bg-[#1A2B26] text-[#267968] dark:text-[#70CDB0] shadow-sm'
                  : 'text-[#5A6B63] dark:text-[#B5C5BC]'
              }`}
            >
              English
            </button>
          </div>
        </div>

        {/* Appearance / Theme */}
        <div className="flex items-center justify-between pt-3 border-t border-[#E1E8E3] dark:border-[#344A40]">
          <div className="flex items-center gap-2.5">
            {isDark ? (
              <Moon className="w-4 h-4 text-[#70CDB0]" />
            ) : (
              <Sun className="w-4 h-4 text-amber-500" />
            )}
            <div>
              <div className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
                {t('settings_theme')}
              </div>
              <div className="text-[11px] text-[#5A6B63] dark:text-[#B5C5BC]">
                {isDark ? t('settings_theme_dark') : t('settings_theme_light')}
              </div>
            </div>
          </div>

          <button
            onClick={toggleTheme}
            className="px-3.5 py-1.5 bg-[#EDF2EE] dark:bg-[#253A33] text-[#19332D] dark:text-[#F0F4EC] rounded-xl text-xs font-bold hover:bg-[#E1E8E3] dark:hover:bg-[#344A40] transition-colors"
          >
            {isDark ? t('settings_theme_light') : t('settings_theme_dark')}
          </button>
        </div>
      </div>

      {/* Backup & Data Management */}
      <div className="bg-white dark:bg-[#1A2B26] p-5 rounded-3xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm space-y-3">
        <h3 className="text-sm font-bold text-[#19332D] dark:text-[#F0F4EC]">
          {t('settings_backup')}
        </h3>
        <p className="text-xs text-[#5A6B63] dark:text-[#B5C5BC]">
          {t('backup_photo_warning')}
        </p>

        <div className="grid grid-cols-2 gap-2 pt-1">
          {/* Export JSON */}
          <button
            onClick={handleExportBackup}
            className="flex items-center justify-center gap-2 p-3 bg-[#EDF2EE] hover:bg-[#E1E8E3] dark:bg-[#253A33] dark:hover:bg-[#344A40] text-[#19332D] dark:text-[#F0F4EC] rounded-2xl text-xs font-bold transition-all active:scale-95"
          >
            <Download className="w-4 h-4 text-[#267968] dark:text-[#70CDB0]" />
            <span>{t('backup_create')}</span>
          </button>

          {/* Import JSON */}
          <label className="cursor-pointer flex items-center justify-center gap-2 p-3 bg-[#EDF2EE] hover:bg-[#E1E8E3] dark:bg-[#253A33] dark:hover:bg-[#344A40] text-[#19332D] dark:text-[#F0F4EC] rounded-2xl text-xs font-bold transition-all active:scale-95">
            <Upload className="w-4 h-4 text-[#267968] dark:text-[#70CDB0]" />
            <span>{t('backup_restore')}</span>
            <input type="file" accept=".json" onChange={handleImportBackup} className="hidden" />
          </label>
        </div>

        {/* Export Printable PDF / Inventory */}
        <button
          onClick={handlePrintInventory}
          className="w-full flex items-center justify-center gap-2 p-3 bg-[#EDF2EE] hover:bg-[#E1E8E3] dark:bg-[#253A33] dark:hover:bg-[#344A40] text-[#19332D] dark:text-[#F0F4EC] rounded-2xl text-xs font-bold transition-all active:scale-95"
        >
          <FileText className="w-4 h-4 text-[#267968] dark:text-[#70CDB0]" />
          <span>{t('settings_pdf')}</span>
        </button>

        {/* Danger Reset */}
        <div className="pt-2 border-t border-[#E1E8E3] dark:border-[#344A40]">
          <button
            onClick={handleResetData}
            className="w-full flex items-center justify-center gap-2 p-3 bg-rose-50 hover:bg-rose-100 dark:bg-rose-950/30 dark:hover:bg-rose-900/40 text-rose-700 dark:text-rose-300 rounded-2xl text-xs font-bold transition-colors"
          >
            <Trash2 className="w-4 h-4" />
            <span>{t('backup_reset')}</span>
          </button>
        </div>
      </div>

      {/* Help & FAQ Accordion */}
      <div className="bg-white dark:bg-[#1A2B26] p-5 rounded-3xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm space-y-3">
        <div className="flex items-center gap-2 text-[#19332D] dark:text-[#F0F4EC]">
          <HelpCircle className="w-4 h-4 text-[#267968] dark:text-[#70CDB0]" />
          <h3 className="text-sm font-bold">{t('help_title')}</h3>
        </div>

        <div className="space-y-2">
          {FAQS.map((faq, idx) => {
            const isOpen = openFaq === idx;
            return (
              <div
                key={idx}
                className="border border-[#E1E8E3] dark:border-[#344A40] rounded-2xl overflow-hidden"
              >
                <button
                  onClick={() => setOpenFaq(isOpen ? null : idx)}
                  className="w-full p-3 flex items-center justify-between text-left text-xs font-bold text-[#19332D] dark:text-[#F0F4EC] hover:bg-[#EDF2EE] dark:hover:bg-[#253A33] transition-colors"
                >
                  <span>{faq.q}</span>
                  {isOpen ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                </button>
                {isOpen && (
                  <div className="p-3 bg-[#F5F7F4] dark:bg-[#101C19] text-xs text-[#5A6B63] dark:text-[#B5C5BC] leading-relaxed border-t border-[#E1E8E3] dark:border-[#344A40]">
                    {faq.a}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* App Version Info */}
      <div className="text-center text-[11px] text-[#5A6B63] dark:text-[#B5C5BC] py-2">
        <p className="font-bold">Expira v1.1.3 (Web Migration)</p>
        <p className="mt-0.5">Local Storage • No tracking • Fast & Private</p>
      </div>
    </div>
  );
};
