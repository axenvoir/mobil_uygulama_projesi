import React from 'react';
import {StyleSheet, Text, TextInput, View, ViewStyle} from 'react-native';
import {COLORS, SPACING, TYPOGRAPHY} from '../theme';

interface InputProps {
  label?: string;
  value: string;
  onChangeText: (text: string) => void;
  placeholder?: string;
  multiline?: boolean;
  error?: string | null;
  style?: ViewStyle;
}

export const Input: React.FC<InputProps> = ({
  label,
  value,
  onChangeText,
  placeholder,
  multiline = false,
  error,
  style,
}) => (
  <View style={[styles.wrap, style]}>
    {label ? <Text style={styles.label}>{label}</Text> : null}
    <TextInput
      style={[styles.input, multiline && styles.multiline, error && styles.inputError]}
      value={value}
      onChangeText={onChangeText}
      placeholder={placeholder}
      placeholderTextColor={COLORS.textLight}
      multiline={multiline}
    />
    {error ? <Text style={styles.error}>{error}</Text> : null}
  </View>
);

const styles = StyleSheet.create({
  wrap: {marginBottom: SPACING.md},
  label: {
    fontSize: TYPOGRAPHY.caption,
    fontWeight: '600',
    color: COLORS.text,
    marginBottom: SPACING.xs,
  },
  input: {
    backgroundColor: COLORS.surface,
    borderWidth: 1,
    borderColor: COLORS.border,
    borderRadius: 12,
    paddingHorizontal: SPACING.md,
    height: 48,
    fontSize: TYPOGRAPHY.body,
    color: COLORS.text,
  },
  multiline: {height: 120, textAlignVertical: 'top', paddingTop: SPACING.sm},
  inputError: {borderColor: COLORS.expired},
  error: {color: COLORS.expired, fontSize: TYPOGRAPHY.small, marginTop: SPACING.xs},
});
