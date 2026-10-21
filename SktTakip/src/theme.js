export const LIGHT_COLORS = {
  primary: '#267968',
  primaryDark: '#153F38',
  accent: '#DCEB83',
  onPrimary: '#FFFFFF',
  onAccent: '#18362F',
  background: '#F4F6EF',
  surface: '#FFFFFF',
  surfaceAlt: '#EAF0E7',
  white: '#FFFFFF',
  text: '#19332D',
  textLight: '#65766F',
  textInverse: '#FFFFFF',
  danger: '#B8433D',
  onDanger: '#FFFFFF',
  warning: '#A86722',
  onWarning: '#FFFFFF',
  success: '#39815A',
  onSuccess: '#FFFFFF',
  info: '#396F9F',
  onInfo: '#FFFFFF',
  border: '#DCE4DA',
  tabBar: '#FFFFFF',
  tabBarActive: '#267968',
  tabBarInactive: '#7D8D85',
  statusBarStyle: 'dark-content',
};

export const DARK_COLORS = {
  primary: '#70CDB0',
  primaryDark: '#0D2623',
  accent: '#DCEB83',
  onPrimary: '#102B26',
  onAccent: '#18362F',
  background: '#101C19',
  surface: '#1A2B26',
  surfaceAlt: '#253A33',
  white: '#FFFFFF',
  text: '#F0F4EC',
  textLight: '#A5B5AC',
  textInverse: '#19332D',
  onDanger: '#421E1C',
  onWarning: '#392919',
  onSuccess: '#193022',
  onInfo: '#1B2C38',
  danger: '#E58A7F',
  warning: '#E4B66F',
  success: '#7BC49A',
  info: '#83B9E3',
  border: '#344A40',
  tabBar: '#172720',
  tabBarActive: '#8BD8BA',
  tabBarInactive: '#83958A',
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
  display: 36,
  h1: 32,
  h2: 26,
  h3: 22,
  body: 16,
  caption: 14,
  small: 12,
};

export const SHADOWS = {
  soft: { shadowColor: '#153F38', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.06, shadowRadius: 10, elevation: 2 },
  medium: { shadowColor: '#153F38', shadowOffset: { width: 0, height: 5 }, shadowOpacity: 0.09, shadowRadius: 16, elevation: 4 },
  heavy: { shadowColor: '#153F38', shadowOffset: { width: 0, height: 8 }, shadowOpacity: 0.12, shadowRadius: 20, elevation: 7 },
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

export const CATEGORY_TRANSLATION_KEYS = {
  all: 'category_all',
  dairy: 'category_dairy',
  meat: 'category_meat',
  grain: 'category_grain',
  breakfast: 'category_breakfast',
  veggie: 'category_veggie',
  drink: 'category_drink',
  frozen: 'category_frozen',
  snack: 'category_snack',
  other: 'category_other',
};