import React from 'react';
import {StyleSheet, Text} from 'react-native';
import {COLORS, SPACING, TYPOGRAPHY} from '../theme';

interface EmptyStateProps {
  icon: string;
  title: string;
  hint?: string;
}

export const EmptyState: React.FC<EmptyStateProps> = ({icon, title, hint}) => (
  <>
    <Text style={styles.icon}>{icon}</Text>
    <Text style={styles.title}>{title}</Text>
    {hint ? <Text style={styles.hint}>{hint}</Text> : null}
  </>
);

const styles = StyleSheet.create({
  icon: {fontSize: 56, marginBottom: SPACING.md},
  title: {fontSize: TYPOGRAPHY.h3, fontWeight: '700', color: COLORS.text, textAlign: 'center'},
  hint: {
    fontSize: TYPOGRAPHY.body,
    color: COLORS.textLight,
    textAlign: 'center',
    marginTop: SPACING.xs,
  },
});
