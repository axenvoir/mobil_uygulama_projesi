import React from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  ViewStyle,
} from 'react-native';
import {COLORS, SHADOWS, SPACING, TYPOGRAPHY} from '../theme';

interface ButtonProps {
  label: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary' | 'danger';
  disabled?: boolean;
  loading?: boolean;
  style?: ViewStyle;
}

export const Button: React.FC<ButtonProps> = ({
  label,
  onPress,
  variant = 'primary',
  disabled = false,
  loading = false,
  style,
}) => {
  const bg =
    variant === 'primary'
      ? COLORS.primary
      : variant === 'danger'
        ? COLORS.expired
        : COLORS.surface;
  const fg =
    variant === 'primary' || variant === 'danger' ? COLORS.onPrimary : COLORS.text;

  return (
    <Pressable
      onPress={onPress}
      disabled={disabled || loading}
      style={({pressed}) => [
        styles.base,
        {backgroundColor: bg, opacity: disabled ? 0.5 : pressed ? 0.85 : 1},
        variant !== 'primary' && styles.bordered,
        style,
      ]}>
      {loading ? (
        <ActivityIndicator color={fg} />
      ) : (
        <Text style={[styles.label, {color: fg}]}>{label}</Text>
      )}
    </Pressable>
  );
};

const styles = StyleSheet.create({
  base: {
    height: 48,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: SPACING.lg,
    ...SHADOWS.soft,
  },
  bordered: {
    borderWidth: 1,
    borderColor: COLORS.border,
    shadowOpacity: 0,
    elevation: 0,
  },
  label: {
    fontSize: TYPOGRAPHY.title,
    fontWeight: '700',
  },
});
