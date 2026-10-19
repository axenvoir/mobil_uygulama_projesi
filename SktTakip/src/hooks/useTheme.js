import { useContext } from 'react';
import { ThemeContext } from '../context/ThemeContext';

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    // Fallback: eğer Provider yoksa light tema döndür
    const { LIGHT_COLORS } = require('../theme');
    return { mode: 'light', isDark: false, colors: LIGHT_COLORS, changeMode: () => {}, loaded: true };
  }
  return context;
};