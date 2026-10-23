import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, Modal, TouchableOpacity, Animated, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { SHADOWS, TYPOGRAPHY } from '../theme';

export default function UpdateModal({ visible, status, onUpdate, onCancel, errorMessage }) {
  const { colors } = useTheme();
  const responsive = useResponsive();
  const { scale } = responsive;
  const styles = React.useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [scaleAnim] = useState(() => new Animated.Value(0.9));
  const [opacityAnim] = useState(() => new Animated.Value(0));

  useEffect(() => {
    if (visible) {
      Animated.parallel([
        Animated.spring(scaleAnim, { toValue: 1, useNativeDriver: true, tension: 80, friction: 10 }),
        Animated.timing(opacityAnim, { toValue: 1, duration: 200, useNativeDriver: true }),
      ]).start();
    } else {
      Animated.parallel([
        Animated.timing(scaleAnim, { toValue: 0.9, duration: 150, useNativeDriver: true }),
        Animated.timing(opacityAnim, { toValue: 0, duration: 150, useNativeDriver: true }),
      ]).start();
    }
  }, [visible, scaleAnim, opacityAnim]);

  if (!visible) return null;

  const renderContent = () => {
    switch (status) {
      case 'checking':
        return (
          <>
            <ActivityIndicator size="large" color={colors.primary} style={{ marginBottom: scale(16) }} />
            <Text style={styles.title}>Checking for Updates...</Text>
            <Text style={styles.message}>Please wait, connecting to the server.</Text>
          </>
        );
      case 'available':
        return (
          <>
            <View style={[styles.iconBox, { backgroundColor: colors.primary + '15' }]}>
              <Ionicons name="cloud-download-outline" size={scale(40)} color={colors.primary} />
            </View>
            <Text style={styles.title}>New Update Available!</Text>
            <Text style={styles.message}>A new version of Expira is available. We recommend updating for performance improvements and bug fixes.</Text>
            <View style={styles.btnRow}>
              <TouchableOpacity style={[styles.btn, styles.cancelBtn, { borderColor: colors.border }]} onPress={onCancel}>
                <Text style={[styles.btnText, { color: colors.textLight }]}>Later</Text>
              </TouchableOpacity>
              <TouchableOpacity style={[styles.btn, styles.confirmBtn, { backgroundColor: colors.primary }]} onPress={onUpdate}>
                <Text style={[styles.btnText, { color: colors.onPrimary }]}>Download & Install</Text>
              </TouchableOpacity>
            </View>
          </>
        );
      case 'downloading':
        return (
          <>
            <ActivityIndicator size="large" color={colors.warning} style={{ marginBottom: scale(16) }} />
            <Text style={styles.title}>Downloading Update...</Text>
            <Text style={styles.message}>Please do not close the app. You will be prompted to restart once the download is complete.</Text>
          </>
        );
      case 'ready':
        return (
          <>
            <View style={[styles.iconBox, { backgroundColor: colors.success + '15' }]}>
              <Ionicons name="checkmark-circle-outline" size={scale(40)} color={colors.success} />
            </View>
            <Text style={styles.title}>Update Downloaded!</Text>
            <Text style={styles.message}>To start using the new version, you need to restart the app.</Text>
            <View style={styles.btnRow}>
              <TouchableOpacity style={[styles.btn, styles.cancelBtn, { borderColor: colors.border }]} onPress={onCancel}>
                <Text style={[styles.btnText, { color: colors.textLight }]}>Later</Text>
              </TouchableOpacity>
              <TouchableOpacity style={[styles.btn, styles.confirmBtn, { backgroundColor: colors.success }]} onPress={onUpdate}>
                <Text style={[styles.btnText, { color: colors.onSuccess }]}>Restart</Text>
              </TouchableOpacity>
            </View>
          </>
        );
      case 'error':
        return (
          <>
            <View style={[styles.iconBox, { backgroundColor: colors.danger + '15' }]}>
              <Ionicons name="warning-outline" size={scale(40)} color={colors.danger} />
            </View>
            <Text style={styles.title}>Update Error</Text>
            <Text style={styles.message}>{errorMessage || 'An error occurred while checking for updates. Please check your internet connection.'}</Text>
            <View style={styles.btnRow}>
              <TouchableOpacity style={[styles.btn, { backgroundColor: colors.surfaceAlt }]} onPress={onCancel}>
                <Text style={[styles.btnText, { color: colors.text }]}>Close</Text>
              </TouchableOpacity>
            </View>
          </>
        );
      case 'not-available':
        return (
          <>
            <View style={[styles.iconBox, { backgroundColor: colors.success + '15' }]}>
              <Ionicons name="checkmark-done-circle-outline" size={scale(40)} color={colors.success} />
            </View>
            <Text style={styles.title}>Your App is Up to Date!</Text>
            <Text style={styles.message}>You have the latest version available.</Text>
            <View style={styles.btnRow}>
              <TouchableOpacity style={[styles.btn, { backgroundColor: colors.primary }]} onPress={onCancel}>
                <Text style={[styles.btnText, { color: colors.onPrimary }]}>OK</Text>
              </TouchableOpacity>
            </View>
          </>
        );
      default:
        return null;
    }
  };

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onCancel}>
      <View style={styles.overlay}>
        <Animated.View style={[styles.container, { backgroundColor: colors.surface, transform: [{ scale: scaleAnim }], opacity: opacityAnim }]}>
          {renderContent()}
        </Animated.View>
      </View>
    </Modal>
  );
}

const createStyles = (colors, responsive) => {
  const { scale, moderateScale } = responsive;
  return StyleSheet.create({
    overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.6)', justifyContent: 'center', alignItems: 'center', padding: scale(24) },
    container: { width: '100%', maxWidth: scale(340), borderRadius: scale(24), padding: scale(24), alignItems: 'center', ...SHADOWS.heavy },
    iconBox: { width: scale(72), height: scale(72), borderRadius: scale(36), justifyContent: 'center', alignItems: 'center', marginBottom: scale(16) },
    title: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '900', marginBottom: scale(8), textAlign: 'center', color: colors.text },
    message: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), textAlign: 'center', lineHeight: scale(22), marginBottom: scale(24), fontWeight: '500', color: colors.textLight },
    btnRow: { flexDirection: 'row', width: '100%', gap: scale(12) },
    btn: { flex: 1, height: scale(50), borderRadius: scale(25), justifyContent: 'center', alignItems: 'center' },
    cancelBtn: { borderWidth: 1.5, backgroundColor: 'transparent' },
    confirmBtn: { ...SHADOWS.medium },
    btnText: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800' },
  });
};