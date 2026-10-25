import * as React from 'react';
import {getLocales} from 'react-native-localize';
import {tr, type TranslationKey} from './tr';
import {en} from './en';

export type LanguageCode = 'tr' | 'en';

const TRANSLATIONS: Record<LanguageCode, Record<TranslationKey, string>> = {tr, en};

export const resolveDeviceLocale = (): LanguageCode => {
  try {
    const locales = getLocales();
    return locales?.[0]?.languageCode === 'tr' ? 'tr' : 'en';
  } catch {
    return 'en';
  }
};

export const resolveLocale = (pref: 'tr' | 'en' | 'system'): LanguageCode =>
  pref === 'system' ? resolveDeviceLocale() : pref;

export const translate = (locale: LanguageCode, key: TranslationKey): string =>
  TRANSLATIONS[locale]?.[key] ?? TRANSLATIONS.en[key] ?? key;

export const useT = (locale: LanguageCode) =>
  React.useCallback((key: TranslationKey) => translate(locale, key), [locale]);
