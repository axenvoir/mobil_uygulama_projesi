import React, { useContext, useEffect, useState, useMemo, useCallback } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Animated } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as LocalAuthentication from 'expo-local-authentication';
import { AppContext } from '../context/AppContext';
import { useTheme } from '../hooks/useTheme';

export default function BiometricLockScreen() {
  const { unlockApp, userProfile } = useContext(AppContext);
  const { colors } = useTheme();
  const styles = useMemo(() => createStyles(colors), [colors]);

  const [error, setError] = useState('');
  const [scaleAnim] = useState(() => new Animated.Value(0.9));

  const authenticate = useCallback(async () => {
    try {
      const hasHardware = await LocalAuthentication.hasHardwareAsync();
      const isEnrolled = await LocalAuthentication.isEnrolledAsync();
      if (!hasHardware || !isEnrolled) {
        unlockApp();
        return;
      }
      const result = await LocalAuthentication.authenticateAsync({
        promptMessage: 'Expira kilidini aç',
        fallbackLabel: 'Şifre Kullan',
        cancelLabel: 'İptal',
      });
      if (result.success) unlockApp();
      else setError('Kimlik doğrulanamadı');
    } catch (e) {
      setError('Beklenmeyen hata: ' + e.message);
    }
  }, [unlockApp]);

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

  return (
    <View style={styles.container}>
      <Animated.View style={[styles.logoBox, { transform: [{ scale: scaleAnim }] }]}>
        <Ionicons name="finger-print" size={60} color={colors.primary} />
      </Animated.View>
      <Text style={styles.title}>Expira Kilitli</Text>
      <Text style={styles.subtitle}>Merhaba {userProfile.name}, devam etmek için kimliğini doğrula.</Text>
      {error ? <Text style={styles.error}>{error}</Text> : null}
      <TouchableOpacity style={styles.unlockBtn} onPress={handleUnlock}>
        <Ionicons name="lock-open-outline" size={20} color={colors.onPrimary} />
        <Text style={styles.unlockText}>Kilidi Aç</Text>
      </TouchableOpacity>
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