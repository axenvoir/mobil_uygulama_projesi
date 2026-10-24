import React, { useState, useMemo } from 'react';
import { useApp } from '../context/AppContext';
import { Note } from '../types';
import {
  Pin,
  Plus,
  Search,
  Trash2,
  Edit2,
  X,
  Check,
  StickyNote,
} from 'lucide-react';

const NOTE_COLORS = [
  '#267968', // Primary Teal
  '#F59E0B', // Amber
  '#EF4444', // Rose/Red
  '#10B981', // Emerald
  '#3B82F6', // Blue
  '#8B5CF6', // Purple
  '#EC4899', // Pink
];

export const NotesScreen: React.FC = () => {
  const { notes, addNote, updateNote, deleteNote, togglePinNote, t } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingNote, setEditingNote] = useState<Note | null>(null);

  // Form states
  const [formTitle, setFormTitle] = useState('');
  const [formDesc, setFormDesc] = useState('');
  const [formColor, setFormColor] = useState(NOTE_COLORS[0]);
  const [formPinned, setFormPinned] = useState(false);

  const filteredNotes = useMemo(() => {
    if (!searchQuery.trim()) return notes;
    const q = searchQuery.toLowerCase().trim();
    return notes.filter(
      (n) => n.title.toLowerCase().includes(q) || n.desc.toLowerCase().includes(q)
    );
  }, [notes, searchQuery]);

  const pinnedNotes = filteredNotes.filter((n) => n.pinned);
  const regularNotes = filteredNotes.filter((n) => !n.pinned);

  const openCreateModal = () => {
    setFormTitle('');
    setFormDesc('');
    setFormColor(NOTE_COLORS[0]);
    setFormPinned(false);
    setEditingNote(null);
    setShowAddModal(true);
  };

  const openEditModal = (note: Note) => {
    setFormTitle(note.title);
    setFormDesc(note.desc);
    setFormColor(note.color || NOTE_COLORS[0]);
    setFormPinned(note.pinned);
    setEditingNote(note);
    setShowAddModal(true);
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formTitle.trim()) return;

    if (editingNote) {
      updateNote(editingNote.id, {
        title: formTitle.trim(),
        desc: formDesc.trim(),
        color: formColor,
        pinned: formPinned,
      });
    } else {
      addNote({
        title: formTitle.trim(),
        desc: formDesc.trim(),
        color: formColor,
        pinned: formPinned,
      });
    }
    setShowAddModal(false);
  };

  const handleDelete = (id: string, title: string) => {
    if (!window.confirm(t('note_delete_message', { title }))) return;
    deleteNote(id, true);
  };

  const renderNoteCard = (note: Note) => (
    <div
      key={note.id}
      className="bg-white dark:bg-[#1A2B26] rounded-2xl p-4 border border-[#E1E8E3] dark:border-[#344A40] shadow-sm flex flex-col justify-between gap-3 relative overflow-hidden group hover:shadow-md transition-shadow"
      style={{ borderLeftColor: note.color, borderLeftWidth: 5 }}
    >
      <div>
        <div className="flex items-start justify-between gap-2 mb-1.5">
          <h4 className="text-sm font-bold text-[#19332D] dark:text-[#F0F4EC] leading-snug line-clamp-1">
            {note.title}
          </h4>
          <button
            onClick={() => togglePinNote(note.id)}
            className={`p-1 rounded-lg transition-colors ${
              note.pinned
                ? 'text-[#267968] dark:text-[#70CDB0] bg-teal-50 dark:bg-teal-950/40'
                : 'text-[#5A6B63]/40 hover:text-[#5A6B63]'
            }`}
            title={note.pinned ? t('notes_unpin') : t('notes_pin')}
          >
            <Pin className={`w-3.5 h-3.5 ${note.pinned ? 'fill-current' : ''}`} />
          </button>
        </div>

        <p className="text-xs text-[#5A6B63] dark:text-[#B5C5BC] whitespace-pre-line line-clamp-4 leading-relaxed">
          {note.desc}
        </p>
      </div>

      <div className="flex items-center justify-between pt-2 border-t border-[#E1E8E3]/60 dark:border-[#344A40]/60 text-[11px] text-[#5A6B63] dark:text-[#B5C5BC]">
        <span>{note.date || ''}</span>
        <div className="flex items-center gap-1">
          <button
            onClick={() => openEditModal(note)}
            className="p-1 hover:text-[#267968] dark:hover:text-[#70CDB0] rounded"
            title={t('edit')}
          >
            <Edit2 className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={() => handleDelete(note.id, note.title)}
            className="p-1 hover:text-rose-600 rounded"
            title={t('delete')}
          >
            <Trash2 className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  );

  return (
    <div className="space-y-5 pb-24">
      {/* Header row */}
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-extrabold text-[#19332D] dark:text-[#F0F4EC]">
          {t('notes_title')}
        </h2>
        <button
          onClick={openCreateModal}
          className="inline-flex items-center gap-1.5 px-3.5 py-2 bg-[#267968] hover:bg-[#1e6153] dark:bg-[#70CDB0] dark:text-[#0D2623] text-white text-xs font-bold rounded-xl shadow transition-transform active:scale-95"
        >
          <Plus className="w-4 h-4" />
          <span>{t('add')}</span>
        </button>
      </div>

      {/* Search */}
      <div className="relative">
        <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-[#5A6B63] dark:text-[#B5C5BC]">
          <Search className="w-4 h-4" />
        </div>
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder={t('notes_search_placeholder')}
          className="w-full pl-10 pr-10 py-3 bg-white dark:bg-[#1A2B26] text-[#19332D] dark:text-[#F0F4EC] rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] text-sm focus:outline-none focus:ring-2 focus:ring-[#267968] shadow-sm placeholder:text-[#5A6B63]/60"
        />
        {searchQuery && (
          <button
            onClick={() => setSearchQuery('')}
            className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-[#5A6B63] hover:text-[#19332D]"
          >
            <X className="w-4 h-4" />
          </button>
        )}
      </div>

      {filteredNotes.length === 0 ? (
        <div className="bg-white dark:bg-[#1A2B26] p-10 rounded-3xl text-center border border-[#E1E8E3] dark:border-[#344A40]">
          <div className="w-14 h-14 bg-[#EDF2EE] dark:bg-[#253A33] rounded-2xl flex items-center justify-center mx-auto mb-3 text-[#267968] dark:text-[#70CDB0]">
            <StickyNote className="w-7 h-7" />
          </div>
          <h4 className="text-base font-bold text-[#19332D] dark:text-[#F0F4EC] mb-1">
            {t('notes_empty_title')}
          </h4>
          <p className="text-xs text-[#5A6B63] dark:text-[#B5C5BC] max-w-xs mx-auto mb-4">
            {t('notes_empty_description')}
          </p>
          <button
            onClick={openCreateModal}
            className="inline-flex items-center gap-2 px-4 py-2 bg-[#267968] dark:bg-[#70CDB0] text-white dark:text-[#0D2623] text-xs font-bold rounded-xl shadow"
          >
            <Plus className="w-4 h-4" />
            <span>{t('notes_empty_action')}</span>
          </button>
        </div>
      ) : (
        <div className="space-y-5">
          {/* Pinned section */}
          {pinnedNotes.length > 0 && (
            <div className="space-y-2.5">
              <div className="flex items-center gap-1.5 text-xs font-extrabold text-[#19332D] dark:text-[#F0F4EC]">
                <Pin className="w-3.5 h-3.5 text-[#267968] dark:text-[#70CDB0] fill-current" />
                <span>{t('notes_pinned')}</span>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {pinnedNotes.map(renderNoteCard)}
              </div>
            </div>
          )}

          {/* Regular section */}
          {regularNotes.length > 0 && (
            <div className="space-y-2.5">
              {pinnedNotes.length > 0 && (
                <div className="text-xs font-bold text-[#5A6B63] dark:text-[#B5C5BC]">
                  {t('notes_all')}
                </div>
              )}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {regularNotes.map(renderNoteCard)}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Add / Edit Note Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 animate-in fade-in">
          <div className="bg-white dark:bg-[#1A2B26] max-w-md w-full rounded-3xl shadow-2xl border border-[#E1E8E3] dark:border-[#344A40] p-6 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-extrabold text-[#19332D] dark:text-[#F0F4EC]">
                {editingNote ? t('note_edit_title') : t('note_new_title')}
              </h3>
              <button
                onClick={() => setShowAddModal(false)}
                className="p-1 text-[#5A6B63] hover:text-[#19332D]"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSave} className="space-y-4">
              <div className="space-y-1">
                <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
                  {t('note_title_label')} *
                </label>
                <input
                  type="text"
                  value={formTitle}
                  onChange={(e) => setFormTitle(e.target.value)}
                  placeholder={t('note_title_placeholder')}
                  required
                  className="w-full px-4 py-2.5 bg-[#F5F7F4] dark:bg-[#101C19] text-[#19332D] dark:text-[#F0F4EC] rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] text-sm focus:outline-none focus:ring-2 focus:ring-[#267968]"
                />
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
                  {t('note_content_label')}
                </label>
                <textarea
                  rows={4}
                  value={formDesc}
                  onChange={(e) => setFormDesc(e.target.value)}
                  placeholder={t('note_content_placeholder')}
                  className="w-full px-4 py-2.5 bg-[#F5F7F4] dark:bg-[#101C19] text-[#19332D] dark:text-[#F0F4EC] rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] text-xs focus:outline-none focus:ring-2 focus:ring-[#267968]"
                />
              </div>

              {/* Color Picker */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-[#19332D] dark:text-[#F0F4EC]">
                  {t('note_color_label')}
                </label>
                <div className="flex items-center gap-2">
                  {NOTE_COLORS.map((col) => (
                    <button
                      key={col}
                      type="button"
                      onClick={() => setFormColor(col)}
                      className="w-7 h-7 rounded-full flex items-center justify-center transition-transform active:scale-95 shadow-sm"
                      style={{ backgroundColor: col }}
                    >
                      {formColor === col && <Check className="w-4 h-4 text-white" strokeWidth={3} />}
                    </button>
                  ))}
                </div>
              </div>

              {/* Pin Checkbox */}
              <label className="flex items-center gap-2.5 cursor-pointer text-xs font-semibold text-[#19332D] dark:text-[#F0F4EC]">
                <input
                  type="checkbox"
                  checked={formPinned}
                  onChange={(e) => setFormPinned(e.target.checked)}
                  className="w-4 h-4 rounded text-[#267968] focus:ring-[#267968]"
                />
                <span>{t('notes_pin')}</span>
              </label>

              <button
                type="submit"
                className="w-full py-3 bg-[#267968] hover:bg-[#1e6153] dark:bg-[#70CDB0] dark:text-[#0D2623] text-white text-xs font-extrabold rounded-xl shadow transition-transform active:scale-98"
              >
                {t('save')}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
