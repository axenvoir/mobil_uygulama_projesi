import React, { useState, useEffect } from 'react';
import { useApp } from '../context/AppContext';
import { CATEGORIES } from '../theme';
import { addDaysToToday } from '../utils/formatters';
import { CategoryIcon } from '../components/CategoryIcon';
import { Camera, Check, X, AlertCircle } from 'lucide-react';

export const EditProductModal: React.FC = () => {
  const { editingProduct, setEditingProduct, updateProduct, t } = useApp();

  const [name, setName] = useState('');
  const [productCode, setProductCode] = useState('');
  const [category, setCategory] = useState('dairy');
  const [expDate, setExpDate] = useState('');
  const [quantity, setQuantity] = useState('1');
  const [note, setNote] = useState('');
  const [imageUri, setImageUri] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (editingProduct) {
      setName(editingProduct.name || '');
      setProductCode(editingProduct.productCode || '');
      setCategory(editingProduct.category || 'dairy');
      setExpDate(editingProduct.expDate || '');
      setQuantity(editingProduct.quantity ? String(editingProduct.quantity) : '');
      setNote(editingProduct.note || '');
      setImageUri(editingProduct.imageUri || null);
    }
  }, [editingProduct]);

  if (!editingProduct) return null;

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        setImageUri(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError(t('product_name_required'));
      return;
    }
    if (!expDate) {
      setError(t('product_expiry_required'));
      return;
    }

    updateProduct(editingProduct.id, {
      name: name.trim(),
      productCode: productCode.trim() || undefined,
      category,
      expDate,
      quantity: quantity.trim() || undefined,
      note: note.trim() || undefined,
      imageUri,
    });

    setEditingProduct(null);
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto animate-in fade-in">
      <div className="bg-white dark:bg-[#1A2B26] max-w-md w-full rounded-3xl shadow-2xl border border-[#E1E8E3] dark:border-[#344A40] p-6 space-y-4 my-8">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-extrabold text-[#19332D] dark:text-[#F0F4EC]">
            {t('product_edit_title')}
          </h3>
          <button
            onClick={() => setEditingProduct(null)}
            className="p-1 text-[#5A6B63] dark:text-[#B5C5BC] hover:text-[#19332D] rounded-lg"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {error && (
          <div className="p-3 bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900/50 rounded-2xl flex items-center gap-2 text-rose-700 dark:text-rose-300 text-xs font-semibold">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSave} className="space-y-4">
          <div className="space-y-1">
            <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
              {t('product_name')} *
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => {
                setName(e.target.value);
                if (error) setError(null);
              }}
              className="w-full px-4 py-2.5 bg-[#F5F7F4] dark:bg-[#101C19] text-[#19332D] dark:text-[#F0F4EC] rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] text-sm focus:outline-none focus:ring-2 focus:ring-[#267968]"
            />
          </div>

          <div className="space-y-2">
            <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
              {t('expiry_date')} *
            </label>
            <div className="grid grid-cols-5 gap-1.5 text-xs font-semibold mb-1">
              {[
                { label: t('date_picker_today'), days: 0 },
                { label: t('date_picker_week'), days: 7 },
                { label: t('date_picker_month'), days: 30 },
                { label: t('date_picker_six_months'), days: 180 },
                { label: t('date_picker_year'), days: 365 },
              ].map((p, i) => (
                <button
                  key={i}
                  type="button"
                  onClick={() => setExpDate(addDaysToToday(p.days))}
                  className="py-1 bg-[#EDF2EE] dark:bg-[#253A33] hover:bg-[#E1E8E3] dark:hover:bg-[#344A40] text-[#19332D] dark:text-[#F0F4EC] rounded-lg text-center text-[11px]"
                >
                  {p.label}
                </button>
              ))}
            </div>
            <input
              type="date"
              value={expDate}
              onChange={(e) => setExpDate(e.target.value)}
              className="w-full px-4 py-2.5 bg-[#F5F7F4] dark:bg-[#101C19] text-[#19332D] dark:text-[#F0F4EC] rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] text-sm focus:outline-none focus:ring-2 focus:ring-[#267968]"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
              {t('category')}
            </label>
            <div className="grid grid-cols-5 gap-1.5">
              {CATEGORIES.filter((c) => c.id !== 'all').map((cat) => (
                <button
                  key={cat.id}
                  type="button"
                  onClick={() => setCategory(cat.id)}
                  className={`p-1.5 rounded-xl border flex flex-col items-center justify-center gap-1 text-[10px] font-semibold ${
                    category === cat.id
                      ? 'border-[#267968] bg-teal-50 dark:bg-teal-950/40 text-[#267968] dark:text-[#70CDB0]'
                      : 'border-[#E1E8E3] dark:border-[#344A40] text-[#5A6B63] dark:text-[#B5C5BC]'
                  }`}
                >
                  <CategoryIcon category={cat.id} size={14} />
                  <span className="truncate max-w-full">{t(cat.nameKey)}</span>
                </button>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
                {t('quantity')}
              </label>
              <input
                type="text"
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                className="w-full px-3 py-2 bg-[#F5F7F4] dark:bg-[#101C19] text-[#19332D] dark:text-[#F0F4EC] rounded-xl border border-[#E1E8E3] dark:border-[#344A40] text-sm"
              />
            </div>
            <div className="space-y-1">
              <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
                {t('product_code_optional')}
              </label>
              <input
                type="text"
                value={productCode}
                onChange={(e) => setProductCode(e.target.value)}
                className="w-full px-3 py-2 bg-[#F5F7F4] dark:bg-[#101C19] text-[#19332D] dark:text-[#F0F4EC] rounded-xl border border-[#E1E8E3] dark:border-[#344A40] text-sm font-mono"
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
              {t('note')}
            </label>
            <textarea
              rows={2}
              value={note}
              onChange={(e) => setNote(e.target.value)}
              className="w-full px-3 py-2 bg-[#F5F7F4] dark:bg-[#101C19] text-[#19332D] dark:text-[#F0F4EC] rounded-xl border border-[#E1E8E3] dark:border-[#344A40] text-xs"
            />
          </div>

          <div className="flex items-center justify-between pt-2">
            <label className="cursor-pointer inline-flex items-center gap-1.5 px-3 py-2 bg-[#EDF2EE] dark:bg-[#253A33] rounded-xl text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
              <Camera className="w-4 h-4 text-[#267968] dark:text-[#70CDB0]" />
              <span>{imageUri ? t('product_retake_photo') : t('add_photo')}</span>
              <input type="file" accept="image/*" onChange={handleImageChange} className="hidden" />
            </label>

            <button
              type="submit"
              className="px-6 py-2.5 bg-[#267968] hover:bg-[#1e6153] dark:bg-[#70CDB0] dark:text-[#0D2623] text-white text-xs font-extrabold rounded-xl shadow transition-transform active:scale-95 flex items-center gap-1.5"
            >
              <Check className="w-4 h-4" />
              <span>{t('save')}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
