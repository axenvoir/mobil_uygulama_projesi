import React, { createContext, useState, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useColorScheme } from 'react-native';
import { LIGHT_COLORS, DARK_COLORS } from '../theme';

export const ThemeContext = createContext();

export const ThemeProvider = ({ children }) => {
  const systemScheme = useColorScheme();
  const [mode, setMode] = useState('system');
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const stored = await AsyncStorage.getItem('@themeMode');
        if (stored && ['light', 'dark', 'system'].includes(stored)) {
          setMode(stored);
        }
      } catch (e) {
        if (__DEV__) console.warn('Theme load error:', e);
      }
      setLoaded(true);
    })();
  }, []);

  const changeMode = async (newMode) => {
    setMode(newMode);
    try {
      await AsyncStorage.setItem('@themeMode', newMode);
    } catch (e) {
      if (__DEV__) console.warn('Theme save error:', e);
    }
  };

  const isDark = mode === 'dark' || (mode === 'system' && systemScheme === 'dark');
  const colors = isDark ? DARK_COLORS : LIGHT_COLORS;

  return (
    <ThemeContext.Provider value={{ mode, isDark, colors, changeMode, loaded }}>
      {children}
    </ThemeContext.Provider>
  );
};