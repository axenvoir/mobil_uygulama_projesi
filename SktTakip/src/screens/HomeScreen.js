import React, { useContext, useState, useEffect, useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, Animated } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { AppContext } from '../context/AppContext';
import { CATEGORIES, CATEGORY_TRANSLATION_KEYS, SHADOWS, TYPOGRAPHY } from '../theme';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { useTranslation } from '../hooks/useTranslation';
import { formatDate, getDaysLeft } from '../utils/formatters';
import { Button, Card } from '../components';
import EmptyState from '../components/EmptyState';

export default function HomeScreen({ navigation }) {
  const { products, userProfile, setSelectedCategory } = useContext(AppContext);
  const { colors } = useTheme();
  const { t, locale } = useTranslation();
  const responsive = useResponsive();
  const { isTablet, scale } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [fadeAnim] = useState(() => new Animated.Value(0));

  useEffect(() => {
    Animated.timing(fadeAnim, { toValue: 1, duration: 600, useNativeDriver: true }).start();
  }, [fadeAnim]);

  const productsByExpiry = products
    .map(product => ({ product, daysLeft: getDaysLeft(product.expDate) }))
    .filter(item => Number.isFinite(item.daysLeft));
  const approaching = productsByExpiry.filter(item => item.daysLeft > 0 && item.daysLeft <= 7);
  const overdue = productsByExpiry.filter(item => item.daysLeft < 0);
  const todayItems = productsByExpiry.filter(item => item.daysLeft === 0);
  const upcomingProducts = productsByExpiry
    .filter(item => item.daysLeft >= 0)
    .sort((a, b) => a.daysLeft - b.daysLeft)
    .map(item => item.product)
    .slice(0, 3);
  const recentProducts = upcomingProducts.length > 0
    ? upcomingProducts
    : products.slice(-3).reverse();
  const openProducts = (filter) => {
    setSelectedCategory('All');
    navigation.navigate('Products', { filter });
  };

  return (
    <Animated.View style={{ flex: 1, opacity: fadeAnim, backgroundColor: colors.background }}>
      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.content}
      >
        <View style={styles.innerWrap}>
          <View style={[styles.header, { minHeight: scale(60) }]}>
            <View style={{ flex: 1 }}>
              <Text numberOfLines={1} adjustsFontSizeToFit style={[styles.headerTitle, { flexShrink: 1, minWidth: 0 }]}>Expira</Text>
              <Text numberOfLines={1} style={styles.headerSubtitle}>{t('tagline')}</Text>
            </View>
            <Button type="ghost" size="small" icon={<Ionicons name="stats-chart-outline" size={scale(22)} color={colors.text} />} accessibilityLabel={t('home_statistics')} onPress={() => navigation.navigate('Statistics')} style={[styles.bellIcon, { padding: scale(8) }]} />
          </View>

          <View style={styles.greetingRow}>
            <View style={[styles.avatar, { backgroundColor: userProfile.avatarColor }]}>
              <Text style={styles.avatarText}>{userProfile.name.charAt(0).toUpperCase()}</Text>
            </View>
            <View style={styles.greetingTextWrap}>
              <Text numberOfLines={1} adjustsFontSizeToFit style={styles.greeting}>{t('greeting', { name: userProfile.name })} 👋</Text>
              <Text numberOfLines={1} style={styles.greetingSub}>{t('greeting_sub')}</Text>
            </View>
            <Button type="primary" size="small" icon={<Ionicons name="add" size={scale(16)} color={colors.onPrimary} />} title={t('add')} onPress={() => navigation.navigate('AddProduct')} style={styles.addButton} textStyle={styles.addButtonText} />
          </View>

          <Card shadow="medium" padding="large" style={styles.summaryCard}>
            <View style={styles.summaryTextContainer}>
              <Text style={styles.summaryTitle}>{t('home_upcoming_title')}</Text>
              <Text style={styles.summaryCount}>{approaching.length + todayItems.length}</Text>
              <Text style={styles.summarySub}>{t('home_product_count')}</Text>
              <Text style={styles.summaryDesc}>{t('home_next_seven_days')}</Text>
              <Button type="primary" size="small" title={t('home_view_details')} rightIcon={<Ionicons name="chevron-forward" size={scale(16)} color={colors.onPrimary} />} onPress={() => openProducts('approaching')} style={styles.detailButton} textStyle={styles.detailButtonText} />
            </View>
            <Ionicons
              name="calendar"
              size={scale(isTablet ? 110 : 85)}
              color={colors.accent}
              style={{ opacity: 0.1, position: 'absolute', right: scale(5), bottom: scale(5) }}
            />
          </Card>

          <View style={styles.statsRow}>
            <Card shadow="soft" style={[styles.statBox, { borderBottomColor: colors.primary }]} onPress={() => openProducts('today')}>
              <Text style={styles.statNumber}>{todayItems.length}</Text>
              <Text style={styles.statLabel} numberOfLines={1} adjustsFontSizeToFit>{t('home_today')}</Text>
            </Card>
            <Card shadow="soft" style={[styles.statBox, { borderBottomColor: colors.warning }]} onPress={() => openProducts('approaching')}>
              <Text style={styles.statNumber}>{approaching.length}</Text>
              <Text style={styles.statLabel} numberOfLines={1} adjustsFontSizeToFit>{t('home_this_week')}</Text>
            </Card>
            <Card shadow="soft" style={[styles.statBox, { borderBottomColor: colors.danger }]} onPress={() => openProducts('expired')}>
              <Text style={styles.statNumber}>{overdue.length}</Text>
              <Text style={styles.statLabel} numberOfLines={1} adjustsFontSizeToFit>{t('home_overdue')}</Text>
            </Card>
          </View>

          <Card shadow="soft" style={styles.statsShortcut} onPress={() => navigation.navigate('Statistics')}>
            <View style={styles.statsShortcutLeft}>
              <Ionicons name="bar-chart-outline" size={scale(26)} color={colors.primary} />
              <View style={{ marginLeft: scale(12), flex: 1 }}>
                <Text style={styles.statsShortcutTitle} numberOfLines={1} adjustsFontSizeToFit>{t('home_statistics')}</Text>
                <Text style={styles.statsShortcutDesc} numberOfLines={1} adjustsFontSizeToFit>{t('home_statistics_desc')}</Text>
              </View>
            </View>
            <Ionicons name="chevron-forward" size={scale(20)} color={colors.primary} />
          </Card>

          {products.length === 0 ? (
            <EmptyState
              icon="basket-outline"
              title={t('home_empty_title')}
              description={t('home_empty_description')}
              actionText={t('home_empty_action')}
              onAction={() => navigation.navigate('AddProduct')}
            />
          ) : (
            <>
              <View style={styles.sectionHeader}>
                <Text style={styles.sectionTitle} numberOfLines={1}>
                  {upcomingProducts.length > 0 ? t('home_upcoming_products') : t('home_recent_products')}
                </Text>
                <Button type="ghost" size="small" title={t('home_view_all') + ' >'} onPress={() => openProducts('all')} textStyle={styles.seeAll} />
              </View>

              <View style={isTablet ? styles.productGridTablet : null}>
                {recentProducts.map((item) => {
                  const cat = CATEGORIES.find(c => c.name === item.category);
                  return (
                    <Card key={item.id} shadow="soft" style={[styles.productItem, isTablet && styles.productItemTablet]} onPress={() => navigation.navigate('ProductDetail', { productId: item.id })}>
                      <View style={styles.productIconBg}>
                        <Ionicons name={cat?.icon || 'cube-outline'} size={scale(22)} color={colors.primary} />
                      </View>
                      <View style={{ flex: 1, marginLeft: scale(12), marginRight: scale(8) }}>
                        <Text numberOfLines={1} adjustsFontSizeToFit style={styles.productName}>{item.name}</Text>
                        <Text numberOfLines={1} style={styles.productDate}>{formatDate(item.expDate, locale)} • {t(CATEGORY_TRANSLATION_KEYS[cat?.id] || 'category_other')}</Text>
                      </View>
                      <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} />
                    </Card>
                  );
                })}
              </View>
            </>
          )}
        </View>
      </ScrollView>
    </Animated.View>
  );
}

const createStyles = (colors, responsive) => {
  const { isTablet, isWideScreen, isSmall, horizontalPadding, contentMaxWidth, scale, moderateScale } = responsive;
  return StyleSheet.create({
    container: { flex: 1, backgroundColor: colors.background },
    content: { paddingBottom: scale(120), alignItems: 'center' },
    innerWrap: { width: '100%', maxWidth: contentMaxWidth, paddingHorizontal: horizontalPadding, paddingTop: scale(20) },
    header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: scale(24) },
    headerTitle: { fontSize: moderateScale(TYPOGRAPHY.h1, 0.3), fontWeight: '900', color: colors.text, letterSpacing: -0.5 },
    headerSubtitle: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, marginTop: 2, fontWeight: '500' },
    bellIcon: { padding: scale(12), backgroundColor: colors.surface, borderRadius: scale(50), ...SHADOWS.soft },
    greetingRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: scale(24) },
    greetingTextWrap: { flex: 1, marginLeft: scale(12), marginRight: scale(12) },
    avatar: { width: scale(46), height: scale(46), borderRadius: scale(23), justifyContent: 'center', alignItems: 'center', ...SHADOWS.soft },
    avatarText: { color: colors.white, fontWeight: '900', fontSize: scale(20) },
    greeting: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text },
    greetingSub: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: 2, fontWeight: '500' },
    addButton: { flexDirection: 'row', backgroundColor: colors.primary, paddingHorizontal: scale(16), paddingVertical: scale(10), borderRadius: scale(22), alignItems: 'center', flexShrink: 0, ...SHADOWS.soft },
    addButtonText: { color: colors.onPrimary, marginLeft: scale(6), fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), fontWeight: '800' },
    summaryCard: { backgroundColor: colors.primaryDark, borderRadius: scale(24), padding: scale(24), marginBottom: scale(24), overflow: 'hidden', ...SHADOWS.medium },
    summaryTextContainer: { zIndex: 1 },
    summaryTitle: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.white },
    summaryCount: { fontSize: scale(56), fontWeight: '900', color: colors.accent, marginVertical: scale(4), letterSpacing: -1 },
    summarySub: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), color: colors.white, marginTop: scale(-8), fontWeight: '600', opacity: 0.9 },
    summaryDesc: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.white, marginBottom: scale(16), fontWeight: '500', opacity: 0.8 },
    detailButton: { flexDirection: 'row', backgroundColor: colors.accent, paddingHorizontal: scale(18), paddingVertical: scale(10), borderRadius: scale(22), alignSelf: 'flex-start', alignItems: 'center' },
    detailButtonText: { color: colors.onAccent, fontWeight: '800', marginRight: scale(4), fontSize: moderateScale(TYPOGRAPHY.caption, 0.3) },
    statsRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: scale(20) },
    statBox: { flex: 1, backgroundColor: colors.surface, paddingVertical: scale(isSmall ? 14 : 18), paddingHorizontal: scale(4), borderRadius: scale(20), marginHorizontal: scale(5), borderBottomWidth: 4, ...SHADOWS.soft, alignItems: 'center' },
    statNumber: { fontSize: scale(isTablet ? 32 : isSmall ? 24 : 28), fontWeight: '900', color: colors.text },
    statLabel: { fontSize: moderateScale(TYPOGRAPHY.small, 0.2), color: colors.textLight, marginTop: scale(4), textAlign: 'center', fontWeight: '700' },
    statsShortcut: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', backgroundColor: colors.surface, padding: scale(20), borderRadius: scale(20), marginBottom: scale(24), ...SHADOWS.soft, borderLeftWidth: 6, borderLeftColor: colors.primary },
    statsShortcutLeft: { flexDirection: 'row', alignItems: 'center', flex: 1 },
    statsShortcutTitle: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text },
    statsShortcutDesc: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: scale(2), fontWeight: '500' },
    sectionHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: scale(16) },
    sectionTitle: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text },
    seeAll: { color: colors.primary, fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), fontWeight: '800' },
    productGridTablet: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'space-between' },
    productItem: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, padding: scale(16), borderRadius: scale(20), marginBottom: scale(12), ...SHADOWS.soft },
    productItemTablet: { width: isWideScreen ? '31.5%' : '48.5%' },
    productIconBg: { width: scale(48), height: scale(48), borderRadius: scale(14), backgroundColor: colors.surfaceAlt, justifyContent: 'center', alignItems: 'center' },
    productName: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text, textTransform: 'capitalize' },
    productDate: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: scale(2), fontWeight: '500' },
  });
};