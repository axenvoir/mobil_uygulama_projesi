import React, { useState, useContext, useMemo } from 'react';
import { View, Text, StyleSheet, TextInput, TouchableOpacity, ScrollView, Image, Keyboard, KeyboardAvoidingView, Platform, TouchableWithoutFeedback } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import { AppContext } from '../context/AppContext';
import { CATEGORIES, SHADOWS, TYPOGRAPHY } from '../theme';
import DatePickerModal from '../components/DatePickerModal';
import { notifySuccess, notifyError } from '../utils/haptics';
import { addProductToCalendar } from '../utils/calendarUtils';
import { isValidDateString, isValidQuantity, sanitizeInput } from '../utils/validators';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const NAME_CATEGORY_MAP = {
  'süt': 'Süt Ürünleri', 'yoğurt': 'Süt Ürünleri', 'peynir': 'Süt Ürünleri', 'kefir': 'Süt Ürünleri',
  'tereyağı': 'Süt Ürünleri', 'kaymak': 'Süt Ürünleri', 'ayran': 'Süt Ürünleri',
  'et': 'Et & Tavuk', 'tavuk': 'Et & Tavuk', 'kıyma': 'Et & Tavuk', 'balık': 'Et & Tavuk',
  'sucuk': 'Et & Tavuk', 'salam': 'Et & Tavuk', 'sosis': 'Et & Tavuk', 'hindi': 'Et & Tavuk',
  'yumurta': 'Kahvaltılık', 'zeytin': 'Kahvaltılık', 'bal': 'Kahvaltılık', 'reçel': 'Kahvaltılık',
  'tahin': 'Kahvaltılık', 'pekmez': 'Kahvaltılık', 'makarna': 'Bakliyat', 'pirinç': 'Bakliyat', 'bulgur': 'Bakliyat', 'mercimek': 'Bakliyat',
  'nohut': 'Bakliyat', 'fasulye': 'Bakliyat', 'un': 'Bakliyat', 'şeker': 'Bakliyat', 'tuz': 'Bakliyat',
  'domates': 'Sebze & Meyve', 'salatalık': 'Sebze & Meyve', 'biber': 'Sebze & Meyve',
  'patates': 'Sebze & Meyve', 'soğan': 'Sebze & Meyve', 'elma': 'Sebze & Meyve',
  'muz': 'Sebze & Meyve', 'portakal': 'Sebze & Meyve', 'limon': 'Sebze & Meyve', 'marul': 'Sebze & Meyve',
  'su': 'İçecek', 'meyve suyu': 'İçecek', 'kola': 'İçecek', 'çay': 'İçecek', 'kahve': 'İçecek', 'soda': 'İçecek',
  'dondurma': 'Dondurulmuş', 'pizza': 'Dondurulmuş', 'mısır': 'Dondurulmuş',
  'cips': 'Atıştırmalık', 'çikolata': 'Atıştırmalık', 'bisküvi': 'Atıştırmalık', 'kek': 'Atıştırmalık',
  'kraker': 'Atıştırmalık', 'kuruyemiş': 'Atıştırmalık', 'fındık': 'Atıştırmalık', 'leblebi': 'Atıştırmalık',
};

const guessCategory = (name) => {
  if (!name) return null;
  const lower = name.toLowerCase();
  for (const key of Object.keys(NAME_CATEGORY_MAP)) {
    if (lower.includes(key)) return NAME_CATEGORY_MAP[key];
  }
  return null;
};

const getDefaultExpiry = () => {
  const d = new Date();
  d.setDate(d.getDate() + 7);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

export default function AddProductScreen({ navigation }) {
  const { addProduct, showToast, settings, products } = useContext(AppContext);
  const { colors } = useTheme();
  const responsive = useResponsive();
  const { isTablet, horizontalPadding, contentMaxWidth, scale } = responsive;
  const insets = useSafeAreaInsets();
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [name, setName] = useState('');
  const [productCode, setProductCode] = useState('');
  const [category, setCategory] = useState('');
  const [expDate, setExpDate] = useState('');
  const [quantity, setQuantity] = useState('');
  const [note, setNote] = useState('');
  const [imageUri, setImageUri] = useState(null);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [showDetails, setShowDetails] = useState(false);
  const [addToCalendar, setAddToCalendar] = useState(false);
  const [showSuggestions, setShowSuggestions] = useState(false);

  const suggestions = useMemo(() => {
    if (!name || name.length < 2) return [];
    const lower = name.toLowerCase();
    const seen = new Set();
    const result = [];
    for (const p of products) {
      if (seen.has(p.name.toLowerCase())) continue;
      if (!p.name.toLowerCase().includes(lower)) continue;
      seen.add(p.name.toLowerCase());
      result.push(p);
      if (result.length >= 5) break;
    }
    return result;
  }, [name, products]);

  const applySuggestion = (product) => {
    setName(product.name);
    if (product.productCode) setProductCode(product.productCode);
    if (product.category) setCategory(product.category);
    if (product.imageUri) setImageUri(product.imageUri);
    setShowSuggestions(false);
    Keyboard.dismiss();
    showToast(`"${product.name}" bilgileri dolduruldu`, 'info');
  };

  const handleNameChange = (text) => {
    setName(text);
    setShowSuggestions(text.length >= 2);
    if (!category) {
      const guess = guessCategory(text);
      if (guess) setCategory(guess);
    }
  };

  const pickImage = async (fromCamera = false) => {
    try {
      if (fromCamera) {
        const { status } = await ImagePicker.requestCameraPermissionsAsync();
        if (status !== 'granted') { showToast('Kamera izni gerekli.', 'error'); return; }
        const result = await ImagePicker.launchCameraAsync({ allowsEditing: true, aspect: [1, 1], quality: 0.7 });
        if (!result.canceled) setImageUri(result.assets[0].uri);
      } else {
        if (Platform.OS === 'ios') {
          const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
          if (status !== 'granted') { showToast('Galeri izni gerekli.', 'error'); return; }
        }
        const result = await ImagePicker.launchImageLibraryAsync({ mediaTypes: ImagePicker.MediaTypeOptions.Images, allowsEditing: true, aspect: [1, 1], quality: 0.7 });
        if (!result.canceled) setImageUri(result.assets[0].uri);
      }
    } catch (error) { console.error('Ürün fotoğrafı yüklenemedi:', error); showToast('Fotoğraf yüklenemedi.', 'error'); }
  };

  const saveProduct = async (finalDate) => {
    const newProduct = {
      name: sanitizeInput(name),
      productCode: sanitizeInput(productCode),
      category: category || 'Diğer',
      expDate: finalDate,
      quantity: sanitizeInput(quantity),
      note: sanitizeInput(note),
      imageUri
    };

    addProduct(newProduct);

    if (addToCalendar) {
      const res = await addProductToCalendar(newProduct);
      if (res.success) showToast('Ürün eklendi ve takvime kaydedildi!', 'success');
      else showToast('Ürün eklendi ancak takvime eklenemedi.', 'warning');
    } else {
      if (settings.hapticsEnabled) await notifySuccess();
      showToast('Ürün başarıyla eklendi!', 'success');
    }
    navigation.goBack();
  };

  const handleSave = async () => {
    if (!name.trim()) { if (settings.hapticsEnabled) await notifyError(); showToast('Ürün adı boş bırakılamaz.', 'error'); return; }
    if (quantity && !isValidQuantity(quantity)) { if (settings.hapticsEnabled) await notifyError(); showToast('Lütfen geçerli bir miktar girin (0-10000 arası).', 'error'); return; }
    const finalDate = expDate || getDefaultExpiry();
    if (!isValidDateString(finalDate)) { if (settings.hapticsEnabled) await notifyError(); showToast('Geçersiz bir tarih girdiniz.', 'error'); return; }
    if (!expDate) showToast('SKT otomatik +7 gün atandı.', 'info');
    await saveProduct(finalDate);
  };

  const photoSize = scale(isTablet ? 200 : 160);

  return (
    <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={styles.container}>
      <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
        <ScrollView style={styles.scroll} contentContainerStyle={styles.scrollContent} keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
          <View style={styles.innerWrap}>
            <View style={styles.header}>
              <TouchableOpacity onPress={() => navigation.goBack()} activeOpacity={0.7}>
                <Ionicons name="arrow-back" size={scale(24)} color={colors.text} />
              </TouchableOpacity>
              <Text style={styles.headerTitle}>Yeni Ürün</Text>
              <View style={{ width: scale(24) }} />
            </View>

            <View style={styles.photoSection}>
              {imageUri ? (
                <View style={styles.photoWrapper}>
                  <Image source={{ uri: imageUri }} style={[styles.photo, { width: photoSize, height: photoSize }]} />
                  <TouchableOpacity style={[styles.removePhotoBtn, { backgroundColor: colors.danger }]} onPress={() => setImageUri(null)} activeOpacity={0.8}>
                    <Ionicons name="close" size={scale(18)} color={colors.onDanger} />
                  </TouchableOpacity>
                </View>
              ) : (
                <TouchableOpacity style={[styles.photoPlaceholder, { width: photoSize, height: photoSize }]} onPress={() => pickImage(true)} activeOpacity={0.8}>
                  <Ionicons name="camera" size={scale(48)} color={colors.primary} />
                  <Text style={styles.photoPlaceholderText}>Fotoğraf Çek</Text>
                  <Text style={styles.photoPlaceholderSub}>veya galeriden seç</Text>
                </TouchableOpacity>
              )}
              {imageUri ? (
                <View style={styles.photoButtonsRow}>
                  <TouchableOpacity style={styles.photoBtn} onPress={() => pickImage(true)} activeOpacity={0.7}>
                    <Ionicons name="camera-outline" size={scale(16)} color={colors.primary} />
                    <Text style={styles.photoBtnText}>Yeniden Çek</Text>
                  </TouchableOpacity>
                  <TouchableOpacity style={styles.photoBtn} onPress={() => pickImage(false)} activeOpacity={0.7}>
                    <Ionicons name="images-outline" size={scale(16)} color={colors.primary} />
                    <Text style={styles.photoBtnText}>Galeri</Text>
                  </TouchableOpacity>
                </View>
              ) : (
                <TouchableOpacity style={styles.galleryBtn} onPress={() => pickImage(false)} activeOpacity={0.7}>
                  <Ionicons name="images-outline" size={scale(16)} color={colors.primary} />
                  <Text style={styles.galleryBtnText}>Galeriden Seç</Text>
                </TouchableOpacity>
              )}
            </View>

            <Text style={styles.label}>Ürün Adı *</Text>
            <View style={styles.inputContainer}>
              <TextInput style={styles.input} placeholder="Örn: Süt, Yumurta, Ekmek" placeholderTextColor={colors.textLight} value={name} onChangeText={handleNameChange} onBlur={() => setTimeout(() => setShowSuggestions(false), 200)} />
              <Ionicons name="pricetag-outline" size={scale(20)} color={colors.textLight} />
            </View>

            {showSuggestions && suggestions.length > 0 && (
              <View style={styles.suggestionsBox}>
                <View style={styles.suggestionsHeader}>
                  <Ionicons name="sparkles" size={scale(14)} color={colors.primary} />
                  <Text style={styles.suggestionsTitle}>Daha önce ekledin</Text>
                </View>
                {suggestions.map((p) => (
                  <TouchableOpacity key={p.id} style={styles.suggestionItem} onPress={() => applySuggestion(p)} activeOpacity={0.7}>
                    {p.imageUri ? ( <Image source={{ uri: p.imageUri }} style={styles.suggestionImage} /> ) : ( <View style={styles.suggestionIcon}><Ionicons name="cube-outline" size={scale(18)} color={colors.primary} /></View> )}
                    <View style={{ flex: 1, marginLeft: scale(10), marginRight: scale(8) }}>
                      <Text style={styles.suggestionName} numberOfLines={1}>{p.name}</Text>
                      <Text style={styles.suggestionSub} numberOfLines={1}>{p.category}{p.productCode ? ` • ${p.productCode}` : ''}</Text>
                    </View>
                    <Ionicons name="add-circle-outline" size={scale(22)} color={colors.primary} />
                  </TouchableOpacity>
                ))}
              </View>
            )}

            <Text style={styles.label}>Kısa Kod (isteğe bağlı)</Text>
            <View style={styles.inputContainer}>
              <TextInput style={styles.input} placeholder="Örn: SUT-01" placeholderTextColor={colors.textLight} value={productCode} onChangeText={setProductCode} autoCapitalize="characters" />
              <Ionicons name="pricetags-outline" size={scale(20)} color={colors.textLight} />
            </View>

            {category ? ( <View style={styles.autoRow}><Ionicons name="sparkles" size={scale(16)} color={colors.primary} /><Text style={styles.autoText} numberOfLines={1}>Kategori: <Text style={{ fontWeight: '800' }}>{category}</Text></Text><TouchableOpacity onPress={() => setShowDetails(true)}><Text style={styles.autoChange}>Değiştir</Text></TouchableOpacity></View> ) : null}
            {expDate ? ( <View style={styles.autoRow}><Ionicons name="calendar" size={scale(16)} color={colors.primary} /><Text style={styles.autoText} numberOfLines={1}>SKT: <Text style={{ fontWeight: '800' }}>{expDate}</Text></Text><TouchableOpacity onPress={() => setShowDatePicker(true)}><Text style={styles.autoChange}>Değiştir</Text></TouchableOpacity></View> ) : null}

            <TouchableOpacity style={styles.detailsToggle} onPress={() => setShowDetails(!showDetails)} activeOpacity={0.7}>
              <Ionicons name={showDetails ? 'chevron-up' : 'chevron-down'} size={scale(20)} color={colors.primary} />
              <Text style={styles.detailsToggleText}>{showDetails ? 'Detayları Gizle' : 'Diğer Detaylar'}</Text>
            </TouchableOpacity>

            {showDetails && (
              <View style={styles.detailsSection}>
                <Text style={styles.label}>Kategori</Text>
                <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.categoryScroll} contentContainerStyle={{ paddingRight: scale(20) }}>
                  {CATEGORIES.filter(c => c.id !== 'all').map((cat) => (
                    <TouchableOpacity key={cat.id} style={[styles.categoryChip, category === cat.name && { backgroundColor: colors.primary, borderColor: colors.primary }]} onPress={() => setCategory(cat.name)} activeOpacity={0.7}>
                      <Ionicons name={cat.icon} size={scale(14)} color={category === cat.name ? colors.onPrimary : colors.primary} />
                      <Text style={[styles.categoryText, category === cat.name && { color: colors.onPrimary }]}>{cat.name}</Text>
                    </TouchableOpacity>
                  ))}
                </ScrollView>

                <Text style={styles.label}>Son Kullanma Tarihi</Text>
                <TouchableOpacity style={styles.inputContainer} onPress={() => setShowDatePicker(true)} activeOpacity={0.7}>
                  <Text style={[styles.input, { color: expDate ? colors.text : colors.textLight }]}>{expDate || 'Tarih seçmek için dokun'}</Text>
                  <Ionicons name="calendar" size={scale(20)} color={colors.primary} />
                </TouchableOpacity>

                <Text style={styles.label}>Miktar</Text>
                <View style={styles.inputContainer}>
                  <TextInput style={styles.input} placeholder="Örn: 2" placeholderTextColor={colors.textLight} keyboardType="numeric" value={quantity} onChangeText={setQuantity} />
                  <Text style={styles.unitText}>adet</Text>
                </View>

                <Text style={styles.label}>Not</Text>
                <View style={[styles.inputContainer, { height: scale(80), alignItems: 'flex-start', paddingTop: scale(12) }]}>
                  <TextInput style={styles.input} placeholder="Not ekle..." placeholderTextColor={colors.textLight} multiline value={note} onChangeText={setNote} />
                </View>

                <TouchableOpacity style={styles.calendarToggle} onPress={() => setAddToCalendar(!addToCalendar)} activeOpacity={0.7}>
                  <View style={[styles.checkbox, addToCalendar && { backgroundColor: colors.primary, borderColor: colors.primary }]}>
                    {addToCalendar && <Ionicons name="checkmark" size={scale(14)} color={colors.onPrimary} />}
                  </View>
                  <Ionicons name="calendar-outline" size={scale(18)} color={colors.primary} style={{ marginLeft: scale(8) }} />
                  <Text style={styles.calendarToggleText} numberOfLines={1}>Takvime SKT hatırlatıcısı ekle</Text>
                </TouchableOpacity>
              </View>
            )}
            <View style={{ height: scale(120) }} />
          </View>
        </ScrollView>
      </TouchableWithoutFeedback>

      <View style={[styles.bottomBarWrap, { paddingBottom: Math.max(insets.bottom, scale(8)) }]}>
        <View style={[styles.bottomBar, { maxWidth: contentMaxWidth, paddingHorizontal: horizontalPadding }]}>
          <TouchableOpacity style={styles.saveButton} onPress={handleSave} activeOpacity={0.8}>
            <Ionicons name="checkmark-circle" size={scale(24)} color={colors.onPrimary} />
            <Text style={styles.saveButtonText}>Kaydet</Text>
          </TouchableOpacity>
        </View>
      </View>

      <DatePickerModal visible={showDatePicker} onClose={() => setShowDatePicker(false)} onSelect={(date) => setExpDate(date)} initialDate={expDate} />
    </KeyboardAvoidingView>
  );
}

const createStyles = (colors, responsive) => {
  const { horizontalPadding, contentMaxWidth, scale, moderateScale } = responsive;
  return StyleSheet.create({
    container: { flex: 1, backgroundColor: colors.background },
    scroll: { flex: 1 },
    scrollContent: { paddingBottom: scale(100), alignItems: 'center' },
    innerWrap: { width: '100%', maxWidth: contentMaxWidth, paddingHorizontal: horizontalPadding, paddingTop: scale(20) },
    header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: scale(24) },
    headerTitle: { fontSize: moderateScale(TYPOGRAPHY.h2, 0.3), fontWeight: '800', color: colors.text },
    photoSection: { alignItems: 'center', marginBottom: scale(24) },
    photoWrapper: { position: 'relative' },
    photo: { borderRadius: scale(24), ...SHADOWS.medium },
    removePhotoBtn: { position: 'absolute', top: scale(-8), right: scale(-8), width: scale(32), height: scale(32), borderRadius: scale(16), justifyContent: 'center', alignItems: 'center', ...SHADOWS.soft },
    photoPlaceholder: { borderRadius: scale(24), backgroundColor: colors.surface, justifyContent: 'center', alignItems: 'center', borderWidth: 2, borderColor: colors.primary, borderStyle: 'dashed' },
    photoPlaceholderText: { color: colors.primary, fontWeight: '800', marginTop: scale(8), fontSize: moderateScale(TYPOGRAPHY.caption, 0.3) },
    photoPlaceholderSub: { color: colors.textLight, fontSize: moderateScale(TYPOGRAPHY.small, 0.3), marginTop: scale(2), fontWeight: '500' },
    photoButtonsRow: { flexDirection: 'row', marginTop: scale(14) },
    photoBtn: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: scale(14), paddingVertical: scale(8), borderRadius: scale(16), backgroundColor: colors.surface, marginHorizontal: scale(6), ...SHADOWS.soft },
    photoBtnText: { color: colors.primary, fontSize: moderateScale(TYPOGRAPHY.small, 0.3), fontWeight: '800', marginLeft: scale(6) },
    galleryBtn: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: scale(16), paddingVertical: scale(10), borderRadius: scale(20), backgroundColor: colors.surface, marginTop: scale(14), ...SHADOWS.soft },
    galleryBtnText: { color: colors.primary, fontSize: moderateScale(TYPOGRAPHY.small, 0.3), fontWeight: '800', marginLeft: scale(8) },
    label: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), fontWeight: '800', color: colors.text, marginBottom: scale(8), marginTop: scale(4) },
    inputContainer: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, borderRadius: scale(16), paddingHorizontal: scale(16), height: scale(54), marginBottom: scale(16), borderWidth: 1, borderColor: colors.border },
    input: { flex: 1, color: colors.text, fontSize: moderateScale(TYPOGRAPHY.body, 0.3) },
    unitText: { color: colors.textLight, marginLeft: scale(8), fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), fontWeight: '600' },
    suggestionsBox: { backgroundColor: colors.surface, borderRadius: scale(16), padding: scale(14), marginBottom: scale(16), borderWidth: 1, borderColor: colors.primary + '40', ...SHADOWS.medium },
    suggestionsHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: scale(10) },
    suggestionsTitle: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.primary, fontWeight: '800', marginLeft: scale(6), letterSpacing: 0.3 },
    suggestionItem: { flexDirection: 'row', alignItems: 'center', paddingVertical: scale(10), borderTopWidth: 1, borderTopColor: colors.border },
    suggestionImage: { width: scale(40), height: scale(40), borderRadius: scale(10) },
    suggestionIcon: { width: scale(40), height: scale(40), borderRadius: scale(10), backgroundColor: colors.surfaceAlt, justifyContent: 'center', alignItems: 'center' },
    suggestionName: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text },
    suggestionSub: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: scale(2), fontWeight: '500' },
    autoRow: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.primary + '15', borderRadius: scale(12), paddingVertical: scale(12), paddingHorizontal: scale(14), marginBottom: scale(12) },
    autoText: { flex: 1, fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.text, marginLeft: scale(8), fontWeight: '600' },
    autoChange: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.primary, fontWeight: '800' },
    detailsToggle: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', paddingVertical: scale(14), marginTop: scale(4), marginBottom: scale(8) },
    detailsToggleText: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.primary, fontWeight: '800', marginLeft: scale(6) },
    detailsSection: { backgroundColor: colors.surfaceAlt, borderRadius: scale(20), padding: scale(20), marginBottom: scale(20) },
    categoryScroll: { marginBottom: scale(16), maxHeight: scale(42) },
    categoryChip: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, paddingHorizontal: scale(14), paddingVertical: scale(8), borderRadius: scale(20), marginRight: scale(10), borderWidth: 1.5, borderColor: colors.primary + '40' },
    categoryText: { color: colors.primary, marginLeft: scale(6), fontSize: moderateScale(TYPOGRAPHY.small, 0.3), fontWeight: '800' },
    calendarToggle: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, padding: scale(16), borderRadius: scale(16), marginTop: scale(8) },
    checkbox: { width: scale(22), height: scale(22), borderRadius: scale(6), borderWidth: 2, borderColor: colors.border, justifyContent: 'center', alignItems: 'center' },
    calendarToggleText: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.text, marginLeft: scale(10), flex: 1, fontWeight: '600' },
    bottomBarWrap: { position: 'absolute', left: 0, right: 0, bottom: 0, backgroundColor: colors.background, borderTopWidth: 1, borderTopColor: colors.border, alignItems: 'center' },
    bottomBar: { width: '100%', paddingVertical: scale(16) },
    saveButton: { flexDirection: 'row', backgroundColor: colors.primary, height: scale(60), borderRadius: scale(30), justifyContent: 'center', alignItems: 'center', ...SHADOWS.medium },
    saveButtonText: { color: colors.onPrimary, fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', marginLeft: scale(8) },
  });
};