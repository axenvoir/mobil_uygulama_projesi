import React, { useState, useContext, useMemo, useRef } from 'react';
import { View, Text, StyleSheet, ScrollView, Image, Keyboard, KeyboardAvoidingView, Platform, TouchableWithoutFeedback, TouchableOpacity, TextInput } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import { AppContext } from '../context/AppContext';
import { CATEGORIES, CATEGORY_TRANSLATION_KEYS, SHADOWS, TYPOGRAPHY } from '../theme';
import { Input } from '../components';
import DatePickerModal from '../components/DatePickerModal';
import { notifySuccess, notifyError } from '../utils/haptics';
import { addProductToCalendar } from '../utils/calendarUtils';
import { isValidDateString, isValidQuantity, sanitizeInput } from '../utils/validators';
import { persistProductImage } from '../utils/imagePersistence';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { useTranslation } from '../hooks/useTranslation';
import { formatDate } from '../utils/formatters';

const NAME_CATEGORY_MAP = {
  'sut': 'Dairy', 'yogurt': 'Dairy', 'peynir': 'Dairy', 'kefir': 'Dairy',
  'tereyagi': 'Dairy', 'kaymak': 'Dairy', 'ayran': 'Dairy',
  'et': 'Meat & Poultry', 'tavuk': 'Meat & Poultry', 'kima': 'Meat & Poultry', 'balik': 'Meat & Poultry',
  'sucuk': 'Et & Tavuk', 'salam': 'Et & Tavuk', 'sosis': 'Et & Tavuk', 'hindi': 'Et & Tavuk',
  'yumurta': 'Breakfast', 'zeytin': 'Breakfast', 'bal': 'Breakfast', 'recel': 'Breakfast',
  'tahin': 'Breakfast', 'pekmez': 'Breakfast', 'makarna': 'Legumes', 'pirinc': 'Legumes', 'bulgur': 'Legumes', 'mercimek': 'Legumes',
  'nohut': 'Legumes', 'fasulye': 'Legumes', 'un': 'Legumes', 'seker': 'Legumes', 'tuz': 'Legumes',
  'domates': 'Vegetables & Fruits', 'salatalik': 'Vegetables & Fruits', 'biber': 'Vegetables & Fruits',
  'patates': 'Vegetables & Fruits', 'sogan': 'Vegetables & Fruits', 'elma': 'Vegetables & Fruits',
  'muz': 'Sebze & Meyve', 'portakal': 'Sebze & Meyve', 'limon': 'Sebze & Meyve', 'marul': 'Sebze & Meyve',
  'su': 'Drinks', 'meyve_suyu': 'Drinks', 'kola': 'Drinks', 'cay': 'Drinks', 'kahve': 'Drinks', 'soda': 'Drinks',
  'dondurma': 'Frozen', 'pizza': 'Frozen', 'misir': 'Frozen',
  'cips': 'Snacks', 'cikolata': 'Snacks', 'biskuvi': 'Snacks', 'kek': 'Snacks',
  'kraker': 'Snacks', 'kuruyemis': 'Snacks', 'findik': 'Snacks', 'leblebi': 'Snacks',
};

const guessCategory = (name) => {
  if (!name) return null;
  const lower = name.toLowerCase();
  for (const key of Object.keys(NAME_CATEGORY_MAP)) {
    if (lower.includes(key)) return NAME_CATEGORY_MAP[key];
  }
  return null;
};

export default function AddProductScreen({ navigation }) {
  const { addProduct, showToast, settings, products } = useContext(AppContext);
  const { colors } = useTheme();
  const { t, locale } = useTranslation();
  const responsive = useResponsive();
  const { isTablet, horizontalPadding, contentMaxWidth, scale } = responsive;
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
  const [isSaving, setIsSaving] = useState(false);
  const savingRef = useRef(false);

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
    showToast(t('product_suggestion_filled', { name: product.name }), 'info');
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
      let pickedUri = null;
      if (fromCamera) {
        const { status } = await ImagePicker.requestCameraPermissionsAsync();
        if (status !== 'granted') { showToast(t('product_camera_permission'), 'error'); return; }
        const result = await ImagePicker.launchCameraAsync({ allowsEditing: true, aspect: [1, 1], quality: 0.7 });
        if (!result.canceled) pickedUri = result.assets[0].uri;
      } else {
        if (Platform.OS === 'ios') {
          const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
          if (status !== 'granted') { showToast(t('product_photo_permission'), 'error'); return; }
        }
        const result = await ImagePicker.launchImageLibraryAsync({ mediaTypes: ImagePicker.MediaTypeOptions.Images, allowsEditing: true, aspect: [1, 1], quality: 0.7 });
        if (!result.canceled) pickedUri = result.assets[0].uri;
      }

      if (!pickedUri) return;
      const persisted = await persistProductImage(pickedUri);
      if (!persisted) {
        showToast(t('product_photo_persist_failed'), 'error');
        return;
      }
      setImageUri(persisted);
    } catch (error) {
      if (__DEV__) console.error('Product photo could not be uploaded:', error);
      showToast(t('product_photo_failed'), 'error');
    }
  };

  const saveProduct = async (finalDate) => {
    if (savingRef.current) return;
    savingRef.current = true;
    setIsSaving(true);
    const newProduct = {
      name: sanitizeInput(name),
      productCode: sanitizeInput(productCode),
      category: category || 'Other',
      expDate: finalDate,
      quantity: sanitizeInput(quantity),
      note: sanitizeInput(note),
      imageUri
    };

    addProduct(newProduct);

    try {
      if (addToCalendar) {
        const res = await addProductToCalendar(newProduct);
        if (res.success) showToast(t('product_added_calendar'), 'success');
        else showToast(t('product_added_calendar_failed'), 'warning');
      } else {
        if (settings.hapticsEnabled) await notifySuccess();
        showToast(t('product_added'), 'success');
      }
      navigation.goBack();
    } catch (error) {
      if (__DEV__) console.error('Product could not be saved:', error);
      showToast(t('product_save_failed'), 'error');
      savingRef.current = false;
      setIsSaving(false);
    }
  };

  const handleSave = async () => {
    if (savingRef.current) return;
    if (!name.trim()) { if (settings.hapticsEnabled) await notifyError(); showToast(t('product_name_required'), 'error'); return; }
    if (quantity && !isValidQuantity(quantity)) { if (settings.hapticsEnabled) await notifyError(); showToast(t('product_quantity_invalid'), 'error'); return; }
    if (!expDate) { if (settings.hapticsEnabled) await notifyError(); showToast(t('product_expiry_required'), 'error'); return; }
    const finalDate = expDate;
    if (!isValidDateString(finalDate)) { if (settings.hapticsEnabled) await notifyError(); showToast(t('product_expiry_invalid'), 'error'); return; }
    await saveProduct(finalDate);
  };

  const photoSize = scale(isTablet ? 200 : 160);

  return (
    <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={styles.container}>
      <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
        <ScrollView style={styles.scroll} contentContainerStyle={styles.scrollContent} keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
          <View style={styles.innerWrap}>
            <View style={styles.header}>
              <TouchableOpacity onPress={() => navigation.goBack()} activeOpacity={0.7}>
                <Ionicons name="arrow-back" size={scale(24)} color={colors.text} />
              </TouchableOpacity>
              <Text style={styles.headerTitle}>{t('product_new_title')}</Text>
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
                  <Text style={styles.photoPlaceholderText}>{t('product_take_photo')}</Text>
                  <Text style={styles.photoPlaceholderSub}>{t('product_choose_gallery')}</Text>
                </TouchableOpacity>
              )}
              {imageUri ? (
                <View style={styles.photoButtonsRow}>
                  <TouchableOpacity style={styles.photoBtn} onPress={() => pickImage(true)} activeOpacity={0.7}>
                    <Ionicons name="camera-outline" size={scale(16)} color={colors.primary} />
                    <Text style={styles.photoBtnText}>{t('product_retake_photo')}</Text>
                  </TouchableOpacity>
                  <TouchableOpacity style={styles.photoBtn} onPress={() => pickImage(false)} activeOpacity={0.7}>
                    <Ionicons name="images-outline" size={scale(16)} color={colors.primary} />
                    <Text style={styles.photoBtnText}>{t('product_choose_gallery')}</Text>
                  </TouchableOpacity>
                </View>
              ) : (
                <TouchableOpacity style={styles.galleryBtn} onPress={() => pickImage(false)} activeOpacity={0.7}>
                  <Ionicons name="images-outline" size={scale(16)} color={colors.primary} />
                  <Text style={styles.galleryBtnText}>{t('product_choose_gallery')}</Text>
                </TouchableOpacity>
              )}
            </View>

            <Input
              label={t('product_name') + ' *'}
              placeholder={t('product_name_example')}
              value={name}
              onChangeText={handleNameChange}
              onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
              icon={<Ionicons name="pricetag-outline" size={scale(20)} color={colors.textLight} />}
              autoCapitalize="words"
              accessibilityLabel={t('product_name')}
            />

            {showSuggestions && suggestions.length > 0 && (
              <View style={styles.suggestionsBox}>
                <View style={styles.suggestionsHeader}>
                  <Ionicons name="sparkles" size={scale(14)} color={colors.primary} />
                  <Text style={styles.suggestionsTitle}>{t('product_suggestions')}</Text>
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

            <Input
              label={t('product_code_optional')}
              placeholder={t('product_code_example')}
              value={productCode}
              onChangeText={setProductCode}
              icon={<Ionicons name="pricetags-outline" size={scale(20)} color={colors.textLight} />}
              keyboardType="default"
              autoCapitalize="characters"
              accessibilityLabel={t('product_code')}
            />

            {category ? ( <View style={styles.autoRow}><Ionicons name="sparkles" size={scale(16)} color={colors.primary} /><Text style={styles.autoText} numberOfLines={1}>{t('product_category_auto')}: <Text style={{ fontWeight: '800' }}>{t(CATEGORY_TRANSLATION_KEYS[CATEGORIES.find(item => item.name === category)?.id] || 'category_other')}</Text></Text><TouchableOpacity accessibilityRole="button" onPress={() => setShowDetails(true)}><Text style={styles.autoChange}>{t('product_change')}</Text></TouchableOpacity></View> ) : null}
            <Text style={styles.label}>{t('expiry_date')} *</Text>
            <TouchableOpacity style={[styles.inputContainer, !expDate && styles.requiredDateInput]} onPress={() => setShowDatePicker(true)} activeOpacity={0.7}>
              <Ionicons name="calendar-outline" size={scale(20)} color={expDate ? colors.primary : colors.textLight} />
              <Text style={[styles.input, styles.dateInputText, { color: expDate ? colors.text : colors.textLight }]}>
                {expDate ? formatDate(expDate, locale) : t('product_expiry_select')}
              </Text>
              <Ionicons name="chevron-forward" size={scale(18)} color={colors.textLight} />
            </TouchableOpacity>
            <Text style={styles.helperText}>{t('product_expiry_helper')}</Text>

            <TouchableOpacity style={styles.detailsToggle} onPress={() => setShowDetails(!showDetails)} activeOpacity={0.7}>
              <Ionicons name={showDetails ? 'chevron-up' : 'chevron-down'} size={scale(20)} color={colors.primary} />
              <Text style={styles.detailsToggleText}>{t(showDetails ? 'product_hide_details' : 'product_more_details')}</Text>
            </TouchableOpacity>

            {showDetails && (
              <View style={styles.detailsSection}>
                <Text style={styles.label}>{t('product_category_label')}</Text>
                <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.categoryScroll} contentContainerStyle={{ paddingRight: scale(20) }}>
                  {CATEGORIES.filter(c => c.id !== 'all').map((cat) => (
                    <TouchableOpacity key={cat.id} style={[styles.categoryChip, category === cat.name && { backgroundColor: colors.primary, borderColor: colors.primary }]} onPress={() => setCategory(cat.name)} activeOpacity={0.7}>
                      <Ionicons name={cat.icon} size={scale(14)} color={category === cat.name ? colors.onPrimary : colors.primary} />
                      <Text style={[styles.categoryText, category === cat.name && { color: colors.onPrimary }]}>{t(CATEGORY_TRANSLATION_KEYS[cat.id])}</Text>
                    </TouchableOpacity>
                  ))}
                </ScrollView>

                <Text style={styles.label}>{t('quantity')}</Text>
                <View style={styles.inputContainer}>
                  <TextInput style={styles.input} accessibilityLabel={t('quantity')} placeholder="2" placeholderTextColor={colors.textLight} keyboardType="decimal-pad" value={quantity} onChangeText={setQuantity} />
                  <Text style={styles.unitText}>{t('product_quantity_unit')}</Text>
                </View>

                <Text style={styles.label}>{t('note')}</Text>
                <View style={[styles.inputContainer, { height: scale(80), alignItems: 'flex-start', paddingTop: scale(12) }]}>
                  <TextInput style={styles.input} accessibilityLabel={t('note')} placeholder={t('product_note_placeholder')} placeholderTextColor={colors.textLight} multiline value={note} onChangeText={setNote} />
                </View>

                <TouchableOpacity style={styles.calendarToggle} onPress={() => setAddToCalendar(!addToCalendar)} activeOpacity={0.7}>
                  <View style={[styles.checkbox, addToCalendar && { backgroundColor: colors.primary, borderColor: colors.primary }]}>
                    {addToCalendar && <Ionicons name="checkmark" size={scale(14)} color={colors.onPrimary} />}
                  </View>
                  <Ionicons name="calendar-outline" size={scale(18)} color={colors.primary} style={{ marginLeft: scale(8) }} />
                  <Text style={styles.calendarToggleText} numberOfLines={1}>{t('product_calendar_reminder')}</Text>
                </TouchableOpacity>
              </View>
            )}
            <View style={{ height: scale(120) }} />
          </View>
        </ScrollView>
      </TouchableWithoutFeedback>

      <View style={[styles.bottomBarWrap, { paddingBottom: scale(12) }]}>
        <View style={[styles.bottomBar, { maxWidth: contentMaxWidth, paddingHorizontal: horizontalPadding }]}>
          <TouchableOpacity style={[styles.saveButton, isSaving && { opacity: 0.65 }]} onPress={handleSave} disabled={isSaving} activeOpacity={0.8}>
            <Ionicons name="checkmark-circle" size={scale(24)} color={colors.onPrimary} />
            <Text style={styles.saveButtonText}>{isSaving ? t('saving') : t('save')}</Text>
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
    requiredDateInput: { borderColor: colors.warning, borderWidth: 1.5 },
    dateInputText: { marginLeft: scale(10) },
    helperText: { color: colors.textLight, fontSize: moderateScale(TYPOGRAPHY.small, 0.3), marginTop: scale(-8), marginBottom: scale(16) },
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