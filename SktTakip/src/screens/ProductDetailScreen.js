import React, { useMemo, useContext } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Image } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { AppContext } from '../context/AppContext';
import { CATEGORIES, CATEGORY_TRANSLATION_KEYS, SHADOWS, TYPOGRAPHY } from '../theme';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { useTranslation } from '../hooks/useTranslation';
import { shareSingleProduct } from '../utils/shareUtils';
import { formatDate, getDaysLeft } from '../utils/formatters';
import * as Clipboard from 'expo-clipboard';

export default function ProductDetailScreen({ route, navigation }) {
  const { product } = route.params;
  const { showToast } = useContext(AppContext);
  const { colors } = useTheme();
  const { t, locale } = useTranslation();
  const responsive = useResponsive();
  const { isTablet, scale } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const daysLeft = getDaysLeft(product.expDate);

  const getStatus = () => {
    if (daysLeft < 0) return { text: t('product_status_expired'), color: colors.danger, bg: colors.danger + '15', icon: 'alert-circle' };
    if (daysLeft === 0) return { text: t('product_status_today'), color: colors.danger, bg: colors.danger + '15', icon: 'warning' };
    if (daysLeft <= 3) return { text: t('product_status_days', { count: daysLeft }), color: colors.danger, bg: colors.danger + '15', icon: 'time' };
    if (daysLeft <= 7) return { text: t('product_status_days', { count: daysLeft }), color: colors.warning, bg: colors.warning + '15', icon: 'time' };
    if (daysLeft <= 30) return { text: t('product_status_days', { count: daysLeft }), color: colors.success, bg: colors.success + '15', icon: 'checkmark-circle' };
    return { text: t('product_status_fresh'), color: colors.success, bg: colors.success + '15', icon: 'checkmark-circle' };
  };

  const status = getStatus();
  const cat = CATEGORIES.find(c => c.name === product.category);
  const catIcon = cat?.icon || 'cube-outline';
  const formattedDate = formatDate(product.expDate, locale);

  const handleShare = async () => {
    const result = await shareSingleProduct({ ...product, expDate: formattedDate });
    if (!result.success) showToast(t('share_failed'), 'error');
  };

  const handleCopyCode = async () => {
    if (!product.productCode) return;
    await Clipboard.setStringAsync(product.productCode);
    showToast(t('product_code_copied'), 'success');
  };

  const handleEdit = () => {
    navigation.replace('EditProduct', { product });
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
      <View style={styles.innerWrap}>
        <View style={styles.header}>
          <TouchableOpacity style={styles.headerBtn} onPress={() => navigation.goBack()} activeOpacity={0.7}>
            <Ionicons name="arrow-back" size={scale(24)} color={colors.text} />
          </TouchableOpacity>
          <Text style={styles.headerTitle} numberOfLines={1}>{t('product_detail_title')}</Text>
          <View style={styles.headerRight}>
            <TouchableOpacity style={styles.headerBtn} onPress={handleShare} activeOpacity={0.7}>
              <Ionicons name="share-social-outline" size={scale(22)} color={colors.primary} />
            </TouchableOpacity>
            <TouchableOpacity style={styles.headerBtn} onPress={handleEdit} activeOpacity={0.7}>
              <Ionicons name="create-outline" size={scale(22)} color={colors.primary} />
            </TouchableOpacity>
          </View>
        </View>

        <View style={styles.photoSection}>
          {product.imageUri ? (
            <Image source={{ uri: product.imageUri }} style={styles.photo} />
          ) : (
            <View style={[styles.photoPlaceholder, { backgroundColor: status.bg }]}>
              <Ionicons name={catIcon} size={scale(isTablet ? 80 : 60)} color={status.color} />
            </View>
          )}
        </View>

        <Text style={styles.productName}>{product.name}</Text>
        <View style={styles.categoryRow}>
          <Ionicons name={catIcon} size={scale(16)} color={colors.textLight} />
          <Text style={styles.categoryText}>{t(CATEGORY_TRANSLATION_KEYS[cat?.id] || 'category_other')}</Text>
        </View>

        <View style={[styles.statusCard, { backgroundColor: status.bg, borderLeftColor: status.color }]}>
          <Ionicons name={status.icon} size={scale(32)} color={status.color} />
          <View style={{ flex: 1, marginLeft: scale(12) }}>
            <Text style={[styles.statusText, { color: status.color }]}>{status.text}</Text>
            <Text style={styles.statusDate}>{t('expiry_date_short')}: {formattedDate}</Text>
          </View>
        </View>

        <View style={styles.infoGrid}>
          {product.productCode ? (
            <TouchableOpacity style={styles.infoCard} onPress={handleCopyCode} activeOpacity={0.7}>
              <View style={[styles.infoIconBox, { backgroundColor: colors.primary + '15' }]}>
                <Ionicons name="pricetags-outline" size={scale(20)} color={colors.primary} />
              </View>
              <Text style={styles.infoLabel}>{t('product_code')}</Text>
              <View style={{ flexDirection: 'row', alignItems: 'center', width: '100%' }}>
                <Text style={styles.infoValue} numberOfLines={1} ellipsizeMode="tail">{product.productCode}</Text>
                <Ionicons name="copy-outline" size={scale(14)} color={colors.textLight} style={{ marginLeft: scale(6) }} />
              </View>
            </TouchableOpacity>
          ) : null}

          {product.quantity ? (
            <View style={styles.infoCard}>
              <View style={[styles.infoIconBox, { backgroundColor: colors.info + '15' }]}>
                <Ionicons name="flask-outline" size={scale(20)} color={colors.info} />
              </View>
              <Text style={styles.infoLabel}>{t('quantity')}</Text>
              <Text style={styles.infoValue} numberOfLines={1} ellipsizeMode="tail">{t('product_quantity', { quantity: product.quantity })}</Text>
            </View>
          ) : null}

          <View style={styles.infoCard}>
            <View style={[styles.infoIconBox, { backgroundColor: colors.warning + '15' }]}>
              <Ionicons name="calendar-outline" size={scale(20)} color={colors.warning} />
            </View>
            <Text style={styles.infoLabel}>{t('expiry_date_short')}</Text>
            <Text style={styles.infoValue} numberOfLines={1} ellipsizeMode="tail">{formattedDate}</Text>
          </View>
        </View>

        {product.note ? (
          <View style={styles.noteCard}>
            <View style={styles.noteHeader}>
              <Ionicons name="document-text-outline" size={scale(18)} color={colors.primary} />
              <Text style={styles.noteLabel}>{t('note')}</Text>
            </View>
            <Text style={styles.noteText}>{product.note}</Text>
          </View>
        ) : null}

        <TouchableOpacity style={styles.editButton} onPress={handleEdit} activeOpacity={0.8}>
          <Ionicons name="create-outline" size={scale(22)} color={colors.onPrimary} />
          <Text style={styles.editButtonText}>{t('edit')}</Text>
        </TouchableOpacity>

        <TouchableOpacity style={[styles.deleteButton, { backgroundColor: colors.danger + '15', borderColor: colors.danger + '30' }]} onPress={() => navigation.goBack()} activeOpacity={0.8}>
          <Ionicons name="close-outline" size={scale(20)} color={colors.danger} />
          <Text style={[styles.deleteButtonText, { color: colors.danger }]}>{t('close')}</Text>
        </TouchableOpacity>
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
    headerBtn: { width: scale(44), height: scale(44), borderRadius: scale(22), backgroundColor: colors.surface, justifyContent: 'center', alignItems: 'center', marginLeft: scale(6), ...SHADOWS.soft },
    headerTitle: { flex: 1, fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text, textAlign: 'center', paddingHorizontal: scale(8) },
    headerRight: { flexDirection: 'row' },
    photoSection: { alignItems: 'center', marginBottom: scale(24) },
    photo: { width: scale(isTablet ? 260 : 200), height: scale(isTablet ? 260 : 200), borderRadius: scale(24), ...SHADOWS.medium },
    photoPlaceholder: { width: scale(isTablet ? 260 : 200), height: scale(isTablet ? 260 : 200), borderRadius: scale(24), justifyContent: 'center', alignItems: 'center' },
    productName: { fontSize: moderateScale(TYPOGRAPHY.h1, 0.3), fontWeight: '900', color: colors.text, textAlign: 'center', marginBottom: scale(8), textTransform: 'capitalize', letterSpacing: -0.5 },
    categoryRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', marginBottom: scale(24) },
    categoryText: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, marginLeft: scale(6), fontWeight: '700' },
    statusCard: { flexDirection: 'row', alignItems: 'center', padding: scale(20), borderRadius: scale(20), borderLeftWidth: 6, marginBottom: scale(24) },
    statusText: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800' },
    statusDate: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, marginTop: scale(4), fontWeight: '500' },
    infoGrid: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'space-between', marginBottom: scale(20) },
    infoCard: { width: '48.5%', backgroundColor: colors.surface, borderRadius: scale(16), padding: scale(16), marginBottom: scale(12), ...SHADOWS.soft },
    infoIconBox: { width: scale(40), height: scale(40), borderRadius: scale(20), justifyContent: 'center', alignItems: 'center', marginBottom: scale(10) },
    infoLabel: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, fontWeight: '700', marginBottom: scale(4) },
    infoValue: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text, flexShrink: 1 },
    noteCard: { backgroundColor: colors.surface, borderRadius: scale(16), padding: scale(20), marginBottom: scale(24), borderLeftWidth: 5, borderLeftColor: colors.primary, ...SHADOWS.soft },
    noteHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: scale(10) },
    noteLabel: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), fontWeight: '800', color: colors.primary, marginLeft: scale(6), letterSpacing: 0.5 },
    noteText: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), color: colors.text, lineHeight: scale(24), fontWeight: '500' },
    editButton: { flexDirection: 'row', backgroundColor: colors.primary, height: scale(60), borderRadius: scale(30), justifyContent: 'center', alignItems: 'center', marginBottom: scale(12), marginTop: scale(8), ...SHADOWS.medium },
    editButtonText: { color: colors.onPrimary, fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', marginLeft: scale(8) },
    deleteButton: { flexDirection: 'row', height: scale(54), borderRadius: scale(27), justifyContent: 'center', alignItems: 'center', borderWidth: 1.5 },
    deleteButtonText: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', marginLeft: scale(6) },
  });
};