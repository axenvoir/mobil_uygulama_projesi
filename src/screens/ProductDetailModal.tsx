import React from 'react';
import { useApp } from '../context/AppContext';
import { CATEGORIES } from '../theme';
import { formatDate, getDaysLeft, getExpiryStatus } from '../utils/formatters';
import { CategoryIcon } from '../components/CategoryIcon';
import {
  X,
  Edit2,
  Trash2,
  Share2,
  Copy,
  Calendar,
  Layers,
  FileText,
  AlertTriangle,
  Clock,
  CheckCircle2,
  Hash,
} from 'lucide-react';

export const ProductDetailModal: React.FC = () => {
  const {
    viewProductDetail,
    setViewProductDetail,
    setEditingProduct,
    deleteProduct,
    t,
    locale,
    showToast,
  } = useApp();

  if (!viewProductDetail) return null;

  const product = viewProductDetail;
  const daysLeft = getDaysLeft(product.expDate);
  const status = getExpiryStatus(daysLeft);
  const catObj = CATEGORIES.find((c) => c.id === product.category) || CATEGORIES[CATEGORIES.length - 1];

  const handleCopyCode = () => {
    if (!product.productCode) {
      showToast(t('product_code_copy_empty'), 'warning');
      return;
    }
    navigator.clipboard.writeText(product.productCode);
    showToast(t('product_code_copied'), 'success');
  };

  const handleShare = () => {
    const text = `📦 ${product.name}\n${t('category')}: ${t(catObj.nameKey)}\n${t('expiry_date')}: ${formatDate(product.expDate, locale)}\n${t('quantity')}: ${product.quantity || '1'}\n${product.note ? `${t('note')}: ${product.note}` : ''}`;
    navigator.clipboard.writeText(text);
    showToast(t('copied'), 'success');
  };

  const handleDelete = () => {
    if (!window.confirm(t('product_delete_message', { name: product.name }))) return;
    deleteProduct(product.id, true);
    setViewProductDetail(null);
  };

  const getStatusBadge = () => {
    if (status === 'expired') {
      return (
        <div className="flex items-center gap-2 p-3 rounded-2xl bg-rose-50 text-rose-800 dark:bg-rose-950/60 dark:text-rose-200 border border-rose-200 dark:border-rose-900/50">
          <AlertTriangle className="w-5 h-5 shrink-0" />
          <div className="text-xs">
            <span className="font-extrabold block text-sm">{t('product_status_expired')}</span>
            <span>{Math.abs(daysLeft)} {t('statistics_days_overdue', { count: Math.abs(daysLeft) })}</span>
          </div>
        </div>
      );
    }
    if (status === 'today') {
      return (
        <div className="flex items-center gap-2 p-3 rounded-2xl bg-amber-50 text-amber-800 dark:bg-amber-950/60 dark:text-amber-200 border border-amber-200 dark:border-amber-900/50 animate-pulse">
          <Clock className="w-5 h-5 shrink-0" />
          <div className="text-xs">
            <span className="font-extrabold block text-sm">{t('product_status_today')}</span>
            <span>{t('notification_check_recommendation')}</span>
          </div>
        </div>
      );
    }
    if (status === 'approaching') {
      return (
        <div className="flex items-center gap-2 p-3 rounded-2xl bg-amber-50 text-amber-800 dark:bg-amber-950/60 dark:text-amber-200 border border-amber-200 dark:border-amber-900/50">
          <Clock className="w-5 h-5 shrink-0" />
          <div className="text-xs">
            <span className="font-extrabold block text-sm">{t('product_status_days', { count: daysLeft })}</span>
            <span>{t('home_next_seven_days')}</span>
          </div>
        </div>
      );
    }
    return (
      <div className="flex items-center gap-2 p-3 rounded-2xl bg-emerald-50 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-200 border border-emerald-200 dark:border-emerald-900/50">
        <CheckCircle2 className="w-5 h-5 shrink-0" />
        <div className="text-xs">
          <span className="font-extrabold block text-sm">{t('product_status_fresh')}</span>
          <span>{daysLeft} {t('statistics_days_remaining', { count: daysLeft })}</span>
        </div>
      </div>
    );
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto animate-in fade-in">
      <div className="bg-white dark:bg-[#1A2B26] max-w-md w-full rounded-3xl shadow-2xl border border-[#E1E8E3] dark:border-[#344A40] overflow-hidden">
        {/* Header Photo or Category banner */}
        <div className="relative h-48 bg-gradient-to-tr from-[#267968] to-[#153F38] flex items-center justify-center text-white overflow-hidden">
          {product.imageUri ? (
            <img src={product.imageUri} alt={product.name} className="w-full h-full object-cover" />
          ) : (
            <div className="flex flex-col items-center gap-2 opacity-90">
              <div
                className="w-16 h-16 rounded-2xl flex items-center justify-center text-white shadow-lg"
                style={{ backgroundColor: catObj.color }}
              >
                <CategoryIcon category={product.category} size={32} />
              </div>
              <span className="text-xs font-bold uppercase tracking-wider text-[#C4D639]">
                {t(catObj.nameKey)}
              </span>
            </div>
          )}

          {/* Close button */}
          <button
            onClick={() => setViewProductDetail(null)}
            className="absolute top-4 right-4 p-2 bg-black/40 hover:bg-black/60 text-white rounded-full transition-colors backdrop-blur-sm"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6 space-y-4">
          <div>
            <div className="flex items-start justify-between gap-2">
              <h3 className="text-xl font-extrabold text-[#19332D] dark:text-[#F0F4EC]">
                {product.name}
              </h3>
              {product.quantity && (
                <span className="px-2.5 py-1 bg-[#EDF2EE] dark:bg-[#253A33] text-[#19332D] dark:text-[#F0F4EC] rounded-xl text-xs font-bold shrink-0">
                  {product.quantity}
                </span>
              )}
            </div>
            {product.productCode && (
              <div className="flex items-center gap-2 mt-1">
                <button
                  onClick={handleCopyCode}
                  className="inline-flex items-center gap-1 text-xs text-[#5A6B63] dark:text-[#B5C5BC] font-mono hover:text-[#267968] dark:hover:text-[#70CDB0]"
                >
                  <Hash className="w-3.5 h-3.5" />
                  <span>{product.productCode}</span>
                  <Copy className="w-3 h-3 ml-0.5" />
                </button>
              </div>
            )}
          </div>

          {/* Status badge */}
          {getStatusBadge()}

          {/* Details list */}
          <div className="space-y-2.5 bg-[#F5F7F4] dark:bg-[#101C19] p-4 rounded-2xl text-xs">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-[#5A6B63] dark:text-[#B5C5BC]">
                <Calendar className="w-4 h-4 text-[#267968] dark:text-[#70CDB0]" />
                <span className="font-semibold">{t('expiry_date')}</span>
              </div>
              <span className="font-bold text-[#19332D] dark:text-[#F0F4EC]">
                {formatDate(product.expDate, locale)}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-[#5A6B63] dark:text-[#B5C5BC]">
                <Layers className="w-4 h-4 text-[#267968] dark:text-[#70CDB0]" />
                <span className="font-semibold">{t('category')}</span>
              </div>
              <span className="font-bold text-[#19332D] dark:text-[#F0F4EC]">
                {t(catObj.nameKey)}
              </span>
            </div>

            {product.note && (
              <div className="pt-2 border-t border-[#E1E8E3] dark:border-[#344A40]">
                <div className="flex items-center gap-1.5 text-[#5A6B63] dark:text-[#B5C5BC] mb-1 font-semibold">
                  <FileText className="w-3.5 h-3.5" />
                  <span>{t('note')}</span>
                </div>
                <p className="text-[#19332D] dark:text-[#F0F4EC] leading-relaxed">
                  {product.note}
                </p>
              </div>
            )}
          </div>

          {/* Actions: Edit, Delete, Share */}
          <div className="grid grid-cols-3 gap-2 pt-2">
            <button
              onClick={() => {
                setViewProductDetail(null);
                setEditingProduct(product);
              }}
              className="flex items-center justify-center gap-1.5 py-2.5 bg-[#EDF2EE] dark:bg-[#253A33] hover:bg-[#E1E8E3] dark:hover:bg-[#344A40] text-[#19332D] dark:text-[#F0F4EC] rounded-xl text-xs font-bold transition-colors"
            >
              <Edit2 className="w-3.5 h-3.5 text-[#267968] dark:text-[#70CDB0]" />
              <span>{t('edit')}</span>
            </button>

            <button
              onClick={handleShare}
              className="flex items-center justify-center gap-1.5 py-2.5 bg-[#EDF2EE] dark:bg-[#253A33] hover:bg-[#E1E8E3] dark:hover:bg-[#344A40] text-[#19332D] dark:text-[#F0F4EC] rounded-xl text-xs font-bold transition-colors"
            >
              <Share2 className="w-3.5 h-3.5 text-[#267968] dark:text-[#70CDB0]" />
              <span>{t('share')}</span>
            </button>

            <button
              onClick={handleDelete}
              className="flex items-center justify-center gap-1.5 py-2.5 bg-rose-50 dark:bg-rose-950/40 hover:bg-rose-100 dark:hover:bg-rose-900/40 text-rose-700 dark:text-rose-300 rounded-xl text-xs font-bold transition-colors"
            >
              <Trash2 className="w-3.5 h-3.5" />
              <span>{t('delete')}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
