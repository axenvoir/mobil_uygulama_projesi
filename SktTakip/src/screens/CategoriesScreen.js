import React, { useContext, useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { AppContext } from '../context/AppContext';
import { CATEGORIES, CATEGORY_TRANSLATION_KEYS, SHADOWS, TYPOGRAPHY } from '../theme';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { useTranslation } from '../hooks/useTranslation';

export default function CategoriesScreen({ navigation }) {
  const { products, setSelectedCategory } = useContext(AppContext);
  const { colors } = useTheme();
  const { t } = useTranslation();
  const responsive = useResponsive();
  const { scale } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const getCategoryCount = (categoryName) => products.filter(p => p.category === categoryName).length;
  const totalProducts = products.length;

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.innerWrap}>
        <View style={styles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()} activeOpacity={0.7}>
            <Ionicons name="arrow-back" size={scale(24)} color={colors.text} />
          </TouchableOpacity>
          <Text style={styles.headerTitle}>{t('categories')}</Text>
          <View style={{ width: scale(24) }} />
        </View>

        <View style={[styles.infoBox, { backgroundColor: colors.primary + '15' }]}>
          <Ionicons name="information-circle-outline" size={scale(22)} color={colors.primary} />
          <Text style={styles.infoText}>
            {t('categories_summary', { products: totalProducts, categories: CATEGORIES.length - 1 })}
          </Text>
        </View>

        {CATEGORIES.filter(c => c.id !== 'all').map((cat) => {
          const count = getCategoryCount(cat.name);
          const percent = totalProducts > 0 ? Math.round((count / totalProducts) * 100) : 0;

          return (
            <TouchableOpacity
              key={cat.id}
              style={styles.categoryCard}
              onPress={() => {
                setSelectedCategory(cat.name);
                navigation.navigate('MainTabs', { screen: 'Products', params: { filter: 'all' } });
              }}
              activeOpacity={0.7}
            >
              <View style={[styles.categoryIconBox, { backgroundColor: colors.primary + '15' }]}>
                <Ionicons name={cat.icon} size={scale(24)} color={colors.primary} />
              </View>
              <View style={{ flex: 1, marginLeft: scale(14), marginRight: scale(8) }}>
                <Text style={styles.categoryName} numberOfLines={1} adjustsFontSizeToFit>{t(CATEGORY_TRANSLATION_KEYS[cat.id])}</Text>
                <View style={[styles.barContainer, { backgroundColor: colors.surfaceAlt }]}>
                  <View style={[styles.barFill, { width: `${percent}%`, backgroundColor: colors.primary }]} />
                </View>
              </View>
              <View style={styles.countBox}>
                <Text style={[styles.categoryCount, { color: colors.primary }]}>{count}</Text>
                <Text style={styles.categoryPercent}>%{percent}</Text>
              </View>
            </TouchableOpacity>
          );
        })}

        <View style={[styles.tipCard, { backgroundColor: colors.warning + '15' }]}>
          <Ionicons name="bulb-outline" size={scale(22)} color={colors.warning} />
          <Text style={styles.tipText} numberOfLines={4}>
            {t('categories_tip')}
          </Text>
        </View>

        <View style={{ height: scale(60) }} />
      </View>
    </ScrollView>
  );
}

const createStyles = (colors, responsive) => {
  const { horizontalPadding, contentMaxWidth, scale, moderateScale } = responsive;
  return StyleSheet.create({
    container: { flex: 1, backgroundColor: colors.background },
    content: { paddingBottom: scale(60), alignItems: 'center' },
    innerWrap: { width: '100%', maxWidth: contentMaxWidth, paddingHorizontal: horizontalPadding, paddingTop: scale(20) },

    header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: scale(24) },
    headerTitle: { fontSize: moderateScale(TYPOGRAPHY.h2, 0.3), fontWeight: '900', color: colors.text },

    infoBox: { flexDirection: 'row', alignItems: 'center', padding: scale(16), borderRadius: scale(16), marginBottom: scale(20) },
    infoText: { flex: 1, marginLeft: scale(12), fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.text, fontWeight: '700' },

    categoryCard: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, padding: scale(16), borderRadius: scale(20), marginBottom: scale(12), ...SHADOWS.soft },
    categoryIconBox: { width: scale(52), height: scale(52), borderRadius: scale(14), justifyContent: 'center', alignItems: 'center' },
    categoryName: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text, marginBottom: scale(8) },
    barContainer: { height: scale(8), borderRadius: scale(4), overflow: 'hidden' },
    barFill: { height: '100%', borderRadius: scale(4) },
    countBox: { alignItems: 'center', marginLeft: scale(12), width: scale(44) },
    categoryCount: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '900' },
    categoryPercent: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: scale(2), fontWeight: '600' },

    tipCard: { flexDirection: 'row', alignItems: 'center', padding: scale(16), borderRadius: scale(16), marginTop: scale(16) },
    tipText: { flex: 1, marginLeft: scale(12), fontSize: moderateScale(TYPOGRAPHY.small, 0.3), lineHeight: scale(20), color: colors.text, fontWeight: '500' },
  });
};