import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { CATEGORIES, SUGGESTED_PRODUCTS } from '../theme';
import { addDaysToToday } from '../utils/formatters';
import { CategoryIcon } from '../components/CategoryIcon';
import { Camera, Calendar, Check, AlertCircle, X, Sparkles } from 'lucide-react';

export const AddProductModal: React.FC = () => {
  const { t, addProduct, setActiveTab, showToast } = useApp();

  const [name, setName] = useState('');
  const [productCode, setProductCode] = useState('');
  const [category, setCategory] = useState('dairy');
  const [expDate, setExpDate] = useState(addDaysToToday(7));
  const [quantity, setQuantity] = useState('1');
  const [note, setNote] = useState('');
  const [imageUri, setImageUri] = useState<string | null>(null);
  const [showMore, setShowMore] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleApplySuggestion = (sug: typeof SUGGESTED_PRODUCTS[0]) => {
    // Extract primary name before parenthesis if present
    const cleanName = sug.name.split(' (')[0];
    setName(cleanName);
    setCategory(sug.category);
    setExpDate(addDaysToToday(sug.days));
    showToast(t('product_suggestion_filled', { name: cleanName }), 'info');
  };

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

    addProduct({
      name: name.trim(),
      productCode: productCode.trim() || undefined,
      category,
      expDate,
      quantity: quantity.trim() || undefined,
      note: note.trim() || undefined,
      imageUri,
    });

    setActiveTab('products');
  };

  return (
    <div className="space-y-5 pb-24 max-w-lg mx-auto">
      {/* Screen Title */}
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-extrabold text-[#19332D] dark:text-[#F0F4EC]">
          {t('product_new_title')}
        </h2>
        <button
          onClick={() => setActiveTab('products')}
          className="text-xs font-bold text-[#5A6B63] dark:text-[#B5C5BC] hover:underline"
        >
          {t('cancel')}
        </button>
      </div>

      {error && (
        <div className="p-3 bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900/50 rounded-2xl flex items-center gap-2 text-rose-700 dark:text-rose-300 text-xs font-semibold">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Suggestions Row */}
      <div>
        <div className="flex items-center gap-1.5 mb-2 text-xs font-bold text-[#5A6B63] dark:text-[#B5C5BC]">
          <Sparkles className="w-3.5 h-3.5 text-[#C4D639]" />
          <span>{t('product_suggestions')}</span>
        </div>
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-none -mx-1 px-1">
          {SUGGESTED_PRODUCTS.slice(0, 6).map((sug, idx) => (
            <button
              key={idx}
              type="button"
              onClick={() => handleApplySuggestion(sug)}
              className="px-2.5 py-1 bg-[#EDF2EE] dark:bg-[#253A33] hover:bg-[#E1E8E3] dark:hover:bg-[#344A40] text-[#19332D] dark:text-[#F0F4EC] rounded-lg text-xs font-semibold whitespace-nowrap transition-colors"
            >
              + {sug.name.split(' (')[0]}
            </button>
          ))}
        </div>
      </div>

      <form onSubmit={handleSave} className="space-y-4">
        {/* Name input */}
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
            placeholder={t('product_name_example')}
            className="w-full px-4 py-3 bg-white dark:bg-[#1A2B26] text-[#19332D] dark:text-[#F0F4EC] rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] text-sm focus:outline-none focus:ring-2 focus:ring-[#267968] dark:focus:ring-[#70CDB0] shadow-sm"
          />
        </div>

        {/* Expiry Date Section */}
        <div className="space-y-2">
          <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC] flex items-center justify-between">
            <span>{t('expiry_date')} *</span>
            <span className="text-[11px] font-normal text-[#5A6B63] dark:text-[#B5C5BC]">
              {t('product_expiry_helper')}
            </span>
          </label>

          {/* Quick Date Presets */}
          <div className="grid grid-cols-5 gap-1.5 text-xs font-semibold">
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
                className="py-1.5 bg-[#EDF2EE] dark:bg-[#253A33] hover:bg-[#E1E8E3] dark:hover:bg-[#344A40] text-[#19332D] dark:text-[#F0F4EC] rounded-xl text-center transition-colors"
              >
                {p.label}
              </button>
            ))}
          </div>

          <div className="relative">
            <input
              type="date"
              value={expDate}
              onChange={(e) => setExpDate(e.target.value)}
              className="w-full px-4 py-3 bg-white dark:bg-[#1A2B26] text-[#19332D] dark:text-[#F0F4EC] rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] text-sm focus:outline-none focus:ring-2 focus:ring-[#267968] dark:focus:ring-[#70CDB0] shadow-sm"
            />
          </div>
        </div>

        {/* Category Selector */}
        <div className="space-y-2">
          <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
            {t('category')}
          </label>
          <div className="grid grid-cols-3 sm:grid-cols-5 gap-2">
            {CATEGORIES.filter((c) => c.id !== 'all').map((cat) => {
              const isSelected = category === cat.id;
              return (
                <button
                  key={cat.id}
                  type="button"
                  onClick={() => setCategory(cat.id)}
                  className={`p-2 rounded-2xl border flex flex-col items-center justify-center gap-1.5 transition-all text-xs font-semibold ${
                    isSelected
                      ? 'border-[#267968] dark:border-[#70CDB0] bg-teal-50 dark:bg-teal-950/40 text-[#267968] dark:text-[#70CDB0] shadow-sm'
                      : 'border-[#E1E8E3] dark:border-[#344A40] bg-white dark:bg-[#1A2B26] text-[#5A6B63] dark:text-[#B5C5BC]'
                  }`}
                >
                  <div
                    className="w-7 h-7 rounded-xl flex items-center justify-center text-white shrink-0"
                    style={{ backgroundColor: cat.color }}
                  >
                    <CategoryIcon category={cat.id} size={16} />
                  </div>
                  <span className="truncate max-w-full text-[11px]">{t(cat.nameKey)}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Quantity & Code */}
        <div className="grid grid-cols-2 gap-3">
          <div className="space-y-1">
            <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
              {t('quantity')}
            </label>
            <input
              type="text"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              placeholder="e.g. 1, 500g, 2L"
              className="w-full px-4 py-2.5 bg-white dark:bg-[#1A2B26] text-[#19332D] dark:text-[#F0F4EC] rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] text-sm focus:outline-none focus:ring-2 focus:ring-[#267968]"
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
              placeholder={t('product_code_example')}
              className="w-full px-4 py-2.5 bg-white dark:bg-[#1A2B26] text-[#19332D] dark:text-[#F0F4EC] rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] text-sm font-mono focus:outline-none focus:ring-2 focus:ring-[#267968]"
            />
          </div>
        </div>

        {/* More details accordion: Note & Photo */}
        <div className="pt-1">
          <button
            type="button"
            onClick={() => setShowMore(!showMore)}
            className="text-xs font-bold text-[#267968] dark:text-[#70CDB0] hover:underline"
          >
            {showMore ? `▲ ${t('product_hide_details')}` : `▼ ${t('product_more_details')}`}
          </button>

          {showMore && (
            <div className="mt-3 space-y-4 bg-white dark:bg-[#1A2B26] p-4 rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] animate-in fade-in">
              <div className="space-y-1">
                <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
                  {t('note')}
                </label>
                <textarea
                  rows={2}
                  value={note}
                  onChange={(e) => setNote(e.target.value)}
                  placeholder={t('product_note_placeholder')}
                  className="w-full px-3 py-2 bg-[#F5F7F4] dark:bg-[#101C19] text-[#19332D] dark:text-[#F0F4EC] rounded-xl border border-[#E1E8E3] dark:border-[#344A40] text-xs focus:outline-none focus:ring-2 focus:ring-[#267968]"
                />
              </div>

              {/* Photo Upload */}
              <div className="space-y-2">
                <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
                  {t('add_photo')}
                </label>
                <div className="flex items-center gap-3">
                  {imageUri ? (
                    <div className="relative w-16 h-16 rounded-xl overflow-hidden border border-[#E1E8E3] dark:border-[#344A40]">
                      <img src={imageUri} alt="Preview" className="w-full h-full object-cover" />
                      <button
                        type="button"
                        onClick={() => setImageUri(null)}
                        className="absolute top-1 right-1 p-0.5 bg-black/70 text-white rounded-full hover:bg-black"
                      >
                        <X className="w-3 h-3" />
                      </button>
                    </div>
                  ) : null}

                  <label className="cursor-pointer inline-flex items-center gap-2 px-3 py-2 bg-[#EDF2EE] dark:bg-[#253A33] text-[#19332D] dark:text-[#F0F4EC] rounded-xl text-xs font-bold hover:bg-[#E1E8E3] dark:hover:bg-[#344A40] transition-colors">
                    <Camera className="w-4 h-4 text-[#267968] dark:text-[#70CDB0]" />
                    <span>{imageUri ? t('product_retake_photo') : t('product_choose_gallery')}</span>
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handleImageChange}
                      className="hidden"
                    />
                  </label>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Submit Save Button */}
        <div className="pt-2">
          <button
            type="submit"
            className="w-full py-3.5 bg-gradient-to-r from-[#267968] to-[#1e6153] hover:from-[#1e6153] hover:to-[#174e42] dark:from-[#70CDB0] dark:to-[#4db394] text-white dark:text-[#0D2623] text-sm font-extrabold rounded-2xl shadow-md transition-all active:scale-[0.98] flex items-center justify-center gap-2"
          >
            <Check className="w-4 h-4" strokeWidth={2.5} />
            <span>{t('save')}</span>
          </button>
        </div>
      </form>
    </div>
  );
};
