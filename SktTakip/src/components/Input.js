import React, { forwardRef } from 'react';
import { View, TextInput, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { FONT_WEIGHTS } from '../theme';

/**
 * Input Component
 * 
 * @param {Object} props - Component props
 * @param {string} [props.label] - Input label
 * @param {string} props.placeholder - Placeholder text
 * @param {string} props.value - Input value
 * @param {function} props.onChangeText - Change handler
 * @param {string} [props.error] - Error message
 * @param {string} [props.helperText] - Helper text
 * @param {React.ReactNode} [props.icon] - Left icon
 * @param {React.ReactNode} [props.rightIcon] - Right icon
 * @param {boolean} [props.secureTextEntry=false] - Secure text entry
 * @param {'default' | 'numeric' | 'email-address' | 'phone-pad'} [props.keyboardType='default'] - Keyboard type
 * @param {'none' | 'sentences' | 'words' | 'characters'} [props.autoCapitalize='sentences'] - Auto capitalize
 * @param {boolean} [props.multiline=false] - Multiline input
 * @param {number} [props.numberOfLines=1] - Number of lines for multiline
 * @param {boolean} [props.disabled=false] - Disable state
 * @param {Object} [props.style] - Additional style
 * @param {Object} [props.inputStyle] - Additional input style
 * @param {Object} [props.labelStyle] - Additional label style
 * @param {Object} [props.props] - Additional TextInput props
 */
const Input = forwardRef(({
  label,
  placeholder,
  value,
  onChangeText,
  error,
  helperText,
  icon,
  rightIcon,
  secureTextEntry = false,
  keyboardType = 'default',
  autoCapitalize = 'sentences',
  multiline = false,
  numberOfLines = 1,
  disabled = false,
  style,
  inputStyle,
  labelStyle,
  ...props
}, ref) => {
  const { colors } = useTheme();
  const responsive = useResponsive();
  const { scale, moderateScale } = responsive;

  // Border color based on state
  const borderColor = error 
    ? colors.danger 
    : disabled 
      ? colors.border + '80' 
      : colors.border;

  // Background color based on state
  const backgroundColor = disabled ? colors.surfaceAlt : colors.surface;

  return (
    <View style={[styles.container, style]}>
      {label && (
        <Text 
          style={[
            styles.label,
            { color: colors.text, ...labelStyle }
          ]}
        >
          {label}
        </Text>
      )}

      <View
        style={[
          styles.inputContainer,
          {
            borderColor,
            backgroundColor,
            borderWidth: error ? 2 : 1,
          },
        ]}
      >
        {icon && (
          <View style={styles.iconContainer}>
            {icon}
          </View>
        )}

        <TextInput
          ref={ref}
          style={[
            styles.input,
            {
              color: disabled ? colors.textLight : colors.text,
              fontSize: moderateScale(16, 0.3),
              minHeight: multiline ? scale(100) : scale(48),
            },
            inputStyle,
          ]}
          placeholder={placeholder}
          placeholderTextColor={colors.textLight}
          value={value}
          onChangeText={onChangeText}
          secureTextEntry={secureTextEntry}
          keyboardType={keyboardType}
          autoCapitalize={autoCapitalize}
          multiline={multiline}
          numberOfLines={multiline ? numberOfLines : undefined}
          editable={!disabled}
          selectTextOnFocus={!disabled}
          maxFontSizeMultiplier={2}
          {...props}
        />

        {rightIcon && (
          <TouchableOpacity 
            style={styles.iconContainer} 
            onPress={() => {}}
            disabled={disabled}
          >
            {rightIcon}
          </TouchableOpacity>
        )}
      </View>

      {error && (
        <Text style={[styles.errorText, { color: colors.danger }]}>
          {error}
        </Text>
      )}

      {helperText && !error && (
        <Text style={[styles.helperText, { color: colors.textLight }]}>
          {helperText}
        </Text>
      )}
    </View>
  );
});

const styles = StyleSheet.create({
  container: {
    marginBottom: 16,
  },
  label: {
    fontSize: 14,
    fontWeight: FONT_WEIGHTS.semiBold,
    marginBottom: 8,
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 12,
    paddingHorizontal: 16,
    overflow: 'hidden',
  },
  iconContainer: {
    paddingRight: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  input: {
    flex: 1,
    paddingVertical: 12,
    fontWeight: FONT_WEIGHTS.regular,
  },
  errorText: {
    fontSize: 12,
    fontWeight: FONT_WEIGHTS.regular,
    marginTop: 4,
  },
  helperText: {
    fontSize: 12,
    fontWeight: FONT_WEIGHTS.regular,
    marginTop: 4,
  },
});

Input.displayName = 'Input';

export default Input;
