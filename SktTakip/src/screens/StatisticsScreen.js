import React, { useContext, useMemo, useState, useEffect } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { AppContext } from '../context/AppContext';
import { CATEGORIES, CATEGORY_TRANSLATION_KEYS, SHADOWS, TYPOGRAPHY } from '../theme';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { useTranslation } from '../hooks/useTranslation';
import { formatDate, getDaysLeft } from '../utils/formatters';
import Skeleton from '../components/Skeleton';

export default function StatisticsScreen({ navigation }) {
  const { products } = useContext(AppContext);
  const { colors } = useTheme();
  const { t, locale } = useTranslation();
  const responsive = useResponsive();
  const { isTablet, scale } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);
  const [isPageLoading, setIsPageLoading] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => setIsPageLoading(false), 500);
    return () => clearTimeout(timer);
  }, []);

  const fresh = products.filter(p => getDaysLeft(p.expDate) > 7);
  const approaching = products.filter(p => getDaysLeft(p.expDate) >= 0 && getDaysLeft(p.expDate) <= 7);
  const overdue = products.filter(p => getDaysLeft(p.expDate) < 0);
  const total = products.length || 1;

  const categoryStats = CATEGORIES.filter(c => c.id !== 'all').map(cat => {
    const count = products.filter(p => p.category === cat.name).length;
    return { ...cat, count, percent: products.length > 0 ? Math.round((count / total) * 100) : 0 };
  }).sort((a, b) => b.count - a.count);

  const maxCount = Math.max(...categoryStats.map(c => c.count), 1);

  const upcomingProducts = [...products]
    .filter(p => getDaysLeft(p.expDate) >= 0)
    .sort((a, b) => a.expDate.localeCompare(b.expDate))
    .slice(0, 5);

  const freshnessScore = products.length > 0
    ? Math.round(((fresh.length + approaching.length * 0.5) / products.length) * 100)
    : 0;

  const getScoreColor = (score) => {
    if (score >= 70) return colors.success;
    if (score >= 40) return colors.warning;
    return colors.danger;
  };

  const getScoreMessage = (score) => {
    if (score >= 70) return t('statistics_score_great');
    if (score >= 40) return t('statistics_score_ok');
    return t('statistics_score_low');
  };

  if (isPageLoading) {
    return (
      <ScrollView style={styles.container} contentContainerStyle={styles.content}>
        <View style={styles.innerWrap}>
          <View style={styles.header}>
            <TouchableOpacity onPress={() => navigation.goBack()} activeOpacity={0.7}>
              <Ionicons name="arrow-back" size={scale(24)} color={colors.text} />
            </TouchableOpacity>
            <Text style={styles.headerTitle}>{t('statistics')}</Text>
            <View style={{ width: scale(24) }} />
          </View>
          <Skeleton height={scale(140)} borderRadius={scale(24)} style={{ marginBottom: scale(20) }} />
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginBottom: scale(20) }}>
            <Skeleton width="31%" height={scale(90)} borderRadius={scale(16)} />
            <Skeleton width="31%" height={scale(90)} borderRadius={scale(16)} />
            <Skeleton width="31%" height={scale(90)} borderRadius={scale(16)} />
          </View>
          <Skeleton height={scale(120)} borderRadius={scale(20)} style={{ marginBottom: scale(20) }} />
          <Skeleton height={scale(200)} borderRadius={scale(20)} />
        </View>
      </ScrollView>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.innerWrap}>
        <View style={styles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()} activeOpacity={0.7}>
            <Ionicons name="arrow-back" size={scale(24)} color={colors.text} />
          </TouchableOpacity>
          <Text style={styles.headerTitle}>{t('statistics')}</Text>
          <View style={{ width: scale(24) }} />
        </View>

        <View style={styles.summaryCard}>
          <View style={styles.summaryLeft}>
            <Text style={styles.summaryLabel}>{t('statistics_freshness_score')}</Text>
            <Text style={[styles.summaryScore, { color: getScoreColor(freshnessScore) }]}>{freshnessScore}%</Text>
            <Text style={styles.summaryMessage} numberOfLines={2}>{getScoreMessage(freshnessScore)}</Text>
          </View>
          <View style={[styles.scoreCircle, { borderColor: getScoreColor(freshnessScore) }]}>
            <Ionicons name={freshnessScore >= 70 ? 'happy-outline' : freshnessScore >= 40 ? 'sad-outline' : 'alert-circle-outline'} size={scale(isTablet ? 40 : 32)} color={getScoreColor(freshnessScore)} />
          </View>
        </View>

        <View style={styles.statRow}>
          <View style={[styles.statBox, { borderTopColor: colors.success }]}>
            <Ionicons name="checkmark-circle" size={scale(22)} color={colors.success} />
            <Text style={styles.statNumber}>{fresh.length}</Text>
            <Text style={styles.statLabel}>{t('fresh')}</Text>
          </View>
          <View style={[styles.statBox, { borderTopColor: colors.warning }]}>
            <Ionicons name="time" size={scale(22)} color={colors.warning} />
            <Text style={styles.statNumber}>{approaching.length}</Text>
            <Text style={styles.statLabel}>{t('approaching')}</Text>
          </View>
          <View style={[styles.statBox, { borderTopColor: colors.danger }]}>
            <Ionicons name="close-circle" size={scale(22)} color={colors.danger} />
            <Text style={styles.statNumber}>{overdue.length}</Text>
            <Text style={styles.statLabel}>{t('expired')}</Text>
          </View>
        </View>

        {products.length > 0 && (
          <View style={styles.progressCard}>
            <Text style={styles.sectionTitle}>{t('statistics_overview')}</Text>
            <View style={styles.progressBar}>
              {fresh.length > 0 && <View style={[styles.progressSegment, { flex: fresh.length, backgroundColor: colors.success }]} />}
              {approaching.length > 0 && <View style={[styles.progressSegment, { flex: approaching.length, backgroundColor: colors.warning }]} />}
              {overdue.length > 0 && <View style={[styles.progressSegment, { flex: overdue.length, backgroundColor: colors.danger }]} />}
            </View>
            <View style={styles.legendRow}>
              <View style={styles.legendItem}><View style={[styles.legendDot, { backgroundColor: colors.success }]} /><Text style={styles.legendText}>{t('fresh')} ({fresh.length})</Text></View>
              <View style={styles.legendItem}><View style={[styles.legendDot, { backgroundColor: colors.warning }]} /><Text style={styles.legendText}>{t('approaching')} ({approaching.length})</Text></View>
              <View style={styles.legendItem}><View style={[styles.legendDot, { backgroundColor: colors.danger }]} /><Text style={styles.legendText}>{t('expired')} ({overdue.length})</Text></View>
            </View>
          </View>
        )}

        <View style={styles.categoryCard}>
          <Text style={styles.sectionTitle}>{t('statistics_category_distribution')}</Text>
          {categoryStats.filter(c => c.count > 0).length === 0 ? (
            <View style={styles.emptySmall}><Ionicons name="stats-chart-outline" size={scale(40)} color={colors.textLight} style={{ opacity: 0.5 }} /><Text style={styles.emptySmallText}>{t('statistics_no_data')}</Text></View>
          ) : (
            categoryStats.filter(c => c.count > 0).map(cat => (
              <View key={cat.id} style={styles.categoryRow}>
                <View style={styles.categoryLeft}>
                  <View style={[styles.categoryIconBox, { backgroundColor: colors.primary + '15' }]}><Ionicons name={cat.icon} size={scale(16)} color={colors.primary} /></View>
                  <Text style={styles.categoryName} numberOfLines={1}>{t(CATEGORY_TRANSLATION_KEYS[cat.id])}</Text>
                </View>
                <View style={[styles.categoryBarContainer, { backgroundColor: colors.surfaceAlt }]}>
                  <View style={[styles.categoryBar, { width: `${(cat.count / maxCount) * 100}%`, backgroundColor: colors.primary }]} />
                </View>
                <Text style={[styles.categoryCount, { color: colors.primary }]}>{cat.count}</Text>
              </View>
            ))
          )}
        </View>

        {upcomingProducts.length > 0 && (
          <View style={styles.upcomingCard}>
            <Text style={styles.sectionTitle}>{t('statistics_upcoming_expiry')}</Text>
            {upcomingProducts.map(item => {
              const days = getDaysLeft(item.expDate);
              const isUrgent = days <= 7;
              return (
                <View key={item.id} style={styles.upcomingRow}>
                  <View style={[styles.upcomingDot, { backgroundColor: isUrgent ? colors.danger : colors.primary }]} />
                  <View style={{ flex: 1 }}>
                    <Text style={styles.upcomingName} numberOfLines={1}>{item.name}</Text>
                    <Text style={styles.upcomingDate} numberOfLines={1}>{formatDate(item.expDate, locale)}</Text>
                  </View>
                  <View style={[styles.upcomingBadge, { backgroundColor: isUrgent ? colors.danger + '15' : colors.primary + '15' }]}>
                    <Text style={[styles.upcomingDays, { color: isUrgent ? colors.danger : colors.primary }]}>{days === 0 ? 'Bugün' : days < 0 ? `${Math.abs(days)}g geçti` : `${days}g kaldı`}</Text>
                  </View>
                </View>
              );
            })}
          </View>
        )}
        <View style={{ height: scale(60) }} />
      </View>
    </ScrollView>
  );
}

const createStyles = (colors, responsive) => {
  const { isTablet, horizontalPadding, contentMaxWidth, scale, moderateScale } = responsive;
  return StyleSheet.create({
    container: { flex: 1, backgroundColor: colors.background },
    content: { paddingBottom: scale(60), alignItems: 'center' },
    innerWrap: { width: '100%', maxWidth: contentMaxWidth, paddingHorizontal: horizontalPadding, paddingTop: scale(20) },
    header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: scale(24) },
    headerTitle: { fontSize: moderateScale(TYPOGRAPHY.h2, 0.3), fontWeight: '900', color: colors.text },
    summaryCard: { flexDirection: 'row', backgroundColor: colors.surface, borderRadius: scale(24), padding: scale(isTablet ? 28 : 24), marginBottom: scale(20), alignItems: 'center', ...SHADOWS.medium },
    summaryLeft: { flex: 1 },
    summaryLabel: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, fontWeight: '700' },
    summaryScore: { fontSize: scale(isTablet ? 56 : 48), fontWeight: '900', marginVertical: scale(4) },
    summaryMessage: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, fontWeight: '500' },
    scoreCircle: { width: scale(isTablet ? 90 : 70), height: scale(isTablet ? 90 : 70), borderRadius: scale(isTablet ? 45 : 35), borderWidth: 4, justifyContent: 'center', alignItems: 'center' },
    statRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: scale(20) },
    statBox: { flex: 1, backgroundColor: colors.surface, paddingVertical: scale(16), paddingHorizontal: scale(4), borderRadius: scale(16), marginHorizontal: scale(5), borderTopWidth: 4, ...SHADOWS.soft, alignItems: 'center' },
    statNumber: { fontSize: scale(isTablet ? 28 : 24), fontWeight: '900', color: colors.text, marginTop: scale(6) },
    statLabel: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: scale(2), fontWeight: '700' },
    progressCard: { backgroundColor: colors.surface, borderRadius: scale(20), padding: scale(20), marginBottom: scale(20), ...SHADOWS.soft },
    progressBar: { flexDirection: 'row', height: scale(14), borderRadius: scale(7), overflow: 'hidden', marginVertical: scale(14) },
    progressSegment: { height: '100%' },
    legendRow: { flexDirection: 'row', justifyContent: 'space-between', flexWrap: 'wrap' },
    legendItem: { flexDirection: 'row', alignItems: 'center', marginRight: scale(10), marginBottom: scale(6) },
    legendDot: { width: scale(10), height: scale(10), borderRadius: scale(5), marginRight: scale(4) },
    legendText: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, fontWeight: '700' },
    categoryCard: { backgroundColor: colors.surface, borderRadius: scale(20), padding: scale(20), marginBottom: scale(20), ...SHADOWS.soft },
    categoryRow: { flexDirection: 'row', alignItems: 'center', marginBottom: scale(12) },
    categoryLeft: { flexDirection: 'row', alignItems: 'center', width: scale(isTablet ? 180 : 130) },
    categoryIconBox: { width: scale(30), height: scale(30), borderRadius: scale(10), justifyContent: 'center', alignItems: 'center' },
    categoryName: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.text, marginLeft: scale(8), flex: 1, fontWeight: '700' },
    categoryBarContainer: { flex: 1, height: scale(10), borderRadius: scale(5), overflow: 'hidden', marginHorizontal: scale(8) },
    categoryBar: { height: '100%', borderRadius: scale(5) },
    categoryCount: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), fontWeight: '800', width: scale(30), textAlign: 'right' },
    upcomingCard: { backgroundColor: colors.surface, borderRadius: scale(20), padding: scale(20), marginBottom: scale(20), ...SHADOWS.soft },
    upcomingRow: { flexDirection: 'row', alignItems: 'center', paddingVertical: scale(12), borderBottomWidth: 1, borderBottomColor: colors.border },
    upcomingDot: { width: scale(8), height: scale(8), borderRadius: scale(4), marginRight: scale(10) },
    upcomingName: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.text, fontWeight: '700' },
    upcomingDate: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: scale(2) },
    upcomingBadge: { paddingHorizontal: scale(10), paddingVertical: scale(4), borderRadius: scale(10) },
    upcomingDays: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), fontWeight: '800' },
    sectionTitle: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text, marginBottom: scale(10) },
    emptySmall: { alignItems: 'center', paddingVertical: scale(20) },
    emptySmallText: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, marginTop: scale(8) },
  });
};