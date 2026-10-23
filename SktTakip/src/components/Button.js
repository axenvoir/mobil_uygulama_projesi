import React from 'react';
import { TouchableOpacity, Text, StyleSheet, ActivityIndicator, View } from 'react-native';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { FONT_WEIGHTS } from '../theme';

/**
 * Button Component
 * 
 * @param {Object} props - Component props
 * @param {string} props.title - Button text
 * @param {function} props.onPress - Press handler
 * @param {'primary' | 'secondary' | 'danger' | 'ghost' | 'text'} [props.type='primary'] - Button type
 * @param {'small' | 'medium' | 'large'} [props.size='medium'] - Button size
 * @param {boolean} [props.disabled=false] - Disable state
 * @param {boolean} [props.loading=false] - Loading state
 * @param {React.ReactNode} [props.icon] - Left icon
 * @param {React.ReactNode} [props.rightIcon] - Right icon
 * @param {Object} [props.style] - Additional style
 * @param {Object} [props.textStyle] - Additional text style
 * @param {Object} [props.props] - Additional TouchableOpacity props
 */
const Button = ({
  title,
  onPress,
  type = 'primary',
  size = 'medium',
  disabled = false,
  loading = false,
  icon,
  rightIcon,
  onRightIconPress,
  style,
  textStyle,
  ...props
}) => {
  const { colors } = useTheme();
  const responsive = useResponsive();
  const { scale, moderateScale } = responsive;

  // Button type styles
  const getTypeStyles = () => {
    const base = {
      primary: {
        backgroundColor: colors.primary,
        borderColor: colors.primary,
        borderWidth: 0,
      },
      secondary: {
        backgroundColor: colors.surface,
        borderColor: colors.border,
        borderWidth: 1,
      },
      danger: {
        backgroundColor: colors.danger,
        borderColor: colors.danger,
        borderWidth: 0,
      },
      ghost: {
        backgroundColor: 'transparent',
        borderColor: 'transparent',
        borderWidth: 0,
      },
      text: {
        backgroundColor: 'transparent',
        borderColor: 'transparent',
        borderWidth: 0,
      },
    };

    return base[type] || base.primary;
  };

  // Button size styles
  const getSizeStyles = () => {
    const base = {
      small: {
        paddingVertical: scale(8),
        paddingHorizontal: scale(16),
        minHeight: scale(40),
      },
      medium: {
        paddingVertical: scale(12),
        paddingHorizontal: scale(24),
        minHeight: scale(48),
      },
      large: {
        paddingVertical: scale(16),
        paddingHorizontal: scale(32),
        minHeight: scale(56),
      },
    };

    return base[size] || base.medium;
  };

  // Text color based on type
  const getTextColor = () => {
    if (type === 'primary') return colors.onPrimary;
    if (type === 'danger') return colors.onDanger;
    if (type === 'ghost' || type === 'text') return colors.primary;
    return colors.text;
  };

  // Disabled styles
  const disabledStyle = {
    opacity: 0.5,
  };

  const buttonStyles = [
    styles.button,
    getTypeStyles(),
    getSizeStyles(),
    disabled && disabledStyle,
    style,
  ];

  const textStyles = [
    styles.text,
    {
      color: getTextColor(),
      fontSize: moderateScale(type === 'small' ? 13 : type === 'large' ? 17 : 15, 0.3),
      fontWeight: FONT_WEIGHTS.semiBold,
    },
    textStyle,
  ];

  return (
    <TouchableOpacity
      style={buttonStyles}
      onPress={disabled || loading ? null : onPress}
      activeOpacity={0.7}
      accessibilityRole="button"
      accessibilityState={{ disabled }}
      accessibilityLabel={title}
      {...props}
    >
      {loading ? (
        <ActivityIndicator 
          color={type === 'primary' || type === 'danger' ? colors.onPrimary : colors.primary} 
          size="small" 
        />
      ) : (
        <View style={styles.content}>
          {icon && <View style={styles.iconLeft}>{icon}</View>}
          <Text style={textStyles}>{title}</Text>
          {rightIcon && (
            <TouchableOpacity
              onPress={onRightIconPress}
              hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
              style={styles.iconRight}
            >
              {rightIcon}
            </TouchableOpacity>
          )}
        </View>
      )}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  button: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 12,
    overflow: 'hidden',
  },
  content: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  text: {
    textAlign: 'center',
    letterSpacing: 0.2,
  },
  iconLeft: {
    marginRight: 8,
  },
  iconRight: {
    marginLeft: 8,
  },
});

export default Button;
