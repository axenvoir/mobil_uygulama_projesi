import React from 'react';
import {StyleSheet, Text, View, ViewStyle} from 'react-native';
import {COLORS, SHADOWS, SPACING, TYPOGRAPHY} from '../theme';

interface CardProps {
  children: React.ReactNode;
  style?: ViewStyle;
}

export const Card: React.FC<CardProps> = ({children, style}) => (
  <View style={[styles.card, style]}>{children}</View>
);

interface CardRowProps {
  label: string;
  value: string;
}

export const CardRow: React.FC<CardRowProps> = ({label, value}) => (
  <View style={styles.row}>
    <Text style={styles.rowLabel}>{label}</Text>
    <Text style={styles.rowValue}>{value}</Text>
  </View>
);

const styles = StyleSheet.create({
  card: {
    backgroundColor: COLORS.surface,
    borderRadius: 16,
    padding: SPACING.md,
    ...SHADOWS.soft,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: SPACING.sm,
  },
  rowLabel: {fontSize: TYPOGRAPHY.body, color: COLORS.textLight},
  rowValue: {fontSize: TYPOGRAPHY.body, color: COLORS.text, fontWeight: '600'},
});
