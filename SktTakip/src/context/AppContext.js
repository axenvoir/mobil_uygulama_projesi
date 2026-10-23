import React, { createContext, useState, useEffect, useRef, useCallback } from 'react';
import { AppState } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import i18n, { resolveLocale, resolveDeviceLocale } from '../utils/i18n';
import { normalizeNoteList, normalizeProductList } from '../utils/validators';
import { syncRemindersFromSettings } from '../utils/notifications';

const DEFAULT_SETTINGS = {
  dailyReminder: false,
  reminderTime: '09:00',
  instantNotifications: true,
  hapticsEnabled: true,
};

const DEFAULT_PROFILE = {
  name: i18n.t('default_profile_name'),
  avatarColor: '#14B8A6',
  language: 'system',
  biometricsEnabled: false,
};

const DATA_KEYS = ['@products', '@notes', '@settings', '@userProfile'];
const createId = () => `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;

const parseStoredValue = (value, key, fallback) => {
  if (value === null) return fallback;
  try {
    return JSON.parse(value);
  } catch (error) {
    if (__DEV__) console.error(`Local data could not be read (${key}):`, error);
    return fallback;
  }
};

const normalizeSettings = (loadedSettings) => {
  const merged = { ...DEFAULT_SETTINGS, ...(loadedSettings || {}) };
  if (typeof merged.dailyReminder !== 'boolean') merged.dailyReminder = DEFAULT_SETTINGS.dailyReminder;
  if (typeof merged.instantNotifications !== 'boolean') merged.instantNotifications = DEFAULT_SETTINGS.instantNotifications;
  if (typeof merged.hapticsEnabled !== 'boolean') merged.hapticsEnabled = DEFAULT_SETTINGS.hapticsEnabled;
  if (
    typeof merged.reminderTime !== 'string'
    || !/^\d{2}:\d{2}$/.test(merged.reminderTime)
    || Number(merged.reminderTime.slice(0, 2)) > 23
    || Number(merged.reminderTime.slice(3, 5)) > 59
  ) {
    merged.reminderTime = DEFAULT_SETTINGS.reminderTime;
  }
  if (merged.notificationMessage !== undefined && typeof merged.notificationMessage !== 'string') {
    delete merged.notificationMessage;
  }
  return merged;
};

const normalizeProfile = (loadedProfile) => {
  const profile = { ...DEFAULT_PROFILE, ...(loadedProfile || {}) };
  if (typeof profile.name !== 'string') profile.name = DEFAULT_PROFILE.name;
  if (typeof profile.avatarColor !== 'string' || !/^#[\da-f]{6}$/i.test(profile.avatarColor)) {
    profile.avatarColor = DEFAULT_PROFILE.avatarColor;
  }
  if (!['system', 'tr', 'en'].includes(profile.language)) profile.language = DEFAULT_PROFILE.language;
  if (typeof profile.biometricsEnabled !== 'boolean') profile.biometricsEnabled = false;
  return profile;
};

export const AppContext = createContext();

export const AppProvider = ({ children }) => {
  const [products, setProducts] = useState([]);
  const [notes, setNotes] = useState([]);
  const [settings, setSettings] = useState(DEFAULT_SETTINGS);
  const [userProfile, setUserProfile] = useState(DEFAULT_PROFILE);
  const [locale, setLocale] = useState(i18n.locale);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [loadAttempt, setLoadAttempt] = useState(0);
  const [toast, setToast] = useState(null);
  const [isLocked, setIsLocked] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState('All');

  const undoStack = useRef(null);
  const undoTimer = useRef(null);
  const toastTimer = useRef(null);
  const appState = useRef(AppState.currentState);
  const backgroundTime = useRef(null);
  const settingsRef = useRef(settings);
  const localeRef = useRef(locale);

  useEffect(() => {
    settingsRef.current = settings;
    localeRef.current = locale;
  }, [settings, locale]);

  const showToast = useCallback((message, type = 'success', duration = 2500, action = null) => {
    if (toastTimer.current) clearTimeout(toastTimer.current);
    const id = createId();
    setToast({ message, type, id, action });
    toastTimer.current = setTimeout(() => {
      setToast(current => current?.id === id ? null : current);
    }, duration);
  }, []);

  const hideToast = useCallback(() => {
    if (toastTimer.current) clearTimeout(toastTimer.current);
    setToast(null);
  }, []);

  const queueSave = async (data) => {
    try {
      const entries = [
        ['@products', JSON.stringify(data.products)],
        ['@notes', JSON.stringify(data.notes)],
        ['@settings', JSON.stringify(data.settings)],
        ['@userProfile', JSON.stringify(data.userProfile)],
      ];
      await AsyncStorage.multiSet(entries);
    } catch (error) {
      if (__DEV__) console.error('Data save error:', error);
      throw error;
    }
  };

  useEffect(() => {
    let mounted = true;

    const loadData = async () => {
      setIsLoading(true);
      setLoadError(null);
      try {
        const storedValues = await AsyncStorage.multiGet(DATA_KEYS);
        if (!mounted) return;

        const stored = Object.fromEntries(storedValues);
        const loadedProducts = parseStoredValue(stored['@products'], '@products', []);
        const loadedNotes = parseStoredValue(stored['@notes'], '@notes', []);
        const loadedSettings = parseStoredValue(stored['@settings'], '@settings', {});
        const loadedProfile = parseStoredValue(stored['@userProfile'], '@userProfile', {});

        if (!Array.isArray(loadedProducts) || !Array.isArray(loadedNotes)) {
          throw new Error('Invalid local product or note data format.');
        }
        if (!loadedSettings || typeof loadedSettings !== 'object' || Array.isArray(loadedSettings)) {
          throw new Error('Invalid local settings data format.');
        }
        if (!loadedProfile || typeof loadedProfile !== 'object' || Array.isArray(loadedProfile)) {
          throw new Error('Invalid local profile data format.');
        }

        const nextProducts = normalizeProductList(loadedProducts);
        const nextNotes = normalizeNoteList(loadedNotes);
        const nextSettings = normalizeSettings(loadedSettings);
        const profile = normalizeProfile(loadedProfile);

        setProducts(nextProducts);
        setNotes(nextNotes);
        setSettings(nextSettings);
        setUserProfile(profile);
        i18n.locale = resolveLocale(profile.language);
        setLocale(i18n.locale);
      } catch (error) {
        if (__DEV__) console.error('Application data could not be loaded:', error);
        if (mounted) setLoadError(error);
      } finally {
        if (mounted) setIsLoading(false);
      }
    };

    loadData();
    return () => {
      mounted = false;
    };
  }, [loadAttempt]);

  useEffect(() => {
    if (!isLoading && !loadError) {
      queueSave({ products, notes, settings, userProfile }).catch((error) => {
        if (__DEV__) console.error('Application data could not be saved:', error);
        showToast(i18n.t('data_save_failed'), 'error');
      });
    }
  }, [products, notes, settings, userProfile, isLoading, loadError, showToast]);

  
  useEffect(() => {
    if (isLoading || loadError) return undefined;

    let cancelled = false;
    (async () => {
      try {
        await syncRemindersFromSettings(settingsRef.current, localeRef.current);
      } catch (error) {
        if (__DEV__ && !cancelled) console.error('Reminders could not be synchronized:', error);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [isLoading, loadError, loadAttempt]);

  useEffect(() => {
    const subscription = AppState.addEventListener('change', nextAppState => {
      if (appState.current.match(/inactive|background/) && nextAppState === 'active') {
        if (userProfile.language === 'system') {
          const resolved = resolveDeviceLocale();
          if (i18n.locale !== resolved) {
            i18n.locale = resolved;
            setLocale(resolved);
          }
        }
        if (userProfile.biometricsEnabled) {
          const now = Date.now();
          if (backgroundTime.current && now - backgroundTime.current > 5000) {
            setIsLocked(true);
          }
        }
      } else if (nextAppState.match(/inactive|background/)) {
        backgroundTime.current = Date.now();
      }
      appState.current = nextAppState;
    });
    return () => subscription.remove();
  }, [userProfile.biometricsEnabled, userProfile.language]);

  useEffect(() => () => {
    if (toastTimer.current) clearTimeout(toastTimer.current);
    if (undoTimer.current) clearTimeout(undoTimer.current);
  }, []);

  const addProduct = (product) => setProducts(current => [...current, { ...product, id: createId() }]);

  const deleteProduct = (id, options = {}) => {
    const deleted = products.find(product => product.id === id) || null;
    setProducts(current => current.filter(product => product.id !== id));

    if (!deleted || !options.withUndo) return;

    undoStack.current = { type: 'product', data: deleted };
    if (undoTimer.current) clearTimeout(undoTimer.current);
    showToast(i18n.t('undo_product_deleted', { name: deleted.name }), 'info', 5000, {
      label: i18n.t('undo_action'),
      onPress: () => {
        if (undoStack.current?.type === 'product') {
          const product = undoStack.current.data;
          setProducts(current => current.some(item => item.id === product.id) ? current : [...current, product]);
          undoStack.current = null;
          showToast(i18n.t('undo_done'), 'success');
        }
      },
    });
    undoTimer.current = setTimeout(() => { undoStack.current = null; }, 5000);
  };

  const updateProduct = (id, updatedProduct) => {
    setProducts(current => current.map(product => product.id === id ? { ...updatedProduct, id } : product));
  };

  const updateProducts = (ids, updates) => {
    const idSet = new Set(ids);
    setProducts(current => current.map(product => (
      idSet.has(product.id) ? { ...product, ...updates, id: product.id } : product
    )));
  };

  const addNote = (note) => setNotes(current => [...current, {
    ...note,
    id: createId(),
    pinned: note.pinned ?? false,
    color: note.color || '#14B8A6',
  }]);

  const deleteNote = (id, options = {}) => {
    const deleted = notes.find(note => note.id === id) || null;
    setNotes(current => current.filter(note => note.id !== id));

    if (!deleted || !options.withUndo) return;

    undoStack.current = { type: 'note', data: deleted };
    if (undoTimer.current) clearTimeout(undoTimer.current);
    showToast(i18n.t('undo_note_deleted', { title: deleted.title }), 'info', 5000, {
      label: i18n.t('undo_action'),
      onPress: () => {
        if (undoStack.current?.type === 'note') {
          const note = undoStack.current.data;
          setNotes(current => current.some(item => item.id === note.id) ? current : [...current, note]);
          undoStack.current = null;
          showToast(i18n.t('undo_done'), 'success');
        }
      },
    });
    undoTimer.current = setTimeout(() => { undoStack.current = null; }, 5000);
  };

  const updateNote = (id, updatedNote) => {
    setNotes(current => current.map(note => note.id === id ? { ...note, ...updatedNote, id } : note));
  };

  const togglePin = (id) => {
    setNotes(current => current.map(note => note.id === id ? { ...note, pinned: !note.pinned } : note));
  };

  const updateSettings = (key, value) => setSettings(current => ({ ...current, [key]: value }));
  const updateProfile = (key, value) => setUserProfile(current => ({ ...current, [key]: value }));

  const replaceAppData = async (data = {}) => {
    const nextLocaleCandidate = data.userProfile?.language;
    const nextData = {
      products: normalizeProductList(Array.isArray(data.products) ? data.products : []),
      notes: normalizeNoteList(Array.isArray(data.notes) ? data.notes : []),
      settings: normalizeSettings(data.settings || {}),
      userProfile: normalizeProfile(data.userProfile || {}),
    };

    await queueSave(nextData);
    setProducts(nextData.products);
    setNotes(nextData.notes);
    setSettings(nextData.settings);
    setUserProfile(nextData.userProfile);
    const nextLocale = resolveLocale(
      ['system', 'tr', 'en'].includes(nextData.userProfile.language)
        ? nextData.userProfile.language
        : (['tr', 'en'].includes(nextLocaleCandidate) ? nextLocaleCandidate : DEFAULT_PROFILE.language)
    );
    setLocale(nextLocale);
    i18n.locale = nextLocale;
    setSelectedCategory('All');
    setIsLocked(false);

    const syncResult = await syncRemindersFromSettings(nextData.settings, nextLocale);
    return syncResult;
  };

  const resetAppData = async () => {
    await AsyncStorage.removeItem('@searchHistory');
    return replaceAppData();
  };

  const retryLoadData = useCallback(() => setLoadAttempt(attempt => attempt + 1), []);

  const changeLanguage = (lang) => {
    if (!['system', 'tr', 'en'].includes(lang)) {
      throw new Error(`Unsupported language: ${lang}`);
    }
    const resolved = resolveLocale(lang);
    i18n.locale = resolved;
    setLocale(resolved);
    setUserProfile(prev => ({ ...prev, language: lang }));
  };

  const lockApp = useCallback(() => setIsLocked(true), []);
  const unlockApp = useCallback(() => setIsLocked(false), []);

  return (
    <AppContext.Provider value={{
      products, notes, settings, userProfile, isLoading, loadError, retryLoadData, toast, isLocked, locale,
      selectedCategory, setSelectedCategory,
      addProduct, deleteProduct, updateProduct, updateProducts,
      addNote, deleteNote, updateNote, togglePin,
      updateSettings, updateProfile, replaceAppData, resetAppData, showToast, hideToast,
      changeLanguage, lockApp, unlockApp,
    }}>
      {children}
    </AppContext.Provider>
  );
};
