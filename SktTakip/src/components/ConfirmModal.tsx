import React from 'react';
import {Modal, Pressable, StyleSheet, Text} from 'react-native';
import {COLORS, SHADOWS, SPACING, TYPOGRAPHY} from '../theme';
import {Button} from './Button';

interface ConfirmModalProps {
  visible: boolean;
  title: string;
  message: string;
  confirmLabel: string;
  cancelLabel: string;
  destructive?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export const ConfirmModal: React.FC<ConfirmModalProps> = ({
  visible,
  title,
  message,
  confirmLabel,
  cancelLabel,
  destructive = false,
  onConfirm,
  onCancel,
}) => (
  <Modal visible={visible} transparent animationType="fade" onRequestClose={onCancel}>
    <Pressable style={styles.backdrop} onPress={onCancel}>
      <Pressable style={styles.container} onPress={e => e.stopPropagation()}>
        <Text style={styles.title}>{title}</Text>
        <Text style={styles.message}>{message}</Text>
        <Button label={confirmLabel} onPress={onConfirm} variant={destructive ? 'danger' : 'primary'} />
        <Button label={cancelLabel} onPress={onCancel} variant="secondary" style={{marginTop: SPACING.sm}} />
      </Pressable>
    </Pressable>
  </Modal>
);

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    alignItems: 'center',
    justifyContent: 'center',
    padding: SPACING.lg,
  },
  container: {
    width: '100%',
    maxWidth: 340,
    backgroundColor: COLORS.surface,
    borderRadius: 20,
    padding: SPACING.lg,
    ...SHADOWS.heavy,
  },
  title: {
    fontSize: TYPOGRAPHY.h3,
    fontWeight: '700',
    color: COLORS.text,
    textAlign: 'center',
    marginBottom: SPACING.sm,
  },
  message: {
    fontSize: TYPOGRAPHY.body,
    color: COLORS.textLight,
    textAlign: 'center',
    marginBottom: SPACING.lg,
    lineHeight: 22,
  },
});
