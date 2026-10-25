export const COLORS = {
  primary: '#267968',
  primaryDark: '#153F38',
  accent: '#C4D639',
  onPrimary: '#FFFFFF',
  background: '#F5F7F4',
  surface: '#FFFFFF',
  text: '#19332D',
  textLight: '#5A6B63',
  border: '#E1E8E3',

  fresh: '#39815A',
  freshBg: '#EAF5EF',
  approaching: '#D97706',
  approachingBg: '#FEF3C7',
  expired: '#DC2626',
  expiredBg: '#FEE2E2',

  white: '#FFFFFF',
  black: '#000000',
} as const;

export const SHADOWS = {
  soft: {
    shadowColor: '#000000',
    shadowOffset: {width: 0, height: 2},
    shadowOpacity: 0.06,
    shadowRadius: 4,
    elevation: 2,
  },
  medium: {
    shadowColor: '#000000',
    shadowOffset: {width: 0, height: 4},
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
  },
  heavy: {
    shadowColor: '#000000',
    shadowOffset: {width: 0, height: 8},
    shadowOpacity: 0.16,
    shadowRadius: 16,
    elevation: 8,
  },
} as const;

export const TYPOGRAPHY = {
  h1: 28,
  h2: 22,
  h3: 18,
  title: 16,
  body: 15,
  caption: 13,
  small: 11,
} as const;

export const SPACING = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
} as const;
