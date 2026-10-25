import React, {useMemo, useState} from 'react';
import {FlatList, Pressable, StyleSheet, Text, TextInput, View} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import {useApp} from '../context/AppContext';
import {useT} from '../i18n';
import {COLORS, SPACING, TYPOGRAPHY} from '../theme';
import {
  CATEGORY_IDS,
  filterAndSortProducts,
  freshnessOf,
} from '../utils/categories';
import {EmptyState} from '../components';
import {ProductCard, Screen, ScreenHeader, categoryLabelOf} from './shared';
import type {CategoryId, FreshnessLevel} from '../types';
import type {RootStackParamList} from '../navigation/types';

export const ProductsScreen: React.FC = () => {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const {products, locale} = useApp();
  const t = useT(locale);
  const [query, setQuery] = useState('');
  const [freshness, setFreshness] = useState<FreshnessLevel | 'all'>('all');
  const [category, setCategory] = useState<CategoryId | 'all'>('all');

  const filtered = useMemo(
    () => filterAndSortProducts(products, {query, freshness, category}),
    [products, query, freshness, category],
  );

  const freshnessChips: Array<{key: FreshnessLevel | 'all'; label: string}> = [
    {key: 'all', label: t('all')},
    {key: 'fresh', label: t('fresh')},
    {key: 'approaching', label: t('approaching')},
    {key: 'expired', label: t('expired')},
  ];

  return (
    <Screen>
      <ScreenHeader title={t('tabProducts')} />
      <View style={styles.controls}>
        <TextInput
          style={styles.search}
          value={query}
          onChangeText={setQuery}
          placeholder={t('searchPlaceholder')}
          placeholderTextColor={COLORS.textLight}
        />
        <View style={styles.chipRow}>
          {freshnessChips.map(chip => (
            <Pressable
              key={chip.key}
              onPress={() => setFreshness(chip.key)}
              style={[styles.chip, freshness === chip.key && styles.chipActive]}>
              <Text style={[styles.chipText, freshness === chip.key && styles.chipTextActive]}>
                {chip.label}
              </Text>
            </Pressable>
          ))}
        </View>
        <View style={styles.chipRow}>
          <Pressable
            onPress={() => setCategory('all')}
            style={[styles.chip, category === 'all' && styles.chipActive]}>
            <Text style={[styles.chipText, category === 'all' && styles.chipTextActive]}>
              {t('all')}
            </Text>
          </Pressable>
          {CATEGORY_IDS.map(cat => (
            <Pressable
              key={cat}
              onPress={() => setCategory(cat)}
              style={[styles.chip, category === cat && styles.chipActive]}>
              <Text style={[styles.chipText, category === cat && styles.chipTextActive]}>
                {categoryLabelOf(cat, t)}
              </Text>
            </Pressable>
          ))}
        </View>
      </View>
      <FlatList
        data={filtered}
        keyExtractor={item => item.id}
        contentContainerStyle={styles.list}
        ListEmptyComponent={
          <View style={styles.empty}>
            <EmptyState
              icon="🔍"
              title={products.length ? t('emptyFilterResult') : t('noProducts')}
              hint={products.length ? undefined : t('noProductsHint')}
            />
          </View>
        }
        renderItem={({item}) => (
          <ProductCard
            product={item}
            t={t}
            level={freshnessOf(item)}
            freshnessLabel={t(freshnessOf(item))}
            categoryLabel={categoryLabelOf(item.category, t)}
            dateLocale={locale}
            onPress={() => navigation.navigate('ProductDetail', {id: item.id})}
          />
        )}
      />
      <Pressable
        style={styles.fab}
        onPress={() => navigation.navigate('ProductForm', undefined)}>
        <Text style={styles.fabText}>＋</Text>
      </Pressable>
    </Screen>
  );
};

const styles = StyleSheet.create({
  controls: {paddingHorizontal: SPACING.md, paddingTop: SPACING.sm},
  search: {
    backgroundColor: COLORS.surface,
    borderWidth: 1,
    borderColor: COLORS.border,
    borderRadius: 12,
    height: 44,
    paddingHorizontal: SPACING.md,
    fontSize: TYPOGRAPHY.body,
    color: COLORS.text,
    marginBottom: SPACING.sm,
  },
  chipRow: {flexDirection: 'row', flexWrap: 'wrap', gap: SPACING.xs, marginBottom: SPACING.xs},
  chip: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 999,
    backgroundColor: COLORS.surface,
    borderWidth: 1,
    borderColor: COLORS.border,
  },
  chipActive: {backgroundColor: COLORS.primary, borderColor: COLORS.primary},
  chipText: {fontSize: TYPOGRAPHY.caption, color: COLORS.text, fontWeight: '600'},
  chipTextActive: {color: COLORS.white},
  list: {padding: SPACING.md, paddingBottom: 120},
  empty: {alignItems: 'center', paddingTop: SPACING.xxl},
  fab: {
    position: 'absolute',
    right: SPACING.md,
    bottom: SPACING.xl,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: COLORS.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  fabText: {fontSize: 28, color: COLORS.white, marginTop: -2},
});
