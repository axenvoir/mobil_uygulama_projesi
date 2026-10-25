import React, {useMemo} from 'react';
import {FlatList, StyleSheet, Text, View} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import {useApp} from '../context/AppContext';
import {useT} from '../i18n';
import {COLORS, SPACING, TYPOGRAPHY} from '../theme';
import {countByFreshness, filterAndSortProducts, freshnessOf} from '../utils/categories';
import {Button, EmptyState} from '../components';
import {ProductCard, Screen, ScreenHeader, categoryLabelOf} from './shared';
import type {Product} from '../types';
import type {RootStackParamList} from '../navigation/types';

export const HomeScreen: React.FC = () => {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const {products, locale} = useApp();
  const t = useT(locale);

  const stats = useMemo(() => countByFreshness(products), [products]);
  const expiring = useMemo(
    () => filterAndSortProducts(products).slice(0, 5),
    [products],
  );

  return (
    <Screen>
      <ScreenHeader title={t('appName')} subtitle={t('appSubtitle')} />
      <FlatList
        data={expiring}
        keyExtractor={item => item.id}
        contentContainerStyle={styles.list}
        ListHeaderComponent={
          <View>
            <View style={styles.statsRow}>
              <View style={[styles.statCard, {backgroundColor: COLORS.surface}]}>
                <Text style={[styles.statValue, {color: COLORS.text}]}>{products.length}</Text>
                <Text style={styles.statLabel}>{t('total')}</Text>
              </View>
              <View style={[styles.statCard, {backgroundColor: COLORS.freshBg}]}>
                <Text style={[styles.statValue, {color: COLORS.fresh}]}>{stats.fresh}</Text>
                <Text style={styles.statLabel}>{t('fresh')}</Text>
              </View>
              <View style={[styles.statCard, {backgroundColor: COLORS.approachingBg}]}>
                <Text style={[styles.statValue, {color: COLORS.approaching}]}>{stats.approaching}</Text>
                <Text style={styles.statLabel}>{t('approaching')}</Text>
              </View>
              <View style={[styles.statCard, {backgroundColor: COLORS.expiredBg}]}>
                <Text style={[styles.statValue, {color: COLORS.expired}]}>{stats.expired}</Text>
                <Text style={styles.statLabel}>{t('expired')}</Text>
              </View>
            </View>
            <Button
              label={t('addProduct')}
              onPress={() => navigation.navigate('ProductForm', undefined)}
              style={styles.addButton}
            />
            <Text style={styles.sectionTitle}>{t('expiringSoon')}</Text>
          </View>
        }
        ListEmptyComponent={
          <View style={styles.empty}>
            <EmptyState icon="📦" title={t('noProducts')} hint={t('noProductsHint')} />
          </View>
        }
        renderItem={({item}: {item: Product}) => (
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
    </Screen>
  );
};

const styles = StyleSheet.create({
  list: {padding: SPACING.md},
  statsRow: {
    flexDirection: 'row',
    gap: SPACING.xs,
    marginBottom: SPACING.md,
  },
  statCard: {
    flex: 1,
    borderRadius: 16,
    paddingVertical: SPACING.sm,
    alignItems: 'center',
  },
  statValue: {fontSize: TYPOGRAPHY.h3, fontWeight: '800'},
  statLabel: {fontSize: TYPOGRAPHY.small, color: COLORS.textLight, marginTop: 2},
  addButton: {marginBottom: SPACING.lg},
  sectionTitle: {
    fontSize: TYPOGRAPHY.title,
    fontWeight: '700',
    color: COLORS.text,
    marginBottom: SPACING.sm,
  },
  empty: {alignItems: 'center', paddingTop: SPACING.xxl},
});
