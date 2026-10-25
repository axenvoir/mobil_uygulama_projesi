import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import {AppState} from 'react-native';
import type {AppData, Note, Product, Settings} from '../types';
import {loadData, saveData, clearData} from '../utils/storage';
import {normalizeSettings, createId} from '../utils/validators';
import {resolveLocale, type LanguageCode} from '../i18n';
import {setHapticsEnabled} from '../utils/haptics';
import {scheduleDailyReminder} from '../utils/notifications';

interface AppContextValue {
  ready: boolean;
  products: Product[];
  notes: Note[];
  settings: Settings;
  locale: LanguageCode;
  addProduct: (input: Omit<Product, 'id' | 'createdAt' | 'updatedAt'>) => Product;
  updateProduct: (id: string, patch: Partial<Product>) => void;
  deleteProduct: (id: string) => void;
  addNote: (input: {title: string; body: string}) => Note;
  updateNote: (id: string, patch: Partial<Note>) => void;
  deleteNote: (id: string) => void;
  updateSettings: (patch: Partial<Settings>) => void;
  clearAllData: () => Promise<void>;
}

const AppContext = createContext<AppContextValue | null>(null);

const now = () => Date.now();

export const AppProvider: React.FC<{children: React.ReactNode}> = ({children}) => {
  const [data, setData] = useState<AppData | null>(null);
  const dataRef = useRef<AppData | null>(null);
  dataRef.current = data;
  const [appState, setAppState] = useState(AppState.currentState);

  useEffect(() => {
    let mounted = true;
    (async () => {
      const loaded = await loadData();
      if (mounted) {
        setData(loaded);
        setHapticsEnabled(loaded.settings.hapticsEnabled);
      }
    })();
    const sub = AppState.addEventListener('change', setAppState);
    return () => {
      mounted = false;
      sub.remove();
    };
  }, []);

  // Persist + sync side effects whenever data changes
  useEffect(() => {
    if (!data) {
      return;
    }
    saveData(data).catch(() => undefined);
    scheduleDailyReminder(data.settings).catch(() => undefined);
  }, [data]);

  // Re-check reminder when returning to foreground (missed triggers)
  const reminderHour = data?.settings.reminderHour;
  const reminderMinute = data?.settings.reminderMinute;
  const reminderEnabled = data?.settings.dailyReminder;
  useEffect(() => {
    if (appState === 'active' && reminderEnabled !== undefined) {
      scheduleDailyReminder({
        dailyReminder: reminderEnabled,
        reminderHour: reminderHour ?? 9,
        reminderMinute: reminderMinute ?? 0,
        language: 'system',
        hapticsEnabled: true,
      }).catch(() => undefined);
    }
  }, [appState, reminderEnabled, reminderHour, reminderMinute]);

  const addProduct = useCallback<AppContextValue['addProduct']>(input => {
    const product: Product = {...input, id: createId(), createdAt: now(), updatedAt: now()};
    setData(d => (d ? {...d, products: [product, ...d.products]} : d));
    return product;
  }, []);

  const updateProduct = useCallback<AppContextValue['updateProduct']>((id, patch) => {
    setData(d =>
      d
        ? {
            ...d,
            products: d.products.map(p =>
              p.id === id ? {...p, ...patch, updatedAt: now()} : p,
            ),
          }
        : d,
    );
  }, []);

  const deleteProduct = useCallback<AppContextValue['deleteProduct']>(id => {
    setData(d => (d ? {...d, products: d.products.filter(p => p.id !== id)} : d));
  }, []);

  const addNote = useCallback<AppContextValue['addNote']>(input => {
    const note: Note = {...input, id: createId(), createdAt: now(), updatedAt: now()};
    setData(d => (d ? {...d, notes: [note, ...d.notes]} : d));
    return note;
  }, []);

  const updateNote = useCallback<AppContextValue['updateNote']>((id, patch) => {
    setData(d =>
      d
        ? {...d, notes: d.notes.map(n => (n.id === id ? {...n, ...patch, updatedAt: now()} : n))}
        : d,
    );
  }, []);

  const deleteNote = useCallback<AppContextValue['deleteNote']>(id => {
    setData(d => (d ? {...d, notes: d.notes.filter(n => n.id !== id)} : d));
  }, []);

  const updateSettings = useCallback<AppContextValue['updateSettings']>(patch => {
    setData(d => (d ? {...d, settings: normalizeSettings({...d.settings, ...patch})} : d));
    if (patch.hapticsEnabled !== undefined) {
      setHapticsEnabled(patch.hapticsEnabled);
    }
  }, []);

  const clearAllData = useCallback(async () => {
    await clearData();
    setData({version: 1, products: [], notes: [], settings: normalizeSettings(undefined)});
  }, []);

  const value = useMemo<AppContextValue | null>(() => {
    if (!data) {
      return null;
    }
    return {
      ready: true,
      products: data.products,
      notes: data.notes,
      settings: data.settings,
      locale: resolveLocale(data.settings.language),
      addProduct,
      updateProduct,
      deleteProduct,
      addNote,
      updateNote,
      deleteNote,
      updateSettings,
      clearAllData,
    };
  }, [data, addProduct, updateProduct, deleteProduct, addNote, updateNote, deleteNote, updateSettings, clearAllData]);

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
};

export const useApp = (): AppContextValue => {
  const ctx = useContext(AppContext);
  if (!ctx) {
    throw new Error('useApp must be used within AppProvider');
  }
  return ctx;
};
