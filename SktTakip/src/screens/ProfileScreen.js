import React, { useState, useContext, useMemo, useEffect } from 'react';
import { View, Text, StyleSheet, TextInput, TouchableOpacity, ScrollView, Switch, KeyboardAvoidingView, Platform, Keyboard, TouchableWithoutFeedback } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as Updates from 'expo-updates';
import * as LocalAuthentication from 'expo-local-authentication';
import * as Application from 'expo-application';
import * as Clipboard from 'expo-clipboard';
import { AppContext } from '../context/AppContext';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { useTranslation } from '../hooks/useTranslation';
import { generateInventoryPDF } from '../utils/pdfExport';
import { SHADOWS, TYPOGRAPHY, getContrastingTextColor } from '../theme';
import UpdateModal from '../components/UpdateModal';

const AVATAR_COLORS = ['#14B8A6', '#F59E0B', '#EF4444', '#3B82F6', '#8B5CF6', '#EC4899', '#10B981', '#0F172A'];

export default function ProfileScreen({ navigation }) {
  const { userProfile, updateProfile, products, notes, showToast, settings, updateSettings, changeLanguage, locale } = useContext(AppContext);
  const { colors, mode, changeMode } = useTheme();
  const { t } = useTranslation();
  const responsive = useResponsive();
  const { scale } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [name, setName] = useState(userProfile.name);
  const [color, setColor] = useState(userProfile.avatarColor);
  const [showAbout, setShowAbout] = useState(false);

  
  const [updateModalVisible, setUpdateModalVisible] = useState(false);
  const [updateStatus, setUpdateStatus] = useState('checking');
  const [updateError, setUpdateError] = useState('');

  useEffect(() => {
    
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setName(userProfile.name);
    setColor(userProfile.avatarColor);
  }, [userProfile.name, userProfile.avatarColor]);

  const handleSave = () => {
    if (!name.trim()) { showToast(t('profile_name_empty'), 'error'); return; }
    updateProfile('name', name.trim());
    updateProfile('avatarColor', color);
    showToast(t('profile_saved'), 'success');
  };

  const handleToggleBiometrics = async (value) => {
    if (value) {
      const hasHardware = await LocalAuthentication.hasHardwareAsync();
      const isEnrolled = await LocalAuthentication.isEnrolledAsync();
      if (!hasHardware || !isEnrolled) { showToast('Biometric authentication is not available on this device.', 'error'); return; }
      const result = await LocalAuthentication.authenticateAsync({ promptMessage: 'Enable biometric lock' });
      if (result.success) { updateProfile('biometricsEnabled', true); showToast('Biometric lock is enabled.', 'success'); }
    } else {
      updateProfile('biometricsEnabled', false);
      showToast('Biometric lock is disabled.', 'info');
    }
  };

  const checkForUpdates = async () => {
    setUpdateError('');
    setUpdateStatus('checking');
    setUpdateModalVisible(true);
    try {
      const update = await Updates.checkForUpdateAsync();
      if (update.isAvailable) {
        setUpdateStatus('available');
      } else {
        setUpdateStatus('not-available');
      }
    } catch (error) {
      setUpdateStatus('error');
      setUpdateError(error.message);
    }
  };

  const handleDownloadUpdate = async () => {
    setUpdateStatus('downloading');
    try {
      await Updates.fetchUpdateAsync();
      setUpdateStatus('ready');
    } catch (error) {
      setUpdateStatus('error');
      setUpdateError(error.message);
    }
  };

  const handleApplyUpdate = () => {
    Updates.reloadAsync();
  };

  const handleExportPDF = async () => {
    if (products.length === 0) { showToast('Add a product first for PDF.', 'error'); return; }
    showToast('Creating PDF...', 'info');
    const result = await generateInventoryPDF(products, userProfile.name);
    if (result.success) showToast('PDF is ready!', 'success');
    else showToast(`PDF could not be created: ${result.error || 'Unknown error'}`, 'error');
  };

  const handleCopyUserId = async () => {
    try {
      const info = `Expira v${Application.nativeApplicationVersion} (${Application.nativeBuildVersion})\nPackage: ${Application.applicationId}`;
      await Clipboard.setStringAsync(info);
      showToast('Copied!', 'success');
    } catch (error) { if (__DEV__) console.error('Profile info could not be copied to clipboard:', error); showToast('Could not copy.', 'error'); }
  };

  const cycleTheme = () => {
    const order = ['system', 'light', 'dark'];
    const next = order[(order.indexOf(mode) + 1) % order.length];
    changeMode(next);
    showToast(`${t(`settings_theme_${next}`)} ${t('settings_theme_active')}`, 'info');
  };

  const themeIcon = mode === 'system' ? 'phone-portrait-outline' : mode === 'light' ? 'sunny-outline' : 'moon';
  const themeBadge = t(`settings_theme_${mode}`).toLocaleUpperCase(locale);
  const initial = name.charAt(0).toUpperCase() || '?';
  const avatarTextColor = getContrastingTextColor(color);

  return (
    <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={styles.container}>
      <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
        <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
          <View style={styles.innerWrap}>
            <View style={styles.header}>
              <TouchableOpacity onPress={() => navigation.goBack()} activeOpacity={0.7}>
                <Ionicons name="arrow-back" size={scale(24)} color={colors.text} />
              </TouchableOpacity>
              <Text style={styles.headerTitle}>{t('profile_title')}</Text>
              <View style={{ width: scale(24) }} />
            </View>

            <View style={styles.avatarSection}>
              <View style={[styles.avatarBig, { backgroundColor: color }]}>
                <Text style={[styles.avatarBigText, { color: avatarTextColor }]}>{initial}</Text>
              </View>
              <Text style={styles.avatarName} numberOfLines={1} adjustsFontSizeToFit>{name || 'Anonymous'}</Text>
              <Text style={styles.avatarSub}>{t('profile_member')}</Text>
            </View>

            <View style={styles.statsRow}>
              <View style={styles.statBox}>
                <Text style={styles.statNumber}>{products.length}</Text>
                <Text style={styles.statLabel}>{t('products')}</Text>
              </View>
              <View style={styles.statBox}>
                <Text style={styles.statNumber}>{notes.length}</Text>
                <Text style={styles.statLabel}>{t('notes')}</Text>
              </View>
              <View style={styles.statBox}>
                <Text style={styles.statNumber}>{products.length + notes.length}</Text>
                <Text style={styles.statLabel}>{t('profile_total')}</Text>
              </View>
            </View>

            <Text style={styles.label}>{t('profile_user_name')}</Text>
            <View style={styles.inputContainer}>
              <TextInput style={styles.input} placeholder={t('profile_name_placeholder')} placeholderTextColor={colors.textLight} value={name} onChangeText={setName} maxLength={20} />
              <Ionicons name="person-outline" size={scale(20)} color={colors.textLight} />
            </View>

            <Text style={styles.label}>{t('profile_avatar_color')}</Text>
            <View style={styles.colorRow}>
              {AVATAR_COLORS.map((c) => (
                <TouchableOpacity key={c} style={[styles.colorCircle, { backgroundColor: c, borderWidth: color === c ? 3 : 0, borderColor: colors.text }]} onPress={() => setColor(c)} activeOpacity={0.8}>
                  {color === c && <Ionicons name="checkmark" size={scale(20)} color={getContrastingTextColor(c)} />}
                </TouchableOpacity>
              ))}
            </View>

            <Text style={styles.sectionTitle}>{t('settings_appearance')}</Text>

            <View style={styles.languageCard}>
              <View style={styles.languageHeading}>
                <Ionicons name="globe-outline" size={scale(22)} color={colors.primary} />
                <View style={{ flex: 1, marginLeft: scale(12) }}>
                  <Text style={styles.settingTitle}>{t('settings_language')}</Text>
                  <Text style={styles.settingDesc}>{t('language_desc')}</Text>
                </View>
              </View>
              <View style={styles.languageOptions}>
                {['system', 'en', 'tr'].map((language) => {
                  const selected = userProfile.language === language;
                  return (
                    <TouchableOpacity
                      key={language}
                      accessibilityRole="button"
                      accessibilityState={{ selected }}
                      onPress={() => changeLanguage(language)}
                      style={[styles.languageOption, selected && { backgroundColor: colors.primary, borderColor: colors.primary }]}
                    >
                      <Text style={[styles.languageOptionText, selected && { color: colors.onPrimary }]}>
                        {language === 'system' ? t('system_language') : language === 'en' ? t('english') : t('turkish')}
                      </Text>
                    </TouchableOpacity>
                  );
                })}
              </View>
            </View>

            <TouchableOpacity style={styles.settingItem} onPress={cycleTheme} activeOpacity={0.7}>
              <Ionicons name={themeIcon} size={scale(22)} color={colors.primary} />
              <View style={{ flex: 1, marginLeft: scale(12), marginRight: scale(8) }}>
                <Text style={styles.settingTitle}>{t('settings_theme')}</Text>
                <Text style={styles.settingDesc} numberOfLines={1}>{t(`settings_theme_${mode === 'system' ? 'system' : mode}`)} — {t('settings_theme_hint')}</Text>
              </View>
              <View style={[styles.themeBadge, { backgroundColor: colors.primary + '15' }]}>
                <Text style={[styles.themeBadgeText, { color: colors.primary }]}>{themeBadge}</Text>
              </View>
            </TouchableOpacity>

            <View style={styles.settingItem}>
              <Ionicons name="phone-portrait-outline" size={scale(22)} color={colors.primary} />
              <View style={{ flex: 1, marginLeft: scale(12) }}>
                <Text style={styles.settingTitle}>{t('settings_haptics')}</Text>
                <Text style={styles.settingDesc}>{t('settings_haptics_desc')}</Text>
              </View>
              <Switch value={settings.hapticsEnabled} onValueChange={(val) => updateSettings('hapticsEnabled', val)} trackColor={{ false: colors.border, true: colors.primary }} thumbColor={colors.white} />
            </View>

            <View style={styles.settingItem}>
              <Ionicons name="finger-print-outline" size={scale(22)} color={colors.primary} />
              <View style={{ flex: 1, marginLeft: scale(12) }}>
                <Text style={styles.settingTitle}>{t('settings_biometrics')}</Text>
                <Text style={styles.settingDesc}>{t('settings_biometrics_desc')}</Text>
              </View>
              <Switch value={userProfile.biometricsEnabled} onValueChange={handleToggleBiometrics} trackColor={{ false: colors.border, true: colors.primary }} thumbColor={colors.white} />
            </View>

            <Text style={styles.sectionTitle}>{t('settings_data')}</Text>

            <TouchableOpacity style={styles.settingItem} onPress={checkForUpdates} activeOpacity={0.7}>
              <Ionicons name="cloud-download-outline" size={scale(22)} color={colors.primary} />
              <View style={{ flex: 1, marginLeft: scale(12) }}>
                <Text style={styles.settingTitle}>{t('settings_check_updates')}</Text>
                <Text style={styles.settingDesc}>{t('settings_check_updates_desc')}</Text>
              </View>
              <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} />
            </TouchableOpacity>

            <TouchableOpacity style={styles.settingItem} onPress={() => navigation.navigate('Backup')} activeOpacity={0.7}>
              <Ionicons name="archive-outline" size={scale(22)} color={colors.primary} />
              <View style={{ flex: 1, marginLeft: scale(12) }}>
                <Text style={styles.settingTitle}>{t('settings_backup')}</Text>
                <Text style={styles.settingDesc}>{t('settings_backup_desc')}</Text>
              </View>
              <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} />
            </TouchableOpacity>

            <TouchableOpacity style={styles.settingItem} onPress={handleExportPDF} activeOpacity={0.7}>
              <Ionicons name="document-text-outline" size={scale(22)} color={colors.primary} />
              <View style={{ flex: 1, marginLeft: scale(12) }}>
                <Text style={styles.settingTitle}>{t('settings_pdf')}</Text>
                <Text style={styles.settingDesc}>{t('settings_pdf_desc')}</Text>
              </View>
              <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} />
            </TouchableOpacity>

            <TouchableOpacity style={styles.settingItem} onPress={() => setShowAbout(!showAbout)} activeOpacity={0.7}>
              <Ionicons name="information-circle-outline" size={scale(22)} color={colors.primary} />
              <View style={{ flex: 1, marginLeft: scale(12) }}>
                <Text style={styles.settingTitle}>{t('settings_about')}</Text>
                <Text style={styles.settingDesc}>{t('settings_about_desc')}</Text>
              </View>
              <Ionicons name={showAbout ? 'chevron-up' : 'chevron-down'} size={scale(20)} color={colors.textLight} />
            </TouchableOpacity>

            {showAbout && (
              <View style={styles.aboutBox}>
                <View style={styles.aboutRow}>
                  <Text style={styles.aboutLabel}>Application</Text>
                  <Text style={styles.aboutValue}>Expira</Text>
                </View>
                <View style={styles.aboutRow}>
                  <Text style={styles.aboutLabel}>Version</Text>
                  <Text style={styles.aboutValue}>{Application.nativeApplicationVersion || '1.0.0'}</Text>
                </View>
                <View style={styles.aboutRow}>
                  <Text style={styles.aboutLabel}>Build</Text>
                  <Text style={styles.aboutValue}>{Application.nativeBuildVersion || '1'}</Text>
                </View>
                <TouchableOpacity style={[styles.copyInfoBtn, { backgroundColor: colors.primary + '15' }]} onPress={handleCopyUserId} activeOpacity={0.7}>
                  <Ionicons name="copy-outline" size={scale(16)} color={colors.primary} />
                  <Text style={[styles.copyInfoText, { color: colors.primary }]}>Copy Info</Text>
                </TouchableOpacity>
              </View>
            )}

            <TouchableOpacity style={styles.saveButton} onPress={handleSave} activeOpacity={0.8}>
              <Ionicons name="checkmark-circle-outline" size={scale(24)} color={colors.onPrimary} />
              <Text style={styles.saveButtonText}>{t('settings_save')}</Text>
            </TouchableOpacity>

            <Text style={styles.version}>Expira v1.0.0</Text>
            <View style={{ height: scale(60) }} />
          </View>
        </ScrollView>
      </TouchableWithoutFeedback>

      <UpdateModal
        visible={updateModalVisible}
        status={updateStatus}
        onUpdate={updateStatus === 'ready' ? handleApplyUpdate : handleDownloadUpdate}
        onCancel={() => setUpdateModalVisible(false)}
        errorMessage={updateError}
      />
    </KeyboardAvoidingView>
  );
}

const createStyles = (colors, responsive) => {
  const { isTablet, horizontalPadding, contentMaxWidth, scale, moderateScale } = responsive;
  return StyleSheet.create({
    container: { flex: 1, backgroundColor: colors.background },
    content: { paddingBottom: scale(60), alignItems: 'center' },
    innerWrap: { width: '100%', maxWidth: Math.min(contentMaxWidth, 760), paddingHorizontal: horizontalPadding, paddingTop: scale(20) },
    header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: scale(24) },
    headerTitle: { fontSize: moderateScale(TYPOGRAPHY.h2, 0.3), fontWeight: '900', color: colors.text },
    avatarSection: { alignItems: 'center', marginBottom: scale(24) },
    avatarBig: { width: scale(isTablet ? 120 : 100), height: scale(isTablet ? 120 : 100), borderRadius: scale(isTablet ? 60 : 50), justifyContent: 'center', alignItems: 'center', marginBottom: scale(12), ...SHADOWS.medium },
    avatarBigText: { fontSize: scale(isTablet ? 52 : 44), fontWeight: '900', color: colors.white },
    avatarName: { fontSize: moderateScale(TYPOGRAPHY.h2, 0.3), fontWeight: '800', color: colors.text },
    avatarSub: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, marginTop: scale(4), fontWeight: '500' },
    statsRow: { flexDirection: 'row', marginBottom: scale(24) },
    statBox: { flex: 1, backgroundColor: colors.surface, paddingVertical: scale(16), borderRadius: scale(16), marginHorizontal: scale(4), alignItems: 'center', ...SHADOWS.soft },
    statNumber: { fontSize: moderateScale(TYPOGRAPHY.h2, 0.3), fontWeight: '900', color: colors.primary },
    statLabel: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: scale(4), fontWeight: '600' },
    label: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), fontWeight: '800', color: colors.text, marginBottom: scale(8) },
    inputContainer: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, borderRadius: scale(16), paddingHorizontal: scale(16), height: scale(54), marginBottom: scale(16), borderWidth: 1, borderColor: colors.border },
    input: { flex: 1, color: colors.text, fontSize: moderateScale(TYPOGRAPHY.body, 0.3) },
    colorRow: { flexDirection: 'row', flexWrap: 'wrap', marginBottom: scale(24) },
    colorCircle: { width: scale(48), height: scale(48), borderRadius: scale(24), justifyContent: 'center', alignItems: 'center', marginRight: scale(12), marginBottom: scale(12) },
    sectionTitle: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text, marginTop: scale(8), marginBottom: scale(14) },
    languageCard: { backgroundColor: colors.surface, borderRadius: scale(18), borderWidth: 1, borderColor: colors.border, padding: scale(16), marginBottom: scale(12) },
    languageHeading: { flexDirection: 'row', alignItems: 'center', marginBottom: scale(14) },
    languageOptions: { flexDirection: 'row', gap: scale(10) },
    languageOption: { flex: 1, minHeight: scale(42), justifyContent: 'center', alignItems: 'center', borderRadius: scale(12), borderWidth: 1, borderColor: colors.border, backgroundColor: colors.surfaceAlt },
    languageOptionText: { color: colors.text, fontWeight: '700', fontSize: moderateScale(TYPOGRAPHY.caption, 0.3) },
    settingItem: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, padding: scale(18), borderRadius: scale(16), marginBottom: scale(10), ...SHADOWS.soft },
    settingTitle: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text },
    settingDesc: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: scale(2), fontWeight: '500' },
    themeBadge: { paddingHorizontal: scale(12), paddingVertical: scale(6), borderRadius: scale(12) },
    themeBadgeText: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), fontWeight: '800', letterSpacing: 1 },
    aboutBox: { backgroundColor: colors.surface, borderRadius: scale(16), padding: scale(18), marginBottom: scale(10), ...SHADOWS.medium },
    aboutRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: scale(8) },
    aboutLabel: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, fontWeight: '600' },
    aboutValue: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.text, fontWeight: '800', maxWidth: '60%' },
    copyInfoBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', marginTop: scale(12), paddingVertical: scale(10), borderRadius: scale(12) },
    copyInfoText: { fontWeight: '800', fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), marginLeft: scale(6) },
    saveButton: { flexDirection: 'row', backgroundColor: colors.primary, height: scale(60), borderRadius: scale(30), justifyContent: 'center', alignItems: 'center', marginTop: scale(24), marginBottom: scale(12), ...SHADOWS.medium },
    saveButtonText: { color: colors.onPrimary, fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', marginLeft: scale(8) },
    version: { textAlign: 'center', fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginBottom: scale(20), fontWeight: '500' },
  });
};