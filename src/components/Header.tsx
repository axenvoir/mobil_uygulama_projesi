import React from 'react';
import { useApp } from '../context/AppContext';
import { BarChart3, Sun, Moon, Layers, User } from 'lucide-react';

export const Header: React.FC = () => {
  const { t, userProfile, changeLanguage, isDark, toggleTheme, activeTab, setActiveTab } = useApp();

  return (
    <header className="sticky top-0 z-40 bg-[#F5F7F4]/90 dark:bg-[#101C19]/90 backdrop-blur-md border-b border-[#E1E8E3] dark:border-[#344A40] transition-colors">
      <div className="max-w-2xl mx-auto px-4 h-16 flex items-center justify-between">
        {/* Brand */}
        <button
          onClick={() => setActiveTab('home')}
          className="flex items-center gap-2.5 text-left group transition-transform active:scale-95"
        >
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-[#267968] to-[#3aa790] dark:from-[#70CDB0] dark:to-[#4bb393] flex items-center justify-center text-white dark:text-[#102B26] shadow-sm font-bold text-lg">
            <span className="tracking-tighter">Ex</span>
          </div>
          <div>
            <h1 className="font-extrabold text-xl leading-tight text-[#19332D] dark:text-[#F0F4EC] tracking-tight">
              Expira
            </h1>
            <p className="text-[11px] font-medium text-[#5A6B63] dark:text-[#B5C5BC] truncate max-w-[170px] sm:max-w-none">
              {t('tagline')}
            </p>
          </div>
        </button>

        {/* Right utility buttons */}
        <div className="flex items-center gap-1.5">
          {/* Language Toggle */}
          <button
            onClick={() => changeLanguage(userProfile.language === 'tr' ? 'en' : 'tr')}
            className="px-2 py-1 text-xs font-bold rounded-lg border border-[#E1E8E3] dark:border-[#344A40] text-[#19332D] dark:text-[#F0F4EC] hover:bg-[#EDF2EE] dark:hover:bg-[#1A2B26] transition-colors uppercase"
            title="Change language / Dil değiştir"
          >
            {userProfile.language === 'tr' ? 'TR' : 'EN'}
          </button>

          {/* Theme Toggle */}
          <button
            onClick={toggleTheme}
            className="p-2 rounded-xl text-[#5A6B63] dark:text-[#B5C5BC] hover:bg-[#EDF2EE] dark:hover:bg-[#1A2B26] transition-colors"
            title={isDark ? 'Açık Mod (Light)' : 'Karanlık Mod (Dark)'}
            aria-label="Toggle theme"
          >
            {isDark ? <Sun className="w-5 h-5 text-amber-300" /> : <Moon className="w-5 h-5" />}
          </button>

          {/* Categories icon */}
          <button
            onClick={() => setActiveTab('categories')}
            className={`p-2 rounded-xl transition-colors ${
              activeTab === 'categories'
                ? 'bg-[#267968]/15 text-[#267968] dark:text-[#70CDB0]'
                : 'text-[#5A6B63] dark:text-[#B5C5BC] hover:bg-[#EDF2EE] dark:hover:bg-[#1A2B26]'
            }`}
            title={t('categories')}
            aria-label="Categories"
          >
            <Layers className="w-5 h-5" />
          </button>

          {/* Statistics icon */}
          <button
            onClick={() => setActiveTab('stats')}
            className={`p-2 rounded-xl transition-colors ${
              activeTab === 'stats'
                ? 'bg-[#267968]/15 text-[#267968] dark:text-[#70CDB0]'
                : 'text-[#5A6B63] dark:text-[#B5C5BC] hover:bg-[#EDF2EE] dark:hover:bg-[#1A2B26]'
            }`}
            title={t('statistics')}
            aria-label="Statistics"
          >
            <BarChart3 className="w-5 h-5" />
          </button>

          {/* Profile / Avatar */}
          <button
            onClick={() => setActiveTab('profile')}
            className="ml-1 relative rounded-full p-0.5 border-2 border-transparent hover:border-[#267968] dark:hover:border-[#70CDB0] transition-colors"
            title={t('profile')}
          >
            <div
              className="w-8 h-8 rounded-full flex items-center justify-center text-white text-xs font-bold shadow-sm"
              style={{ backgroundColor: userProfile.avatarColor || '#267968' }}
            >
              {userProfile.name ? userProfile.name.charAt(0).toUpperCase() : <User className="w-4 h-4" />}
            </div>
          </button>
        </div>
      </div>
    </header>
  );
};
