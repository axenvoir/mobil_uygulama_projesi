export const LIGHT_COLORS = {
  primary: '#267968',
  primaryDark: '#153F38',
  accent: '#C4D639',
  onPrimary: '#FFFFFF',
  onAccent: '#18362F',
  background: '#F5F7F4',
  surface: '#FFFFFF',
  surfaceAlt: '#EDF2EE',
  white: '#FFFFFF',
  text: '#19332D',
  textLight: '#5A6B63',
  textInverse: '#FFFFFF',
  danger: '#B8433D',
  onDanger: '#FFFFFF',
  warning: '#A86722',
  onWarning: '#FFFFFF',
  success: '#39815A',
  onSuccess: '#FFFFFF',
  info: '#396F9F',
  onInfo: '#FFFFFF',
  border: '#E1E8E3',
  tabBar: '#FFFFFF',
  tabBarActive: '#267968',
  tabBarInactive: '#7D8D85',
  statusBarStyle: 'dark-content',
};

export const DARK_COLORS = {
  primary: '#70CDB0',
  primaryDark: '#0D2623',
  accent: '#C4D639',
  onPrimary: '#102B26',
  onAccent: '#18362F',
  background: '#101C19',
  surface: '#1A2B26',
  surfaceAlt: '#253A33',
  white: '#FFFFFF',
  text: '#F0F4EC',
  textLight: '#B5C5BC',
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

export const TYPOGRAPHY = {
  display: 36,
  h1: 32,
  h2: 26,
  h3: 22,
  body: 16,
  caption: 14,
  small: 13,
};

export const SHADOWS = {
  soft: { shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.04, shadowRadius: 8, elevation: 1 },
  medium: { shadowColor: '#000', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.07, shadowRadius: 12, elevation: 2 },
  heavy: { shadowColor: '#000', shadowOffset: { width: 0, height: 6 }, shadowOpacity: 0.1, shadowRadius: 18, elevation: 4 },
};

export const CATEGORIES = [
  { id: 'all', name: 'All', icon: 'apps-outline' },
  { id: 'dairy', name: 'Dairy', icon: 'water-outline' },
  { id: 'meat', name: 'Meat & Poultry', icon: 'restaurant-outline' },
  { id: 'grain', name: 'Legumes', icon: 'leaf-outline' },
  { id: 'breakfast', name: 'Breakfast', icon: 'cafe-outline' },
  { id: 'veggie', name: 'Vegetables & Fruits', icon: 'nutrition-outline' },
  { id: 'drink', name: 'Drinks', icon: 'beer-outline' },
  { id: 'frozen', name: 'Frozen', icon: 'snow-outline' },
  { id: 'snack', name: 'Snacks', icon: 'fast-food-outline' },
  { id: 'other', name: 'Other', icon: 'ellipsis-horizontal-outline' },
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