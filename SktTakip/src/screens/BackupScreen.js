import React, { useState, useContext, useMemo } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { AppContext } from '../context/AppContext';
import { createBackup, restoreBackup } from '../utils/backupUtils';
import { notifySuccess, notifyError } from '../utils/haptics';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { SHADOWS, TYPOGRAPHY } from '../theme';
import ConfirmModal from '../components/ConfirmModal';

export default function BackupScreen({ navigation }) {
  const { showToast, settings, products, notes, userProfile, replaceAppData, resetAppData } = useContext(AppContext);
  const { colors, mode, changeMode } = useTheme();
  const responsive = useResponsive();
  const { scale } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalType, setModalType] = useState('warning');
  const [modalTitle, setModalTitle] = useState('');
  const [modalMessage, setModalMessage] = useState('');
  const [modalConfirmAction, setModalConfirmAction] = useState(null);
  const [modalConfirmText, setModalConfirmText] = useState('Evet');

  const summary = { products: products.length, notes: notes.length };

  const openModal = (type, title, message, confirmText, onConfirm) => {
    setModalType(type);
    setModalTitle(title);
    setModalMessage(message);
    setModalConfirmText(confirmText);
    setModalConfirmAction(() => onConfirm);
    setModalVisible(true);
  };

  const handleBackup = () => {
    openModal('info', 'Yedek Oluştur', 'Ürün fotoğrafları yedek dosyasına dahil edilmez. Ürünler, notlar ve ayarlar yedeklenir. Devam etmek istiyor musun?', 'Yedekle', async () => {
      setModalVisible(false);
      setLoading(true);
      try {
        const result = await createBackup({ products, notes, settings, userProfile, themeMode: mode });
        if (result.success) {
          if (settings.hapticsEnabled) await notifySuccess();
          showToast('Yedek oluşturuldu!', 'success');
        } else {
          if (settings.hapticsEnabled) await notifyError();
          showToast('Yedek oluşturulamadı: ' + result.error, 'error');
        }
      } catch (error) {
        if (settings.hapticsEnabled) await notifyError();
        showToast('Yedek oluşturulamadı: ' + error.message, 'error');
      } finally {
        setLoading(false);
      }
    });
  };

  const handleRestore = () => {
    openModal('warning', 'Yedekten Yükle', 'Mevcut veriler yedektekilerle DEĞİŞTİRİLECEK. Devam etmek istiyor musun?', 'Devam Et', async () => {
      setModalVisible(false);
      setLoading(true);
      try {
        const result = await restoreBackup();
        if (result.canceled) {
          showToast('İptal edildi.', 'info');
        } else if (result.success) {
          await replaceAppData(result.data);
          if (result.data.themeMode) await changeMode(result.data.themeMode);
          if (settings.hapticsEnabled) await notifySuccess();
          showToast(`${result.stats.products} ürün ve ${result.stats.notes} not geri yüklendi.`, 'success');
        } else {
          if (settings.hapticsEnabled) await notifyError();
          showToast('Yedek yüklenemedi: ' + result.error, 'error');
        }
      } catch (error) {
        if (settings.hapticsEnabled) await notifyError();
        showToast('Yedek yüklenemedi: ' + error.message, 'error');
      } finally {
        setLoading(false);
      }
    });
  };

  const handleReset = () => {
    openModal('danger', 'Tüm Verileri Sil', 'TÜM veriler kalıcı olarak silinecek. Bu işlem GERİ ALINAMAZ! Devam etmek istiyor musun?', 'SİL', async () => {
      setModalVisible(false);
      setLoading(true);
      try {
        await resetAppData();
        await changeMode('system');
        showToast('Veriler silindi.', 'info');
      } catch (error) {
        showToast('Veriler silinemedi: ' + error.message, 'error');
      } finally {
        setLoading(false);
      }
    });
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.innerWrap}>
        <View style={styles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()} activeOpacity={0.7}>
            <Ionicons name="arrow-back" size={scale(24)} color={colors.text} />
          </TouchableOpacity>
          <Text style={styles.headerTitle}>Yedekleme</Text>
          <View style={{ width: scale(24) }} />
        </View>

        <View style={styles.summaryCard}>
          <Ionicons name="archive-outline" size={scale(40)} color={colors.primary} />
          <Text style={styles.summaryTitle}>Mevcut Veriler</Text>
          <View style={styles.summaryGrid}>
            <View style={styles.summaryItem}>
              <Text style={styles.summaryNumber}>{summary.products}</Text>
              <Text style={styles.summaryLabel}>Ürün</Text>
            </View>
            <View style={styles.summaryItem}>
              <Text style={styles.summaryNumber}>{summary.notes}</Text>
              <Text style={styles.summaryLabel}>Not</Text>
            </View>
          </View>
        </View>

        <Text style={styles.sectionTitle}>İşlemler</Text>

        <TouchableOpacity style={styles.actionCard} onPress={handleBackup} disabled={loading} activeOpacity={0.7}>
          <View style={[styles.actionIcon, { backgroundColor: colors.primary + '15' }]}>
            <Ionicons name="cloud-upload-outline" size={scale(24)} color={colors.primary} />
          </View>
          <View style={{ flex: 1, marginLeft: scale(16), marginRight: scale(8) }}>
            <Text style={styles.actionTitle}>Yedek Oluştur</Text>
            <Text style={styles.actionDesc} numberOfLines={2}>Tüm verilerini JSON olarak paylaş</Text>
          </View>
          <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} />
        </TouchableOpacity>

        <TouchableOpacity style={styles.actionCard} onPress={handleRestore} disabled={loading} activeOpacity={0.7}>
          <View style={[styles.actionIcon, { backgroundColor: colors.info + '15' }]}>
            <Ionicons name="cloud-download-outline" size={scale(24)} color={colors.info} />
          </View>
          <View style={{ flex: 1, marginLeft: scale(16), marginRight: scale(8) }}>
            <Text style={styles.actionTitle}>Yedekten Yükle</Text>
            <Text style={styles.actionDesc} numberOfLines={2}>Daha önce oluşturduğun yedeği geri yükle</Text>
          </View>
          <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} />
        </TouchableOpacity>

        <Text style={[styles.sectionTitle, { color: colors.danger, marginTop: scale(24) }]}>Tehlikeli Bölge</Text>

        <TouchableOpacity style={[styles.actionCard, { borderColor: colors.danger + '40', borderWidth: 1 }]} onPress={handleReset} activeOpacity={0.7}>
          <View style={[styles.actionIcon, { backgroundColor: colors.danger + '15' }]}>
            <Ionicons name="trash-outline" size={scale(24)} color={colors.danger} />
          </View>
          <View style={{ flex: 1, marginLeft: scale(16), marginRight: scale(8) }}>
            <Text style={[styles.actionTitle, { color: colors.danger }]}>Tüm Verileri Sil</Text>
            <Text style={styles.actionDesc} numberOfLines={2}>Tüm verileri sıfırla (geri alınamaz)</Text>
          </View>
          <Ionicons name="chevron-forward" size={scale(20)} color={colors.danger} />
        </TouchableOpacity>

        <View style={[styles.infoBox, { backgroundColor: colors.primary + '15' }]}>
          <Ionicons name="information-circle-outline" size={scale(20)} color={colors.primary} />
          <Text style={styles.infoText} numberOfLines={3}>Ürün fotoğrafları yedeklenmez. Yedek dosyanı güvenli bir yerde sakla.</Text>
        </View>

        <View style={{ height: scale(60) }} />

        {loading && (
          <View style={styles.loadingOverlay}>
            <View style={styles.loadingBox}>
              <ActivityIndicator size="large" color={colors.primary} />
              <Text style={styles.loadingText}>İşlem yapılıyor...</Text>
            </View>
          </View>
        )}
      </View>

      <ConfirmModal visible={modalVisible} title={modalTitle} message={modalMessage} type={modalType} confirmText={modalConfirmText} onConfirm={modalConfirmAction} onCancel={() => setModalVisible(false)} />
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
    summaryCard: { backgroundColor: colors.surface, borderRadius: scale(24), padding: scale(24), alignItems: 'center', marginBottom: scale(24), ...SHADOWS.medium },
    summaryTitle: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text, marginTop: scale(12), marginBottom: scale(20) },
    summaryGrid: { flexDirection: 'row', justifyContent: 'space-around', width: '100%' },
    summaryItem: { alignItems: 'center', paddingVertical: scale(12) },
    summaryNumber: { fontSize: scale(36), fontWeight: '900', color: colors.primary },
    summaryLabel: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, marginTop: scale(4), fontWeight: '600' },
    sectionTitle: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text, marginBottom: scale(14) },
    actionCard: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, padding: scale(18), borderRadius: scale(20), marginBottom: scale(12), ...SHADOWS.soft },
    actionIcon: { width: scale(52), height: scale(52), borderRadius: scale(26), justifyContent: 'center', alignItems: 'center' },
    actionTitle: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text },
    actionDesc: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: scale(2), lineHeight: scale(18), fontWeight: '500' },
    infoBox: { flexDirection: 'row', alignItems: 'center', padding: scale(16), borderRadius: scale(16), marginTop: scale(20) },
    infoText: { flex: 1, fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.text, marginLeft: scale(10), lineHeight: scale(20), fontWeight: '500' },
    loadingOverlay: { position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'center', alignItems: 'center', zIndex: 999 },
    loadingBox: { backgroundColor: colors.surface, padding: scale(24), borderRadius: scale(20), alignItems: 'center', ...SHADOWS.heavy },
    loadingText: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.text, marginTop: scale(12), fontWeight: '800' },
  });
};