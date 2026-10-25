import React, { createContext, useContext, useState, useEffect, useRef, useCallback } from 'react';
import { Product, Note, Settings, UserProfile, ToastInfo, TabType } from '../types';
import { resolveLocale, translate } from '../utils/i18n';
import { addDaysToToday } from '../utils/formatters';

interface AppContextType {
  products: Product[];
  notes: Note[];
  settings: Settings;
  userProfile: UserProfile;
  locale: 'tr' | 'en';
  isDark: boolean;
  activeTab: TabType;
  selectedCategory: string;
  productFilter: 'all' | 'fresh' | 'approaching' | 'expired' | 'today';
  viewProductDetail: Product | null;
  editingProduct: Product | null;
  toast: ToastInfo | null;
  setActiveTab: (tab: TabType) => void;
  setSelectedCategory: (cat: string) => void;
  setProductFilter: (filter: 'all' | 'fresh' | 'approaching' | 'expired' | 'today') => void;
  setViewProductDetail: (product: Product | null) => void;
  setEditingProduct: (product: Product | null) => void;
  t: (key: string, params?: Record<string, string | number>) => string;
  changeLanguage: (lang: 'system' | 'tr' | 'en') => void;
  toggleTheme: () => void;
  addProduct: (product: Omit<Product, 'id'>) => void;
  updateProduct: (id: string, product: Partial<Product>) => void;
  deleteProduct: (id: string, withUndo?: boolean) => void;
  bulkDeleteProducts: (ids: string[]) => void;
  bulkUpdateCategory: (ids: string[], newCategory: string) => void;
  addNote: (note: Omit<Note, 'id' | 'date'>) => void;
  updateNote: (id: string, note: Partial<Note>) => void;
  deleteNote: (id: string, withUndo?: boolean) => void;
  togglePinNote: (id: string) => void;
  updateSettings: (newSettings: Partial<Settings>) => void;
  updateProfile: (newProfile: Partial<UserProfile>) => void;
  showToast: (message: string, type?: 'success' | 'info' | 'warning' | 'error', action?: { label: string; onPress: () => void } | null) => void;
  hideToast: () => void;
  resetAppData: () => void;
  exportBackupData: () => string;
  importBackupData: (jsonString: string) => { success: boolean; count: number; error?: string };
}

const DEFAULT_SETTINGS: Settings = {
  dailyReminder: true,
  reminderTime: '09:00',
  instantNotifications: true,
  hapticsEnabled: true,
  notificationMessage: "Don't forget to check your fridge!",
};

const DEFAULT_PROFILE: UserProfile = {
  name: 'Alex',
  avatarColor: '#267968',
  language: 'system',
  biometricsEnabled: false,
  theme: 'system',
};

const INITIAL_SAMPLE_PRODUCTS: Product[] = [
  {
    id: 'prod-1',
    name: 'Süt (Whole Milk)',
    productCode: 'MILK-01',
    category: 'dairy',
    expDate: addDaysToToday(0), // Today!
    quantity: '2',
    note: 'Keep refrigerated on middle shelf',
    imageUri: null,
  },
  {
    id: 'prod-2',
    name: 'Yumurta (Organic Eggs)',
    productCode: 'EGG-12',
    category: 'breakfast',
    expDate: addDaysToToday(3), // Approaching
    quantity: '12',
    note: 'Best used for baking this weekend',
    imageUri: null,
  },
  {
    id: 'prod-3',
    name: 'Tavuk Göğsü (Chicken Breast)',
    productCode: 'CHICK-04',
    category: 'meat',
    expDate: addDaysToToday(1), // Approaching
    quantity: '500g',
    note: 'Freeze if not cooked by tomorrow',
    imageUri: null,
  },
  {
    id: 'prod-4',
    name: 'Kaşar Peyniri (Cheddar)',
    productCode: 'CHEESE-02',
    category: 'dairy',
    expDate: addDaysToToday(14), // Fresh
    quantity: '400g',
    note: 'Unopened package',
    imageUri: null,
  },
  {
    id: 'prod-5',
    name: 'Yoğurt (Greek Yogurt)',
    productCode: 'YOG-01',
    category: 'dairy',
    expDate: addDaysToToday(-2), // Expired 2 days ago
    quantity: '1',
    note: 'Check smell/texture before discarding',
    imageUri: null,
  },
  {
    id: 'prod-6',
    name: 'Domates (Vine Tomatoes)',
    productCode: 'TOM-05',
    category: 'veggie',
    expDate: addDaysToToday(5), // Approaching
    quantity: '6',
    note: 'Store on kitchen counter',
    imageUri: null,
  },
  {
    id: 'prod-7',
    name: 'Makarna (Penne Rigate)',
    productCode: 'PASTA-09',
    category: 'grain',
    expDate: addDaysToToday(180), // Fresh
    quantity: '2',
    note: 'Pantry top shelf',
    imageUri: null,
  },
];

const INITIAL_SAMPLE_NOTES: Note[] = [
  {
    id: 'note-1',
    title: 'Haftalık Alışveriş Listesi (Grocery List)',
    desc: '• Zeytinyağı (Olive oil)\n• Ekmek (Whole wheat bread)\n• Kahve çekirdeği (Coffee beans)\n• Maden suyu (Mineral water)',
    pinned: true,
    color: '#267968',
    date: new Date().toISOString().slice(0, 10),
  },
  {
    id: 'note-2',
    title: 'Buzluk Düzeni (Freezer Storage)',
    desc: 'Buzluğun alt çekmecesindeki balıkların SKT si önümüzdeki ay doluyor.',
    pinned: false,
    color: '#F59E0B',
    date: new Date().toISOString().slice(0, 10),
  },
];

const AppContext = createContext<AppContextType | null>(null);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [products, setProducts] = useState<Product[]>(() => {
    try {
      const stored = localStorage.getItem('expira_products');
      return stored ? JSON.parse(stored) : INITIAL_SAMPLE_PRODUCTS;
    } catch {
      return INITIAL_SAMPLE_PRODUCTS;
    }
  });

  const [notes, setNotes] = useState<Note[]>(() => {
    try {
      const stored = localStorage.getItem('expira_notes');
      return stored ? JSON.parse(stored) : INITIAL_SAMPLE_NOTES;
    } catch {
      return INITIAL_SAMPLE_NOTES;
    }
  });

  const [settings, setSettings] = useState<Settings>(() => {
    try {
      const stored = localStorage.getItem('expira_settings');
      return stored ? { ...DEFAULT_SETTINGS, ...JSON.parse(stored) } : DEFAULT_SETTINGS;
    } catch {
      return DEFAULT_SETTINGS;
    }
  });

  const [userProfile, setUserProfile] = useState<UserProfile>(() => {
    try {
      const stored = localStorage.getItem('expira_profile');
      return stored ? { ...DEFAULT_PROFILE, ...JSON.parse(stored) } : DEFAULT_PROFILE;
    } catch {
      return DEFAULT_PROFILE;
    }
  });

  const [activeTab, setActiveTab] = useState<TabType>('home');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [productFilter, setProductFilter] = useState<'all' | 'fresh' | 'approaching' | 'expired' | 'today'>('all');
  const [viewProductDetail, setViewProductDetail] = useState<Product | null>(null);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [toast, setToast] = useState<ToastInfo | null>(null);

  const undoStack = useRef<{ type: 'product' | 'note'; data: any } | null>(null);
  const toastTimeoutRef = useRef<any>(null);

  const locale = resolveLocale(userProfile.language);

  // Theme calculation
  const [isDark, setIsDark] = useState<boolean>(() => {
    if (userProfile.theme === 'dark') return true;
    if (userProfile.theme === 'light') return false;
    return typeof window !== 'undefined' && window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  });

  useEffect(() => {
    if (userProfile.theme === 'dark') {
      setIsDark(true);
      document.documentElement.classList.add('dark');
    } else if (userProfile.theme === 'light') {
      setIsDark(false);
      document.documentElement.classList.remove('dark');
    } else {
      const match = window.matchMedia('(prefers-color-scheme: dark)').matches;
      setIsDark(match);
      if (match) document.documentElement.classList.add('dark');
      else document.documentElement.classList.remove('dark');
    }
  }, [userProfile.theme]);

  // Persist to localStorage
  useEffect(() => {
    localStorage.setItem('expira_products', JSON.stringify(products));
  }, [products]);

  useEffect(() => {
    localStorage.setItem('expira_notes', JSON.stringify(notes));
  }, [notes]);

  useEffect(() => {
    localStorage.setItem('expira_settings', JSON.stringify(settings));
  }, [settings]);

  useEffect(() => {
    localStorage.setItem('expira_profile', JSON.stringify(userProfile));
  }, [userProfile]);

  const t = useCallback(
    (key: string, params?: Record<string, string | number>) => translate(key, locale, params),
    [locale]
  );

  const showToast = useCallback(
    (message: string, type: 'success' | 'info' | 'warning' | 'error' = 'success', action?: { label: string; onPress: () => void } | null) => {
      if (toastTimeoutRef.current) clearTimeout(toastTimeoutRef.current);
      const id = `${Date.now()}`;
      setToast({ id, message, type, action });
      toastTimeoutRef.current = setTimeout(() => {
        setToast((curr) => (curr?.id === id ? null : curr));
      }, 4000);
    },
    []
  );

  const hideToast = useCallback(() => {
    if (toastTimeoutRef.current) clearTimeout(toastTimeoutRef.current);
    setToast(null);
  }, []);

  const addProduct = (product: Omit<Product, 'id'>) => {
    const newProduct: Product = {
      ...product,
      id: `prod-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
    };
    setProducts((curr) => [newProduct, ...curr]);
    showToast(t('product_added'), 'success');
  };

  const updateProduct = (id: string, updates: Partial<Product>) => {
    setProducts((curr) => curr.map((p) => (p.id === id ? { ...p, ...updates } : p)));
    showToast(t('product_updated'), 'success');
    if (viewProductDetail && viewProductDetail.id === id) {
      setViewProductDetail((curr) => (curr ? { ...curr, ...updates } : null));
    }
  };

  const deleteProduct = (id: string, withUndo = true) => {
    const target = products.find((p) => p.id === id);
    if (!target) return;

    setProducts((curr) => curr.filter((p) => p.id !== id));
    if (viewProductDetail?.id === id) setViewProductDetail(null);

    if (withUndo) {
      undoStack.current = { type: 'product', data: target };
      showToast(t('undo_product_deleted', { name: target.name }), 'info', {
        label: t('undo_action'),
        onPress: () => {
          if (undoStack.current?.type === 'product') {
            setProducts((curr) => [undoStack.current!.data, ...curr]);
            undoStack.current = null;
            showToast(t('undo_done'), 'success');
          }
        },
      });
    } else {
      showToast(t('undo_product_deleted', { name: target.name }), 'success');
    }
  };

  const bulkDeleteProducts = (ids: string[]) => {
    const idSet = new Set(ids);
    setProducts((curr) => curr.filter((p) => !idSet.has(p.id)));
    showToast(t('products_deleted', { count: ids.length }), 'success');
  };

  const bulkUpdateCategory = (ids: string[], newCategory: string) => {
    const idSet = new Set(ids);
    setProducts((curr) => curr.map((p) => (idSet.has(p.id) ? { ...p, category: newCategory } : p)));
    showToast(t('product_category_updated', { count: ids.length }), 'success');
  };

  const addNote = (note: Omit<Note, 'id' | 'date'>) => {
    const newNote: Note = {
      ...note,
      id: `note-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
      date: new Date().toISOString().slice(0, 10),
    };
    setNotes((curr) => [newNote, ...curr]);
    showToast(t('note_added'), 'success');
  };

  const updateNote = (id: string, updates: Partial<Note>) => {
    setNotes((curr) => curr.map((n) => (n.id === id ? { ...n, ...updates } : n)));
    showToast(t('note_updated'), 'success');
  };

  const deleteNote = (id: string, withUndo = true) => {
    const target = notes.find((n) => n.id === id);
    if (!target) return;

    setNotes((curr) => curr.filter((n) => n.id !== id));

    if (withUndo) {
      undoStack.current = { type: 'note', data: target };
      showToast(t('undo_note_deleted', { title: target.title }), 'info', {
        label: t('undo_action'),
        onPress: () => {
          if (undoStack.current?.type === 'note') {
            setNotes((curr) => [undoStack.current!.data, ...curr]);
            undoStack.current = null;
            showToast(t('undo_done'), 'success');
          }
        },
      });
    } else {
      showToast(t('note_deleted'), 'success');
    }
  };

  const togglePinNote = (id: string) => {
    setNotes((curr) =>
      curr.map((n) => {
        if (n.id === id) {
          const next = !n.pinned;
          showToast(next ? t('note_pinned') : t('note_unpinned'), 'info');
          return { ...n, pinned: next };
        }
        return n;
      })
    );
  };

  const updateSettings = (newSettings: Partial<Settings>) => {
    setSettings((curr) => ({ ...curr, ...newSettings }));
    showToast(t('notification_settings_saved'), 'success');
  };

  const updateProfile = (newProfile: Partial<UserProfile>) => {
    setUserProfile((curr) => ({ ...curr, ...newProfile }));
    showToast(t('profile_saved'), 'success');
  };

  const changeLanguage = (lang: 'system' | 'tr' | 'en') => {
    setUserProfile((curr) => ({ ...curr, language: lang }));
  };

  const toggleTheme = () => {
    const nextTheme = isDark ? 'light' : 'dark';
    setUserProfile((curr) => ({ ...curr, theme: nextTheme }));
  };

  const resetAppData = () => {
    setProducts([]);
    setNotes([]);
    setSettings(DEFAULT_SETTINGS);
    setUserProfile(DEFAULT_PROFILE);
    localStorage.clear();
    showToast(t('backup_reset_done'), 'info');
  };

  const exportBackupData = () => {
    const payload = {
      app: 'Expira',
      version: '1.1.3',
      exportDate: new Date().toISOString(),
      products,
      notes,
      settings,
      userProfile,
      checksum: btoa(JSON.stringify({ p: products.length, n: notes.length, t: Date.now() })),
    };
    return JSON.stringify(payload, null, 2);
  };

  const importBackupData = (jsonString: string) => {
    try {
      const data = JSON.parse(jsonString);
      if (!data || (!data.products && !data.notes)) {
        return { success: false, count: 0, error: t('backup_invalid') };
      }
      const loadedProducts = Array.isArray(data.products) ? data.products : [];
      const loadedNotes = Array.isArray(data.notes) ? data.notes : [];

      setProducts(loadedProducts);
      setNotes(loadedNotes);
      if (data.settings) setSettings({ ...DEFAULT_SETTINGS, ...data.settings });
      if (data.userProfile) setUserProfile({ ...DEFAULT_PROFILE, ...data.userProfile });

      showToast(t('backup_restored', { products: loadedProducts.length, notes: loadedNotes.length }), 'success');
      return { success: true, count: loadedProducts.length + loadedNotes.length };
    } catch (err: any) {
      return { success: false, count: 0, error: err.message || t('backup_corrupted') };
    }
  };

  return (
    <AppContext.Provider
      value={{
        products,
        notes,
        settings,
        userProfile,
        locale,
        isDark,
        activeTab,
        selectedCategory,
        productFilter,
        viewProductDetail,
        editingProduct,
        toast,
        setActiveTab,
        setSelectedCategory,
        setProductFilter,
        setViewProductDetail,
        setEditingProduct,
        t,
        changeLanguage,
        toggleTheme,
        addProduct,
        updateProduct,
        deleteProduct,
        bulkDeleteProducts,
        bulkUpdateCategory,
        addNote,
        updateNote,
        deleteNote,
        togglePinNote,
        updateSettings,
        updateProfile,
        showToast,
        hideToast,
        resetAppData,
        exportBackupData,
        importBackupData,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) throw new Error('useApp must be used within AppProvider');
  return context;
};
