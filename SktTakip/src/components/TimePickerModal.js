import React, { useState, useEffect, useMemo } from 'react';
import { View, Text, StyleSheet, Modal, TouchableOpacity, ScrollView } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { TYPOGRAPHY } from '../theme';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useTranslation } from '../hooks/useTranslation';

const parseInitialTime = (initialTime) => {
  if (initialTime && initialTime.includes(':')) {
    const [hour, minute] = initialTime.split(':').map(Number);
    if (hour >= 0 && hour < 24 && minute >= 0 && minute < 60) return { hour, minute };
  }
  return { hour: 9, minute: 0 };
};

export default function TimePickerModal({ visible, onClose, onSelect, initialTime }) {
  const { colors } = useTheme();
  const { t } = useTranslation();
  const responsive = useResponsive();
  const { scale } = responsive;
  const insets = useSafeAreaInsets();
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [hour, setHour] = useState(9);
  const [minute, setMinute] = useState(0);

  useEffect(() => {
    if (visible) {
      const t = parseInitialTime(initialTime);
      // Reinitialize the selection when the modal is reopened.
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setHour(t.hour);
      setMinute(t.minute);
    }
  }, [visible, initialTime]);

  const hours = Array.from({ length: 24 }, (_, i) => i);
  const minutes = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55];

  const handleConfirm = () => {
    onSelect(`${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`);
    onClose();
  };

  const presets = [
    { label: t('time_picker_morning'), h: 8, m: 0 },
    { label: t('time_picker_noon'), h: 12, m: 0 },
    { label: t('time_picker_evening'), h: 18, m: 0 },
    { label: t('time_picker_night'), h: 21, m: 0 },
  ];

  return (
    <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}>
      <View style={[styles.overlay, { backgroundColor: 'rgba(0,0,0,0.7)' }]}>
        <View style={[styles.container, { paddingBottom: Math.max(insets.bottom, scale(20)) }]}>
          <ScrollView showsVerticalScrollIndicator={false} nestedScrollEnabled>
          <View style={styles.header}>
            <Text style={styles.title}>{t('time_picker_title')}</Text>
            <TouchableOpacity onPress={onClose} activeOpacity={0.7}>
              <Ionicons name="close" size={scale(24)} color={colors.text} />
            </TouchableOpacity>
          </View>

          <View style={styles.presetsRow}>
            {presets.map((p) => (
              <TouchableOpacity key={p.label} style={[styles.presetBtn, hour === p.h && minute === p.m && styles.presetBtnActive]} onPress={() => { setHour(p.h); setMinute(p.m); }} activeOpacity={0.7}>
                <Text style={[styles.presetText, hour === p.h && minute === p.m && styles.presetTextActive]}>{p.label}</Text>
              </TouchableOpacity>
            ))}
          </View>

          <View style={styles.previewBox}>
            <Text style={styles.previewTime}>{String(hour).padStart(2, '0')}:{String(minute).padStart(2, '0')}</Text>
            <Text style={styles.previewLabel}>{t('time_picker_selected')}</Text>
          </View>

          <Text style={styles.pickerLabel}>{t('time_picker_hour')}</Text>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.pickerScroll}>
            {hours.map((h) => (
              <TouchableOpacity key={h} style={[styles.timeChip, hour === h && styles.timeChipActive]} onPress={() => setHour(h)} activeOpacity={0.7}>
                <Text style={[styles.timeChipText, hour === h && styles.timeChipTextActive]}>{String(h).padStart(2, '0')}</Text>
              </TouchableOpacity>
            ))}
          </ScrollView>

          <Text style={styles.pickerLabel}>{t('time_picker_minute')}</Text>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.pickerScroll}>
            {minutes.map((m) => (
              <TouchableOpacity key={m} style={[styles.timeChip, minute === m && styles.timeChipActive]} onPress={() => setMinute(m)} activeOpacity={0.7}>
                <Text style={[styles.timeChipText, minute === m && styles.timeChipTextActive]}>{String(m).padStart(2, '0')}</Text>
              </TouchableOpacity>
            ))}
          </ScrollView>

          <View style={styles.btnRow}>
            <TouchableOpacity style={styles.cancelBtn} onPress={onClose} activeOpacity={0.7}>
              <Text style={styles.cancelBtnText}>{t('cancel')}</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.confirmBtn} onPress={handleConfirm} activeOpacity={0.8}>
              <Ionicons name="checkmark" size={scale(20)} color={colors.onPrimary} />
              <Text style={styles.confirmBtnText}>{t('confirm')}</Text>
            </TouchableOpacity>
          </View>
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
}

const createStyles = (colors, responsive) => {
  const { scale, moderateScale } = responsive;
  return StyleSheet.create({
    overlay: { flex: 1, justifyContent: 'flex-end' },
    container: { width: '100%', maxWidth: 560, alignSelf: 'center', backgroundColor: colors.surface, borderTopLeftRadius: scale(24), borderTopRightRadius: scale(24), padding: scale(20), maxHeight: '85%' },
    header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: scale(16) },
    title: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text },
    presetsRow: { flexDirection: 'row', flexWrap: 'wrap', marginBottom: scale(16) },
    presetBtn: { paddingHorizontal: scale(10), paddingVertical: scale(6), borderRadius: scale(14), backgroundColor: colors.surfaceAlt, marginRight: scale(6), marginBottom: scale(6) },
    presetBtnActive: { backgroundColor: colors.primary },
    presetText: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), fontWeight: '600', color: colors.textLight },
    presetTextActive: { color: colors.onPrimary },
    previewBox: { alignItems: 'center', backgroundColor: colors.primary + '15', paddingVertical: scale(16), borderRadius: scale(16), marginBottom: scale(16) },
    previewTime: { fontSize: scale(44), fontWeight: 'bold', color: colors.primary },
    previewLabel: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, fontWeight: '600', marginTop: scale(2) },
    pickerLabel: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), fontWeight: 'bold', color: colors.text, marginBottom: scale(8) },
    pickerScroll: { maxHeight: scale(50), marginBottom: scale(12) },
    timeChip: { width: scale(48), height: scale(44), borderRadius: scale(12), backgroundColor: colors.surfaceAlt, justifyContent: 'center', alignItems: 'center', marginRight: scale(8) },
    timeChipActive: { backgroundColor: colors.primary },
    timeChipText: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: 'bold', color: colors.textLight },
    timeChipTextActive: { color: colors.onPrimary },
    btnRow: { flexDirection: 'row', marginTop: scale(8) },
    cancelBtn: { flex: 1, height: scale(50), borderRadius: scale(25), justifyContent: 'center', alignItems: 'center', borderWidth: 1, borderColor: colors.border, marginRight: scale(6) },
    cancelBtnText: { color: colors.text, fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '600' },
    confirmBtn: { flex: 1, height: scale(50), borderRadius: scale(25), justifyContent: 'center', alignItems: 'center', backgroundColor: colors.primary, marginLeft: scale(6), flexDirection: 'row' },
    confirmBtnText: { color: colors.onPrimary, fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: 'bold', marginLeft: scale(6) },
  });
};