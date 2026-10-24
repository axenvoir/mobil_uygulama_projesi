import React from 'react';
import { View, StyleSheet } from 'react-native';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { SHADOWS } from '../theme';

/**
 * Card Component
 * 
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Card content
 * @param {'none' | 'soft' | 'medium' | 'heavy'} [props.shadow='soft'] - Shadow level
 * @param {'none' | 'xs' | 'sm' | 'md' | 'lg' | 'xl'} [props.padding='md'] - Internal padding
 * @param {number} [props.borderRadius] - Custom border radius
 * @param {Object} [props.style] - Additional style
 * @param {Object} [props.props] - Additional View props
 */
const Card = ({
  children,
  shadow = 'soft',
  padding = 'md',
  borderRadius,
  style,
  ...props
}) => {
  const { colors } = useTheme();
  const responsive = useResponsive();
  const { scale } = responsive;

  // Padding mapping
  const paddingMap = {
    none: 0,
    xs: scale(8),
    sm: scale(12),
    md: scale(16),
    lg: scale(24),
    xl: scale(32),
  };

  // Shadow mapping
  const shadowMap = {
    none: {},
    soft: SHADOWS.soft,
    medium: SHADOWS.medium,
    heavy: SHADOWS.heavy,
  };

  return (
    <View
      style={[
        styles.card,
        {
          backgroundColor: colors.surface,
          borderRadius: borderRadius || scale(16),
          padding: paddingMap[padding] || paddingMap.md,
          ...shadowMap[shadow],
        },
        style,
      ]}
      accessibilityRole="region"
      {...props}
    >
      {children}
    </View>
  );
};

const styles = StyleSheet.create({
  card: {
    overflow: 'hidden',
  },
});

export default Card;
