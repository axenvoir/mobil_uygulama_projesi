import React, { useContext, useState, useEffect, useMemo, useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, Switch, TouchableOpacity, RefreshControl, Platform, TextInput, KeyboardAvoidingView, Keyboard, TouchableWithoutFeedback } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { AppContext } from '../context/AppContext';
import TimePickerModal from '../components/TimePickerModal';
import { requestPermissions, scheduleDailyReminder, cancelAllReminders, sendTestNotification, getScheduledNotifications, isNotificationsAvailable } from '../utils/notifications';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { SHADOWS, TYPOGRAPHY } from '../theme';
import { getDaysLeft } from '../utils/formatters';

export default function NotificationsScreen({ navigation }) {
  const { settings, updateSettings, products, showToast } = useContext(AppContext);
  const { colors } = useTheme();
  const responsive = useResponsive();
  const { scale } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [refreshing, setRefreshing] = useState(false);
  const [showTimePicker, setShowTimePicker] = useState(false);
  const [scheduledCount, setScheduledCount] = useState(0);
  const [notificationMessage, setNotificationMessage] = useState(
    settings.notificationMessage || 'Buzdolabını kontrol etmeyi unutma!'
  );

  const approaching = products.filter(p => {
    const days = getDaysLeft(p.expDate);
    return days >= 0 && days <= 7;
  });
  const overdue = products.filter(p => getDaysLeft(p.expDate) < 0);

  const loadScheduledCount = useCallback(async () => {
    const scheduled = await getScheduledNotifications();
    setScheduledCount(scheduled.length);
  }, []);

  // Read the OS notification store when this screen opens.
  /* eslint-disable react-hooks/set-state-in-effect */
  useEffect(() => { loadScheduledCount(); }, [loadScheduledCount]);
  /* eslint-enable react-hooks/set-state-in-effect */

  const handleToggleDailyReminder = async (value) => {
    if (value) {
      const granted = await requestPermissions();
      if (!granted) { showToast('Bildirim izni verilmedi.', 'error'); return; }
      const [h, m] = settings.reminderTime.split(':').map(n => parseInt(n));
      const id = await scheduleDailyReminder(h, m, notificationMessage);
      if (id) {
        updateSettings('dailyReminder', true);
        showToast(`Günlük hatırlatma ${settings.reminderTime}'de aktif.`, 'success');
        loadScheduledCount();
      } else { showToast('Bildirim planlanamadı.', 'error'); }
    } else {
      await cancelAllReminders();
      updateSettings('dailyReminder', false);
      showToast('Günlük hatırlatma kapatıldı.', 'info');
      loadScheduledCount();
    }
  };

  const handleTimeChange = async (newTime) => {
    updateSettings('reminderTime', newTime);
    if (settings.dailyReminder) {
      const [h, m] = newTime.split(':').map(n => parseInt(n));
      const id = await scheduleDailyReminder(h, m, notificationMessage);
      if (id) { showToast(`Hatırlatma saat ${newTime} olarak güncellendi.`, 'success'); loadScheduledCount(); }
    } else { showToast(`Saat ${newTime} kaydedildi.`, 'info'); }
  };

  const handleMessageBlur = async () => {
    updateSettings('notificationMessage', notificationMessage);
    if (settings.dailyReminder) {
      const [h, m] = settings.reminderTime.split(':').map(n => parseInt(n));
      await scheduleDailyReminder(h, m, notificationMessage);
    }
    showToast('Hatırlatma metni kaydedildi.', 'success');
  };

  const handleTestNotification = async () => {
    const granted = await requestPermissions();
    if (!granted) { showToast('Önce bildirim izni ver.', 'error'); return; }
    const success = await sendTestNotification(notificationMessage);
    if (success) showToast('Test bildirimi gönderildi!', 'success');
    else showToast('Test bildirimi gönderilemedi.', 'error');
  };

  return (
    <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={styles.container}>
      <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
        <ScrollView
          contentContainerStyle={styles.content}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={async () => { setRefreshing(true); await loadScheduledCount(); setTimeout(() => setRefreshing(false), 600); }} colors={[colors.primary]} tintColor={colors.primary} />}
        >
          <View style={styles.innerWrap}>
            <View style={styles.header}>
              <Text style={styles.headerTitle}>Bildirimler</Text>
              <Ionicons name="settings-outline" size={scale(24)} color={colors.text} />
            </View>

            <TouchableOpacity style={[styles.alertCard, { backgroundColor: colors.primary + '15' }]} onPress={() => navigation.navigate('Products')} activeOpacity={0.7}>
              <View style={[styles.alertIcon, { backgroundColor: colors.primary + '25' }]}>
                <Ionicons name="notifications" size={scale(24)} color={colors.primary} />
                <View style={[styles.alertDot, { backgroundColor: colors.warning }]} />
              </View>
              <View style={{ flex: 1, marginHorizontal: scale(12) }}>
                <Text numberOfLines={2} style={styles.alertTitle}>Yakın zamanda {approaching.length} ürünün SKT&apos;si dolacak</Text>
                <Text style={styles.alertDesc}>Kontrol etmeni öneririz.</Text>
              </View>
              <Ionicons name="chevron-forward" size={scale(20)} color={colors.primary} />
            </TouchableOpacity>

            <TouchableOpacity style={[styles.alertCard, { backgroundColor: colors.danger + '15' }]} onPress={() => navigation.navigate('Products')} activeOpacity={0.7}>
              <View style={[styles.alertIcon, { backgroundColor: colors.danger + '25' }]}>
                <Ionicons name="calendar" size={scale(24)} color={colors.danger} />
                <View style={[styles.alertDot, { backgroundColor: colors.danger }]} />
              </View>
              <View style={{ flex: 1, marginHorizontal: scale(12) }}>
                <Text numberOfLines={2} style={[styles.alertTitle, { color: colors.danger }]}>{overdue.length} ürünün son kullanma tarihi geçti</Text>
                <Text style={styles.alertDesc}>İncele ve güncelle</Text>
              </View>
              <Ionicons name="chevron-forward" size={scale(20)} color={colors.danger} />
            </TouchableOpacity>

            <Text style={styles.sectionTitle}>Bildirim tercihleri</Text>

            <View style={styles.settingItem}>
              <Ionicons name="notifications-outline" size={scale(22)} color={colors.primary} />
              <View style={{ flex: 1, marginLeft: scale(12), marginRight: scale(8) }}>
                <Text style={styles.settingTitle}>Günlük hatırlatma</Text>
                <Text style={styles.settingDesc} numberOfLines={1}>{!isNotificationsAvailable ? 'Expo Go’da kullanılamaz; development build gerekir.' : settings.dailyReminder ? 'Aktif - Her gün bildirim' : 'Kapalı'}</Text>
              </View>
              <Switch disabled={!isNotificationsAvailable} value={isNotificationsAvailable && settings.dailyReminder} onValueChange={handleToggleDailyReminder} trackColor={{ false: colors.border, true: colors.primary }} thumbColor={colors.white} />
            </View>

            <TouchableOpacity style={styles.settingItem} onPress={() => setShowTimePicker(true)} activeOpacity={0.7}>
              <Ionicons name="time-outline" size={scale(22)} color={colors.primary} />
              <View style={{ flex: 1, marginLeft: scale(12) }}>
                <Text style={styles.settingTitle}>Bildirim zamanı</Text>
                <Text style={styles.settingDesc}>Her gün saat</Text>
              </View>
              <View style={[styles.timeBadge, { backgroundColor: colors.primary + '15' }]}>
                <Text style={[styles.timeText, { color: colors.primary }]}>{settings.reminderTime}</Text>
                <Ionicons name="chevron-down" size={scale(16)} color={colors.primary} />
              </View>
            </TouchableOpacity>

            <View style={styles.settingItem}>
              <Ionicons name="chatbubble-outline" size={scale(22)} color={colors.primary} />
              <View style={{ flex: 1, marginLeft: scale(12), marginRight: scale(8) }}>
                <Text style={styles.settingTitle}>Hatırlatma Metni</Text>
                <Text style={styles.settingDesc}>Bildirimde görünecek mesaj</Text>
              </View>
            </View>
            <View style={styles.messageInputWrapper}>
              <TextInput style={styles.messageInput} value={notificationMessage} onChangeText={setNotificationMessage} onBlur={handleMessageBlur} placeholder="Buzdolabını kontrol etmeyi unutma!" placeholderTextColor={colors.textLight} multiline maxLength={100} />
              <Text style={styles.charCount}>{notificationMessage.length}/100</Text>
            </View>

            <View style={[styles.statusCard, { borderLeftColor: colors.primary }]}>
              <View style={[styles.statusIconBox, { backgroundColor: colors.primary + '15' }]}>
                <Ionicons name={scheduledCount > 0 ? 'checkmark-circle' : 'information-circle'} size={scale(22)} color={scheduledCount > 0 ? colors.success : colors.textLight} />
              </View>
              <View style={{ flex: 1, marginLeft: scale(12), marginRight: scale(8) }}>
                <Text style={styles.statusTitle} numberOfLines={2}>{scheduledCount > 0 ? `${scheduledCount} aktif bildirim planlandı` : 'Aktif planlanmış bildirim yok'}</Text>
                <Text style={styles.statusDesc} numberOfLines={2}>{Platform.OS === 'android' && !settings.dailyReminder ? 'Günlük hatırlatmayı açarak başla.' : 'Bildirim ayarların kaydedildi.'}</Text>
              </View>
            </View>

            <TouchableOpacity style={[styles.testBtn, !isNotificationsAvailable && { opacity: 0.5 }]} onPress={handleTestNotification} activeOpacity={0.8} disabled={!isNotificationsAvailable}>
              <Ionicons name="paper-plane-outline" size={scale(20)} color={colors.onPrimary} />
              <Text style={styles.testBtnText}>{isNotificationsAvailable ? 'Test Bildirimi Gönder' : 'Test Bildirimi (Development Build Gerekli)'}</Text>
            </TouchableOpacity>

            <Text style={styles.sectionTitle}>Ayarlar</Text>

            <TouchableOpacity style={styles.menuItem} onPress={() => navigation.navigate('Profile')} activeOpacity={0.7}>
              <Ionicons name="person-outline" size={scale(20)} color={colors.text} />
              <Text style={styles.menuText}>Profilim</Text>
              <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} style={{ marginLeft: 'auto' }} />
            </TouchableOpacity>
            <TouchableOpacity style={styles.menuItem} onPress={() => navigation.navigate('Categories')} activeOpacity={0.7}>
              <Ionicons name="folder-outline" size={scale(20)} color={colors.text} />
              <Text style={styles.menuText}>Kategoriler</Text>
              <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} style={{ marginLeft: 'auto' }} />
            </TouchableOpacity>
            <TouchableOpacity style={styles.menuItem} onPress={() => navigation.navigate('Help')} activeOpacity={0.7}>
              <Ionicons name="help-circle-outline" size={scale(20)} color={colors.text} />
              <Text style={styles.menuText}>Yardım ve Destek</Text>
              <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} style={{ marginLeft: 'auto' }} />
            </TouchableOpacity>
            <View style={{ height: scale(60) }} />
          </View>
        </ScrollView>
      </TouchableWithoutFeedback>

      <TimePickerModal visible={showTimePicker} onClose={() => setShowTimePicker(false)} onSelect={handleTimeChange} initialTime={settings.reminderTime} />
    </KeyboardAvoidingView>
  );
}

const createStyles = (colors, responsive) => {
  const { horizontalPadding, contentMaxWidth, scale, moderateScale } = responsive;
  return StyleSheet.create({
    container: { flex: 1, backgroundColor: colors.background },
    content: { paddingBottom: scale(60), alignItems: 'center' },
    innerWrap: { width: '100%', maxWidth: contentMaxWidth, paddingHorizontal: horizontalPadding, paddingTop: scale(20) },
    header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: scale(24) },
    headerTitle: { fontSize: moderateScale(TYPOGRAPHY.h2, 0.3), fontWeight: '900', color: colors.text, letterSpacing: -0.5 },
    alertCard: { flexDirection: 'row', alignItems: 'center', padding: scale(16), borderRadius: scale(20), marginBottom: scale(12) },
    alertIcon: { width: scale(48), height: scale(48), borderRadius: scale(24), justifyContent: 'center', alignItems: 'center', position: 'relative' },
    alertDot: { position: 'absolute', top: 0, right: 0, width: scale(12), height: scale(12), borderRadius: scale(6), borderWidth: 2, borderColor: colors.surface },
    alertTitle: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text, marginBottom: scale(4) },
    alertDesc: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, fontWeight: '500' },
    sectionTitle: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text, marginTop: scale(24), marginBottom: scale(14) },
    settingItem: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, padding: scale(18), borderRadius: scale(16), marginBottom: scale(10), ...SHADOWS.soft },
    settingTitle: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text },
    settingDesc: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: scale(2), fontWeight: '500' },
    timeBadge: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: scale(14), paddingVertical: scale(8), borderRadius: scale(16) },
    timeText: { fontWeight: '800', marginRight: scale(6), fontSize: moderateScale(TYPOGRAPHY.caption, 0.3) },
    messageInputWrapper: { backgroundColor: colors.surface, borderRadius: scale(16), paddingHorizontal: scale(16), paddingTop: scale(16), paddingBottom: scale(12), marginBottom: scale(10), borderWidth: 1, borderColor: colors.border, ...SHADOWS.soft },
    messageInput: { color: colors.text, fontSize: moderateScale(TYPOGRAPHY.body, 0.3), minHeight: scale(60), textAlignVertical: 'top', fontWeight: '500' },
    charCount: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, textAlign: 'right', marginTop: scale(6), fontWeight: '600' },
    statusCard: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, padding: scale(16), borderRadius: scale(16), marginTop: scale(10), marginBottom: scale(14), borderLeftWidth: 5, ...SHADOWS.soft },
    statusIconBox: { width: scale(40), height: scale(40), borderRadius: scale(20), justifyContent: 'center', alignItems: 'center' },
    statusTitle: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), fontWeight: '800', color: colors.text },
    statusDesc: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: scale(2), fontWeight: '500' },
    testBtn: { flexDirection: 'row', backgroundColor: colors.primary, paddingVertical: scale(16), borderRadius: scale(16), justifyContent: 'center', alignItems: 'center', marginBottom: scale(10), ...SHADOWS.medium },
    testBtnText: { color: colors.onPrimary, fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', marginLeft: scale(8) },
    menuItem: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, padding: scale(18), borderRadius: scale(16), marginBottom: scale(10), ...SHADOWS.soft },
    menuText: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '700', color: colors.text, marginLeft: scale(12) },
  });
};