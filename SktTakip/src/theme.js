export const LIGHT_COLORS = {
  primary: '#0F766E',
  primaryDark: '#115E59',
  onPrimary: '#FFFFFF',
  background: '#F6F8FC',
  surface: '#FFFFFF',
  surfaceAlt: '#F1F5F9',
  white: '#FFFFFF',
  text: '#0F172A',
  textLight: '#64748B',
  textInverse: '#FFFFFF',
  danger: '#B91C1C',
  onDanger: '#FFFFFF',
  warning: '#B45309',
  onWarning: '#FFFFFF',
  success: '#047857',
  onSuccess: '#FFFFFF',
  info: '#1D4ED8',
  onInfo: '#FFFFFF',
  border: '#E2E8F0',
  tabBar: '#FFFFFF',
  tabBarActive: '#0F766E',
  tabBarInactive: '#94A3B8',
  statusBarStyle: 'dark-content',
};

export const DARK_COLORS = {
  primary: '#2DD4BF',
  primaryDark: '#020617',
  onPrimary: '#042F2E',
  background: '#0B1121',
  surface: '#1E293B',
  surfaceAlt: '#334155',
  white: '#FFFFFF',
  text: '#F8FAFC',
  textLight: '#94A3B8',
  textInverse: '#0F172A',
  onDanger: '#450A0A',
  onWarning: '#422006',
  onSuccess: '#052E16',
  onInfo: '#0C1A3A',
  danger: '#F87171',
  warning: '#FBBF24',
  success: '#34D399',
  info: '#60A5FA',
  border: '#334155',
  tabBar: '#0F172A',
  tabBarActive: '#2DD4BF',
  tabBarInactive: '#64748B',
  statusBarStyle: 'light-content',
};

export const COLORS = LIGHT_COLORS;

export const getContrastingTextColor = (hexColor) => {
  if (typeof hexColor !== 'string') return '#000000';
  const hex = hexColor.replace('#', '');
  if (!/^[0-9a-f]{6}$/i.test(hex)) return '#000000';

  const channels = [0, 2, 4].map((offset) => parseInt(hex.slice(offset, offset + 2), 16) / 255);
  const luminance = channels
    .map((channel) => channel <= 0.04045 ? channel / 12.92 : ((channel + 0.055) / 1.055) ** 2.4)
    .reduce((total, channel, index) => total + channel * [0.2126, 0.7152, 0.0722][index], 0);

  return luminance > 0.179 ? '#000000' : '#FFFFFF';
};

// YAZI BOYUTLARI BÜYÜTÜLDÜ
export const TYPOGRAPHY = {
  h1: 32,
  h2: 26,
  h3: 22,
  body: 17,
  caption: 15,
  small: 13,
};

export const SHADOWS = {
  soft: { shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.08, shadowRadius: 8, elevation: 3 },
  medium: { shadowColor: '#000', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.12, shadowRadius: 12, elevation: 5 },
  heavy: { shadowColor: '#000', shadowOffset: { width: 0, height: 8 }, shadowOpacity: 0.18, shadowRadius: 16, elevation: 8 },
};

export const CATEGORIES = [
  { id: 'all', name: 'Tümü', icon: 'apps-outline' },
  { id: 'dairy', name: 'Süt Ürünleri', icon: 'water-outline' },
  { id: 'meat', name: 'Et & Tavuk', icon: 'restaurant-outline' },
  { id: 'grain', name: 'Bakliyat', icon: 'leaf-outline' },
  { id: 'breakfast', name: 'Kahvaltılık', icon: 'cafe-outline' },
  { id: 'veggie', name: 'Sebze & Meyve', icon: 'nutrition-outline' },
  { id: 'drink', name: 'İçecek', icon: 'beer-outline' },
  { id: 'frozen', name: 'Dondurulmuş', icon: 'snow-outline' },
  { id: 'snack', name: 'Atıştırmalık', icon: 'fast-food-outline' },
  { id: 'other', name: 'Diğer', icon: 'ellipsis-horizontal-outline' },
];