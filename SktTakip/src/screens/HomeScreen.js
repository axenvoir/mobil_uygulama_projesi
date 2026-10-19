import React, { useContext, useState, useEffect, useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, RefreshControl, Animated } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { AppContext } from '../context/AppContext';
import { CATEGORIES, SHADOWS, TYPOGRAPHY } from '../theme';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { formatDate, getDaysLeft } from '../utils/formatters';
import EmptyState from '../components/EmptyState'; // YENİ EKLENDİ

export default function HomeScreen({ navigation }) {
  const { products, showToast, userProfile } = useContext(AppContext);
  const { colors } = useTheme();
  const responsive = useResponsive();
  const { isTablet, scale } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [refreshing, setRefreshing] = useState(false);
  const [fadeAnim] = useState(() => new Animated.Value(0));

  useEffect(() => {
    Animated.timing(fadeAnim, { toValue: 1, duration: 600, useNativeDriver: true }).start();
  }, [fadeAnim]);

  const approaching = products.filter(p => getDaysLeft(p.expDate) > 0 && getDaysLeft(p.expDate) <= 7);
  const overdue = products.filter(p => getDaysLeft(p.expDate) < 0);
  const todayItems = products.filter(p => getDaysLeft(p.expDate) === 0);

  const onRefresh = () => {
    setRefreshing(true);
    setTimeout(() => {
      setRefreshing(false);
      showToast('Güncel veriler yüklendi.', 'info');
    }, 800);
  };

  const recentProducts = products.slice(-3).reverse();

  return (
    <Animated.View style={{ flex: 1, opacity: fadeAnim, backgroundColor: colors.background }}>
      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.content}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={[colors.primary]} tintColor={colors.primary} />
        }
      >
        <View style={styles.innerWrap}>
          <View style={styles.header}>
            <View style={{ flex: 1 }}>
              <Text numberOfLines={1} adjustsFontSizeToFit style={styles.headerTitle}>Expira</Text>
              <Text numberOfLines={1} style={styles.headerSubtitle}>Son kullanma tarihini unutma</Text>
            </View>
            <TouchableOpacity style={styles.bellIcon} onPress={() => navigation.navigate('Statistics')} activeOpacity={0.7}>
              <Ionicons name="stats-chart-outline" size={scale(22)} color={colors.text} />
            </TouchableOpacity>
          </View>

          <View style={styles.greetingRow}>
            <View style={[styles.avatar, { backgroundColor: userProfile.avatarColor }]}>
              <Text style={styles.avatarText}>{userProfile.name.charAt(0).toUpperCase()}</Text>
            </View>
            <View style={styles.greetingTextWrap}>
              <Text numberOfLines={1} adjustsFontSizeToFit style={styles.greeting}>Merhaba, {userProfile.name} 👋</Text>
              <Text numberOfLines={1} style={styles.greetingSub}>Bugün düzenli, yarın güvenle.</Text>
            </View>
            <TouchableOpacity style={styles.addButton} onPress={() => navigation.navigate('AddProduct')} activeOpacity={0.8}>
              <Ionicons name="add" size={scale(16)} color={colors.onPrimary} />
              <Text style={styles.addButtonText}>Ekle</Text>
            </TouchableOpacity>
          </View>

          <View style={styles.summaryCard}>
            <View style={styles.summaryTextContainer}>
              <Text style={styles.summaryTitle}>Yaklaşan SKT&apos;ler</Text>
              <Text style={styles.summaryCount}>{approaching.length + todayItems.length}</Text>
              <Text style={styles.summarySub}>ürün</Text>
              <Text style={styles.summaryDesc}>7 gün içinde sona erecek</Text>
              <TouchableOpacity style={styles.detailButton} onPress={() => navigation.navigate('Products')} activeOpacity={0.8}>
                <Text style={styles.detailButtonText}>Detayları gör</Text>
                <Ionicons name="chevron-forward" size={scale(16)} color={colors.onPrimary} />
              </TouchableOpacity>
            </View>
            <Ionicons
              name="calendar"
              size={scale(isTablet ? 110 : 85)}
              color={colors.primary}
              style={{ opacity: 0.1, position: 'absolute', right: scale(5), bottom: scale(5) }}
            />
          </View>

          <View style={styles.statsRow}>
            <TouchableOpacity style={[styles.statBox, { borderBottomColor: colors.primary }]} onPress={() => navigation.navigate('Products')} activeOpacity={0.8}>
              <Text style={styles.statNumber}>{todayItems.length}</Text>
              <Text style={styles.statLabel} numberOfLines={1} adjustsFontSizeToFit>Bugün</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.statBox, { borderBottomColor: colors.warning }]} onPress={() => navigation.navigate('Products')} activeOpacity={0.8}>
              <Text style={styles.statNumber}>{approaching.length}</Text>
              <Text style={styles.statLabel} numberOfLines={1} adjustsFontSizeToFit>Bu hafta</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.statBox, { borderBottomColor: colors.danger }]} onPress={() => navigation.navigate('Products')} activeOpacity={0.8}>
              <Text style={styles.statNumber}>{overdue.length}</Text>
              <Text style={styles.statLabel} numberOfLines={1} adjustsFontSizeToFit>Geciken</Text>
            </TouchableOpacity>
          </View>

          <TouchableOpacity style={styles.statsShortcut} onPress={() => navigation.navigate('Statistics')} activeOpacity={0.8}>
            <View style={styles.statsShortcutLeft}>
              <Ionicons name="bar-chart-outline" size={scale(26)} color={colors.primary} />
              <View style={{ marginLeft: scale(12), flex: 1 }}>
                <Text style={styles.statsShortcutTitle} numberOfLines={1} adjustsFontSizeToFit>İstatistikleri Gör</Text>
                <Text style={styles.statsShortcutDesc} numberOfLines={1} adjustsFontSizeToFit>Kategori analizi ve israf skoru</Text>
              </View>
            </View>
            <Ionicons name="chevron-forward" size={scale(20)} color={colors.primary} />
          </TouchableOpacity>

          {products.length === 0 ? (
            <EmptyState
              icon="basket-outline"
              title="Buzdolabın şu an boş görünüyor"
              description="İlk ürününü ekleyerek son kullanma tarihlerini takip etmeye başla ve israfı önle."
              actionText="İlk Ürünü Ekle"
              onAction={() => navigation.navigate('AddProduct')}
            />
          ) : (
            <>
              <View style={styles.sectionHeader}>
                <Text style={styles.sectionTitle} numberOfLines={1}>Son eklenen ürünler</Text>
                <TouchableOpacity onPress={() => navigation.navigate('Products')}>
                  <Text style={styles.seeAll}>Tümü {'>'}</Text>
                </TouchableOpacity>
              </View>

              <View style={isTablet ? styles.productGridTablet : null}>
                {recentProducts.map((item) => {
                  const cat = CATEGORIES.find(c => c.name === item.category);
                  return (
                    <TouchableOpacity key={item.id} style={[styles.productItem, isTablet && styles.productItemTablet]} onPress={() => navigation.navigate('ProductDetail', { product: item })} activeOpacity={0.7}>
                      <View style={styles.productIconBg}>
                        <Ionicons name={cat?.icon || 'cube-outline'} size={scale(22)} color={colors.primary} />
                      </View>
                      <View style={{ flex: 1, marginLeft: scale(12), marginRight: scale(8) }}>
                        <Text numberOfLines={1} adjustsFontSizeToFit style={styles.productName}>{item.name}</Text>
                        <Text numberOfLines={1} style={styles.productDate}>{formatDate(item.expDate)} • {item.category || 'Kategorisiz'}</Text>
                      </View>
                      <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} />
                    </TouchableOpacity>
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
  const { isTablet, isSmall, horizontalPadding, contentMaxWidth, scale, moderateScale } = responsive;
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
    summaryCard: { backgroundColor: colors.surface, borderRadius: scale(24), padding: scale(24), marginBottom: scale(24), overflow: 'hidden', ...SHADOWS.medium, borderWidth: 1, borderColor: colors.border },
    summaryTextContainer: { zIndex: 1 },
    summaryTitle: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text },
    summaryCount: { fontSize: scale(56), fontWeight: '900', color: colors.primary, marginVertical: scale(4), letterSpacing: -1 },
    summarySub: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), color: colors.textLight, marginTop: scale(-8), fontWeight: '600' },
    summaryDesc: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, marginBottom: scale(16), fontWeight: '500' },
    detailButton: { flexDirection: 'row', backgroundColor: colors.primary, paddingHorizontal: scale(18), paddingVertical: scale(10), borderRadius: scale(22), alignSelf: 'flex-start', alignItems: 'center' },
    detailButtonText: { color: colors.onPrimary, fontWeight: '800', marginRight: scale(4), fontSize: moderateScale(TYPOGRAPHY.caption, 0.3) },
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
    productItemTablet: { width: '48.5%' },
    productIconBg: { width: scale(48), height: scale(48), borderRadius: scale(14), backgroundColor: colors.surfaceAlt, justifyContent: 'center', alignItems: 'center' },
    productName: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text, textTransform: 'capitalize' },
    productDate: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: scale(2), fontWeight: '500' },
  });
};