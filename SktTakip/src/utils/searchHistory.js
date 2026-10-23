import AsyncStorage from '@react-native-async-storage/async-storage';

const KEY = '@searchHistory';
const MAX_HISTORY = 8;

export const getSearchHistory = async () => {
  try {
    const stored = await AsyncStorage.getItem(KEY);
    if (!stored) return [];
    const parsed = JSON.parse(stored);
    if (!Array.isArray(parsed)) {
      if (__DEV__) console.error('Arama geçmişi geçerli bir liste değil.');
      return [];
    }
    return parsed.filter(term => typeof term === 'string' && term.trim()).slice(0, MAX_HISTORY);
  } catch (error) {
    if (__DEV__) console.error('Arama geçmişi okunamadı:', error);
    return [];
  }
};

export const addSearchTerm = async (term) => {
  if (!term || term.trim().length < 2) return [];
  try {
    const trimmed = term.trim();
    const history = await getSearchHistory();
    const filtered = history.filter(h => h.toLocaleLowerCase() !== trimmed.toLocaleLowerCase());
    const updated = [trimmed, ...filtered].slice(0, MAX_HISTORY);
    await AsyncStorage.setItem(KEY, JSON.stringify(updated));
    return updated;
  } catch (error) {
    if (__DEV__) console.error('Arama terimi kaydedilemedi:', error);
    return [];
  }
};

export const removeSearchTerm = async (term) => {
  try {
    const history = await getSearchHistory();
    const updated = history.filter(h => h !== term);
    await AsyncStorage.setItem(KEY, JSON.stringify(updated));
    return updated;
  } catch (error) {
    if (__DEV__) console.error('Arama geçmişi güncellenemedi:', error);
    return [];
  }
};

export const clearSearchHistory = async () => {
  try {
    await AsyncStorage.removeItem(KEY);
    return [];
  } catch (error) {
    if (__DEV__) console.error('Arama geçmişi temizlenemedi:', error);
    return [];
  }
};