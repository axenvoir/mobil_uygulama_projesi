import { useContext } from 'react';
import i18n from '../utils/i18n';
import { AppContext } from '../context/AppContext';

export const useTranslation = () => {
  const { locale } = useContext(AppContext);
  const t = (key, options) => i18n.t(key, options);

  return { t, locale };
};
