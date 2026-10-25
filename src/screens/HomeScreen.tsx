import React from 'react';
import { useApp } from '../context/AppContext';
import { CATEGORIES } from '../theme';
import { formatDate, getDaysLeft, getExpiryStatus } from '../utils/formatters';
import { CategoryIcon } from '../components/CategoryIcon';
import { Plus, ChevronRight, Calendar, AlertTriangle, Clock, CheckCircle2, ArrowRight } from 'lucide-react';

export const HomeScreen: React.FC = () => {
  const {
    products,
    userProfile,
    t,
    locale,
    setActiveTab,
    setSelectedCategory,
    setProductFilter,
    setViewProductDetail,
  } = useApp();

  // Expiry buckets
  const itemsWithDays = products
    .map((p) => ({ product: p, daysLeft: getDaysLeft(p.expDate) }))
    .filter((item) => Number.isFinite(item.daysLeft));

  const todayItems = itemsWithDays.filter((i) => i.daysLeft === 0);
  const approaching = itemsWithDays.filter((i) => i.daysLeft > 0 && i.daysLeft <= 7);
  const overdue = itemsWithDays.filter((i) => i.daysLeft < 0);
  const fresh = itemsWithDays.filter((i) => i.daysLeft > 7);

  const upcomingProducts = [...itemsWithDays]
    .sort((a, b) => a.daysLeft - b.daysLeft)
    .slice(0, 4);

  const openFilter = (filter: 'all' | 'today' | 'approaching' | 'expired') => {
    setSelectedCategory('all');
    setProductFilter(filter);
    setActiveTab('products');
  };

  const getStatusBadge = (daysLeft: number) => {
    const status = getExpiryStatus(daysLeft);
    if (status === 'expired') {
      return (
        <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded-full bg-rose-100 text-rose-800 dark:bg-rose-950/60 dark:text-rose-300">
          <AlertTriangle className="w-3 h-3" />
          {t('status_expired')} ({Math.abs(daysLeft)}d)
        </span>
      );
    }
    if (status === 'today') {
      return (
        <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded-full bg-amber-100 text-amber-800 dark:bg-amber-950/60 dark:text-amber-300 animate-pulse">
          <Clock className="w-3 h-3" />
          {t('home_today')}
        </span>
      );
    }
    if (status === 'approaching') {
      return (
        <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded-full bg-amber-100 text-amber-800 dark:bg-amber-950/60 dark:text-amber-300">
          <Clock className="w-3 h-3" />
          {t('product_status_days', { count: daysLeft })}
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300">
        <CheckCircle2 className="w-3 h-3" />
        {t('status_fresh')} ({daysLeft}d)
      </span>
    );
  };

  return (
    <div className="space-y-6 pb-24">
      {/* User Greeting Bar */}
      <div className="flex items-center justify-between gap-3 bg-white dark:bg-[#1A2B26] p-4 rounded-2xl shadow-sm border border-[#E1E8E3] dark:border-[#344A40]">
        <div className="flex items-center gap-3 min-w-0">
          <div
            className="w-12 h-12 rounded-full flex items-center justify-center text-white text-lg font-black shrink-0 shadow"
            style={{ backgroundColor: userProfile.avatarColor || '#267968' }}
          >
            {userProfile.name ? userProfile.name.charAt(0).toUpperCase() : 'E'}
          </div>
          <div className="min-w-0">
            <h2 className="text-base font-bold text-[#19332D] dark:text-[#F0F4EC] truncate">
              {t('greeting', { name: userProfile.name })} 👋
            </h2>
            <p className="text-xs text-[#5A6B63] dark:text-[#B5C5BC] truncate">
              {t('greeting_sub')}
            </p>
          </div>
        </div>

        <button
          onClick={() => setActiveTab('add')}
          className="flex items-center gap-1.5 px-3.5 py-2 bg-[#267968] hover:bg-[#1f6355] text-white dark:bg-[#70CDB0] dark:text-[#0D2623] dark:hover:bg-[#5dbb9d] text-xs font-bold rounded-xl shadow-sm transition-all active:scale-95 shrink-0"
        >
          <Plus className="w-4 h-4" />
          <span>{t('add')}</span>
        </button>
      </div>

      {/* Main Expiring Soon Highlight Hero Card */}
      <div className="relative overflow-hidden bg-gradient-to-br from-[#267968] to-[#153F38] text-white p-6 rounded-3xl shadow-lg border border-teal-800/40">
        <div className="relative z-10 max-w-sm">
          <span className="text-xs font-semibold tracking-wider uppercase text-[#C4D639] inline-block mb-1">
            {t('home_upcoming_title')}
          </span>
          <div className="flex items-baseline gap-2 mb-1">
            <span className="text-5xl font-black tracking-tight">
              {approaching.length + todayItems.length}
            </span>
            <span className="text-base font-medium text-teal-100">{t('home_product_count')}</span>
          </div>
          <p className="text-xs text-teal-100/90 mb-4">{t('home_next_seven_days')}</p>

          <button
            onClick={() => openFilter('approaching')}
            className="inline-flex items-center gap-2 px-4 py-2 bg-[#C4D639] text-[#18362F] hover:bg-[#b5c732] text-xs font-bold rounded-xl shadow transition-transform active:scale-95"
          >
            <span>{t('home_view_details')}</span>
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>

        {/* Ambient background calendar graphic */}
        <div className="absolute -right-4 -bottom-6 text-white/10 pointer-events-none select-none">
          <Calendar className="w-48 h-48 -rotate-12" />
        </div>
      </div>

      {/* 3 Metric Stat Cards */}
      <div className="grid grid-cols-3 gap-3">
        {/* Today */}
        <button
          onClick={() => openFilter('today')}
          className="bg-white dark:bg-[#1A2B26] p-3.5 rounded-2xl shadow-sm border border-[#E1E8E3] dark:border-[#344A40] border-b-4 border-b-[#267968] text-left hover:shadow-md transition-all active:scale-95"
        >
          <div className="text-2xl font-black text-[#19332D] dark:text-[#F0F4EC]">
            {todayItems.length}
          </div>
          <div className="text-xs font-semibold text-[#5A6B63] dark:text-[#B5C5BC] truncate mt-0.5">
            {t('home_today')}
          </div>
        </button>

        {/* This Week */}
        <button
          onClick={() => openFilter('approaching')}
          className="bg-white dark:bg-[#1A2B26] p-3.5 rounded-2xl shadow-sm border border-[#E1E8E3] dark:border-[#344A40] border-b-4 border-b-[#A86722] text-left hover:shadow-md transition-all active:scale-95"
        >
          <div className="text-2xl font-black text-[#A86722] dark:text-[#E4B66F]">
            {approaching.length}
          </div>
          <div className="text-xs font-semibold text-[#5A6B63] dark:text-[#B5C5BC] truncate mt-0.5">
            {t('home_this_week')}
          </div>
        </button>

        {/* Overdue */}
        <button
          onClick={() => openFilter('expired')}
          className="bg-white dark:bg-[#1A2B26] p-3.5 rounded-2xl shadow-sm border border-[#E1E8E3] dark:border-[#344A40] border-b-4 border-b-[#B8433D] text-left hover:shadow-md transition-all active:scale-95"
        >
          <div className="text-2xl font-black text-[#B8433D] dark:text-[#E58A7F]">
            {overdue.length}
          </div>
          <div className="text-xs font-semibold text-[#5A6B63] dark:text-[#B5C5BC] truncate mt-0.5">
            {t('home_overdue')}
          </div>
        </button>
      </div>

      {/* Category Pills Quick Row */}
      <div>
        <div className="flex items-center justify-between mb-2.5">
          <h3 className="text-sm font-bold text-[#19332D] dark:text-[#F0F4EC]">
            {t('categories')}
          </h3>
          <button
            onClick={() => setActiveTab('categories')}
            className="text-xs font-semibold text-[#267968] dark:text-[#70CDB0] hover:underline flex items-center gap-0.5"
          >
            <span>{t('home_view_all')}</span>
            <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none -mx-1 px-1">
          {CATEGORIES.slice(0, 6).map((cat) => {
            const count = products.filter((p) => p.category === cat.id).length;
            return (
              <button
                key={cat.id}
                onClick={() => {
                  setSelectedCategory(cat.id);
                  setProductFilter('all');
                  setActiveTab('products');
                }}
                className="flex items-center gap-2 px-3 py-2 bg-white dark:bg-[#1A2B26] rounded-xl border border-[#E1E8E3] dark:border-[#344A40] shrink-0 text-xs font-semibold text-[#19332D] dark:text-[#F0F4EC] hover:border-[#267968] transition-colors"
              >
                <div
                  className="w-6 h-6 rounded-lg flex items-center justify-center text-white"
                  style={{ backgroundColor: cat.color }}
                >
                  <CategoryIcon category={cat.id} size={14} className="w-3.5 h-3.5" />
                </div>
                <span>{t(cat.nameKey)}</span>
                <span className="text-[10px] bg-[#EDF2EE] dark:bg-[#253A33] px-1.5 py-0.5 rounded-full text-[#5A6B63] dark:text-[#B5C5BC]">
                  {count}
                </span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Expiring Next List */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-sm font-bold text-[#19332D] dark:text-[#F0F4EC]">
            {t('home_upcoming_products')}
          </h3>
          <button
            onClick={() => {
              setSelectedCategory('all');
              setProductFilter('all');
              setActiveTab('products');
            }}
            className="text-xs font-semibold text-[#267968] dark:text-[#70CDB0] hover:underline flex items-center gap-0.5"
          >
            <span>{t('home_view_all')} ({products.length})</span>
            <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {upcomingProducts.length === 0 ? (
          <div className="bg-white dark:bg-[#1A2B26] p-8 rounded-3xl text-center border border-[#E1E8E3] dark:border-[#344A40]">
            <div className="w-14 h-14 bg-[#EDF2EE] dark:bg-[#253A33] rounded-2xl flex items-center justify-center mx-auto mb-3 text-[#267968] dark:text-[#70CDB0]">
              <Calendar className="w-7 h-7" />
            </div>
            <h4 className="text-base font-bold text-[#19332D] dark:text-[#F0F4EC] mb-1">
              {t('home_empty_title')}
            </h4>
            <p className="text-xs text-[#5A6B63] dark:text-[#B5C5BC] max-w-xs mx-auto mb-4">
              {t('home_empty_description')}
            </p>
            <button
              onClick={() => setActiveTab('add')}
              className="inline-flex items-center gap-2 px-4 py-2 bg-[#267968] dark:bg-[#70CDB0] text-white dark:text-[#0D2623] text-xs font-bold rounded-xl shadow"
            >
              <Plus className="w-4 h-4" />
              <span>{t('home_empty_action')}</span>
            </button>
          </div>
        ) : (
          <div className="space-y-2.5">
            {upcomingProducts.map(({ product, daysLeft }) => {
              const catObj = CATEGORIES.find((c) => c.id === product.category) || CATEGORIES[CATEGORIES.length - 1];

              return (
                <div
                  key={product.id}
                  onClick={() => setViewProductDetail(product)}
                  className="bg-white dark:bg-[#1A2B26] p-3.5 rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm flex items-center justify-between gap-3 cursor-pointer hover:border-[#267968] dark:hover:border-[#70CDB0] transition-all active:scale-[0.99]"
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div
                      className="w-11 h-11 rounded-xl flex items-center justify-center text-white shrink-0 shadow-sm"
                      style={{ backgroundColor: catObj.color }}
                    >
                      <CategoryIcon category={product.category} size={20} />
                    </div>

                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <h4 className="text-sm font-bold text-[#19332D] dark:text-[#F0F4EC] truncate">
                          {product.name}
                        </h4>
                        {product.quantity && (
                          <span className="text-[11px] text-[#5A6B63] dark:text-[#B5C5BC] bg-[#EDF2EE] dark:bg-[#253A33] px-1.5 py-0.5 rounded">
                            {product.quantity}
                          </span>
                        )}
                      </div>
                      <div className="flex items-center gap-2 mt-0.5 text-xs text-[#5A6B63] dark:text-[#B5C5BC]">
                        <span>{formatDate(product.expDate, locale)}</span>
                        {product.productCode && (
                          <>
                            <span>•</span>
                            <span className="font-mono text-[11px]">{product.productCode}</span>
                          </>
                        )}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 shrink-0">
                    {getStatusBadge(daysLeft)}
                    <ChevronRight className="w-4 h-4 text-[#5A6B63] dark:text-[#B5C5BC]" />
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Freshness Banner link to insights */}
      <div
        onClick={() => setActiveTab('stats')}
        className="bg-[#EDF2EE] dark:bg-[#253A33] p-4 rounded-2xl flex items-center justify-between gap-3 cursor-pointer hover:opacity-90 transition-opacity"
      >
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#267968]/20 dark:bg-[#70CDB0]/20 text-[#267968] dark:text-[#70CDB0] flex items-center justify-center">
            <CheckCircle2 className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
              {t('home_statistics')}
            </h4>
            <p className="text-[11px] text-[#5A6B63] dark:text-[#B5C5BC]">
              {t('home_statistics_desc')}
            </p>
          </div>
        </div>
        <ArrowRight className="w-4 h-4 text-[#267968] dark:text-[#70CDB0]" />
      </div>
    </div>
  );
};
