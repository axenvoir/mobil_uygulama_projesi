import React, { useContext, useState, useRef, useMemo, useEffect, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, TextInput, ScrollView, Image, Animated, Keyboard, Modal } from 'react-native';
import { Swipeable } from 'react-native-gesture-handler';
import { Ionicons } from '@expo/vector-icons';
import * as Clipboard from 'expo-clipboard';
import * as SMS from 'expo-sms';
import { AppContext } from '../context/AppContext';
import { CATEGORIES, CATEGORY_TRANSLATION_KEYS, SHADOWS, TYPOGRAPHY } from '../theme';
import { shareProductList } from '../utils/shareUtils';
import { generateInventoryPDF } from '../utils/pdfExport';
import { notifySuccess } from '../utils/haptics';
import { getSearchHistory, addSearchTerm, removeSearchTerm, clearSearchHistory } from '../utils/searchHistory';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { useTranslation } from '../hooks/useTranslation';
import { formatDate, getDaysLeft } from '../utils/formatters';
import ConfirmModal from '../components/ConfirmModal';
import EmptyState from '../components/EmptyState';

export default function ProductsScreen({ route, navigation }) {
  const { products, deleteProduct, updateProduct, showToast, userProfile, settings, selectedCategory, setSelectedCategory } = useContext(AppContext);
  const { colors } = useTheme();
  const { t, locale } = useTranslation();
  const responsive = useResponsive();
  const { isTablet, isSmall, scale, itemColumns, itemGap } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [activeTab, setActiveTab] = useState('all');
  const [searchText, setSearchText] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [sortBy, setSortBy] = useState('date');
  const [selectionMode, setSelectionMode] = useState(false);
  const [selectedIds, setSelectedIds] = useState([]);
  const [searchHistory, setSearchHistory] = useState([]);
  const [showHistory, setShowHistory] = useState(false);
  const [showCategoryModal, setShowCategoryModal] = useState(false);

  // Onay Modal State'leri
  const [modalVisible, setModalVisible] = useState(false);
  const [modalConfig, setModalConfig] = useState({ type: 'warning', title: '', message: '', confirmText: 'Evet', onConfirm: null });

  const swipeRefs = useRef({});
  const searchRef = useRef(null);

  useEffect(() => {
    const timer = setTimeout(() => { setDebouncedSearch(searchText); }, 500);
    return () => clearTimeout(timer);
  }, [searchText]);

  /* eslint-disable react-hooks/set-state-in-effect */
  useEffect(() => {
    const filter = route.params?.filter;
    if (['all', 'today', 'approaching', 'expired'].includes(filter)) {
      setActiveTab(filter);
      navigation.setParams({ filter: undefined });
    }
  }, [navigation, route.params?.filter]);
  /* eslint-enable react-hooks/set-state-in-effect */

  const loadSearchHistory = useCallback(async () => {
    const h = await getSearchHistory();
    setSearchHistory(h);
  }, []);

  // Hydrate the search-history UI from local storage on first display.
  /* eslint-disable react-hooks/set-state-in-effect */
  useEffect(() => { loadSearchHistory(); }, [loadSearchHistory]);
  /* eslint-enable react-hooks/set-state-in-effect */

  const handleSearchFocus = () => { if (searchHistory.length > 0 && !searchText) setShowHistory(true); };
  const handleSearchBlur = () => { setTimeout(() => setShowHistory(false), 200); };
  const handleSearchSubmit = async () => {
    if (searchText.trim().length >= 2) {
      const updated = await addSearchTerm(searchText);
      setSearchHistory(updated);
    }
    Keyboard.dismiss();
    setShowHistory(false);
  };
  const handleHistoryItemPress = (term) => {
    setSearchText(term);
    setDebouncedSearch(term);
    setShowHistory(false);
    Keyboard.dismiss();
  };
  const handleRemoveHistoryItem = async (term) => {
    const updated = await removeSearchTerm(term);
    setSearchHistory(updated);
  };

  const openConfirm = (type, title, message, confirmText, onConfirm) => {
    setModalConfig({ type, title, message, confirmText, onConfirm });
    setModalVisible(true);
  };

  const handleClearHistory = () => {
    openConfirm('danger', 'Arama Geçmişi', 'Tüm arama geçmişi silinsin mi?', 'Sil', async () => {
      setModalVisible(false);
      const updated = await clearSearchHistory();
      setSearchHistory(updated);
      showToast('Geçmiş temizlendi.', 'info');
    });
  };

  const filteredProducts = products.filter(p => {
    const daysLeft = getDaysLeft(p.expDate);
    const matchesSearch = p.name.toLowerCase().includes(debouncedSearch.toLowerCase()) || (p.productCode && p.productCode.toLowerCase().includes(debouncedSearch.toLowerCase()));
    const matchesCategory = selectedCategory === 'Tümü' || p.category === selectedCategory;
    if (activeTab === 'today') return daysLeft === 0 && matchesSearch && matchesCategory;
    if (activeTab === 'approaching') return daysLeft >= 0 && daysLeft <= 7 && matchesSearch && matchesCategory;
    if (activeTab === 'expired') return daysLeft < 0 && matchesSearch && matchesCategory;
    return matchesSearch && matchesCategory;
  }).sort((a, b) => {
    if (sortBy === 'name') return a.name.localeCompare(b.name);
    if (sortBy === 'category') return (a.category || '').localeCompare(b.category || '');
    if (sortBy === 'added') return parseInt(b.id) - parseInt(a.id);
    return a.expDate.localeCompare(b.expDate);
  });

  const getStatus = (daysLeft) => {
    if (daysLeft < 0) return { text: t('status_expired'), color: colors.danger, bg: colors.danger + '15' };
    if (daysLeft <= 7) return { text: t('status_approaching'), color: colors.warning, bg: colors.warning + '15' };
    return { text: t('status_fresh'), color: colors.success, bg: colors.success + '15' };
  };

  // SİLME FONKSİYONU DÜZELTİLDİ
  const handleDelete = (item) => {
    if (swipeRefs.current[item.id]) {
      swipeRefs.current[item.id].close();
    }
    openConfirm(
      'danger',
      'Ürünü Sil',
      `"${item.name}" adlı ürünü silmek istediğine emin misin?`,
      'Sil',
      async () => {
        setModalVisible(false);
        await deleteProduct(item.id, { withUndo: true });
        if (settings.hapticsEnabled) await notifySuccess();
      }
    );
  };

  const handleBulkDelete = () => {
    if (selectedIds.length === 0) return;
    const count = selectedIds.length;
    openConfirm('danger', 'Toplu Sil', `${count} ürün silinsin mi? Bu işlem geri alınamaz.`, 'Sil', async () => {
      setModalVisible(false);
      selectedIds.forEach(id => deleteProduct(id));
      setSelectedIds([]);
      setSelectionMode(false);
      if (settings.hapticsEnabled) await notifySuccess();
      showToast(`${count} ürün silindi.`, 'info');
    });
  };

  const handleShare = async () => {
    if (products.length === 0) { showToast('Paylaşacak ürün yok.', 'error'); return; }
    const result = await shareProductList(products, userProfile.name);
    if (!result.success) showToast('Paylaşım başarısız.', 'error');
  };

  const handleShareSMS = async () => {
    if (products.length === 0) { showToast('Paylaşacak ürün yok.', 'error'); return; }
    const isAvailable = await SMS.isAvailableAsync();
    if (!isAvailable) { showToast('SMS bu cihazda kullanılamıyor.', 'error'); return; }
    const lines = products.map((p, i) => `${i + 1}. ${p.name} - ${t('expiry_date_short')}: ${formatDate(p.expDate, locale)}`).join('\n');
    try { await SMS.sendSMSAsync([], `🧊 Expira Envanteri\n${lines}`); } catch (error) { console.error('SMS gönderilemedi:', error); showToast('SMS gönderilemedi.', 'error'); }
  };

  const handleExportPDF = async () => {
    if (products.length === 0) { showToast('PDF için ürün ekle.', 'error'); return; }
    showToast('PDF oluşturuluyor...', 'info');
    const result = await generateInventoryPDF(products, userProfile.name);
    if (result.success) showToast('PDF hazır!', 'success');
  };

  const toggleSelection = (id) => {
    if (selectedIds.includes(id)) setSelectedIds(selectedIds.filter(x => x !== id));
    else setSelectedIds([...selectedIds, id]);
  };

  const handleBulkCopy = async () => {
    if (selectedIds.length === 0) return;
    const selected = products.filter(p => selectedIds.includes(p.id));
    const text = selected.map(p => `${p.name}${p.productCode ? ` [${p.productCode}]` : ''} - ${t('expiry_date_short')}: ${formatDate(p.expDate, locale)}`).join('\n');
    await Clipboard.setStringAsync(text);
    showToast(`${selectedIds.length} ürün kopyalandı!`, 'success');
    setSelectionMode(false);
    setSelectedIds([]);
  };

  const handleBulkCategoryChange = (newCategory) => {
    if (selectedIds.length === 0) { showToast('Önce ürün seç.', 'info'); return; }
    try {
      selectedIds.forEach(id => {
        const product = products.find(p => p.id === id);
        if (product) updateProduct(id, { ...product, category: newCategory });
      });
      showToast(`${selectedIds.length} ürünün kategorisi "${newCategory}" olarak güncellendi.`, 'success');
      setSelectionMode(false);
      setSelectedIds([]);
      setShowCategoryModal(false);
    } catch (error) { console.error('Toplu kategori güncellenemedi:', error); showToast('Kategori güncellenemedi.', 'error'); }
  };

  const openDetail = (item) => { navigation.navigate('ProductDetail', { productId: item.id }); };

  const renderRightActions = (progress, dragX, item) => {
    const trans = dragX.interpolate({ inputRange: [-100, 0], outputRange: [0, 100], extrapolate: 'clamp' });
    return (
      <TouchableOpacity style={[styles.swipeDelete, { backgroundColor: colors.danger }]} onPress={() => handleDelete(item)}>
        <Animated.View style={{ transform: [{ translateX: trans }], alignItems: 'center' }}>
          <Ionicons name="trash" size={scale(24)} color={colors.onDanger} />
          <Text style={styles.swipeDeleteText}>Sil</Text>
        </Animated.View>
      </TouchableOpacity>
    );
  };

  const renderLeftActions = (progress, dragX, item) => {
    const trans = dragX.interpolate({ inputRange: [0, 100], outputRange: [-100, 0], extrapolate: 'clamp' });
    return (
      <TouchableOpacity style={[styles.swipeEdit, { backgroundColor: colors.primary }]} onPress={() => { swipeRefs.current[item.id]?.close(); openDetail(item); }}>
        <Animated.View style={{ transform: [{ translateX: trans }], alignItems: 'center' }}>
          <Ionicons name="eye" size={scale(24)} color={colors.onPrimary} />
          <Text style={styles.swipeDeleteText}>Görüntüle</Text>
        </Animated.View>
      </TouchableOpacity>
    );
  };

  const renderEmpty = () => {
    if (products.length === 0) {
      return (
        <EmptyState
          icon="cube-outline"
          title="Henüz ürün eklemedin"
          description="Buzdolabındaki ürünleri ekleyerek son kullanma tarihlerini takip etmeye başla."
          actionText="İlk Ürünü Ekle"
          onAction={() => navigation.navigate('AddProduct')}
        />
      );
    }
    return (
      <EmptyState
        icon="search-outline"
        title="Sonuç bulunamadı"
        description={debouncedSearch ? `"${debouncedSearch}" için sonuç bulunamadı. Farklı bir kelime deneyin.` : 'Filtreyi değiştirerek tekrar deneyin.'}
      />
    );
  };

  const renderProduct = ({ item }) => {
    const daysLeft = getDaysLeft(item.expDate);
    const status = getStatus(daysLeft);
    const cat = CATEGORIES.find(c => c.name === item.category);
    const isSelected = selectedIds.includes(item.id);

    const cardContent = (
      <TouchableOpacity
        style={[styles.productCard, isSelected && { borderWidth: 2, borderColor: colors.primary }]}
        onPress={() => selectionMode ? toggleSelection(item.id) : openDetail(item)}
        onLongPress={() => { setSelectionMode(true); toggleSelection(item.id); }}
        activeOpacity={0.7}
      >
        <View style={[styles.colorStrip, { backgroundColor: status.color }]} />
        {selectionMode && (
          <View style={[styles.checkbox, isSelected && { backgroundColor: colors.primary, borderColor: colors.primary }]}>
            {isSelected && <Ionicons name="checkmark" size={scale(14)} color={colors.onPrimary} />}
          </View>
        )}
        {item.imageUri ? (
          <Image source={{ uri: item.imageUri }} style={styles.productImage} />
        ) : (
          <View style={[styles.productIconBox, { backgroundColor: status.bg }]}>
            <Ionicons name={cat?.icon || 'cube-outline'} size={scale(22)} color={status.color} />
          </View>
        )}
        <View style={styles.productInfo}>
          <Text numberOfLines={1} adjustsFontSizeToFit style={styles.productName}>{item.name}</Text>
          <View style={{ flexDirection: 'row', alignItems: 'center', marginTop: scale(4), flexWrap: 'nowrap' }}>
            {item.productCode ? (
              <View style={styles.codeBadge}>
                <Text style={styles.codeText} numberOfLines={1} adjustsFontSizeToFit>{item.productCode}</Text>
              </View>
            ) : null}
            <Text style={styles.productCategory} numberOfLines={1} adjustsFontSizeToFit>{t(CATEGORY_TRANSLATION_KEYS[cat?.id] || 'category_other')}</Text>
          </View>
        </View>
        <View style={styles.dateContainer}>
          <Text style={styles.dateText} numberOfLines={1} adjustsFontSizeToFit>{formatDate(item.expDate, locale)}</Text>
          <View style={[styles.statusBadge, { backgroundColor: status.bg }]}>
            <Text style={[styles.statusText, { color: status.color }]} numberOfLines={1} adjustsFontSizeToFit>{status.text}</Text>
          </View>
        </View>
        {!selectionMode && (
          <Ionicons name="chevron-forward" size={scale(18)} color={colors.textLight} style={{ marginLeft: scale(4) }} />
        )}
      </TouchableOpacity>
    );

    if (selectionMode || isTablet) return cardContent;

    return (
      <Swipeable
        ref={(ref) => { swipeRefs.current[item.id] = ref; }}
        renderRightActions={(progress, dragX) => renderRightActions(progress, dragX, item)}
        renderLeftActions={(progress, dragX) => renderLeftActions(progress, dragX, item)}
        overshootRight={false}
        overshootLeft={false}
        rightThreshold={40}
        leftThreshold={40}
      >
        {cardContent}
      </Swipeable>
    );
  };

  const sortLabels = {
    date: t('products_sort_date'),
    name: t('products_sort_name'),
    category: t('products_sort_category'),
    added: t('products_sort_added'),
  };
  const tabs = [
    { value: 'all', label: t('all') },
    { value: 'today', label: t('today') },
    { value: 'approaching', label: t('approaching') },
    { value: 'expired', label: t('expired') },
  ];

  return (
    <View style={styles.container}>
      <View style={styles.innerWrap}>
        <View style={styles.header}>
          <Text style={styles.headerTitle} numberOfLines={1} adjustsFontSizeToFit>
            {selectionMode ? t('products_selected', { count: selectedIds.length }) : t('my_products')}
          </Text>
          <View style={styles.headerActions}>
            {selectionMode ? (
              <>
                <TouchableOpacity style={styles.iconBtn} onPress={() => setShowCategoryModal(true)}>
                  <Ionicons name="folder-outline" size={scale(20)} color={colors.primary} />
                </TouchableOpacity>
                <TouchableOpacity style={styles.iconBtn} onPress={handleBulkCopy}>
                  <Ionicons name="copy-outline" size={scale(20)} color={colors.primary} />
                </TouchableOpacity>
                <TouchableOpacity style={styles.iconBtn} onPress={handleBulkDelete}>
                  <Ionicons name="trash-outline" size={scale(20)} color={colors.danger} />
                </TouchableOpacity>
                <TouchableOpacity style={styles.iconBtn} onPress={() => { setSelectionMode(false); setSelectedIds([]); }}>
                  <Ionicons name="close-outline" size={scale(22)} color={colors.text} />
                </TouchableOpacity>
              </>
            ) : (
              <>
                {!isSmall && (
                  <>
                    <TouchableOpacity style={styles.iconBtn} onPress={handleShareSMS}>
                      <Ionicons name="chatbubble-outline" size={scale(20)} color={colors.primary} />
                    </TouchableOpacity>
                    <TouchableOpacity style={styles.iconBtn} onPress={handleExportPDF}>
                      <Ionicons name="document-text-outline" size={scale(20)} color={colors.primary} />
                    </TouchableOpacity>
                  </>
                )}
                <TouchableOpacity style={styles.iconBtn} onPress={handleShare}>
                  <Ionicons name="share-social-outline" size={scale(20)} color={colors.primary} />
                </TouchableOpacity>
                <TouchableOpacity style={styles.addBtn} onPress={() => navigation.navigate('AddProduct')}>
                  <Ionicons name="add" size={scale(20)} color={colors.primary} />
                </TouchableOpacity>
              </>
            )}
          </View>
        </View>

        <View style={styles.searchWrapper}>
          <View style={styles.searchContainer}>
            <Ionicons name="search" size={scale(20)} color={colors.textLight} />
            <TextInput
              ref={searchRef}
              style={styles.searchInput}
              placeholder={t('products_search_placeholder')}
              placeholderTextColor={colors.textLight}
              value={searchText}
              onChangeText={setSearchText}
              onFocus={handleSearchFocus}
              onBlur={handleSearchBlur}
              onSubmitEditing={handleSearchSubmit}
              returnKeyType="search"
            />
            {searchText.length > 0 && (
              <TouchableOpacity onPress={() => { setSearchText(''); setDebouncedSearch(''); searchRef.current?.focus(); }}>
                <Ionicons name="close-circle" size={scale(20)} color={colors.textLight} />
              </TouchableOpacity>
            )}
          </View>

          {showHistory && searchHistory.length > 0 && (
            <View style={styles.historyDropdown}>
              <View style={styles.historyDropdownHeader}>
                <Text style={styles.historyDropdownTitle}>{t('products_recent_searches')}</Text>
                <TouchableOpacity onPress={handleClearHistory}>
                  <Text style={styles.historyClear}>{t('products_clear')}</Text>
                </TouchableOpacity>
              </View>
              {searchHistory.map((term, i) => (
                <TouchableOpacity key={i} style={styles.historyRow} onPress={() => handleHistoryItemPress(term)} activeOpacity={0.7}>
                  <Ionicons name="time-outline" size={scale(16)} color={colors.textLight} />
                  <Text style={styles.historyText} numberOfLines={1}>{term}</Text>
                  <TouchableOpacity onPress={() => handleRemoveHistoryItem(term)} hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}>
                    <Ionicons name="close" size={scale(16)} color={colors.textLight} />
                  </TouchableOpacity>
                </TouchableOpacity>
              ))}
            </View>
          )}
        </View>

        <View style={styles.tabContainer}>
          {tabs.map(tab => (
            <TouchableOpacity key={tab.value} style={[styles.tab, activeTab === tab.value && { borderBottomColor: colors.primary }]} onPress={() => setActiveTab(tab.value)}>
              <Text numberOfLines={1} adjustsFontSizeToFit style={[styles.tabText, activeTab === tab.value && { color: colors.primary }]}>{tab.label}</Text>
            </TouchableOpacity>
          ))}
        </View>

        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.categoryScroll} contentContainerStyle={{ paddingRight: scale(20), alignItems: 'center' }}>
          {CATEGORIES.map((cat) => (
            <TouchableOpacity key={cat.id} style={[styles.categoryChip, selectedCategory === cat.name && { backgroundColor: colors.primary, borderColor: colors.primary }]} onPress={() => setSelectedCategory(cat.name)} activeOpacity={0.7}>
              <Ionicons name={cat.icon} size={scale(13)} color={selectedCategory === cat.name ? colors.onPrimary : colors.primary} />
              <Text style={[styles.categoryText, selectedCategory === cat.name && { color: colors.onPrimary }]} numberOfLines={1} adjustsFontSizeToFit>{t(CATEGORY_TRANSLATION_KEYS[cat.id])}</Text>
            </TouchableOpacity>
          ))}
        </ScrollView>

        <View style={styles.sortRow}>
          <Text style={styles.resultCount}>{`${filteredProducts.length} ${t('products')}`}</Text>
          <View style={{ flexDirection: 'row' }}>
            <TouchableOpacity style={styles.sortBtn} onPress={() => { setSelectionMode(!selectionMode); setSelectedIds([]); }}>
              <Ionicons name="checkmark-done-outline" size={scale(16)} color={colors.primary} />
              <Text style={styles.sortText}>{selectionMode ? t('cancel') : t('select')}</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.sortBtn} onPress={() => {
                const order = ['date', 'name', 'category', 'added'];
                const next = order[(order.indexOf(sortBy) + 1) % order.length];
                setSortBy(next);
            }}>
              <Ionicons name="swap-vertical-outline" size={scale(16)} color={colors.primary} />
              <Text style={styles.sortText}>{sortLabels[sortBy]}</Text>
            </TouchableOpacity>
          </View>
        </View>

        <FlatList
          data={filteredProducts}
          keyExtractor={item => item.id}
          key={`columns-${itemColumns}`}
          numColumns={itemColumns}
          columnWrapperStyle={itemColumns > 1 ? { gap: itemGap } : undefined}
          contentContainerStyle={{ paddingBottom: scale(100), flexGrow: 1 }}
          ListEmptyComponent={renderEmpty}
          renderItem={renderProduct}
          keyboardShouldPersistTaps="handled"
        />
      </View>

      <Modal visible={showCategoryModal} transparent animationType="slide" onRequestClose={() => setShowCategoryModal(false)}>
        <View style={[styles.modalOverlay, { backgroundColor: 'rgba(0,0,0,0.7)' }]}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>Kategori Değiştir</Text>
              <TouchableOpacity onPress={() => setShowCategoryModal(false)}><Ionicons name="close" size={scale(24)} color={colors.text} /></TouchableOpacity>
            </View>
            <Text style={styles.modalDesc}>Seçili {selectedIds.length} ürünün kategorisini değiştir:</Text>
            <ScrollView style={{ maxHeight: scale(400) }}>
              {CATEGORIES.filter(c => c.id !== 'all').map((cat) => (
                <TouchableOpacity key={cat.id} style={styles.modalCategoryRow} onPress={() => handleBulkCategoryChange(cat.name)} activeOpacity={0.7}>
                  <View style={[styles.modalCategoryIcon, { backgroundColor: colors.primary + '15' }]}><Ionicons name={cat.icon} size={scale(20)} color={colors.primary} /></View>
                  <Text style={styles.modalCategoryText} numberOfLines={1}>{t(CATEGORY_TRANSLATION_KEYS[cat.id])}</Text>
                  <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} />
                </TouchableOpacity>
              ))}
            </ScrollView>
          </View>
        </View>
      </Modal>

      <ConfirmModal visible={modalVisible} type={modalConfig.type} title={modalConfig.title} message={modalConfig.message} confirmText={modalConfig.confirmText} onConfirm={modalConfig.onConfirm} onCancel={() => setModalVisible(false)} />
    </View>
  );
}

const createStyles = (colors, responsive) => {
  const { horizontalPadding, contentMaxWidth, scale, moderateScale, itemColumns } = responsive;
  return StyleSheet.create({
    container: { flex: 1, backgroundColor: colors.background, alignItems: 'center' },
    innerWrap: { flex: 1, width: '100%', maxWidth: contentMaxWidth, paddingHorizontal: horizontalPadding, paddingTop: scale(20) },
    header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: scale(20) },
    headerTitle: { fontSize: moderateScale(TYPOGRAPHY.h2, 0.3), fontWeight: '900', color: colors.text, flex: 1, marginRight: scale(8), letterSpacing: -0.5 },
    headerActions: { flexDirection: 'row', alignItems: 'center' },
    iconBtn: { width: scale(40), height: scale(40), borderRadius: scale(20), backgroundColor: colors.surface, justifyContent: 'center', alignItems: 'center', marginRight: scale(6), ...SHADOWS.soft },
    addBtn: { width: scale(40), height: scale(40), borderRadius: scale(20), borderWidth: 1.5, borderColor: colors.primary, justifyContent: 'center', alignItems: 'center' },
    searchWrapper: { position: 'relative', zIndex: 100, marginBottom: scale(16) },
    searchContainer: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, borderRadius: scale(16), paddingHorizontal: scale(14), height: scale(52), ...SHADOWS.soft },
    searchInput: { flex: 1, marginLeft: scale(8), color: colors.text, fontSize: moderateScale(TYPOGRAPHY.body, 0.3) },
    historyDropdown: { position: 'absolute', top: scale(58), left: 0, right: 0, backgroundColor: colors.surface, borderRadius: scale(16), padding: scale(10), ...SHADOWS.heavy, zIndex: 999 },
    historyDropdownHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: scale(8), paddingVertical: scale(8), borderBottomWidth: 1, borderBottomColor: colors.border, marginBottom: scale(6) },
    historyDropdownTitle: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), fontWeight: '800', color: colors.textLight, letterSpacing: 0.5 },
    historyClear: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.danger, fontWeight: '800' },
    historyRow: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: scale(8), paddingVertical: scale(12) },
    historyText: { flex: 1, fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.text, marginLeft: scale(10), marginRight: scale(8), fontWeight: '500' },
    tabContainer: { flexDirection: 'row', marginBottom: scale(16) },
    tab: { flex: 1, paddingVertical: scale(12), alignItems: 'center', borderBottomWidth: 2.5, borderBottomColor: 'transparent' },
    tabText: { color: colors.textLight, fontWeight: '700', fontSize: moderateScale(TYPOGRAPHY.caption, 0.3) },
    categoryScroll: { marginBottom: scale(16), maxHeight: scale(42), flexGrow: 0 },
    categoryChip: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, paddingHorizontal: scale(14), paddingVertical: scale(8), borderRadius: scale(20), marginRight: scale(10), borderWidth: 1.5, borderColor: colors.primary + '40', height: scale(38), ...SHADOWS.soft },
    categoryText: { color: colors.primary, marginLeft: scale(6), fontSize: moderateScale(TYPOGRAPHY.small, 0.3), fontWeight: '800', letterSpacing: 0.2 },
    sortRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: scale(16) },
    resultCount: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, fontWeight: '700' },
    sortBtn: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, paddingHorizontal: scale(12), paddingVertical: scale(7), borderRadius: scale(12), marginLeft: scale(8), ...SHADOWS.soft },
    sortText: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.primary, fontWeight: '800', marginLeft: scale(5) },
    productCard: { flexDirection: 'row', backgroundColor: colors.surface, borderRadius: scale(20), padding: scale(14), marginBottom: scale(12), alignItems: 'center', ...SHADOWS.medium, flex: itemColumns > 1 ? 1 : undefined },
    colorStrip: { width: scale(5), height: '80%', borderRadius: scale(3), position: 'absolute', left: 0, top: scale(14), bottom: scale(14) },
    checkbox: { width: scale(24), height: scale(24), borderRadius: scale(8), borderWidth: 2, borderColor: colors.border, justifyContent: 'center', alignItems: 'center', marginLeft: scale(8), marginRight: scale(4) },
    productImage: { width: scale(50), height: scale(50), borderRadius: scale(14), marginLeft: scale(8) },
    productIconBox: { width: scale(50), height: scale(50), borderRadius: scale(14), justifyContent: 'center', alignItems: 'center', marginLeft: scale(8) },
    productInfo: { flex: 1, marginLeft: scale(12), marginRight: scale(8), minWidth: 0 },
    productName: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text, textTransform: 'capitalize' },
    productCategory: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginLeft: scale(6), fontWeight: '500', flexShrink: 1 },
    codeBadge: { backgroundColor: colors.surfaceAlt, paddingHorizontal: scale(8), paddingVertical: scale(3), borderRadius: scale(8), flexShrink: 1 },
    codeText: { fontSize: moderateScale(10, 0.3), color: colors.primary, fontWeight: '800', letterSpacing: 0.5 },
    dateContainer: { alignItems: 'flex-end', marginRight: scale(8) },
    dateText: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginBottom: scale(6), fontWeight: '500' },
    statusBadge: { paddingHorizontal: scale(10), paddingVertical: scale(4), borderRadius: scale(10) },
    statusText: { fontSize: moderateScale(10, 0.3), fontWeight: '800' },
    modalOverlay: { flex: 1, justifyContent: 'flex-end' },
    modalContent: { backgroundColor: colors.surface, borderTopLeftRadius: scale(28), borderTopRightRadius: scale(28), padding: scale(24), maxHeight: '80%' },
    modalHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: scale(14) },
    modalTitle: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text },
    modalDesc: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, marginBottom: scale(16), fontWeight: '500' },
    modalCategoryRow: { flexDirection: 'row', alignItems: 'center', paddingVertical: scale(14), borderBottomWidth: 1, borderBottomColor: colors.border },
    modalCategoryIcon: { width: scale(44), height: scale(44), borderRadius: scale(22), justifyContent: 'center', alignItems: 'center', marginRight: scale(14) },
    modalCategoryText: { flex: 1, fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '700', color: colors.text },
    swipeDelete: { width: scale(88), justifyContent: 'center', alignItems: 'center', borderRadius: scale(20), marginBottom: scale(12), marginLeft: scale(8) },
    swipeEdit: { width: scale(88), justifyContent: 'center', alignItems: 'center', borderRadius: scale(20), marginBottom: scale(12), marginRight: scale(8) },
    swipeDeleteText: { color: colors.white, fontSize: moderateScale(TYPOGRAPHY.small, 0.3), fontWeight: '800', marginTop: scale(4) },
  });
};