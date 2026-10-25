import React from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import {COLORS, SHADOWS, SPACING, TYPOGRAPHY} from '../theme';

export interface ToastData {
  id: number;
  message: string;
  tone?: 'success' | 'error';
}

interface ToastHostProps {
  toast: ToastData | null;
}

export const ToastHost: React.FC<ToastHostProps> = ({toast}) => {
  if (!toast) {
    return null;
  }
  return (
    <View style={styles.host} pointerEvents="none">
      <View
        style={[
          styles.toast,
          toast.tone === 'error' ? styles.toastError : styles.toastSuccess,
          SHADOWS.medium,
        ]}>
        <Text style={styles.message}>{toast.message}</Text>
      </View>
    </View>
  );
};

export const useToast = (): [ToastData | null, (message: string, tone?: 'success' | 'error') => void] => {
  const [toast, setToast] = React.useState<ToastData | null>(null);
  const show = React.useCallback((message: string, tone: 'success' | 'error' = 'success') => {
    setToast({id: Date.now(), message, tone});
  }, []);
  React.useEffect(() => {
    if (!toast) {
      return;
    }
    const timer = setTimeout(() => setToast(null), 2600);
    return () => clearTimeout(timer);
  }, [toast]);
  return [toast, show];
};

export const ToastButton: React.FC<{label: string; onPress: () => void}> = ({label, onPress}) => (
  <Pressable onPress={onPress} style={styles.dismiss}>
    <Text style={styles.dismissText}>{label}</Text>
  </Pressable>
);

const styles = StyleSheet.create({
  host: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 100,
    alignItems: 'center',
  },
  toast: {
    paddingHorizontal: SPACING.lg,
    paddingVertical: SPACING.sm + 2,
    borderRadius: 24,
    maxWidth: '88%',
  },
  toastSuccess: {backgroundColor: COLORS.primaryDark},
  toastError: {backgroundColor: COLORS.expired},
  message: {color: COLORS.white, fontSize: TYPOGRAPHY.caption, fontWeight: '600'},
  dismiss: {marginTop: SPACING.xs},
  dismissText: {color: COLORS.white, fontSize: TYPOGRAPHY.small},
});
