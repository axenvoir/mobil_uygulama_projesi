import React, { useState, useContext, useMemo, useEffect } from 'react';
import { View, Text, StyleSheet, TextInput, TouchableOpacity, ScrollView, KeyboardAvoidingView, Platform, Keyboard, TouchableWithoutFeedback } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { AppContext } from '../context/AppContext';
import { notifySuccess, notifyError } from '../utils/haptics';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { useTranslation } from '../hooks/useTranslation';
import { SHADOWS, TYPOGRAPHY, getContrastingTextColor } from '../theme';
import ConfirmModal from '../components/ConfirmModal';

export default function AddNoteScreen({ route, navigation }) {
  const { addNote, updateNote, deleteNote, showToast, settings } = useContext(AppContext);
  const { colors } = useTheme();
  const { t, locale } = useTranslation();
  const responsive = useResponsive();
  const { scale } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const editingNote = route.params?.note;
  const isEditing = !!editingNote;

  const [title, setTitle] = useState(editingNote?.title || '');
  const [desc, setDesc] = useState(editingNote?.desc || '');
  const [color, setColor] = useState(editingNote?.color || '#14B8A6');
  const [modalVisible, setModalVisible] = useState(false);

  /* eslint-disable react-hooks/set-state-in-effect */
  useEffect(() => {
    setTitle(editingNote?.title || '');
    setDesc(editingNote?.desc || '');
    setColor(editingNote?.color || '#14B8A6');
  }, [editingNote]);
  /* eslint-enable react-hooks/set-state-in-effect */

  const colorOptions = ['#14B8A6', '#F59E0B', '#EF4444', '#3B82F6', '#8B5CF6', '#EC4899'];

  const handleSave = async () => {
    if (!title.trim()) { if (settings.hapticsEnabled) await notifyError(); showToast(t('note_title_required'), 'error'); return; }
    if (!desc.trim()) { if (settings.hapticsEnabled) await notifyError(); showToast(t('note_content_required'), 'error'); return; }

    const dateStr = new Intl.DateTimeFormat(locale === 'en' ? 'en-US' : 'tr-TR', {
      day: 'numeric',
      month: 'long',
    }).format(new Date());

    if (isEditing) {
      updateNote(editingNote.id, { title: title.trim(), desc: desc.trim(), color });
      showToast(t('note_updated'), 'success');
    } else {
      addNote({ title: title.trim(), desc: desc.trim(), color, date: dateStr });
      if (settings.hapticsEnabled) await notifySuccess();
      showToast(t('note_added'), 'success');
    }
    navigation.goBack();
  };

  const confirmDelete = () => {
    setModalVisible(false);
    deleteNote(editingNote.id);
    showToast(t('note_deleted'), 'info');
    navigation.goBack();
  };

  return (
    <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={styles.container}>
      <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
        <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
          <View style={styles.innerWrap}>
            <View style={styles.header}>
              <TouchableOpacity onPress={() => navigation.goBack()} activeOpacity={0.7}>
                <Ionicons name="arrow-back" size={scale(24)} color={colors.text} />
              </TouchableOpacity>
              <Text style={styles.headerTitle}>{t(isEditing ? 'note_edit_title' : 'note_new_title')}</Text>
              {isEditing ? (
                <TouchableOpacity onPress={() => setModalVisible(true)} activeOpacity={0.7}>
                  <Ionicons name="trash-outline" size={scale(24)} color={colors.danger} />
                </TouchableOpacity>
              ) : (
                <View style={{ width: scale(24) }} />
              )}
            </View>

            <Text style={styles.label}>{t('note_title_label')}</Text>
            <View style={styles.inputContainer}>
              <TextInput style={styles.input} placeholder={t('note_title_placeholder')} placeholderTextColor={colors.textLight} value={title} onChangeText={setTitle} maxLength={50} />
              <Ionicons name="document-text-outline" size={scale(20)} color={colors.textLight} />
            </View>

            <Text style={styles.label}>{t('note_content_label')}</Text>
            <View style={[styles.inputContainer, { height: scale(180), alignItems: 'flex-start', paddingTop: scale(14) }]}>
              <TextInput style={[styles.input, { height: '100%' }]} placeholder={t('note_content_placeholder')} placeholderTextColor={colors.textLight} value={desc} onChangeText={setDesc} multiline textAlignVertical="top" />
            </View>

            <Text style={styles.label}>{t('note_color_label')}</Text>
            <View style={styles.colorRow}>
              {colorOptions.map((c) => (
                <TouchableOpacity key={c} style={[styles.colorCircle, { backgroundColor: c, borderWidth: color === c ? 3 : 0, borderColor: colors.text }]} onPress={() => setColor(c)} activeOpacity={0.8}>
                  {color === c && <Ionicons name="checkmark" size={scale(22)} color={getContrastingTextColor(c)} />}
                </TouchableOpacity>
              ))}
            </View>

            <TouchableOpacity style={styles.saveButton} onPress={handleSave} activeOpacity={0.8}>
              <Ionicons name="checkmark-circle-outline" size={scale(24)} color={colors.onPrimary} />
              <Text style={styles.saveButtonText}>{t(isEditing ? 'update' : 'save')}</Text>
            </TouchableOpacity>
            <View style={{ height: scale(40) }} />
          </View>
        </ScrollView>
      </TouchableWithoutFeedback>

      <ConfirmModal
        visible={modalVisible}
        type="danger"
        title={t('note_delete_title')}
        message={t('note_delete_message', { title })}
        confirmText={t('delete')}
        onConfirm={confirmDelete}
        onCancel={() => setModalVisible(false)}
      />
    </KeyboardAvoidingView>
  );
}

const createStyles = (colors, responsive) => {
  const { horizontalPadding, contentMaxWidth, scale, moderateScale } = responsive;
  return StyleSheet.create({
    container: { flex: 1, backgroundColor: colors.background },
    content: { paddingBottom: scale(100), alignItems: 'center' },
    innerWrap: { width: '100%', maxWidth: contentMaxWidth, paddingHorizontal: horizontalPadding, paddingTop: scale(20) },
    header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: scale(24) },
    headerTitle: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text },
    label: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), fontWeight: '800', color: colors.text, marginBottom: scale(8) },
    inputContainer: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, borderRadius: scale(16), paddingHorizontal: scale(16), minHeight: scale(54), marginBottom: scale(16), borderWidth: 1, borderColor: colors.border },
    input: { flex: 1, color: colors.text, fontSize: moderateScale(TYPOGRAPHY.body, 0.3) },
    colorRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: scale(24), paddingHorizontal: scale(4) },
    colorCircle: { width: scale(48), height: scale(48), borderRadius: scale(24), justifyContent: 'center', alignItems: 'center' },
    saveButton: { flexDirection: 'row', backgroundColor: colors.primary, height: scale(60), borderRadius: scale(30), justifyContent: 'center', alignItems: 'center', marginTop: scale(8), ...SHADOWS.medium },
    saveButtonText: { color: colors.onPrimary, fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', marginLeft: scale(8) },
  });
};