import React, { useState, useMemo } from 'react';
import { useApp } from '../context/AppContext';
import { CATEGORIES } from '../theme';
import { formatDate, getDaysLeft, getExpiryStatus } from '../utils/formatters';
import { CategoryIcon } from '../components/CategoryIcon';
import {
  Search,
  X,
  SlidersHorizontal,
  Trash2,
  Share2,
  FolderEdit,
  Plus,
  Check,
  Clock,
  AlertTriangle,
  CheckCircle2,
  ChevronRight,
  Copy,
} from 'lucide-react';

export const ProductsScreen: React.FC = () => {
  const {
    products,
    locale,
    t,
    selectedCategory,
    setSelectedCategory,
    productFilter,
    setProductFilter,
    setViewProductDetail,
    setEditingProduct,
    deleteProduct,
    bulkDeleteProducts,
    bulkUpdateCategory,
    setActiveTab,
    showToast,
  } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState<'date' | 'name' | 'category' | 'added'>('date');
  const [isSelectMode, setIsSelectMode] = useState(false);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [showBulkCategoryModal, setShowBulkCategoryModal] = useState(false);

  // Filter and sort products
  const filteredProducts = useMemo(() => {
    let result = products.map((p) => ({
      ...p,
      daysLeft: getDaysLeft(p.expDate),
    }));

    // Status filter
    if (productFilter === 'fresh') {
      result = result.filter((p) => p.daysLeft > 7);
    } else if (productFilter === 'approaching') {
      result = result.filter((p) => p.daysLeft >= 0 && p.daysLeft <= 7);
    } else if (productFilter === 'expired') {
      result = result.filter((p) => p.daysLeft < 0);
    } else if (productFilter === 'today') {
      result = result.filter((p) => p.daysLeft === 0);
    }

    // Category filter
    if (selectedCategory && selectedCategory !== 'all') {
      result = result.filter((p) => p.category === selectedCategory);
    }

    // Search query
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase().trim();
      result = result.filter(
        (p) =>
          p.name.toLowerCase().includes(q) ||
          (p.productCode && p.productCode.toLowerCase().includes(q)) ||
          (p.note && p.note.toLowerCase().includes(q))
      );
    }

    // Sorting
    result.sort((a, b) => {
      if (sortBy === 'date') return a.daysLeft - b.daysLeft;
      if (sortBy === 'name') return a.name.localeCompare(b.name);
      if (sortBy === 'category') return a.category.localeCompare(b.category);
      // 'added'
      return b.id.localeCompare(a.id);
    });

    return result;
  }, [products, productFilter, selectedCategory, searchQuery, sortBy]);

  const toggleSelect = (id: string) => {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const selectAll = () => {
    if (selectedIds.length === filteredProducts.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(filteredProducts.map((p) => p.id));
    }
  };

  const handleBulkDelete = () => {
    if (!window.confirm(t('bulk_delete_message', { count: selectedIds.length }))) return;
    bulkDeleteProducts(selectedIds);
    setSelectedIds([]);
    setIsSelectMode(false);
  };

  const handleBulkShare = () => {
    const selectedItems = products.filter((p) => selectedIds.includes(p.id));
    const lines = selectedItems.map(
      (p) => `• ${p.name} - ${formatDate(p.expDate, locale)} (${p.quantity || 1})`
    );
    const text = `📦 Expira Inventory:\n` + lines.join('\n');
    navigator.clipboard.writeText(text);
    showToast(t('products_copied', { count: selectedIds.length }), 'success');
  };

  const getStatusBadge = (daysLeft: number) => {
    const status = getExpiryStatus(daysLeft);
    if (status === 'expired') {
      return (
        <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded-full bg-rose-100 text-rose-800 dark:bg-rose-950/60 dark:text-rose-300">
          <AlertTriangle className="w-3 h-3" />
          {t('status_expired')} ({Math.abs(daysLeft)}d)
        </span>
      );
    }
    if (status === 'today') {
      return (
        <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded-full bg-amber-100 text-amber-800 dark:bg-amber-950/60 dark:text-amber-300">
          <Clock className="w-3 h-3" />
          {t('home_today')}
        </span>
      );
    }
    if (status === 'approaching') {
      return (
        <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded-full bg-amber-100 text-amber-800 dark:bg-amber-950/60 dark:text-amber-300">
          <Clock className="w-3 h-3" />
          {t('product_status_days', { count: daysLeft })}
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300">
        <CheckCircle2 className="w-3 h-3" />
        {t('status_fresh')} ({daysLeft}d)
      </span>
    );
  };

  return (
    <div className="space-y-4 pb-24">
      {/* Top Search bar */}
      <div className="relative">
        <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-[#5A6B63] dark:text-[#B5C5BC]">
          <Search className="w-4 h-4" />
        </div>
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder={t('products_search_placeholder')}
          className="w-full pl-10 pr-10 py-3 bg-white dark:bg-[#1A2B26] text-[#19332D] dark:text-[#F0F4EC] rounded-2xl border border-[#E1E8E3] dark:border-[#344A40] text-sm focus:outline-none focus:ring-2 focus:ring-[#267968] dark:focus:ring-[#70CDB0] shadow-sm transition-all placeholder:text-[#5A6B63]/60 dark:placeholder:text-[#B5C5BC]/60"
        />
        {searchQuery && (
          <button
            onClick={() => setSearchQuery('')}
            className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-[#5A6B63] dark:text-[#B5C5BC] hover:text-[#19332D]"
          >
            <X className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Filter Tabs: All, Fresh, Approaching, Expired */}
      <div className="flex bg-[#EDF2EE] dark:bg-[#253A33] p-1 rounded-xl text-xs font-bold text-[#5A6B63] dark:text-[#B5C5BC]">
        <button
          onClick={() => setProductFilter('all')}
          className={`flex-1 py-2 text-center rounded-lg transition-all ${
            productFilter === 'all'
              ? 'bg-white dark:bg-[#1A2B26] text-[#267968] dark:text-[#70CDB0] shadow-sm'
              : 'hover:text-[#19332D] dark:hover:text-[#F0F4EC]'
          }`}
        >
          {t('all')} ({products.length})
        </button>
        <button
          onClick={() => setProductFilter('fresh')}
          className={`flex-1 py-2 text-center rounded-lg transition-all ${
            productFilter === 'fresh'
              ? 'bg-white dark:bg-[#1A2B26] text-[#267968] dark:text-[#70CDB0] shadow-sm'
              : 'hover:text-[#19332D] dark:hover:text-[#F0F4EC]'
          }`}
        >
          {t('fresh')}
        </button>
        <button
          onClick={() => setProductFilter('approaching')}
          className={`flex-1 py-2 text-center rounded-lg transition-all ${
            productFilter === 'approaching'
              ? 'bg-white dark:bg-[#1A2B26] text-amber-600 dark:text-amber-400 shadow-sm'
              : 'hover:text-[#19332D] dark:hover:text-[#F0F4EC]'
          }`}
        >
          {t('approaching')}
        </button>
        <button
          onClick={() => setProductFilter('expired')}
          className={`flex-1 py-2 text-center rounded-lg transition-all ${
            productFilter === 'expired'
              ? 'bg-white dark:bg-[#1A2B26] text-rose-600 dark:text-rose-400 shadow-sm'
              : 'hover:text-[#19332D] dark:hover:text-[#F0F4EC]'
          }`}
        >
          {t('expired')}
        </button>
      </div>

      {/* Categories Horizontal Carousel */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none -mx-1 px-1">
        {CATEGORIES.map((cat) => {
          const isSelected = selectedCategory === cat.id;
          return (
            <button
              key={cat.id}
              onClick={() => setSelectedCategory(cat.id)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold shrink-0 transition-all ${
                isSelected
                  ? 'bg-[#267968] text-white dark:bg-[#70CDB0] dark:text-[#0D2623] shadow-sm'
                  : 'bg-white dark:bg-[#1A2B26] text-[#5A6B63] dark:text-[#B5C5BC] border border-[#E1E8E3] dark:border-[#344A40]'
              }`}
            >
              <CategoryIcon category={cat.id} size={14} />
              <span>{t(cat.nameKey)}</span>
            </button>
          );
        })}
      </div>

      {/* Sort & Multi-Select Toolbar */}
      <div className="flex items-center justify-between text-xs text-[#5A6B63] dark:text-[#B5C5BC] pt-1">
        <div className="flex items-center gap-2">
          <SlidersHorizontal className="w-3.5 h-3.5" />
          <span className="font-semibold">{t('products_sort_date')}:</span>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="bg-transparent font-bold text-[#19332D] dark:text-[#F0F4EC] focus:outline-none cursor-pointer"
          >
            <option value="date" className="dark:bg-[#1A2B26]">{t('products_sort_date')}</option>
            <option value="name" className="dark:bg-[#1A2B26]">{t('products_sort_name')}</option>
            <option value="category" className="dark:bg-[#1A2B26]">{t('products_sort_category')}</option>
            <option value="added" className="dark:bg-[#1A2B26]">{t('products_sort_added')}</option>
          </select>
        </div>

        <button
          onClick={() => {
            setIsSelectMode(!isSelectMode);
            setSelectedIds([]);
          }}
          className={`font-bold px-2 py-1 rounded-lg transition-colors ${
            isSelectMode
              ? 'bg-[#267968]/20 text-[#267968] dark:text-[#70CDB0]'
              : 'hover:text-[#19332D] dark:hover:text-[#F0F4EC]'
          }`}
        >
          {isSelectMode ? t('cancel') : t('select')}
        </button>
      </div>

      {/* Bulk Action Toolbar if Select Mode is Active */}
      {isSelectMode && (
        <div className="bg-[#19332D] dark:bg-[#0D2623] text-white p-3 rounded-2xl shadow-lg flex items-center justify-between gap-2 animate-in fade-in">
          <button onClick={selectAll} className="text-xs font-bold hover:underline">
            {selectedIds.length === filteredProducts.length ? t('products_clear') : t('all')} (
            {selectedIds.length}/{filteredProducts.length})
          </button>

          <div className="flex items-center gap-1.5">
            <button
              disabled={selectedIds.length === 0}
              onClick={() => setShowBulkCategoryModal(true)}
              className="p-2 bg-white/10 hover:bg-white/20 rounded-xl disabled:opacity-40 transition-colors"
              title={t('product_bulk_category_title')}
            >
              <FolderEdit className="w-4 h-4" />
            </button>
            <button
              disabled={selectedIds.length === 0}
              onClick={handleBulkShare}
              className="p-2 bg-white/10 hover:bg-white/20 rounded-xl disabled:opacity-40 transition-colors"
              title={t('share')}
            >
              <Share2 className="w-4 h-4" />
            </button>
            <button
              disabled={selectedIds.length === 0}
              onClick={handleBulkDelete}
              className="p-2 bg-rose-500 hover:bg-rose-600 rounded-xl disabled:opacity-40 transition-colors"
              title={t('delete')}
            >
              <Trash2 className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {/* Products List */}
      {filteredProducts.length === 0 ? (
        <div className="bg-white dark:bg-[#1A2B26] p-10 rounded-3xl text-center border border-[#E1E8E3] dark:border-[#344A40] mt-6">
          <div className="w-14 h-14 bg-[#EDF2EE] dark:bg-[#253A33] rounded-2xl flex items-center justify-center mx-auto mb-3 text-[#5A6B63] dark:text-[#B5C5BC]">
            <Search className="w-7 h-7" />
          </div>
          <h4 className="text-base font-bold text-[#19332D] dark:text-[#F0F4EC] mb-1">
            {searchQuery ? t('empty_search') : t('product_filter_empty')}
          </h4>
          <p className="text-xs text-[#5A6B63] dark:text-[#B5C5BC] max-w-xs mx-auto mb-4">
            {searchQuery ? t('product_search_empty') : t('product_first_add_description')}
          </p>
          <button
            onClick={() => {
              setSearchQuery('');
              setProductFilter('all');
              setSelectedCategory('all');
              setActiveTab('add');
            }}
            className="inline-flex items-center gap-2 px-4 py-2 bg-[#267968] dark:bg-[#70CDB0] text-white dark:text-[#0D2623] text-xs font-bold rounded-xl shadow"
          >
            <Plus className="w-4 h-4" />
            <span>{t('product_first_add_action')}</span>
          </button>
        </div>
      ) : (
        <div className="space-y-2.5">
          {filteredProducts.map((product) => {
            const catObj = CATEGORIES.find((c) => c.id === product.category) || CATEGORIES[CATEGORIES.length - 1];
            const isSelected = selectedIds.includes(product.id);

            return (
              <div
                key={product.id}
                onClick={() => {
                  if (isSelectMode) {
                    toggleSelect(product.id);
                  } else {
                    setViewProductDetail(product);
                  }
                }}
                className={`bg-white dark:bg-[#1A2B26] p-3.5 rounded-2xl border transition-all cursor-pointer flex items-center justify-between gap-3 ${
                  isSelected
                    ? 'border-[#267968] dark:border-[#70CDB0] bg-teal-50/40 dark:bg-teal-950/20 shadow-sm'
                    : 'border-[#E1E8E3] dark:border-[#344A40] hover:border-[#267968]/50 shadow-sm'
                }`}
              >
                <div className="flex items-center gap-3 min-w-0">
                  {/* Select Checkbox if active */}
                  {isSelectMode && (
                    <div
                      className={`w-5 h-5 rounded-md border flex items-center justify-center shrink-0 transition-colors ${
                        isSelected
                          ? 'bg-[#267968] border-[#267968] text-white'
                          : 'border-[#5A6B63]'
                      }`}
                    >
                      {isSelected && <Check className="w-3.5 h-3.5" />}
                    </div>
                  )}

                  {/* Thumbnail / Category Icon */}
                  <div
                    className="w-12 h-12 rounded-xl flex items-center justify-center text-white shrink-0 shadow-sm overflow-hidden"
                    style={{ backgroundColor: catObj.color }}
                  >
                    {product.imageUri ? (
                      <img
                        src={product.imageUri}
                        alt={product.name}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <CategoryIcon category={product.category} size={22} />
                    )}
                  </div>

                  {/* Info */}
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <h4 className="text-sm font-bold text-[#19332D] dark:text-[#F0F4EC] truncate">
                        {product.name}
                      </h4>
                      {product.quantity && (
                        <span className="text-[11px] font-medium text-[#5A6B63] dark:text-[#B5C5BC] bg-[#EDF2EE] dark:bg-[#253A33] px-1.5 py-0.5 rounded">
                          {product.quantity}
                        </span>
                      )}
                    </div>
                    <div className="flex items-center gap-2 mt-0.5 text-xs text-[#5A6B63] dark:text-[#B5C5BC]">
                      <span>{formatDate(product.expDate, locale)}</span>
                      <span>•</span>
                      <span>{t(catObj.nameKey)}</span>
                      {product.productCode && (
                        <>
                          <span>•</span>
                          <span className="font-mono text-[11px]">{product.productCode}</span>
                        </>
                      )}
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2 shrink-0">
                  {getStatusBadge(product.daysLeft)}
                  {!isSelectMode && (
                    <ChevronRight className="w-4 h-4 text-[#5A6B63] dark:text-[#B5C5BC]" />
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Bulk Change Category Modal */}
      {showBulkCategoryModal && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#1A2B26] max-w-sm w-full p-5 rounded-3xl shadow-2xl border border-[#E1E8E3] dark:border-[#344A40] space-y-4">
            <h3 className="text-base font-bold text-[#19332D] dark:text-[#F0F4EC]">
              {t('product_bulk_category_title')}
            </h3>
            <p className="text-xs text-[#5A6B63] dark:text-[#B5C5BC]">
              {t('product_bulk_category_message', { count: selectedIds.length })}
            </p>

            <div className="grid grid-cols-2 gap-2 max-h-60 overflow-y-auto">
              {CATEGORIES.filter((c) => c.id !== 'all').map((cat) => (
                <button
                  key={cat.id}
                  onClick={() => {
                    bulkUpdateCategory(selectedIds, cat.id);
                    setShowBulkCategoryModal(false);
                    setSelectedIds([]);
                    setIsSelectMode(false);
                  }}
                  className="flex items-center gap-2 p-2.5 rounded-xl border border-[#E1E8E3] dark:border-[#344A40] hover:border-[#267968] text-xs font-semibold text-left dark:text-[#F0F4EC] transition-colors"
                >
                  <div
                    className="w-6 h-6 rounded-lg flex items-center justify-center text-white shrink-0"
                    style={{ backgroundColor: cat.color }}
                  >
                    <CategoryIcon category={cat.id} size={14} />
                  </div>
                  <span className="truncate">{t(cat.nameKey)}</span>
                </button>
              ))}
            </div>

            <button
              onClick={() => setShowBulkCategoryModal(false)}
              className="w-full py-2.5 text-xs font-bold text-[#5A6B63] dark:text-[#B5C5BC] hover:bg-[#EDF2EE] dark:hover:bg-[#253A33] rounded-xl transition-colors"
            >
              {t('cancel')}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
