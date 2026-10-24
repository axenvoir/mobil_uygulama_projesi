import React from 'react';
import { useApp } from '../context/AppContext';
import { Home, Package, Plus, StickyNote, Bell } from 'lucide-react';

export const CustomTabBar: React.FC = () => {
  const { activeTab, setActiveTab, t, products } = useApp();

  // Count items approaching or overdue
  const urgentCount = products.filter((p) => {
    const d = new Date(p.expDate).getTime() - new Date().setHours(0, 0, 0, 0);
    const days = Math.round(d / 86400000);
    return days <= 7;
  }).length;

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-white/95 dark:bg-[#172720]/95 backdrop-blur-lg border-t border-[#E1E8E3] dark:border-[#344A40] shadow-[0_-4px_16px_rgba(0,0,0,0.04)]">
      <div className="max-w-2xl mx-auto px-3 h-16 flex items-center justify-around relative">
        {/* Home */}
        <button
          onClick={() => setActiveTab('home')}
          className={`flex flex-col items-center justify-center flex-1 h-full transition-colors active:scale-95 ${
            activeTab === 'home'
              ? 'text-[#267968] dark:text-[#8BD8BA] font-bold'
              : 'text-[#5F6F67] dark:text-[#83958A] font-medium'
          }`}
        >
          <Home className="w-5 h-5 mb-0.5" strokeWidth={activeTab === 'home' ? 2.5 : 2} />
          <span className="text-[11px]">{t('home')}</span>
        </button>

        {/* Products */}
        <button
          onClick={() => setActiveTab('products')}
          className={`flex flex-col items-center justify-center flex-1 h-full transition-colors active:scale-95 ${
            activeTab === 'products'
              ? 'text-[#267968] dark:text-[#8BD8BA] font-bold'
              : 'text-[#5F6F67] dark:text-[#83958A] font-medium'
          }`}
        >
          <Package className="w-5 h-5 mb-0.5" strokeWidth={activeTab === 'products' ? 2.5 : 2} />
          <span className="text-[11px]">{t('products')}</span>
        </button>

        {/* Center Floating + Add Button */}
        <div className="flex-1 flex justify-center -translate-y-3">
          <button
            onClick={() => setActiveTab('add')}
            className={`w-14 h-14 rounded-full flex items-center justify-center shadow-lg transition-transform active:scale-90 ${
              activeTab === 'add'
                ? 'bg-[#C4D639] text-[#18362F] ring-4 ring-[#267968]/30 scale-105'
                : 'bg-gradient-to-tr from-[#267968] to-[#2f8f7c] dark:from-[#70CDB0] dark:to-[#4db596] text-white dark:text-[#0D2623]'
            }`}
            title={t('nav_add')}
            aria-label="Add product"
          >
            <Plus className="w-7 h-7" strokeWidth={2.8} />
          </button>
        </div>

        {/* Notes */}
        <button
          onClick={() => setActiveTab('notes')}
          className={`flex flex-col items-center justify-center flex-1 h-full transition-colors active:scale-95 ${
            activeTab === 'notes'
              ? 'text-[#267968] dark:text-[#8BD8BA] font-bold'
              : 'text-[#5F6F67] dark:text-[#83958A] font-medium'
          }`}
        >
          <StickyNote className="w-5 h-5 mb-0.5" strokeWidth={activeTab === 'notes' ? 2.5 : 2} />
          <span className="text-[11px]">{t('notes')}</span>
        </button>

        {/* Alerts / Notifications */}
        <button
          onClick={() => setActiveTab('alerts')}
          className={`flex flex-col items-center justify-center flex-1 h-full relative transition-colors active:scale-95 ${
            activeTab === 'alerts'
              ? 'text-[#267968] dark:text-[#8BD8BA] font-bold'
              : 'text-[#5F6F67] dark:text-[#83958A] font-medium'
          }`}
        >
          <div className="relative">
            <Bell className="w-5 h-5 mb-0.5" strokeWidth={activeTab === 'alerts' ? 2.5 : 2} />
            {urgentCount > 0 && (
              <span className="absolute -top-1 -right-1 w-4 h-4 bg-rose-500 text-white rounded-full text-[9px] font-bold flex items-center justify-center">
                {urgentCount > 9 ? '9+' : urgentCount}
              </span>
            )}
          </div>
          <span className="text-[11px]">{t('nav_notifications')}</span>
        </button>
      </div>
    </nav>
  );
};
