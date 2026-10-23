import React, { useState, useEffect, useMemo } from 'react';
import { View, Text, StyleSheet, Modal, TouchableOpacity, ScrollView } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { TYPOGRAPHY } from '../theme';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { isValidDateString } from '../utils/validators';
import { useTranslation } from '../hooks/useTranslation';

const parseInitialDate = (initialDate) => {
  if (isValidDateString(initialDate)) {
    const parts = initialDate.split('-');
    if (parts.length === 3) return new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]));
  }
  return new Date();
};

export default function DatePickerModal({ visible, onClose, onSelect, initialDate }) {
  const { colors } = useTheme();
  const { t, locale } = useTranslation();
  const responsive = useResponsive();
  const { scale } = responsive;
  const insets = useSafeAreaInsets();
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(new Date());

  useEffect(() => {
    if (visible) {
      const d = parseInitialDate(initialDate);
      
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setSelectedDate(d);
      setCurrentDate(d);
    }
  }, [visible, initialDate]);

  const getDaysInMonth = (year, month) => new Date(year, month + 1, 0).getDate();
  const getFirstDayOfMonth = (year, month) => {
    const day = new Date(year, month, 1).getDay();
    return day === 0 ? 6 : day - 1;
  };

  const year = currentDate.getFullYear();
  const month = currentDate.getMonth();
  const daysInMonth = getDaysInMonth(year, month);
  const firstDay = getFirstDayOfMonth(year, month);
  const monthLabel = new Intl.DateTimeFormat(locale === 'en' ? 'en-US' : 'tr-TR', {
    month: 'long',
    year: 'numeric',
  }).format(new Date(year, month, 1));
  const weekdays = Array.from({ length: 7 }, (_, index) => {
    const date = new Date(2024, 0, 1 + index);
    return new Intl.DateTimeFormat(locale === 'en' ? 'en-US' : 'tr-TR', { weekday: 'short' }).format(date);
  });
  const selectedDateLabel = new Intl.DateTimeFormat(locale === 'en' ? 'en-US' : 'tr-TR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(selectedDate);

  const today = new Date();
  const isToday = (day) => today.getDate() === day && today.getMonth() === month && today.getFullYear() === year;
  const isSelected = (day) => selectedDate.getDate() === day && selectedDate.getMonth() === month && selectedDate.getFullYear() === year;

  const goToPrevMonth = () => setCurrentDate(new Date(year, month - 1, 1));
  const goToNextMonth = () => setCurrentDate(new Date(year, month + 1, 1));
  const handleDayPress = (day) => setSelectedDate(new Date(year, month, day));

  const handleConfirm = () => {
    const y = selectedDate.getFullYear();
    const m = String(selectedDate.getMonth() + 1).padStart(2, '0');
    const d = String(selectedDate.getDate()).padStart(2, '0');
    onSelect(`${y}-${m}-${d}`);
    onClose();
  };

  const setQuickDate = (daysToAdd) => {
    const d = new Date();
    d.setDate(d.getDate() + daysToAdd);
    setSelectedDate(d);
    setCurrentDate(d);
  };

  const grid = [];
  for (let i = 0; i < firstDay; i++) grid.push(null);
  for (let i = 1; i <= daysInMonth; i++) grid.push(i);

  return (
    <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}>
      <View style={[styles.overlay, { backgroundColor: 'rgba(0,0,0,0.7)' }]}>
        <View style={[styles.container, { paddingBottom: Math.max(insets.bottom, scale(20)) }]}>
          <ScrollView showsVerticalScrollIndicator={false}>
          <View style={styles.header}>
            <Text style={styles.title}>{t('date_picker_title')}</Text>
            <TouchableOpacity accessibilityRole="button" accessibilityLabel={t('close')} onPress={onClose} activeOpacity={0.7}>
              <Ionicons name="close" size={scale(24)} color={colors.text} />
            </TouchableOpacity>
          </View>

          <View style={styles.quickRow}>
            {[
              { label: t('date_picker_today'), days: 0 },
              { label: t('date_picker_week'), days: 7 },
              { label: t('date_picker_month'), days: 30 },
              { label: t('date_picker_six_months'), days: 180 },
              { label: t('date_picker_year'), days: 365 },
            ].map((q) => (
              <TouchableOpacity key={q.label} style={styles.quickBtn} onPress={() => setQuickDate(q.days)} activeOpacity={0.7}>
                <Text style={styles.quickText}>{q.label}</Text>
              </TouchableOpacity>
            ))}
          </View>

          <View style={styles.monthNav}>
            <TouchableOpacity onPress={goToPrevMonth} style={styles.navBtn} activeOpacity={0.7}>
              <Ionicons name="chevron-back" size={scale(22)} color={colors.primary} />
            </TouchableOpacity>
            <Text style={styles.monthText}>{monthLabel}</Text>
            <TouchableOpacity onPress={goToNextMonth} style={styles.navBtn} activeOpacity={0.7}>
              <Ionicons name="chevron-forward" size={scale(22)} color={colors.primary} />
            </TouchableOpacity>
          </View>

          <View style={styles.weekRow}>
            {weekdays.map((d, i) => (
              <Text key={i} style={styles.weekDay}>{d}</Text>
            ))}
          </View>

          <View style={styles.grid}>
            {grid.map((day, i) => (
              <TouchableOpacity
                key={i}
                accessibilityRole="button"
                accessibilityLabel={day ? new Intl.DateTimeFormat(locale === 'en' ? 'en-US' : 'tr-TR', {
                  dateStyle: 'full',
                }).format(new Date(year, month, day)) : undefined}
                accessibilityState={day ? { selected: isSelected(day) } : undefined}
                style={[styles.dayCell, day && isSelected(day) && styles.dayCellSelected, day && isToday(day) && !isSelected(day) && styles.dayCellToday]}
                disabled={!day}
                onPress={() => handleDayPress(day)}
                activeOpacity={0.7}
              >
                {day && <Text style={[styles.dayText, isSelected(day) && styles.dayTextSelected, isToday(day) && !isSelected(day) && styles.dayTextToday]}>{day}</Text>}
              </TouchableOpacity>
            ))}
          </View>

          <View style={styles.selectedInfo}>
            <Ionicons name="calendar" size={scale(18)} color={colors.primary} />
            <Text style={styles.selectedText}>
              {selectedDateLabel}
            </Text>
          </View>

          <View style={styles.btnRow}>
            <TouchableOpacity style={styles.cancelBtn} onPress={onClose} activeOpacity={0.7}>
              <Text style={styles.cancelBtnText}>{t('cancel')}</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.confirmBtn} onPress={handleConfirm} activeOpacity={0.8}>
              <Text style={styles.confirmBtnText}>{t('date_picker_confirm')}</Text>
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
    container: { width: '100%', maxWidth: 560, alignSelf: 'center', backgroundColor: colors.surface, borderTopLeftRadius: scale(24), borderTopRightRadius: scale(24), padding: scale(20), maxHeight: '90%' },
    header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: scale(16) },
    title: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text },
    quickRow: { flexDirection: 'row', marginBottom: scale(16), flexWrap: 'wrap' },
    quickBtn: { paddingHorizontal: scale(12), paddingVertical: scale(6), backgroundColor: colors.primary + '15', borderRadius: scale(16), marginRight: scale(6), marginBottom: scale(6) },
    quickText: { color: colors.primary, fontSize: moderateScale(TYPOGRAPHY.small, 0.3), fontWeight: '600' },
    monthNav: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: scale(12) },
    navBtn: { padding: scale(8), backgroundColor: colors.primary + '15', borderRadius: scale(50) },
    monthText: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text },
    weekRow: { flexDirection: 'row', marginBottom: scale(8) },
    weekDay: { flex: 1, textAlign: 'center', fontSize: moderateScale(TYPOGRAPHY.small, 0.3), fontWeight: 'bold', color: colors.textLight },
    grid: { flexDirection: 'row', flexWrap: 'wrap', marginBottom: scale(16) },
    dayCell: { width: '14.28%', height: scale(42), justifyContent: 'center', alignItems: 'center' },
    dayCellSelected: { backgroundColor: colors.primary, borderRadius: scale(21) },
    dayCellToday: { borderWidth: 1.5, borderColor: colors.primary, borderRadius: scale(21) },
    dayText: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.text },
    dayTextSelected: { color: colors.onPrimary, fontWeight: 'bold' },
    dayTextToday: { color: colors.primary, fontWeight: 'bold' },
    selectedInfo: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', backgroundColor: colors.primary + '15', padding: scale(12), borderRadius: scale(12), marginBottom: scale(16) },
    selectedText: { marginLeft: scale(8), fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), fontWeight: 'bold', color: colors.text },
    btnRow: { flexDirection: 'row' },
    cancelBtn: { flex: 1, height: scale(50), borderRadius: scale(25), justifyContent: 'center', alignItems: 'center', borderWidth: 1, borderColor: colors.border, marginRight: scale(6) },
    cancelBtnText: { color: colors.text, fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '600' },
    confirmBtn: { flex: 1, height: scale(50), borderRadius: scale(25), justifyContent: 'center', alignItems: 'center', backgroundColor: colors.primary, marginLeft: scale(6) },
    confirmBtnText: { color: colors.onPrimary, fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: 'bold' },
  });
};