import React, {useState} from 'react';
import {FlatList, Pressable, StyleSheet, Text, View} from 'react-native';
import {useApp} from '../context/AppContext';
import {useT} from '../i18n';
import {COLORS, SHADOWS, SPACING, TYPOGRAPHY} from '../theme';
import {Button, EmptyState, Input} from '../components';
import {Screen, ScreenHeader} from './shared';

export const NotesScreen: React.FC = () => {
  const {notes, locale, addNote, updateNote} = useApp();
  const t = useT(locale);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');

  const startNew = () => {
    setEditingId('new');
    setTitle('');
    setBody('');
  };

  const startEdit = (id: string) => {
    const note = notes.find(n => n.id === id);
    if (!note) {
      return;
    }
    setEditingId(id);
    setTitle(note.title);
    setBody(note.body);
  };

  const handleSave = () => {
    if (!title.trim() && !body.trim()) {
      return;
    }
    if (editingId === 'new') {
      addNote({title: title.trim(), body});
    } else if (editingId) {
      updateNote(editingId, {title: title.trim(), body});
    }
    setEditingId(null);
  };

  return (
    <Screen>
      <ScreenHeader title={t('tabNotes')} />
      {editingId ? (
        <View style={styles.form}>
          <Input label={t('noteTitle')} value={title} onChangeText={setTitle} placeholder={t('noteTitlePlaceholder')} />
          <Input label={t('noteBody')} value={body} onChangeText={setBody} placeholder={t('noteBodyPlaceholder')} multiline />
          <Button label={t('save')} onPress={handleSave} />
          <Button label={t('cancel')} onPress={() => setEditingId(null)} variant="secondary" />
        </View>
      ) : (
        <FlatList
          data={notes}
          keyExtractor={item => item.id}
          contentContainerStyle={styles.list}
          ListEmptyComponent={
            <View style={styles.empty}>
              <EmptyState icon="📝" title={t('noNotes')} hint={t('noNotesHint')} />
            </View>
          }
          renderItem={({item}) => (
            <Pressable onPress={() => startEdit(item.id)} style={[styles.noteCard, SHADOWS.soft]}>
              <Text style={styles.noteTitle} numberOfLines={1}>{item.title}</Text>
              <Text style={styles.noteBody} numberOfLines={3}>{item.body}</Text>
              <Text style={styles.noteDate}>
                {new Date(item.updatedAt).toLocaleDateString(locale === 'tr' ? 'tr-TR' : 'en-US')}
              </Text>
            </Pressable>
          )}
        />
      )}
      {!editingId && (
        <Pressable style={styles.fab} onPress={startNew}>
          <Text style={styles.fabText}>＋</Text>
        </Pressable>
      )}
    </Screen>
  );
};

const styles = StyleSheet.create({
  list: {padding: SPACING.md, paddingBottom: 120},
  empty: {alignItems: 'center', paddingTop: SPACING.xxl},
  noteCard: {
    backgroundColor: COLORS.surface,
    borderRadius: 16,
    padding: SPACING.md,
    marginBottom: SPACING.sm,
  },
  noteTitle: {fontSize: TYPOGRAPHY.title, fontWeight: '700', color: COLORS.text},
  noteBody: {fontSize: TYPOGRAPHY.body, color: COLORS.textLight, marginTop: 4},
  noteDate: {fontSize: TYPOGRAPHY.small, color: COLORS.textLight, marginTop: SPACING.xs},
  form: {padding: SPACING.md, gap: SPACING.xs},
  fab: {
    position: 'absolute',
    right: SPACING.md,
    bottom: SPACING.xl,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: COLORS.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  fabText: {fontSize: 28, color: COLORS.white, marginTop: -2},
});
