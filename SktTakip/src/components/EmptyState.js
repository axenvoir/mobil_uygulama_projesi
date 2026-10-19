import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { SHADOWS, TYPOGRAPHY } from '../theme';

export default function EmptyState({ icon = 'cube-outline', title, description, actionText, onAction }) {
  const { colors } = useTheme();
  const responsive = useResponsive();
  const { scale } = responsive;
  const styles = React.useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  return (
    <View style={styles.container}>
      <View style={[styles.iconWrap, { backgroundColor: colors.primary + '15' }]}>
        <Ionicons name={icon} size={scale(64)} color={colors.primary} />
        <View style={[styles.iconDot, { backgroundColor: colors.warning }]} />
      </View>
      <Text style={styles.title} numberOfLines={2} adjustsFontSizeToFit>{title}</Text>
      <Text style={styles.description} numberOfLines={3} adjustsFontSizeToFit>{description}</Text>

      {actionText && onAction && (
        <TouchableOpacity style={styles.actionBtn} onPress={onAction} activeOpacity={0.8}>
          <Ionicons name="add-circle-outline" size={scale(20)} color={colors.onPrimary} />
          <Text style={styles.actionText}>{actionText}</Text>
        </TouchableOpacity>
      )}
    </View>
  );
}

const createStyles = (colors, responsive) => {
  const { scale, moderateScale } = responsive;
  return StyleSheet.create({
    container: { flex: 1, justifyContent: 'center', alignItems: 'center', paddingVertical: scale(40), paddingHorizontal: scale(20) },
    iconWrap: { width: scale(120), height: scale(120), borderRadius: scale(60), justifyContent: 'center', alignItems: 'center', marginBottom: scale(20), position: 'relative' },
    iconDot: { position: 'absolute', top: scale(10), right: scale(10), width: scale(16), height: scale(16), borderRadius: scale(8), borderWidth: 3, borderColor: colors.background },
    title: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '900', color: colors.text, marginBottom: scale(8), textAlign: 'center' },
    description: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, textAlign: 'center', lineHeight: scale(22), marginBottom: scale(24), fontWeight: '500' },
    actionBtn: { flexDirection: 'row', backgroundColor: colors.primary, paddingHorizontal: scale(24), paddingVertical: scale(14), borderRadius: scale(25), alignItems: 'center', ...SHADOWS.medium },
    actionText: { color: colors.onPrimary, fontWeight: '800', fontSize: moderateScale(TYPOGRAPHY.body, 0.3), marginLeft: scale(8) },
  });
};