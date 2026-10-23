import { I18n } from 'i18n-js';
import * as Localization from 'expo-localization';
import tr from '../translations/tr.json';
import en from '../translations/en.json';

const i18n = new I18n({ tr, en });


const deviceLocale = Localization.getLocales()[0]?.languageCode || 'tr';
i18n.defaultLocale = 'tr';
i18n.locale = deviceLocale === 'tr' ? 'tr' : 'en';
i18n.enableFallback = true;

export default i18n;