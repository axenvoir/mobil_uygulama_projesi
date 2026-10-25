import React from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import {SafeAreaView} from 'react-native-safe-area-context';
import {COLORS, SHADOWS, SPACING, TYPOGRAPHY} from '../theme';
import type {FreshnessLevel, Product} from '../types';
import {daysLeft, formatDate} from '../utils/date';
import {CATEGORY_COLORS, CATEGORY_LABEL_KEYS} from '../utils/categories';
import type {TranslationKey} from '../i18n/tr';

export const Screen: React.FC<{
  children: React.ReactNode;
  style?: object;
}> = ({children, style}) => (
  <SafeAreaView style={[styles.safe, style]} edges={['top', 'left', 'right']}>
    {children}
  </SafeAreaView>
);

export const ScreenHeader: React.FC<{title: string; subtitle?: string}> = ({title, subtitle}) => (
  <View style={styles.header}>
    <Text style={styles.headerTitle} numberOfLines={1}>
      {title}
    </Text>
    {subtitle ? <Text style={styles.headerSubtitle}>{subtitle}</Text> : null}
  </View>
);

const FRESHNESS_COLORS: Record<FreshnessLevel, {fg: string; bg: string}> = {
  fresh: {fg: COLORS.fresh, bg: COLORS.freshBg},
  approaching: {fg: COLORS.approaching, bg: COLORS.approachingBg},
  expired: {fg: COLORS.expired, bg: COLORS.expiredBg},
};

export const FreshnessBadge: React.FC<{level: FreshnessLevel; label: string}> = ({level, label}) => {
  const c = FRESHNESS_COLORS[level];
  return (
    <View style={[styles.badge, {backgroundColor: c.bg}]}>
      <Text style={[styles.badgeText, {color: c.fg}]}>{label}</Text>
    </View>
  );
};

export const expiryText = (product: Product, t: (k: TranslationKey) => string): string => {
  const d = daysLeft(product.expDate);
  if (d === null) {
    return product.expDate;
  }
  if (d === 0) {
    return t('expiresToday');
  }
  if (d > 0) {
    return `${d} ${t('daysLeft')}`;
  }
  return `${-d} ${t('daysAgo')}`;
};

export interface ProductCardT {
  t: (k: TranslationKey) => string;
}

export const ProductCard: React.FC<{
  product: Product;
  level: FreshnessLevel;
  freshnessLabel: string;
  categoryLabel: string;
  dateLocale: 'tr' | 'en';
  t: (k: TranslationKey) => string;
  onPress: () => void;
  onLongPress?: () => void;
}> = ({product, level, freshnessLabel, categoryLabel, dateLocale, t, onPress, onLongPress}) => (
  <Pressable
    onPress={onPress}
    onLongPress={onLongPress}
    style={({pressed}) => [styles.card, SHADOWS.soft, pressed && {opacity: 0.9}]}>
    <View style={[styles.cardAccent, {backgroundColor: CATEGORY_COLORS[product.category]}]} />
    <View style={styles.cardBody}>
      <View style={styles.cardTop}>
        <Text style={styles.cardName} numberOfLines={1}>
          {product.name}
        </Text>
        <FreshnessBadge level={level} label={freshnessLabel} />
      </View>
      <View style={styles.cardMeta}>
        <Text style={styles.cardCategory}>{categoryLabel}</Text>
        <Text style={styles.cardDate}>{formatDate(product.expDate, dateLocale)}</Text>
      </View>
      <Text style={styles.cardExpiry}>{expiryText(product, t)}</Text>
    </View>
  </Pressable>
);

export const categoryLabelOf = (
  categoryId: Product['category'],
  t: (k: TranslationKey) => string,
): string => t(CATEGORY_LABEL_KEYS[categoryId]);

const styles = StyleSheet.create({
  safe: {flex: 1, backgroundColor: COLORS.background},
  header: {
    backgroundColor: COLORS.primaryDark,
    paddingHorizontal: SPACING.md,
    paddingTop: SPACING.md,
    paddingBottom: SPACING.md,
  },
  headerTitle: {fontSize: TYPOGRAPHY.h2, fontWeight: '700', color: COLORS.white},
  headerSubtitle: {fontSize: TYPOGRAPHY.caption, color: '#A7C4BD', marginTop: 2},
  badge: {paddingHorizontal: 10, paddingVertical: 4, borderRadius: 999},
  badgeText: {fontSize: TYPOGRAPHY.small, fontWeight: '700'},
  card: {
    flexDirection: 'row',
    backgroundColor: COLORS.surface,
    borderRadius: 16,
    overflow: 'hidden',
    marginBottom: SPACING.sm + 2,
  },
  cardAccent: {width: 6},
  cardBody: {flex: 1, padding: SPACING.md},
  cardTop: {flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center'},
  cardName: {flex: 1, fontSize: TYPOGRAPHY.title, fontWeight: '700', color: COLORS.text, marginRight: SPACING.sm},
  cardMeta: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: SPACING.xs,
  },
  cardCategory: {fontSize: TYPOGRAPHY.caption, color: COLORS.textLight},
  cardDate: {fontSize: TYPOGRAPHY.caption, color: COLORS.textLight},
  cardExpiry: {fontSize: TYPOGRAPHY.small, color: COLORS.textLight, marginTop: 2},
});
