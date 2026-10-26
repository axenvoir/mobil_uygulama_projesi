import React, { useState, useMemo, useEffect } from 'react';
import {
  Calendar,
  Clock,
  Plus,
  Trash2,
  Edit2,
  Search,
  Pin,
  Settings,
  Bell,
  RefreshCw,
  Smartphone,
  Layers,
  Sparkles
} from 'lucide-react';

interface Product {
  id: string;
  name: string;
  category: string;
  expiryDate: string;
  quantity: string;
  note: string;
}

interface NoteItem {
  id: string;
  title: string;
  content: string;
  colorHex: string;
  isPinned: boolean;
  updatedAt: string;
}

const CATEGORIES = [
  { id: 'dairy', labelTr: 'Süt Ürünleri', labelEn: 'Dairy', icon: '🥛', color: '#3B82F6' },
  { id: 'meat', labelTr: 'Et & Balık', labelEn: 'Meat & Fish', icon: '🥩', color: '#EF4444' },
  { id: 'veggie', labelTr: 'Meyve & Sebze', labelEn: 'Fruit & Veg', icon: '🥦', color: '#10B981' },
  { id: 'bakery', labelTr: 'Fırın & Ekmek', labelEn: 'Bakery', icon: '🍞', color: '#F59E0B' },
  { id: 'pantry', labelTr: 'Konserve & Kuru', labelEn: 'Pantry', icon: '🥫', color: '#8B5CF6' },
  { id: 'beverage', labelTr: 'İçecekler', labelEn: 'Beverages', icon: '🧃', color: '#06B6D4' },
  { id: 'snack', labelTr: 'Atıştırmalık', labelEn: 'Snacks', icon: '🍪', color: '#EC4899' },
  { id: 'other', labelTr: 'Diğer', labelEn: 'Other', icon: '📦', color: '#6B7280' },
];

const getFutureDate = (days: number): string => {
  const d = new Date();
  d.setDate(d.getDate() + days);
  return d.toISOString().split('T')[0];
};

const INITIAL_PRODUCTS: Product[] = [
  {
    id: 'prod-1',
    name: 'Süt (Whole Milk)',
    category: 'dairy',
    expiryDate: getFutureDate(0),
    quantity: '2 Adet',
    note: 'Buzdolabı orta rafında saklayın',
  },
  {
    id: 'prod-2',
    name: 'Tavuk Göğsü (Chicken)',
    category: 'meat',
    expiryDate: getFutureDate(2),
    quantity: '750g',
    note: 'Dondurucuya aktarılabilir',
  },
  {
    id: 'prod-3',
    name: 'Yoğurt (Greek Yogurt)',
    category: 'dairy',
    expiryDate: getFutureDate(-1),
    quantity: '1 Kutu',
    note: 'Kokusu ve dokusu kontrol edilmeli',
  },
  {
    id: 'prod-4',
    name: 'Kaşar Peyniri (Cheddar)',
    category: 'dairy',
    expiryDate: getFutureDate(14),
    quantity: '400g',
    note: 'Açılmamış paket',
  },
  {
    id: 'prod-5',
    name: 'Domates (Tomatoes)',
    category: 'veggie',
    expiryDate: getFutureDate(4),
    quantity: '1 kg',
    note: 'Tezgâhta muhafaza edilsin',
  },
];

const INITIAL_NOTES: NoteItem[] = [
  {
    id: 'note-1',
    title: 'Haftalık Alışveriş Notları',
    content: '• Zeytinyağı (Soğuk sıkım)\n• Tam buğday ekmeği\n• Filtre kahve çekirdeği\n• Maden suyu (6\'lı paket)',
    colorHex: '#267968',
    isPinned: true,
    updatedAt: 'Bugün 10:30',
  },
  {
    id: 'note-2',
    title: 'Dondurucu Düzeni & Plan',
    content: 'Üst çekmece: Sebzeler ve hazır garnitürler\nOrta çekmece: Kırmızı et ve tavuk\nAlt çekmece: Balık ve hamur işleri',
    colorHex: '#3B82F6',
    isPinned: false,
    updatedAt: 'Dün 18:45',
  },
];

export default function App() {
  const [activeTab, setActiveTab] = useState<'expiry' | 'notes' | 'settings'>('expiry');
  const [products, setProducts] = useState<Product[]>(() => {
    try {
      const saved = localStorage.getItem('expira_v2_products');
      return saved ? JSON.parse(saved) : INITIAL_PRODUCTS;
    } catch {
      return INITIAL_PRODUCTS;
    }
  });

  const [notes, setNotes] = useState<NoteItem[]>(() => {
    try {
      const saved = localStorage.getItem('expira_v2_notes');
      return saved ? JSON.parse(saved) : INITIAL_NOTES;
    } catch {
      return INITIAL_NOTES;
    }
  });

  const [isTurkish, setIsTurkish] = useState<boolean>(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [statusFilter, setStatusFilter] = useState<'all' | 'fresh' | 'approaching' | 'expired'>('all');

  // Modals
  const [showProductModal, setShowProductModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [showNoteModal, setShowNoteModal] = useState(false);
  const [editingNote, setEditingNote] = useState<NoteItem | null>(null);

  // Form states
  const [formName, setFormName] = useState('');
  const [formCategory, setFormCategory] = useState('dairy');
  const [formExpDate, setFormExpDate] = useState(getFutureDate(7));
  const [formQuantity, setFormQuantity] = useState('1 Adet');
  const [formNote, setFormNote] = useState('');

  const [noteTitle, setNoteTitle] = useState('');
  const [noteContent, setNoteContent] = useState('');
  const [noteColor, setNoteColor] = useState('#267968');
  const [notePinned, setNotePinned] = useState(false);

  // Save to localStorage
  useEffect(() => {
    localStorage.setItem('expira_v2_products', JSON.stringify(products));
  }, [products]);

  useEffect(() => {
    localStorage.setItem('expira_v2_notes', JSON.stringify(notes));
  }, [notes]);

  const calculateDaysLeft = (expDate: string): number => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const target = new Date(expDate);
    target.setHours(0, 0, 0, 0);
    const diff = target.getTime() - today.getTime();
    return Math.round(diff / (1000 * 60 * 60 * 24));
  };

  const stats = useMemo(() => {
    let fresh = 0;
    let approaching = 0;
    let expired = 0;
    products.forEach((p) => {
      const days = calculateDaysLeft(p.expiryDate);
      if (days < 0) expired++;
      else if (days <= 3) approaching++;
      else fresh++;
    });
    return { total: products.length, fresh, approaching, expired };
  }, [products]);

  const filteredProducts = useMemo(() => {
    return products.filter((p) => {
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase();
        const matchesName = p.name.toLowerCase().includes(q);
        const matchesNote = (p.note || '').toLowerCase().includes(q);
        if (!matchesName && !matchesNote) return false;
      }
      if (selectedCategory !== 'all' && p.category !== selectedCategory) return false;
      const days = calculateDaysLeft(p.expiryDate);
      if (statusFilter === 'fresh' && days <= 3) return false;
      if (statusFilter === 'approaching' && (days < 0 || days > 3)) return false;
      if (statusFilter === 'expired' && days >= 0) return false;
      return true;
    });
  }, [products, searchQuery, selectedCategory, statusFilter]);

  const openAddProduct = () => {
    setEditingProduct(null);
    setFormName('');
    setFormCategory('dairy');
    setFormExpDate(getFutureDate(7));
    setFormQuantity('1 Adet');
    setFormNote('');
    setShowProductModal(true);
  };

  const openEditProduct = (item: Product) => {
    setEditingProduct(item);
    setFormName(item.name);
    setFormCategory(item.category);
    setFormExpDate(item.expiryDate);
    setFormQuantity(item.quantity);
    setFormNote(item.note);
    setShowProductModal(true);
  };

  const handleSaveProduct = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formName.trim()) return;

    if (editingProduct) {
      setProducts(
        products.map((p) =>
          p.id === editingProduct.id
            ? {
                ...p,
                name: formName.trim(),
                category: formCategory,
                expiryDate: formExpDate,
                quantity: formQuantity.trim(),
                note: formNote.trim(),
              }
            : p
        )
      );
    } else {
      const newP: Product = {
        id: `prod_${Date.now()}`,
        name: formName.trim(),
        category: formCategory,
        expiryDate: formExpDate,
        quantity: formQuantity.trim(),
        note: formNote.trim(),
      };
      setProducts([newP, ...products]);
    }
    setShowProductModal(false);
  };

  const handleDeleteProduct = (id: string) => {
    if (confirm(isTurkish ? 'Bu ürünü silmek istediğinize emin misiniz?' : 'Delete this item?')) {
      setProducts(products.filter((p) => p.id !== id));
    }
  };

  const openAddNote = () => {
    setEditingNote(null);
    setNoteTitle('');
    setNoteContent('');
    setNoteColor('#267968');
    setNotePinned(false);
    setShowNoteModal(true);
  };

  const openEditNote = (note: NoteItem) => {
    setEditingNote(note);
    setNoteTitle(note.title);
    setNoteContent(note.content);
    setNoteColor(note.colorHex);
    setNotePinned(note.isPinned);
    setShowNoteModal(true);
  };

  const handleSaveNote = (e: React.FormEvent) => {
    e.preventDefault();
    if (!noteTitle.trim()) return;

    if (editingNote) {
      setNotes(
        notes.map((n) =>
          n.id === editingNote.id
            ? {
                ...n,
                title: noteTitle.trim(),
                content: noteContent.trim(),
                colorHex: noteColor,
                isPinned: notePinned,
                updatedAt: isTurkish ? 'Şimdi güncellendi' : 'Just updated',
              }
            : n
        )
      );
    } else {
      const newN: NoteItem = {
        id: `note_${Date.now()}`,
        title: noteTitle.trim(),
        content: noteContent.trim(),
        colorHex: noteColor,
        isPinned: notePinned,
        updatedAt: isTurkish ? 'Yeni eklendi' : 'Just created',
      };
      setNotes([newN, ...notes]);
    }
    setShowNoteModal(false);
  };

  const handleDeleteNote = (id: string) => {
    if (confirm(isTurkish ? 'Bu notu silmek istediğinize emin misiniz?' : 'Delete this note?')) {
      setNotes(notes.filter((n) => n.id !== id));
    }
  };

  const togglePinNote = (id: string) => {
    setNotes(
      notes.map((n) => (n.id === id ? { ...n, isPinned: !n.isPinned } : n))
    );
  };

  return (
    <div className="min-h-screen bg-[#F5F7F4] text-[#19332D] flex flex-col font-sans">
      {/* Top Header */}
      <header className="bg-[#153F38] text-white shadow-md sticky top-0 z-30">
        <div className="max-w-2xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <span className="text-2xl">🌿</span>
            <div>
              <h1 className="text-lg font-bold tracking-tight">Expira</h1>
              <p className="text-xs text-[#A7C4BD]">
                {activeTab === 'expiry' && (isTurkish ? 'Son Kullanma Tarihi Takibi' : 'Expiry Tracker')}
                {activeTab === 'notes' && (isTurkish ? 'Kiler & Alışveriş Notları' : 'Pantry & Shopping Notes')}
                {activeTab === 'settings' && (isTurkish ? 'Ayarlar & Sistem Mimarisi' : 'Settings & Architecture')}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setIsTurkish(!isTurkish)}
              className="text-xs font-bold px-2 py-1 rounded-lg bg-white/10 hover:bg-white/20 transition-colors"
            >
              {isTurkish ? '🇹🇷 TR' : '🇬🇧 EN'}
            </button>
            {activeTab === 'expiry' && (
              <button
                onClick={openAddProduct}
                className="bg-[#C4D639] text-[#153F38] font-bold text-xs px-3 py-1.5 rounded-full flex items-center gap-1 hover:brightness-105 transition-all shadow-sm"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>{isTurkish ? 'Ürün Ekle' : 'Add Item'}</span>
              </button>
            )}
            {activeTab === 'notes' && (
              <button
                onClick={openAddNote}
                className="bg-[#C4D639] text-[#153F38] font-bold text-xs px-3 py-1.5 rounded-full flex items-center gap-1 hover:brightness-105 transition-all shadow-sm"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>{isTurkish ? 'Not Yaz' : 'Add Note'}</span>
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="max-w-2xl mx-auto w-full px-4 py-4 flex-1 pb-24">
        {/* TAB 1: SKT TAKİBİ */}
        {activeTab === 'expiry' && (
          <div className="space-y-4">
            {/* Stats Summary Bar */}
            <div className="grid grid-cols-4 gap-2">
              <button
                onClick={() => setStatusFilter('all')}
                className={`p-2.5 rounded-2xl border text-center transition-all ${
                  statusFilter === 'all'
                    ? 'bg-[#153F38] text-white border-[#153F38] shadow-sm'
                    : 'bg-white border-[#E1E8E3] text-[#19332D]'
                }`}
              >
                <div className="text-base font-bold">{stats.total}</div>
                <div className="text-[11px] font-semibold opacity-80">{isTurkish ? 'Toplam' : 'Total'}</div>
              </button>

              <button
                onClick={() => setStatusFilter(statusFilter === 'fresh' ? 'all' : 'fresh')}
                className={`p-2.5 rounded-2xl border text-center transition-all ${
                  statusFilter === 'fresh'
                    ? 'bg-[#10B981] text-white border-[#10B981] shadow-sm'
                    : 'bg-white border-[#E1E8E3] text-[#10B981]'
                }`}
              >
                <div className="text-base font-bold">{stats.fresh}</div>
                <div className="text-[11px] font-semibold opacity-80">{isTurkish ? 'Taze' : 'Fresh'}</div>
              </button>

              <button
                onClick={() => setStatusFilter(statusFilter === 'approaching' ? 'all' : 'approaching')}
                className={`p-2.5 rounded-2xl border text-center transition-all ${
                  statusFilter === 'approaching'
                    ? 'bg-[#D97706] text-white border-[#D97706] shadow-sm'
                    : 'bg-white border-[#E1E8E3] text-[#D97706]'
                }`}
              >
                <div className="text-base font-bold">{stats.approaching}</div>
                <div className="text-[11px] font-semibold opacity-80">{isTurkish ? 'Yaklaşan' : 'Soon'}</div>
              </button>

              <button
                onClick={() => setStatusFilter(statusFilter === 'expired' ? 'all' : 'expired')}
                className={`p-2.5 rounded-2xl border text-center transition-all ${
                  statusFilter === 'expired'
                    ? 'bg-[#DC2626] text-white border-[#DC2626] shadow-sm'
                    : 'bg-white border-[#E1E8E3] text-[#DC2626]'
                }`}
              >
                <div className="text-base font-bold">{stats.expired}</div>
                <div className="text-[11px] font-semibold opacity-80">{isTurkish ? 'Dolmuş' : 'Expired'}</div>
              </button>
            </div>

            {/* Search Input */}
            <div className="relative">
              <Search className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
              <input
                type="text"
                placeholder={isTurkish ? 'Ürün adı veya not ara...' : 'Search items or notes...'}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 bg-white border border-[#E1E8E3] rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#267968]"
              />
              {searchQuery && (
                <button
                  onClick={() => setSearchQuery('')}
                  className="absolute right-3 top-2.5 text-xs text-gray-400 hover:text-gray-600"
                >
                  ✕
                </button>
              )}
            </div>

            {/* Category Pills */}
            <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-none">
              <button
                onClick={() => setSelectedCategory('all')}
                className={`px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap transition-all border ${
                  selectedCategory === 'all'
                    ? 'bg-[#267968] text-white border-[#267968]'
                    : 'bg-white text-[#19332D] border-[#E1E8E3]'
                }`}
              >
                {isTurkish ? 'Tümü' : 'All'}
              </button>
              {CATEGORIES.map((cat) => (
                <button
                  key={cat.id}
                  onClick={() => setSelectedCategory(selectedCategory === cat.id ? 'all' : cat.id)}
                  className={`px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap flex items-center gap-1.5 transition-all border ${
                    selectedCategory === cat.id
                      ? 'bg-[#267968] text-white border-[#267968]'
                      : 'bg-white text-[#19332D] border-[#E1E8E3]'
                  }`}
                >
                  <span>{cat.icon}</span>
                  <span>{isTurkish ? cat.labelTr : cat.labelEn}</span>
                </button>
              ))}
            </div>

            {/* Products List */}
            {filteredProducts.length === 0 ? (
              <div className="text-center py-16 bg-white rounded-3xl border border-[#E1E8E3] p-8">
                <div className="text-4xl mb-3">📦</div>
                <h3 className="font-bold text-base mb-1">
                  {isTurkish ? 'Kayıtlı ürün bulunamadı' : 'No items found'}
                </h3>
                <p className="text-xs text-[#5A6B63] max-w-xs mx-auto mb-4">
                  {isTurkish
                    ? 'Yukarıdaki butona basarak ilk ürününüzü ekleyebilirsiniz.'
                    : 'Tap the Add Item button above to record your first product.'}
                </p>
                <button
                  onClick={openAddProduct}
                  className="bg-[#267968] text-white text-xs font-bold px-4 py-2 rounded-xl"
                >
                  + {isTurkish ? 'Ürün Ekle' : 'Add Item'}
                </button>
              </div>
            ) : (
              <div className="space-y-3">
                {filteredProducts.map((p) => {
                  const days = calculateDaysLeft(p.expiryDate);
                  const cat = CATEGORIES.find((c) => c.id === p.category) || CATEGORIES[7];

                  let badgeBg = 'bg-[#EAF5EF] text-[#10B981] border-[#10B981]/20';
                  let badgeText = `${days} ${isTurkish ? 'gün kaldı' : 'days left'}`;

                  if (days < 0) {
                    badgeBg = 'bg-[#FEE2E2] text-[#DC2626] border-[#DC2626]/20';
                    badgeText = `${Math.abs(days)} ${isTurkish ? 'gün geçti' : 'days ago'}`;
                  } else if (days === 0) {
                    badgeBg = 'bg-[#FEF3C7] text-[#D97706] border-[#D97706]/20';
                    badgeText = isTurkish ? 'Bugün doluyor!' : 'Expires today!';
                  } else if (days <= 3) {
                    badgeBg = 'bg-[#FEF3C7] text-[#D97706] border-[#D97706]/20';
                    badgeText = `${days} ${isTurkish ? 'gün kaldı' : 'days left'}`;
                  }

                  return (
                    <div
                      key={p.id}
                      className="bg-white rounded-2xl p-4 border border-[#E1E8E3] shadow-xs hover:border-[#267968]/40 transition-all"
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div className="flex items-center gap-3 flex-1 min-w-0">
                          <span className="text-2xl p-2 bg-[#F5F7F4] rounded-xl">{cat.icon}</span>
                          <div className="truncate">
                            <h4 className="font-bold text-sm text-[#19332D] truncate">{p.name}</h4>
                            <p className="text-xs text-[#5A6B63]">
                              {isTurkish ? cat.labelTr : cat.labelEn} • {p.quantity}
                            </p>
                          </div>
                        </div>

                        <span
                          className={`text-[11px] font-bold px-2.5 py-1 rounded-lg border ${badgeBg} whitespace-nowrap`}
                        >
                          {badgeText}
                        </span>
                      </div>

                      <div className="mt-3 pt-2.5 border-t border-[#F0F2EE] flex items-center justify-between text-xs text-[#5A6B63]">
                        <div className="flex items-center gap-1.5 font-medium">
                          <Calendar className="w-3.5 h-3.5 text-[#267968]" />
                          <span>
                            {isTurkish ? 'SKT:' : 'EXP:'} <strong className="text-[#19332D]">{p.expiryDate}</strong>
                          </span>
                        </div>

                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => openEditProduct(p)}
                            className="p-1 text-gray-500 hover:text-[#267968] transition-colors"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => handleDeleteProduct(p.id)}
                            className="p-1 text-gray-500 hover:text-red-600 transition-colors"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>

                      {p.note && (
                        <div className="mt-2 text-xs text-[#60736C] bg-[#F5F7F4] p-2 rounded-lg">
                          💬 {p.note}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {/* TAB 2: NOTLAR */}
        {activeTab === 'notes' && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-bold uppercase tracking-wider text-[#5A6B63]">
                {isTurkish ? 'Kiler & Alışveriş Notları' : 'Notes'}
              </h2>
              <span className="text-xs bg-white px-2.5 py-1 rounded-full border border-[#E1E8E3] font-semibold">
                {notes.length} {isTurkish ? 'not' : 'notes'}
              </span>
            </div>

            {notes.length === 0 ? (
              <div className="text-center py-16 bg-white rounded-3xl border border-[#E1E8E3] p-8">
                <div className="text-4xl mb-3">📝</div>
                <h3 className="font-bold text-base mb-1">
                  {isTurkish ? 'Henüz not bulunmuyor' : 'No notes yet'}
                </h3>
                <p className="text-xs text-[#5A6B63] max-w-xs mx-auto mb-4">
                  {isTurkish
                    ? 'Alışveriş listenizi veya yemek tariflerinizi kaydetmek için yeni not oluşturun.'
                    : 'Create a note to track grocery items or recipes.'}
                </p>
                <button
                  onClick={openAddNote}
                  className="bg-[#267968] text-white text-xs font-bold px-4 py-2 rounded-xl"
                >
                  + {isTurkish ? 'Not Yaz' : 'Add Note'}
                </button>
              </div>
            ) : (
              <div className="grid grid-cols-1 gap-3">
                {notes
                  .sort((a, b) => (b.isPinned ? 1 : 0) - (a.isPinned ? 1 : 0))
                  .map((note) => (
                    <div
                      key={note.id}
                      className="bg-white rounded-2xl p-4 border border-[#E1E8E3] shadow-xs relative overflow-hidden"
                    >
                      <div
                        className="absolute left-0 top-0 bottom-0 w-1.5"
                        style={{ backgroundColor: note.colorHex || '#267968' }}
                      />

                      <div className="flex items-start justify-between gap-2 pl-2">
                        <h4 className="font-bold text-sm text-[#19332D] flex items-center gap-1.5">
                          {note.isPinned && <Pin className="w-3.5 h-3.5 text-[#267968] fill-[#267968]" />}
                          <span>{note.title}</span>
                        </h4>

                        <div className="flex items-center gap-1">
                          <button
                            onClick={() => togglePinNote(note.id)}
                            className={`p-1 rounded-lg hover:bg-gray-100 ${
                              note.isPinned ? 'text-[#267968]' : 'text-gray-400'
                            }`}
                          >
                            <Pin className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => openEditNote(note)}
                            className="p-1 rounded-lg text-gray-400 hover:text-gray-700"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => handleDeleteNote(note.id)}
                            className="p-1 rounded-lg text-gray-400 hover:text-red-600"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>

                      {note.content && (
                        <p className="text-xs text-[#5A6B63] mt-2 pl-2 whitespace-pre-wrap leading-relaxed">
                          {note.content}
                        </p>
                      )}

                      <div className="mt-3 pt-2 pl-2 border-t border-[#F0F2EE] text-[10px] text-gray-400">
                        {note.updatedAt}
                      </div>
                    </div>
                  ))}
              </div>
            )}
          </div>
        )}

        {/* TAB 3: AYARLAR & YEREL ANDROID BİLGİSİ */}
        {activeTab === 'settings' && (
          <div className="space-y-4">
            <div className="bg-[#153F38] text-white p-5 rounded-3xl shadow-sm">
              <div className="flex items-center gap-2 mb-2">
                <Smartphone className="w-5 h-5 text-[#C4D639]" />
                <h3 className="font-bold text-base">Saf Yerel Android (Kotlin & Compose)</h3>
              </div>
              <p className="text-xs text-[#A7C4BD] leading-relaxed mb-3">
                Bu uygulama Expo veya hibrit katmanlar olmadan, %100 saf Kotlin, Jetpack Compose, Room Veritabanı ve WorkManager ile derlenmektedir.
              </p>
              <div className="grid grid-cols-2 gap-2 text-xs font-mono">
                <div className="bg-white/10 p-2 rounded-xl">
                  <div className="text-[10px] text-gray-300">Target SDK</div>
                  <div className="font-bold text-[#C4D639]">API 36 (Google Play)</div>
                </div>
                <div className="bg-white/10 p-2 rounded-xl">
                  <div className="text-[10px] text-gray-300">Min SDK</div>
                  <div className="font-bold">API 24 (Android 7+)</div>
                </div>
              </div>
            </div>

            <div className="bg-white rounded-3xl p-5 border border-[#E1E8E3] space-y-4">
              <h4 className="font-bold text-sm text-[#19332D] flex items-center gap-2">
                <Bell className="w-4 h-4 text-[#267968]" />
                <span>{isTurkish ? 'Yerel Bildirimler' : 'Local Notifications'}</span>
              </h4>
              <p className="text-xs text-[#5A6B63]">
                {isTurkish
                  ? 'Android WorkManager her gün arka planda sessizce çalışır ve SKT si 3 gün kalan ürünler için cihaz bildirimi üretir.'
                  : 'Android WorkManager runs daily in the background and creates local device notifications for expiring products.'}
              </p>
              <button
                onClick={() => alert(isTurkish ? '✓ Test bildirimi simüle edildi!' : '✓ Test notification simulated!')}
                className="w-full py-2.5 bg-[#F5F7F4] hover:bg-[#EDF2EE] text-[#19332D] text-xs font-bold rounded-xl border border-[#E1E8E3] flex items-center justify-center gap-2"
              >
                <Bell className="w-3.5 h-3.5 text-[#267968]" />
                <span>{isTurkish ? 'Test Bildirimi Gönder' : 'Send Test Notification'}</span>
              </button>
            </div>

            <div className="bg-white rounded-3xl p-5 border border-[#E1E8E3] space-y-3">
              <h4 className="font-bold text-sm text-[#19332D] flex items-center gap-2">
                <RefreshCw className="w-4 h-4 text-[#267968]" />
                <span>{isTurkish ? 'Veritabanı İşlemleri' : 'Database Actions'}</span>
              </h4>
              <button
                onClick={() => {
                  setProducts(INITIAL_PRODUCTS);
                  setNotes(INITIAL_NOTES);
                  alert(isTurkish ? 'Örnek veriler başarıyla yüklendi!' : 'Sample data loaded!');
                }}
                className="w-full py-2.5 bg-[#EAF5EF] text-[#10B981] hover:bg-[#d8efe2] text-xs font-bold rounded-xl border border-[#10B981]/20 flex items-center justify-center gap-2"
              >
                <Sparkles className="w-3.5 h-3.5" />
                <span>{isTurkish ? 'Örnek Verileri Yükle' : 'Load Sample Data'}</span>
              </button>

              <button
                onClick={() => {
                  if (confirm(isTurkish ? 'Tüm veriler kalıcı olarak silinecektir. Emin misiniz?' : 'Clear all data?')) {
                    setProducts([]);
                    setNotes([]);
                  }
                }}
                className="w-full py-2.5 bg-red-50 text-red-600 hover:bg-red-100 text-xs font-bold rounded-xl border border-red-200 flex items-center justify-center gap-2"
              >
                <Trash2 className="w-3.5 h-3.5" />
                <span>{isTurkish ? 'Tüm Veritabanını Sıfırla' : 'Clear All Data'}</span>
              </button>
            </div>
          </div>
        )}
      </main>

      {/* Floating Bottom Navigation Bar */}
      <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-[#E1E8E3] z-20">
        <div className="max-w-2xl mx-auto flex items-center justify-around py-2 px-4">
          <button
            onClick={() => setActiveTab('expiry')}
            className={`flex flex-col items-center gap-1 py-1 px-4 rounded-xl transition-all ${
              activeTab === 'expiry' ? 'text-[#267968] font-bold' : 'text-gray-400 font-medium'
            }`}
          >
            <Clock className="w-5 h-5" />
            <span className="text-[11px]">{isTurkish ? 'SKT Takibi' : 'Expiry'}</span>
          </button>

          <button
            onClick={() => setActiveTab('notes')}
            className={`flex flex-col items-center gap-1 py-1 px-4 rounded-xl transition-all ${
              activeTab === 'notes' ? 'text-[#267968] font-bold' : 'text-gray-400 font-medium'
            }`}
          >
            <Layers className="w-5 h-5" />
            <span className="text-[11px]">{isTurkish ? 'Notlar' : 'Notes'}</span>
          </button>

          <button
            onClick={() => setActiveTab('settings')}
            className={`flex flex-col items-center gap-1 py-1 px-4 rounded-xl transition-all ${
              activeTab === 'settings' ? 'text-[#267968] font-bold' : 'text-gray-400 font-medium'
            }`}
          >
            <Settings className="w-5 h-5" />
            <span className="text-[11px]">{isTurkish ? 'Ayarlar' : 'Settings'}</span>
          </button>
        </div>
      </nav>

      {/* Product Add / Edit Modal */}
      {showProductModal && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          <div className="bg-white w-full max-w-md rounded-t-3xl sm:rounded-3xl p-6 shadow-2xl max-h-[90vh] overflow-y-auto">
            <h3 className="font-bold text-base text-[#19332D] mb-4">
              {editingProduct
                ? isTurkish
                  ? 'Ürünü Düzenle'
                  : 'Edit Item'
                : isTurkish
                ? 'Yeni Ürün Ekle'
                : 'Add New Item'}
            </h3>

            <form onSubmit={handleSaveProduct} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-[#19332D] mb-1">
                  {isTurkish ? 'Ürün Adı *' : 'Item Name *'}
                </label>
                <input
                  type="text"
                  required
                  placeholder={isTurkish ? 'Örn: Süt, Yoğurt, Peynir' : 'e.g. Milk, Cheese'}
                  value={formName}
                  onChange={(e) => setFormName(e.target.value)}
                  className="w-full px-3 py-2 bg-[#F5F7F4] border border-[#E1E8E3] rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#267968]"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-[#19332D] mb-1">
                  {isTurkish ? 'Kategori' : 'Category'}
                </label>
                <div className="grid grid-cols-4 gap-1.5 max-h-32 overflow-y-auto p-1 bg-[#F5F7F4] rounded-xl">
                  {CATEGORIES.map((cat) => (
                    <button
                      type="button"
                      key={cat.id}
                      onClick={() => setFormCategory(cat.id)}
                      className={`p-2 rounded-lg text-xs font-semibold flex flex-col items-center gap-1 border transition-all ${
                        formCategory === cat.id
                          ? 'bg-[#267968] text-white border-[#267968]'
                          : 'bg-white text-[#19332D] border-[#E1E8E3]'
                      }`}
                    >
                      <span className="text-base">{cat.icon}</span>
                      <span className="text-[10px] truncate max-w-full">
                        {isTurkish ? cat.labelTr : cat.labelEn}
                      </span>
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-[#19332D] mb-1">
                  {isTurkish ? 'Son Kullanma Tarihi' : 'Expiry Date'}
                </label>
                <input
                  type="date"
                  required
                  value={formExpDate}
                  onChange={(e) => setFormExpDate(e.target.value)}
                  className="w-full px-3 py-2 bg-[#F5F7F4] border border-[#E1E8E3] rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#267968]"
                />
                <div className="flex gap-1.5 mt-2 overflow-x-auto pb-1">
                  <button
                    type="button"
                    onClick={() => setFormExpDate(getFutureDate(3))}
                    className="text-[11px] font-bold px-2 py-1 bg-[#EAF5EF] text-[#267968] rounded-lg"
                  >
                    +3 Gün
                  </button>
                  <button
                    type="button"
                    onClick={() => setFormExpDate(getFutureDate(7))}
                    className="text-[11px] font-bold px-2 py-1 bg-[#EAF5EF] text-[#267968] rounded-lg"
                  >
                    +1 Hafta
                  </button>
                  <button
                    type="button"
                    onClick={() => setFormExpDate(getFutureDate(30))}
                    className="text-[11px] font-bold px-2 py-1 bg-[#EAF5EF] text-[#267968] rounded-lg"
                  >
                    +1 Ay
                  </button>
                  <button
                    type="button"
                    onClick={() => setFormExpDate(getFutureDate(180))}
                    className="text-[11px] font-bold px-2 py-1 bg-[#EAF5EF] text-[#267968] rounded-lg"
                  >
                    +6 Ay
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-[#19332D] mb-1">
                  {isTurkish ? 'Miktar / Adet' : 'Quantity'}
                </label>
                <input
                  type="text"
                  placeholder={isTurkish ? 'Örn: 2 Adet, 500g' : 'e.g. 2 packs, 500g'}
                  value={formQuantity}
                  onChange={(e) => setFormQuantity(e.target.value)}
                  className="w-full px-3 py-2 bg-[#F5F7F4] border border-[#E1E8E3] rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#267968]"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-[#19332D] mb-1">
                  {isTurkish ? 'Not' : 'Note'}
                </label>
                <textarea
                  rows={2}
                  placeholder={isTurkish ? 'Dolap rafı, tarif notu...' : 'Pantry note...'}
                  value={formNote}
                  onChange={(e) => setFormNote(e.target.value)}
                  className="w-full px-3 py-2 bg-[#F5F7F4] border border-[#E1E8E3] rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#267968]"
                />
              </div>

              <div className="flex gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowProductModal(false)}
                  className="flex-1 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-bold rounded-xl"
                >
                  {isTurkish ? 'İptal' : 'Cancel'}
                </button>
                <button
                  type="submit"
                  className="flex-2 py-2.5 bg-[#267968] hover:bg-[#153F38] text-white text-xs font-bold rounded-xl shadow-sm"
                >
                  {isTurkish ? 'Kaydet' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Note Add / Edit Modal */}
      {showNoteModal && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          <div className="bg-white w-full max-w-md rounded-t-3xl sm:rounded-3xl p-6 shadow-2xl max-h-[90vh] overflow-y-auto">
            <h3 className="font-bold text-base text-[#19332D] mb-4">
              {editingNote
                ? isTurkish
                  ? 'Notu Düzenle'
                  : 'Edit Note'
                : isTurkish
                ? 'Yeni Not Yaz'
                : 'Add New Note'}
            </h3>

            <form onSubmit={handleSaveNote} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-[#19332D] mb-1">
                  {isTurkish ? 'Not Başlığı *' : 'Note Title *'}
                </label>
                <input
                  type="text"
                  required
                  placeholder={isTurkish ? 'Örn: Haftalık Alışveriş' : 'e.g. Grocery List'}
                  value={noteTitle}
                  onChange={(e) => setNoteTitle(e.target.value)}
                  className="w-full px-3 py-2 bg-[#F5F7F4] border border-[#E1E8E3] rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#267968]"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-[#19332D] mb-1">
                  {isTurkish ? 'Not İçeriği' : 'Content'}
                </label>
                <textarea
                  rows={5}
                  placeholder={isTurkish ? '• Maden suyu\n• Zeytinyağı' : '• Item 1\n• Item 2'}
                  value={noteContent}
                  onChange={(e) => setNoteContent(e.target.value)}
                  className="w-full px-3 py-2 bg-[#F5F7F4] border border-[#E1E8E3] rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#267968]"
                />
              </div>

              <div className="flex items-center justify-between py-1">
                <span className="text-xs font-bold text-[#19332D]">
                  {isTurkish ? 'Başa Sabitle (Pin)' : 'Pin to Top'}
                </span>
                <input
                  type="checkbox"
                  checked={notePinned}
                  onChange={(e) => setNotePinned(e.target.checked)}
                  className="w-4 h-4 accent-[#267968] rounded"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-[#19332D] mb-1.5">
                  {isTurkish ? 'Renk Etiketi' : 'Color Tag'}
                </label>
                <div className="flex gap-2">
                  {['#267968', '#3B82F6', '#8B5CF6', '#EF4444', '#F59E0B', '#10B981'].map((hex) => (
                    <button
                      type="button"
                      key={hex}
                      onClick={() => setNoteColor(hex)}
                      className={`w-7 h-7 rounded-full border-2 transition-transform ${
                        noteColor === hex ? 'scale-115 border-black' : 'border-transparent'
                      }`}
                      style={{ backgroundColor: hex }}
                    />
                  ))}
                </div>
              </div>

              <div className="flex gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowNoteModal(false)}
                  className="flex-1 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-bold rounded-xl"
                >
                  {isTurkish ? 'İptal' : 'Cancel'}
                </button>
                <button
                  type="submit"
                  className="flex-2 py-2.5 bg-[#267968] hover:bg-[#153F38] text-white text-xs font-bold rounded-xl shadow-sm"
                >
                  {isTurkish ? 'Kaydet' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
