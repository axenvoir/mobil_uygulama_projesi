import React from 'react';
import { useApp } from '../context/AppContext';
import { CATEGORIES } from '../theme';
import { getDaysLeft } from '../utils/formatters';
import { CategoryIcon } from '../components/CategoryIcon';
import { ChevronRight, Info } from 'lucide-react';

export const CategoriesScreen: React.FC = () => {
  const { products, t, setSelectedCategory, setProductFilter, setActiveTab } = useApp();

  const nonAllCategories = CATEGORIES.filter((c) => c.id !== 'all');

  const onSelectCategory = (catId: string) => {
    setSelectedCategory(catId);
    setProductFilter('all');
    setActiveTab('products');
  };

  return (
    <div className="space-y-5 pb-24 max-w-lg mx-auto">
      <div>
        <h2 className="text-xl font-extrabold text-[#19332D] dark:text-[#F0F4EC]">
          {t('categories')}
        </h2>
        <p className="text-xs text-[#5A6B63] dark:text-[#B5C5BC] mt-0.5">
          {t('categories_summary', {
            categories: nonAllCategories.length,
            products: products.length,
          })}
        </p>
      </div>

      {/* Helpful Tip Box */}
      <div className="bg-[#EDF2EE] dark:bg-[#253A33] p-4 rounded-2xl flex items-start gap-3 text-xs text-[#19332D] dark:text-[#F0F4EC]">
        <Info className="w-4 h-4 text-[#267968] dark:text-[#70CDB0] shrink-0 mt-0.5" />
        <p className="leading-relaxed">{t('categories_tip')}</p>
      </div>

      {/* Category Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {nonAllCategories.map((cat) => {
          const categoryProducts = products.filter((p) => p.category === cat.id);
          const urgentCount = categoryProducts.filter((p) => {
            const days = getDaysLeft(p.expDate);
            return days <= 7;
          }).length;

          return (
            <div
              key={cat.id}
              onClick={() => onSelectCategory(cat.id)}
              className="bg-white dark:bg-[#1A2B26] p-4 rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm hover:border-[#267968] dark:hover:border-[#70CDB0] cursor-pointer transition-all active:scale-[0.98] flex items-center justify-between gap-3"
            >
              <div className="flex items-center gap-3 min-w-0">
                <div
                  className="w-11 h-11 rounded-xl flex items-center justify-center text-white shrink-0 shadow-sm"
                  style={{ backgroundColor: cat.color }}
                >
                  <CategoryIcon category={cat.id} size={20} />
                </div>

                <div className="min-w-0">
                  <h4 className="text-sm font-bold text-[#19332D] dark:text-[#F0F4EC] truncate">
                    {t(cat.nameKey)}
                  </h4>
                  <div className="flex items-center gap-2 mt-0.5 text-xs text-[#5A6B63] dark:text-[#B5C5BC]">
                    <span>{categoryProducts.length} {t('home_product_count')}</span>
                    {urgentCount > 0 && (
                      <span className="text-[10px] font-bold text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-950/40 px-1.5 py-0.2 rounded-full">
                        {urgentCount} {t('approaching').toLowerCase()}
                      </span>
                    )}
                  </div>
                </div>
              </div>

              <ChevronRight className="w-4 h-4 text-[#5A6B63] dark:text-[#B5C5BC] shrink-0" />
            </div>
          );
        })}
      </div>
    </div>
  );
};
