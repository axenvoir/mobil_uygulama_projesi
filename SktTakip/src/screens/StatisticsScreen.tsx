import React, {useMemo} from 'react';
import {ScrollView, StyleSheet, Text, View} from 'react-native';
import {useApp} from '../context/AppContext';
import {useT} from '../i18n';
import {COLORS, SPACING, TYPOGRAPHY} from '../theme';
import {countByCategory, countByFreshness, CATEGORY_IDS} from '../utils/categories';
import {CATEGORY_COLORS} from '../utils/categories';
import {Card} from '../components';
import {Screen, ScreenHeader, categoryLabelOf} from './shared';
import type {TranslationKey} from '../i18n/tr';

export const StatisticsScreen: React.FC = () => {
  const {products, notes, locale} = useApp();
  const t = useT(locale);

  const freshness = useMemo(() => countByFreshness(products), [products]);
  const categories = useMemo(() => countByCategory(products), [products]);
  const maxCat = Math.max(1, ...CATEGORY_IDS.map(c => categories[c] ?? 0));
  const total = products.length;

  const bars: Array<{label: string; count: number; color: string}> = [
    {label: t('fresh'), count: freshness.fresh, color: COLORS.fresh},
    {label: t('approaching'), count: freshness.approaching, color: COLORS.approaching},
    {label: t('expired'), count: freshness.expired, color: COLORS.expired},
  ];

  return (
    <Screen>
      <ScreenHeader title={t('tabStatistics')} />
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.totalWrap}>
          <Text style={styles.totalValue}>{total}</Text>
          <Text style={styles.totalLabel}>{t('statsTotalTitle')}</Text>
          <Text style={styles.secondary}>{t('tabNotes')}: {notes.length}</Text>
        </View>

        <Card>
          <Text style={styles.cardTitle}>{t('statsFreshnessTitle')}</Text>
          {bars.map(bar => (
            <View key={bar.label} style={styles.barRow}>
              <Text style={styles.barLabel}>{bar.label}</Text>
              <View style={styles.barTrack}>
                <View
                  style={[
                    styles.barFill,
                    {width: `${total ? Math.round((bar.count / total) * 100) : 0}%`, backgroundColor: bar.color},
                  ]}
                />
              </View>
              <Text style={styles.barCount}>{bar.count}</Text>
            </View>
          ))}
        </Card>

        <Card style={{marginTop: SPACING.md}}>
          <Text style={styles.cardTitle}>{t('statsCategoryTitle')}</Text>
          {CATEGORY_IDS.map(cat => {
            const count = categories[cat] ?? 0;
            return (
              <View key={cat} style={styles.barRow}>
                <Text style={styles.barLabel}>{categoryLabelOf(cat, t as (k: TranslationKey) => string)}</Text>
                <View style={styles.barTrack}>
                  <View
                    style={[
                      styles.barFill,
                      {width: `${Math.round((count / maxCat) * 100)}%`, backgroundColor: CATEGORY_COLORS[cat]},
                    ]}
                  />
                </View>
                <Text style={styles.barCount}>{count}</Text>
              </View>
            );
          })}
        </Card>
      </ScrollView>
    </Screen>
  );
};

const styles = StyleSheet.create({
  content: {padding: SPACING.md, paddingBottom: 120},
  totalWrap: {alignItems: 'center', paddingVertical: SPACING.lg},
  totalValue: {fontSize: 56, fontWeight: '800', color: COLORS.primary},
  totalLabel: {fontSize: TYPOGRAPHY.body, color: COLORS.textLight},
  secondary: {fontSize: TYPOGRAPHY.caption, color: COLORS.textLight, marginTop: SPACING.xs},
  cardTitle: {fontSize: TYPOGRAPHY.title, fontWeight: '700', color: COLORS.text, marginBottom: SPACING.sm},
  barRow: {flexDirection: 'row', alignItems: 'center', paddingVertical: 6, gap: SPACING.sm},
  barLabel: {width: 92, fontSize: TYPOGRAPHY.caption, color: COLORS.text},
  barTrack: {flex: 1, height: 10, backgroundColor: COLORS.border, borderRadius: 5, overflow: 'hidden'},
  barFill: {height: '100%', borderRadius: 5},
  barCount: {width: 28, textAlign: 'right', fontSize: TYPOGRAPHY.caption, color: COLORS.textLight},
});
