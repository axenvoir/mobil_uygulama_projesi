import React, { useContext, useState, useEffect, useMemo, useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, Switch, TouchableOpacity, RefreshControl, Platform, TextInput, KeyboardAvoidingView, Keyboard, TouchableWithoutFeedback } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { AppContext } from '../context/AppContext';
import TimePickerModal from '../components/TimePickerModal';
import { requestPermissions, scheduleDailyReminder, cancelDailyReminder, sendTestNotification, getScheduledNotifications, isNotificationsAvailable } from '../utils/notifications';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { useTranslation } from '../hooks/useTranslation';
import { SHADOWS, TYPOGRAPHY } from '../theme';
import { getDaysLeft } from '../utils/formatters';

export default function NotificationsScreen({ navigation }) {
  const { settings, updateSettings, products, showToast, setSelectedCategory } = useContext(AppContext);
  const { colors } = useTheme();
  const { t, locale } = useTranslation();
  const responsive = useResponsive();
  const { scale } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [refreshing, setRefreshing] = useState(false);
  const [showTimePicker, setShowTimePicker] = useState(false);
  const [scheduledCount, setScheduledCount] = useState(0);
  const [notificationMessage, setNotificationMessage] = useState(
    settings.notificationMessage || t('notification_default_message')
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
      if (!granted) { showToast(t('notification_permission_denied'), 'error'); return; }
      const [h, m] = settings.reminderTime.split(':').map(n => parseInt(n, 10));
      const id = await scheduleDailyReminder(h, m, notificationMessage, locale);
      if (id) {
        updateSettings('dailyReminder', true);
        showToast(t('notification_daily_enabled', { time: settings.reminderTime }), 'success');
        loadScheduledCount();
      } else { showToast(t('notification_schedule_failed'), 'error'); }
    } else {
      const cancelled = await cancelDailyReminder();
      if (!cancelled) {
        showToast(t('notification_cancel_failed'), 'error');
        return;
      }
      updateSettings('dailyReminder', false);
      showToast(t('notification_daily_disabled'), 'info');
      loadScheduledCount();
    }
  };

  const handleTimeChange = async (newTime) => {
    updateSettings('reminderTime', newTime);
    if (settings.dailyReminder) {
      const [h, m] = newTime.split(':').map(n => parseInt(n, 10));
      const id = await scheduleDailyReminder(h, m, notificationMessage, locale);
      if (id) {
        showToast(t('notification_time_updated', { time: newTime }), 'success');
        loadScheduledCount();
      } else {
        showToast(t('notification_schedule_failed'), 'error');
      }
    } else {
      showToast(t('notification_time_saved', { time: newTime }), 'info');
    }
  };

  const handleMessageBlur = async () => {
    updateSettings('notificationMessage', notificationMessage);
    if (settings.dailyReminder) {
      const [h, m] = settings.reminderTime.split(':').map(n => parseInt(n, 10));
      const id = await scheduleDailyReminder(h, m, notificationMessage, locale);
      if (id) {
        showToast(t('notification_message_saved'), 'success');
        loadScheduledCount();
      } else {
        showToast(t('notification_schedule_failed'), 'error');
      }
      return;
    }
    showToast(t('notification_message_saved'), 'success');
  };

  const handleTestNotification = async () => {
    const granted = await requestPermissions();
    if (!granted) { showToast(t('notification_permission_needed'), 'error'); return; }
    const success = await sendTestNotification(notificationMessage, locale);
    if (success) showToast(t('notification_test_sent'), 'success');
    else showToast(t('notification_test_failed'), 'error');
  };

  const openProducts = (filter) => {
    setSelectedCategory('Tümü');
    navigation.navigate('Products', { filter });
  };

  const onRefresh = async () => {
    setRefreshing(true);
    try {
      await loadScheduledCount();
    } finally {
      setRefreshing(false);
    }
  };

  return (
    <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={styles.container}>
      <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
        <ScrollView
          contentContainerStyle={styles.content}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={[colors.primary]} tintColor={colors.primary} />}
        >
          <View style={styles.innerWrap}>
            <View style={styles.header}>
              <Text style={styles.headerTitle}>{t('notifications')}</Text>
              <Ionicons name="settings-outline" size={scale(24)} color={colors.text} />
            </View>

            <TouchableOpacity style={[styles.alertCard, { backgroundColor: colors.primary + '15' }]} onPress={() => openProducts('approaching')} activeOpacity={0.7}>
              <View style={[styles.alertIcon, { backgroundColor: colors.primary + '25' }]}>
                <Ionicons name="notifications" size={scale(24)} color={colors.primary} />
                <View style={[styles.alertDot, { backgroundColor: colors.warning }]} />
              </View>
              <View style={{ flex: 1, marginHorizontal: scale(12) }}>
                <Text numberOfLines={2} style={styles.alertTitle}>{t('notification_upcoming_count', { count: approaching.length })}</Text>
                <Text style={styles.alertDesc}>{t('notification_check_recommendation')}</Text>
              </View>
              <Ionicons name="chevron-forward" size={scale(20)} color={colors.primary} />
            </TouchableOpacity>

            <TouchableOpacity style={[styles.alertCard, { backgroundColor: colors.danger + '15' }]} onPress={() => openProducts('expired')} activeOpacity={0.7}>
              <View style={[styles.alertIcon, { backgroundColor: colors.danger + '25' }]}>
                <Ionicons name="calendar" size={scale(24)} color={colors.danger} />
                <View style={[styles.alertDot, { backgroundColor: colors.danger }]} />
              </View>
              <View style={{ flex: 1, marginHorizontal: scale(12) }}>
                <Text numberOfLines={2} style={[styles.alertTitle, { color: colors.danger }]}>{t('notification_overdue_count', { count: overdue.length })}</Text>
                <Text style={styles.alertDesc}>{t('notification_review')}</Text>
              </View>
              <Ionicons name="chevron-forward" size={scale(20)} color={colors.danger} />
            </TouchableOpacity>

            <Text style={styles.sectionTitle}>{t('notification_preferences')}</Text>

            <View style={styles.settingItem}>
              <Ionicons name="notifications-outline" size={scale(22)} color={colors.primary} />
              <View style={{ flex: 1, marginLeft: scale(12), marginRight: scale(8) }}>
                <Text style={styles.settingTitle}>{t('notification_daily_reminder')}</Text>
                <Text style={styles.settingDesc} numberOfLines={1}>{!isNotificationsAvailable ? t('notification_development_build_required') : settings.dailyReminder ? t('notification_active_daily') : t('notification_off')}</Text>
              </View>
              <Switch disabled={!isNotificationsAvailable} value={isNotificationsAvailable && settings.dailyReminder} onValueChange={handleToggleDailyReminder} trackColor={{ false: colors.border, true: colors.primary }} thumbColor={colors.white} />
            </View>

            <TouchableOpacity style={styles.settingItem} onPress={() => setShowTimePicker(true)} activeOpacity={0.7}>
              <Ionicons name="time-outline" size={scale(22)} color={colors.primary} />
              <View style={{ flex: 1, marginLeft: scale(12) }}>
                <Text style={styles.settingTitle}>{t('notification_time')}</Text>
                <Text style={styles.settingDesc}>{t('notification_time_daily')}</Text>
              </View>
              <View style={[styles.timeBadge, { backgroundColor: colors.primary + '15' }]}>
                <Text style={[styles.timeText, { color: colors.primary }]}>{settings.reminderTime}</Text>
                <Ionicons name="chevron-down" size={scale(16)} color={colors.primary} />
              </View>
            </TouchableOpacity>

            <View style={styles.settingItem}>
              <Ionicons name="chatbubble-outline" size={scale(22)} color={colors.primary} />
              <View style={{ flex: 1, marginLeft: scale(12), marginRight: scale(8) }}>
                <Text style={styles.settingTitle}>{t('notification_message')}</Text>
                <Text style={styles.settingDesc}>{t('notification_message_description')}</Text>
              </View>
            </View>
            <View style={styles.messageInputWrapper}>
              <TextInput style={styles.messageInput} value={notificationMessage} onChangeText={setNotificationMessage} onBlur={handleMessageBlur} placeholder={t('notification_default_message')} placeholderTextColor={colors.textLight} multiline maxLength={100} />
              <Text style={styles.charCount}>{notificationMessage.length}/100</Text>
            </View>

            <View style={[styles.statusCard, { borderLeftColor: colors.primary }]}>
              <View style={[styles.statusIconBox, { backgroundColor: colors.primary + '15' }]}>
                <Ionicons name={scheduledCount > 0 ? 'checkmark-circle' : 'information-circle'} size={scale(22)} color={scheduledCount > 0 ? colors.success : colors.textLight} />
              </View>
              <View style={{ flex: 1, marginLeft: scale(12), marginRight: scale(8) }}>
                <Text style={styles.statusTitle} numberOfLines={2}>{scheduledCount > 0 ? t('notification_scheduled_count', { count: scheduledCount }) : t('notification_none_scheduled')}</Text>
                <Text style={styles.statusDesc} numberOfLines={2}>{Platform.OS === 'android' && !settings.dailyReminder ? t('notification_enable_to_start') : t('notification_settings_saved')}</Text>
              </View>
            </View>

            <TouchableOpacity style={[styles.testBtn, !isNotificationsAvailable && { opacity: 0.5 }]} onPress={handleTestNotification} activeOpacity={0.8} disabled={!isNotificationsAvailable}>
              <Ionicons name="paper-plane-outline" size={scale(20)} color={colors.onPrimary} />
              <Text style={styles.testBtnText}>{isNotificationsAvailable ? t('notification_send_test') : t('notification_test_requires_build')}</Text>
            </TouchableOpacity>

            <Text style={styles.sectionTitle}>{t('settings')}</Text>

            <TouchableOpacity style={styles.menuItem} onPress={() => navigation.navigate('Profile')} activeOpacity={0.7}>
              <Ionicons name="person-outline" size={scale(20)} color={colors.text} />
              <Text style={styles.menuText}>{t('profile')}</Text>
              <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} style={{ marginLeft: 'auto' }} />
            </TouchableOpacity>
            <TouchableOpacity style={styles.menuItem} onPress={() => navigation.navigate('Categories')} activeOpacity={0.7}>
              <Ionicons name="folder-outline" size={scale(20)} color={colors.text} />
              <Text style={styles.menuText}>{t('categories')}</Text>
              <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} style={{ marginLeft: 'auto' }} />
            </TouchableOpacity>
            <TouchableOpacity style={styles.menuItem} onPress={() => navigation.navigate('Help')} activeOpacity={0.7}>
              <Ionicons name="help-circle-outline" size={scale(20)} color={colors.text} />
              <Text style={styles.menuText}>{t('help')}</Text>
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