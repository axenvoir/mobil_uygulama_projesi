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

// ============================================
// SEMANTIC COLORS (for better readability)
// ============================================
export const SEMANTIC_COLORS = {
  // Status colors
  success: {
    light: '#D1FAE5',
    DEFAULT: '#34D399',
    dark: '#065F46',
    text: '#065F46',
    border: '#A7F3D0',
  },
  warning: {
    light: '#FEF3C7',
    DEFAULT: '#F59E0B',
    dark: '#92400E',
    text: '#92400E',
    border: '#FDE68A',
  },
  danger: {
    light: '#FEE2E2',
    DEFAULT: '#EF4444',
    dark: '#991B1B',
    text: '#991B1B',
    border: '#FCA5A5',
  },
  info: {
    light: '#DBEAFE',
    DEFAULT: '#3B82F6',
    dark: '#1E40AF',
    text: '#1E40AF',
    border: '#93C5FD',
  },
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

// ============================================
// TYPOGRAPHY SYSTEM
// ============================================
export const TYPOGRAPHY = {
  // Headings
  display: 36,   // Large display text
  h1: 32,        // Screen titles
  h2: 26,        // Section titles
  h3: 22,        // Card titles
  h4: 20,        // Subsection titles
  
  // Body text
  body: 16,      // Primary text
  bodySmall: 14, // Secondary text
  
  // Small text
  caption: 14,   // Captions
  small: 12,     // Helper text, labels
  tiny: 10,      // Micro text
};

// Font weights mapping
export const FONT_WEIGHTS = {
  regular: '400',
  medium: '500',
  semiBold: '600',
  bold: '700',
  extraBold: '800',
  black: '900',
};

// Font families (using system fonts as fallback)
export const FONTS = {
  regular: 'Inter-Regular',
  medium: 'Inter-Medium',
  semiBold: 'Inter-SemiBold',
  bold: 'Inter-Bold',
};



// ============================================
// SPACING SYSTEM (4px base grid)
// ============================================
export const SPACING = {
  none: 0,
  xs: 4,    // 4px  - Micro spacing (icon-text gap)
  sm: 8,    // 8px  - Small spacing (compact elements)
  md: 16,   // 16px - Medium spacing (standard padding)
  lg: 24,   // 24px - Large spacing (cards, sections)
  xl: 32,   // 32px - Extra large (section gaps)
  '2xl': 48, // 48px - 2x large (major sections)
  '3xl': 64, // 64px - 3x large (page margins)
};

// Spacing with scale function helper
export const getSpacing = (key, scaleFn) => {
  const spacingMap = {
    none: 0,
    xs: 4,
    sm: 8,
    md: 16,
    lg: 24,
    xl: 32,
    '2xl': 48,
    '3xl': 64
  };
  return scaleFn ? scaleFn(spacingMap[key] || spacingMap.md) : spacingMap[key];
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