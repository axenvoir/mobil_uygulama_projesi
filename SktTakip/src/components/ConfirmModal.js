import React, { useEffect, useState, useMemo } from 'react';
import { View, Text, StyleSheet, Modal, TouchableOpacity, Animated } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { SHADOWS, TYPOGRAPHY } from '../theme';

export default function ConfirmModal({ visible, title, message, type = 'warning', confirmText = 'Yes't', cancelText = 'Cancel', onConfirm, onCancel }) {
  const { colors } = useTheme();
  const responsive = useResponsive();
  const { scale } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

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

  const getIconConfig = () => {
    switch (type) {
      case 'danger': return { icon: 'alert-circle', color: colors.danger, textColor: colors.onDanger };
      case 'success': return { icon: 'checkmark-circle', color: colors.success, textColor: colors.onSuccess };
      case 'info': return { icon: 'information-circle', color: colors.info, textColor: colors.onInfo };
      default: return { icon: 'warning', color: colors.warning, textColor: colors.onWarning };
    }
  };

  const config = getIconConfig();

  if (!visible) return null;

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onCancel}>
      <View style={styles.overlay}>
        <Animated.View style={[styles.container, { backgroundColor: colors.surface, transform: [{ scale: scaleAnim }], opacity: opacityAnim }]}>

          <View style={[styles.iconBox, { backgroundColor: config.color + '15' }]}>
            <Ionicons name={config.icon} size={scale(36)} color={config.color} />
          </View>

          <Text style={[styles.title, { color: colors.text }]}>{title}</Text>
          <Text style={[styles.message, { color: colors.textLight }]}>{message}</Text>

          <View style={styles.btnRow}>
            <TouchableOpacity style={[styles.cancelBtn, { borderColor: colors.border }]} onPress={onCancel} activeOpacity={0.7}>
              <Text style={[styles.cancelText, { color: colors.textLight }]}>{cancelText}</Text>
            </TouchableOpacity>

            <TouchableOpacity style={[styles.confirmBtn, { backgroundColor: config.color }]} onPress={onConfirm} activeOpacity={0.8}>
              <Text style={[styles.confirmText, { color: config.textColor }]}>{confirmText}</Text>
            </TouchableOpacity>
          </View>

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
    title: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '900', marginBottom: scale(8), textAlign: 'center' },
    message: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), textAlign: 'center', lineHeight: scale(24), marginBottom: scale(24), fontWeight: '500' },
    btnRow: { flexDirection: 'row', width: '100%', gap: scale(12) },
    cancelBtn: { flex: 1, height: scale(50), borderRadius: scale(25), justifyContent: 'center', alignItems: 'center', borderWidth: 1.5 },
    cancelText: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '700' },
    confirmBtn: { flex: 1, height: scale(50), borderRadius: scale(25), justifyContent: 'center', alignItems: 'center' },
    confirmText: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800' },
  });
};