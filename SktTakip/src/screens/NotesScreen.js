import React, { useContext, useState, useMemo } from 'react';
import { View, Text, StyleSheet, TextInput, FlatList, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { AppContext } from '../context/AppContext';
import { SHADOWS, TYPOGRAPHY } from '../theme';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { useTranslation } from '../hooks/useTranslation';
import ConfirmModal from '../components/ConfirmModal';
import EmptyState from '../components/EmptyState'; // YENİ EKLENDİ
import { notifySuccess } from '../utils/haptics';

export default function NotesScreen({ navigation }) {
  const { notes, deleteNote, togglePin, showToast, settings } = useContext(AppContext);
  const { colors } = useTheme();
  const { t } = useTranslation();
  const responsive = useResponsive();
  const { isTablet, scale, itemColumns, itemGap } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [search, setSearch] = useState('');
  const [modalVisible, setModalVisible] = useState(false);
  const [selectedNote, setSelectedNote] = useState(null);

  const filteredNotes = notes.filter(n =>
    n.title.toLowerCase().includes(search.toLowerCase()) ||
    n.desc.toLowerCase().includes(search.toLowerCase())
  );

  const pinnedNotes = filteredNotes.filter(n => n.pinned);
  const otherNotes = filteredNotes.filter(n => !n.pinned);

  const handleDelete = (note) => {
    setSelectedNote(note);
    setModalVisible(true);
  };

  const confirmDelete = async () => {
    setModalVisible(false);
    if (!selectedNote) return;
    deleteNote(selectedNote.id);
    showToast(t('note_deleted'), 'info');
    if (settings.hapticsEnabled) await notifySuccess();
    setSelectedNote(null);
  };

  const renderNote = (note) => (
    <TouchableOpacity
      key={note.id}
      style={[styles.noteCard, { borderLeftColor: note.color }, isTablet && styles.noteCardTablet]}
      onPress={() => navigation.navigate('AddNote', { note })}
      activeOpacity={0.7}
    >
      <View style={styles.noteHeader}>
        <Ionicons name={note.pinned ? 'pin' : 'document-text-outline'} size={scale(18)} color={note.pinned ? colors.primary : note.color} />
        <Text numberOfLines={1} adjustsFontSizeToFit style={styles.noteTitle}>{note.title}</Text>
        <TouchableOpacity
          onPress={() => {
            togglePin(note.id);
            showToast(note.pinned ? 'Sabitleme kaldırıldı.' : 'Not sabitlendi.', 'info');
          }}
          accessibilityRole="button"
          accessibilityLabel={t(note.pinned ? 'notes_unpin' : 'notes_pin')}
          hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
        >
          <Ionicons name={note.pinned ? 'pin' : 'pin-outline'} size={scale(20)} color={colors.primary} />
        </TouchableOpacity>
        <TouchableOpacity onPress={() => handleDelete(note)} hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}>
          <Ionicons name="trash-outline" size={scale(20)} color={colors.danger} />
        </TouchableOpacity>
      </View>
      <Text style={styles.noteDesc} numberOfLines={4}>{note.desc}</Text>
      <View style={styles.dateBadge}>
        <Ionicons name="calendar-outline" size={scale(14)} color={note.color} />
        <Text style={[styles.dateText, { color: note.color }]}>{note.date}</Text>
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <View style={styles.innerWrap}>
        <View style={styles.header}>
          <Text style={styles.headerTitle}>{t('notes_title')}</Text>
          <TouchableOpacity style={styles.addBtn} onPress={() => navigation.navigate('AddNote')} activeOpacity={0.8}>
            <Ionicons name="add" size={scale(22)} color={colors.onPrimary} />
          </TouchableOpacity>
        </View>

        <View style={styles.searchContainer}>
          <Ionicons name="search" size={scale(20)} color={colors.textLight} />
          <TextInput style={styles.searchInput} placeholder={t('notes_search_placeholder')} placeholderTextColor={colors.textLight} value={search} onChangeText={setSearch} />
        </View>

        {notes.length === 0 ? (
          <EmptyState
            icon="document-text-outline"
            title={t('notes_empty_title')}
            description={t('notes_empty_description')}
            actionText={t('notes_empty_action')}
            onAction={() => navigation.navigate('AddNote')}
          />
        ) : (
          <FlatList
            data={[...pinnedNotes, ...otherNotes]}
            keyExtractor={item => item.id}
            key={`columns-${itemColumns}`}
            numColumns={itemColumns}
            columnWrapperStyle={itemColumns > 1 ? { gap: itemGap } : undefined}
            contentContainerStyle={{ paddingBottom: scale(100) }}
            ListEmptyComponent={<EmptyState icon="search-outline" title={t('empty_search')} />}
            ListHeaderComponent={
              pinnedNotes.length > 0 ? (
                <View style={styles.pinnedHeader}>
                  <Ionicons name="pin" size={scale(16)} color={colors.primary} />
                  <Text style={styles.pinnedTitle}>{t('notes_pinned')}</Text>
                </View>
              ) : null
            }
            renderItem={({ item, index }) => {
              const isFirstOther = !item.pinned && (index === 0 || pinnedNotes[index - 1]);
              return (
                <>
                  {isFirstOther && pinnedNotes.length > 0 && !isTablet && <Text style={styles.sectionTitle}>{t('notes_all')}</Text>}
                  {renderNote(item)}
                </>
              );
            }}
          />
        )}
      </View>

      <ConfirmModal
        visible={modalVisible}
        type="danger"
        title={t('note_delete_title')}
        message={t('note_delete_message', { title: selectedNote?.title })}
        confirmText={t('delete')}
        onConfirm={confirmDelete}
        onCancel={() => { setModalVisible(false); setSelectedNote(null); }}
      />
    </View>
  );
}

const createStyles = (colors, responsive) => {
  const { horizontalPadding, contentMaxWidth, scale, moderateScale, itemColumns } = responsive;
  return StyleSheet.create({
    container: { flex: 1, backgroundColor: colors.background, alignItems: 'center' },
    innerWrap: { flex: 1, width: '100%', maxWidth: contentMaxWidth, paddingHorizontal: horizontalPadding, paddingTop: scale(20) },
    header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: scale(20) },
    headerTitle: { fontSize: moderateScale(TYPOGRAPHY.h2, 0.3), fontWeight: '900', color: colors.text, letterSpacing: -0.5 },
    addBtn: { width: scale(48), height: scale(48), borderRadius: scale(24), backgroundColor: colors.primary, justifyContent: 'center', alignItems: 'center', ...SHADOWS.medium },
    searchContainer: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, borderRadius: scale(16), paddingHorizontal: scale(14), height: scale(52), marginBottom: scale(24), ...SHADOWS.soft },
    searchInput: { flex: 1, marginLeft: scale(8), color: colors.text, fontSize: moderateScale(TYPOGRAPHY.body, 0.3) },
    pinnedHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: scale(14) },
    pinnedTitle: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), fontWeight: '800', color: colors.primary, marginLeft: scale(6) },
    sectionTitle: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text, marginVertical: scale(16) },
    noteCard: { backgroundColor: colors.surface, borderRadius: scale(20), padding: scale(20), marginBottom: scale(14), borderLeftWidth: 5, ...SHADOWS.medium, flex: itemColumns > 1 ? 1 : undefined },
    noteCardTablet: { maxWidth: itemColumns === 3 ? '32%' : '48.5%' },
    noteHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: scale(10), gap: scale(10) },
    noteTitle: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text, flex: 1, textTransform: 'capitalize' },
    noteDesc: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, lineHeight: scale(22), marginBottom: scale(14), fontWeight: '500' },
    dateBadge: { flexDirection: 'row', alignItems: 'center', alignSelf: 'flex-start', backgroundColor: colors.surfaceAlt, paddingHorizontal: scale(10), paddingVertical: scale(6), borderRadius: scale(10) },
    dateText: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), marginLeft: scale(6), fontWeight: '800' },
  });
};