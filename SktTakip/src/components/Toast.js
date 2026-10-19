import React, { useEffect, useState, useMemo } from 'react';
import { View, Text, StyleSheet, Animated, TouchableOpacity, Platform } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { SHADOWS } from '../theme';

export default function Toast({ toast, onHide }) {
  const { colors } = useTheme();
  const responsive = useResponsive();
  const { scale, icon } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [translateY] = useState(() => new Animated.Value(-150));
  const [opacity] = useState(() => new Animated.Value(0));

  useEffect(() => {
    if (toast) {
      Animated.parallel([
        Animated.spring(translateY, { toValue: 0, useNativeDriver: true, tension: 70, friction: 9 }),
        Animated.timing(opacity, { toValue: 1, duration: 250, useNativeDriver: true }),
      ]).start();
    } else {
      Animated.parallel([
        Animated.timing(translateY, { toValue: -150, duration: 250, useNativeDriver: true }),
        Animated.timing(opacity, { toValue: 0, duration: 200, useNativeDriver: true }),
      ]).start();
    }
  }, [toast, translateY, opacity]);

  if (!toast) return null;

  const CONFIGS = {
    success: { icon: 'checkmark-circle', color: colors.success, bg: colors.surface, border: colors.success },
    error: { icon: 'close-circle', color: colors.danger, bg: colors.surface, border: colors.danger },
    info: { icon: 'information-circle', color: colors.primary, bg: colors.surface, border: colors.primary },
    warning: { icon: 'warning', color: colors.warning, bg: colors.surface, border: colors.warning },
  };

  const config = CONFIGS[toast.type] || CONFIGS.success;
  const hasAction = toast.action && toast.action.label && toast.action.onPress;

  const handleAction = () => {
    if (toast.action?.onPress) toast.action.onPress();
    if (onHide) onHide();
  };

  return (
    <Animated.View
      style={[
        styles.container,
        {
          backgroundColor: config.bg,
          borderColor: config.border,
          transform: [{ translateY }],
          opacity,
          top: Platform.OS === 'ios' ? scale(50) : scale(40)
        }
      ]}
    >
      <View style={[styles.iconBox, { backgroundColor: config.color + '20' }]}>
        <Ionicons name={config.icon} size={icon(22)} color={config.color} />
      </View>
      <Text style={[styles.text, { color: colors.text }]} numberOfLines={2}>{toast.message}</Text>

      {hasAction && (
        <TouchableOpacity style={[styles.actionBtn, { backgroundColor: config.color }]} onPress={handleAction}>
          <Text style={styles.actionText}>{toast.action.label}</Text>
        </TouchableOpacity>
      )}
    </Animated.View>
  );
}

const createStyles = (colors, responsive) => {
  const { scale, fontSize } = responsive;
  return StyleSheet.create({
    container: {
      position: 'absolute',
      left: scale(16),
      right: scale(16),
      flexDirection: 'row',
      alignItems: 'center',
      padding: scale(14),
      borderRadius: scale(16),
      zIndex: 9999,
      borderWidth: 1.5,
      ...SHADOWS.heavy,
    },
    iconBox: { width: scale(36), height: scale(36), borderRadius: scale(18), justifyContent: 'center', alignItems: 'center', marginRight: scale(12) },
    text: { flex: 1, fontSize: fontSize(14), fontWeight: '600', lineHeight: scale(20) },
    actionBtn: { paddingHorizontal: scale(16), paddingVertical: scale(8), borderRadius: scale(20), marginLeft: scale(8) },
    actionText: { color: '#FFFFFF', fontSize: fontSize(12), fontWeight: 'bold' },
  });
};