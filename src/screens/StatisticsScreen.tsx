import React from 'react';
import { useApp } from '../context/AppContext';
import { CATEGORIES } from '../theme';
import { formatDate, getDaysLeft } from '../utils/formatters';
import { CategoryIcon } from '../components/CategoryIcon';
import {
  TrendingUp,
  AlertTriangle,
  CheckCircle2,
  Clock,
  Package,
} from 'lucide-react';

export const StatisticsScreen: React.FC = () => {
  const { products, locale, t, setViewProductDetail } = useApp();

  const itemsWithDays = products.map((p) => ({
    product: p,
    days: getDaysLeft(p.expDate),
  }));

  const total = products.length;
  const expired = itemsWithDays.filter((i) => i.days < 0).length;
  const today = itemsWithDays.filter((i) => i.days === 0).length;
  const approaching = itemsWithDays.filter((i) => i.days > 0 && i.days <= 7).length;
  const fresh = itemsWithDays.filter((i) => i.days > 7).length;

  // Freshness score calculation
  const freshnessScore = total > 0 ? Math.round(((fresh + approaching * 0.5) / total) * 100) : 100;

  const getScoreMessage = () => {
    if (freshnessScore >= 80) return t('statistics_score_great');
    if (freshnessScore >= 50) return t('statistics_score_ok');
    return t('statistics_score_low');
  };

  const getScoreColor = () => {
    if (freshnessScore >= 80) return 'text-[#267968] dark:text-[#70CDB0]';
    if (freshnessScore >= 50) return 'text-amber-500';
    return 'text-rose-500';
  };

  // Category breakdown
  const categoryStats = CATEGORIES.filter((c) => c.id !== 'all')
    .map((cat) => {
      const count = products.filter((p) => p.category === cat.id).length;
      const percentage = total > 0 ? Math.round((count / total) * 100) : 0;
      return { ...cat, count, percentage };
    })
    .filter((c) => c.count > 0)
    .sort((a, b) => b.count - a.count);

  return (
    <div className="space-y-5 pb-24 max-w-lg mx-auto">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-extrabold text-[#19332D] dark:text-[#F0F4EC]">
          {t('statistics')}
        </h2>
      </div>

      {/* Freshness Score Card */}
      <div className="bg-white dark:bg-[#1A2B26] p-6 rounded-3xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm flex flex-col items-center text-center space-y-3">
        <span className="text-xs font-bold text-[#5A6B63] dark:text-[#B5C5BC] uppercase tracking-wider">
          {t('statistics_freshness_score')}
        </span>

        {/* Circular / Big Metric */}
        <div className="relative flex items-center justify-center">
          <div className="w-32 h-32 rounded-full border-8 border-[#EDF2EE] dark:border-[#253A33] flex items-center justify-center relative">
            <span className={`text-4xl font-black ${getScoreColor()}`}>
              {freshnessScore}%
            </span>
          </div>
        </div>

        <p className="text-xs font-semibold text-[#19332D] dark:text-[#F0F4EC] max-w-xs">
          {getScoreMessage()}
        </p>
      </div>

      {/* 4 Summary Stats */}
      <div className="grid grid-cols-2 gap-3">
        <div className="bg-white dark:bg-[#1A2B26] p-4 rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm">
          <div className="flex items-center gap-2 text-[#5A6B63] dark:text-[#B5C5BC] text-xs font-bold mb-1">
            <Package className="w-4 h-4 text-[#267968]" />
            <span>{t('profile_total')}</span>
          </div>
          <div className="text-2xl font-black text-[#19332D] dark:text-[#F0F4EC]">{total}</div>
        </div>

        <div className="bg-white dark:bg-[#1A2B26] p-4 rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm">
          <div className="flex items-center gap-2 text-emerald-600 dark:text-emerald-400 text-xs font-bold mb-1">
            <CheckCircle2 className="w-4 h-4" />
            <span>{t('status_fresh')}</span>
          </div>
          <div className="text-2xl font-black text-emerald-600 dark:text-emerald-400">{fresh}</div>
        </div>

        <div className="bg-white dark:bg-[#1A2B26] p-4 rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm">
          <div className="flex items-center gap-2 text-amber-600 dark:text-amber-400 text-xs font-bold mb-1">
            <Clock className="w-4 h-4" />
            <span>{t('status_approaching')}</span>
          </div>
          <div className="text-2xl font-black text-amber-600 dark:text-amber-400">{approaching + today}</div>
        </div>

        <div className="bg-white dark:bg-[#1A2B26] p-4 rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm">
          <div className="flex items-center gap-2 text-rose-600 dark:text-rose-400 text-xs font-bold mb-1">
            <AlertTriangle className="w-4 h-4" />
            <span>{t('status_expired')}</span>
          </div>
          <div className="text-2xl font-black text-rose-600 dark:text-rose-400">{expired}</div>
        </div>
      </div>

      {/* Category Distribution */}
      <div className="bg-white dark:bg-[#1A2B26] p-5 rounded-3xl border border-[#E1E8E3] dark:border-[#344A40] shadow-sm space-y-4">
        <h3 className="text-sm font-bold text-[#19332D] dark:text-[#F0F4EC]">
          {t('statistics_category_distribution')}
        </h3>

        {categoryStats.length === 0 ? (
          <div className="text-xs text-[#5A6B63] dark:text-[#B5C5BC] text-center py-4">
            {t('statistics_no_data')}
          </div>
        ) : (
          <div className="space-y-3">
            {categoryStats.map((cat) => (
              <div key={cat.id} className="space-y-1">
                <div className="flex items-center justify-between text-xs">
                  <div className="flex items-center gap-2">
                    <div
                      className="w-5 h-5 rounded-md flex items-center justify-center text-white shrink-0"
                      style={{ backgroundColor: cat.color }}
                    >
                      <CategoryIcon category={cat.id} size={12} />
                    </div>
                    <span className="font-bold text-[#19332D] dark:text-[#F0F4EC]">
                      {t(cat.nameKey)}
                    </span>
                  </div>
                  <span className="text-[#5A6B63] dark:text-[#B5C5BC] font-medium">
                    {cat.count} ({cat.percentage}%)
                  </span>
                </div>
                {/* Progress bar */}
                <div className="h-2 w-full bg-[#EDF2EE] dark:bg-[#253A33] rounded-full overflow-hidden">
                  <div
                    className="h-full rounded-full transition-all duration-500"
                    style={{
                      width: `${cat.percentage}%`,
                      backgroundColor: cat.color,
                    }}
                  />
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
