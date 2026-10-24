import trTranslations from '../translations/tr.json';
import enTranslations from '../translations/en.json';

const translations: Record<string, Record<string, string>> = {
  tr: trTranslations,
  en: enTranslations,
};

export const resolveLocale = (language: string): 'tr' | 'en' => {
  if (language === 'tr') return 'tr';
  if (language === 'en') return 'en';
  // If system, check navigator
  if (typeof navigator !== 'undefined' && navigator.language?.toLowerCase().startsWith('tr')) {
    return 'tr';
  }
  return 'en';
};

export const translate = (key: string, locale: 'tr' | 'en' = 'en', params?: Record<string, string | number>): string => {
  const dict = translations[locale] || translations.en;
  let text = dict[key] || translations.en[key] || key;

  if (params) {
    Object.entries(params).forEach(([paramKey, val]) => {
      text = text.replace(new RegExp(`%\\{${paramKey}\\}`, 'g'), String(val));
      text = text.replace(new RegExp(`\\{\\{${paramKey}\\}\\}`, 'g'), String(val));
    });
  }

  return text;
};
