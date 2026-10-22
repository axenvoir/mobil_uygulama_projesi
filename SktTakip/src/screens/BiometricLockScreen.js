import React, { useContext, useEffect, useState, useMemo, useCallback } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Animated } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as LocalAuthentication from 'expo-local-authentication';
import { AppContext } from '../context/AppContext';
import { useTheme } from '../hooks/useTheme';
import { useTranslation } from '../hooks/useTranslation';

export default function BiometricLockScreen() {
  const { unlockApp, updateProfile, userProfile } = useContext(AppContext);
  const { colors } = useTheme();
  const { t } = useTranslation();
  const styles = useMemo(() => createStyles(colors), [colors]);

  const [error, setError] = useState('');
  const [biometricsUnavailable, setBiometricsUnavailable] = useState(false);
  const [scaleAnim] = useState(() => new Animated.Value(0.9));

  const authenticate = useCallback(async () => {
    try {
      const hasHardware = await LocalAuthentication.hasHardwareAsync();
      const isEnrolled = await LocalAuthentication.isEnrolledAsync();
      if (!hasHardware || !isEnrolled) {
        setBiometricsUnavailable(true);
        setError(t('lock_biometrics_unavailable'));
        return;
      }
      setBiometricsUnavailable(false);
      const result = await LocalAuthentication.authenticateAsync({
        promptMessage: t('lock_prompt'),
        fallbackLabel: t('lock_fallback'),
        cancelLabel: t('cancel'),
      });
      if (result.success) unlockApp();
      else setError(t('lock_auth_failed'));
    } catch (e) {
      setError(t('lock_unexpected_error', { message: e.message }));
    }
  }, [unlockApp, t]);

  // Automatically prompt for biometric authentication when the lock screen opens.
  /* eslint-disable react-hooks/set-state-in-effect */
  useEffect(() => {
    Animated.spring(scaleAnim, { toValue: 1, useNativeDriver: true, tension: 60 }).start();
    authenticate();
  }, [authenticate, scaleAnim]);
  /* eslint-enable react-hooks/set-state-in-effect */

  const handleUnlock = () => {
    setError('');
    authenticate();
  };

  const handleDisableLock = () => {
    updateProfile('biometricsEnabled', false);
    unlockApp();
  };

  return (
    <View style={styles.container}>
      <Animated.View style={[styles.logoBox, { transform: [{ scale: scaleAnim }] }]}>
        <Ionicons name="finger-print" size={60} color={colors.primary} />
      </Animated.View>
      <Text style={styles.title}>{t('lock_title')}</Text>
      <Text style={styles.subtitle}>
        {t('lock_subtitle', { name: userProfile.name || t('profile_anonymous') })}
      </Text>
      {error ? <Text style={styles.error}>{error}</Text> : null}
      {!biometricsUnavailable ? (
        <TouchableOpacity
          style={styles.unlockBtn}
          onPress={handleUnlock}
          accessibilityRole="button"
          accessibilityLabel={t('lock_unlock')}
        >
          <Ionicons name="lock-open-outline" size={20} color={colors.onPrimary} />
          <Text style={styles.unlockText}>{t('lock_unlock')}</Text>
        </TouchableOpacity>
      ) : (
        <TouchableOpacity
          style={styles.unlockBtn}
          onPress={handleDisableLock}
          accessibilityRole="button"
          accessibilityLabel={t('lock_disable')}
        >
          <Ionicons name="shield-outline" size={20} color={colors.onPrimary} />
          <Text style={styles.unlockText}>{t('lock_disable')}</Text>
        </TouchableOpacity>
      )}
    </View>
  );
}

const createStyles = (colors) => StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.primaryDark, justifyContent: 'center', alignItems: 'center', padding: 30 },
  logoBox: { width: 120, height: 120, borderRadius: 30, backgroundColor: colors.surface, justifyContent: 'center', alignItems: 'center', marginBottom: 24 },
  title: { fontSize: 26, fontWeight: 'bold', color: colors.white, marginBottom: 8 },
  subtitle: { fontSize: 14, color: colors.textLight, textAlign: 'center', marginBottom: 24, paddingHorizontal: 20 },
  error: { color: colors.danger, fontSize: 13, marginBottom: 16, textAlign: 'center' },
  unlockBtn: { flexDirection: 'row', backgroundColor: colors.primary, paddingHorizontal: 24, paddingVertical: 14, borderRadius: 25, alignItems: 'center' },
  unlockText: { color: colors.onPrimary, fontWeight: 'bold', marginLeft: 8, fontSize: 15 },
});
