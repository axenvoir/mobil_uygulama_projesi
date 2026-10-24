import { I18n } from 'i18n-js';
import * as Localization from 'expo-localization';
import tr from '../translations/tr.json';
import en from '../translations/en.json';

const i18n = new I18n({ tr, en });

const DEFAULT_LOCALE = 'en';

export const resolveDeviceLocale = () => {
  try {
    const locales = Localization.getLocales();
    const deviceLocale = locales?.[0]?.languageCode;
    return deviceLocale === 'tr' ? 'tr' : 'en';
  } catch (_error) {
    return 'en';
  }
};

export const resolveLocale = (lang) => (lang === 'system' ? resolveDeviceLocale() : lang);

i18n.defaultLocale = DEFAULT_LOCALE;
try {
  i18n.locale = resolveDeviceLocale();
} catch (_error) {
  i18n.locale = DEFAULT_LOCALE;
}
i18n.enableFallback = true;

export default i18n;
