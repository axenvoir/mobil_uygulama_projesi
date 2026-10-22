import React, { useState, useContext, useMemo, useEffect } from 'react';
import { View, Text, StyleSheet, TextInput, TouchableOpacity, ScrollView, Alert, Image, KeyboardAvoidingView, Platform, Keyboard, TouchableWithoutFeedback } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import * as Clipboard from 'expo-clipboard';
import { AppContext } from '../context/AppContext';
import { CATEGORIES, CATEGORY_TRANSLATION_KEYS, SHADOWS, TYPOGRAPHY } from '../theme';
import DatePickerModal from '../components/DatePickerModal';
import { notifySuccess, notifyError } from '../utils/haptics';
import { shareSingleProduct } from '../utils/shareUtils';
import { addProductToCalendar } from '../utils/calendarUtils';
import { isValidDateString, isValidQuantity, sanitizeInput } from '../utils/validators';
import { persistProductImage } from '../utils/imagePersistence';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { useTranslation } from '../hooks/useTranslation';
import { formatDate } from '../utils/formatters';

export default function EditProductScreen({ route, navigation }) {
  const productId = route.params?.productId ?? route.params?.product?.id;
  const { products, updateProduct, deleteProduct, showToast, settings } = useContext(AppContext);
  const product = products.find((item) => item.id === productId) || route.params?.product || null;
  const { colors } = useTheme();
  const { t, locale } = useTranslation();
  const responsive = useResponsive();
  const { isTablet, scale } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [name, setName] = useState(product?.name || '');
  const [productCode, setProductCode] = useState(product?.productCode || '');
  const [category, setCategory] = useState(product?.category || '');
  const [expDate, setExpDate] = useState(product?.expDate || '');
  const [quantity, setQuantity] = useState(product?.quantity || '');
  const [note, setNote] = useState(product?.note || '');
  const [imageUri, setImageUri] = useState(product?.imageUri || null);
  const [showDatePicker, setShowDatePicker] = useState(false);

  useEffect(() => {
    if (!product) {
      showToast(t('product_not_found'), 'error');
      navigation.goBack();
    }
  }, [product, navigation, showToast, t]);

  /* eslint-disable react-hooks/set-state-in-effect */
  useEffect(() => {
    if (!product) return;
    setName(product.name || '');
    setProductCode(product.productCode || '');
    setCategory(product.category || '');
    setExpDate(product.expDate || '');
    setQuantity(product.quantity || '');
    setNote(product.note || '');
    setImageUri(product.imageUri || null);
  }, [product]);
  /* eslint-enable react-hooks/set-state-in-effect */

  if (!product) {
    return <View style={[styles.container, { backgroundColor: colors.background }]} />;
  }

  const handleUpdate = async () => {
    if (!name.trim()) { if (settings.hapticsEnabled) await notifyError(); showToast(t('product_name_required'), 'error'); return; }
    if (!isValidDateString(expDate)) { if (settings.hapticsEnabled) await notifyError(); showToast(t('product_expiry_invalid'), 'error'); return; }
    if (quantity && !isValidQuantity(quantity)) { if (settings.hapticsEnabled) await notifyError(); showToast(t('product_quantity_invalid'), 'error'); return; }

    updateProduct(product.id, {
      name: sanitizeInput(name),
      productCode: sanitizeInput(productCode),
      category: category || 'Diğer',
      expDate,
      quantity: sanitizeInput(quantity),
      note: sanitizeInput(note),
      imageUri,
    });

    if (settings.hapticsEnabled) await notifySuccess();
    showToast(t('product_updated'), 'success');
    navigation.goBack();
  };

  const handleDelete = () => {
    Alert.alert(t('product_delete_title'), t('product_delete_message', { name: product.name }), [
      { text: t('cancel'), style: 'cancel' },
      { text: t('delete'), style: 'destructive', onPress: () => {
          deleteProduct(product.id);
          showToast(t('product_deleted_count', { count: 1 }), 'info');
          navigation.goBack();
      }},
    ]);
  };

  const handleShare = async () => {
    const result = await shareSingleProduct({ name, productCode, category, expDate, quantity });
    if (!result.success) showToast(t('share_failed'), 'error');
  };

  const handleCopyCode = async () => {
    if (!productCode) { showToast(t('product_code_copy_empty'), 'info'); return; }
    await Clipboard.setStringAsync(productCode);
    showToast(t('product_code_copied'), 'success');
  };

  const handleAddToCalendar = async () => {
    const res = await addProductToCalendar({ name, productCode, expDate });
    if (res.success) showToast(t('product_added_calendar'), 'success');
    else showToast(t('product_added_calendar_failed'), 'warning');
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
      console.error('Ürün fotoğrafı yüklenemedi:', error);
      showToast(t('product_photo_failed'), 'error');
    }
  };

  const photoSize = scale(isTablet ? 180 : 140);

  return (
    <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={styles.container}>
      <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
        <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
          <View style={styles.innerWrap}>
            <View style={styles.header}>
              <TouchableOpacity onPress={() => navigation.goBack()} activeOpacity={0.7}>
                <Ionicons name="arrow-back" size={scale(24)} color={colors.text} />
              </TouchableOpacity>
              <Text style={styles.headerTitle}>{t('product_edit_title')}</Text>
              <View style={{ flexDirection: 'row' }}>
                <TouchableOpacity onPress={handleShare} style={{ marginRight: scale(16) }} activeOpacity={0.7}>
                  <Ionicons name="share-social-outline" size={scale(24)} color={colors.primary} />
                </TouchableOpacity>
                <TouchableOpacity onPress={handleDelete} activeOpacity={0.7}>
                  <Ionicons name="trash-outline" size={scale(24)} color={colors.danger} />
                </TouchableOpacity>
              </View>
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
                  <Ionicons name="camera" size={scale(40)} color={colors.primary} />
                  <Text style={styles.photoPlaceholderText}>{t('add_photo')}</Text>
                </TouchableOpacity>
              )}
              <View style={styles.photoButtons}>
                <TouchableOpacity style={styles.photoBtn} onPress={() => pickImage(true)} activeOpacity={0.7}>
                  <Ionicons name="camera-outline" size={scale(16)} color={colors.primary} />
                  <Text style={styles.photoBtnText}>{t('product_take_photo')}</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.photoBtn} onPress={() => pickImage(false)} activeOpacity={0.7}>
                  <Ionicons name="images-outline" size={scale(16)} color={colors.primary} />
                  <Text style={styles.photoBtnText}>{t('product_choose_gallery')}</Text>
                </TouchableOpacity>
              </View>
            </View>

            <Text style={styles.label}>{t('product_name')}</Text>
            <View style={styles.inputContainer}>
              <TextInput style={styles.input} accessibilityLabel={t('product_name')} value={name} onChangeText={setName} />
              <Ionicons name="pricetag-outline" size={scale(20)} color={colors.textLight} />
            </View>

            <Text style={styles.label}>{t('product_code')}</Text>
            <View style={styles.barcodeRow}>
              <View style={styles.barcodeInputWrapper}>
                <TextInput style={styles.barcodeInput} accessibilityLabel={t('product_code')} value={productCode} onChangeText={setProductCode} autoCapitalize="characters" />
                {productCode ? (
                  <TouchableOpacity onPress={handleCopyCode} style={{ padding: scale(4) }} activeOpacity={0.7}>
                    <Ionicons name="copy-outline" size={scale(18)} color={colors.primary} />
                  </TouchableOpacity>
                ) : null}
              </View>
            </View>

            <Text style={styles.label}>{t('product_category_label')}</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.categoryScroll} contentContainerStyle={{ paddingRight: scale(20) }}>
              {CATEGORIES.filter(c => c.id !== 'all').map((cat) => (
                <TouchableOpacity key={cat.id} style={[styles.categoryChip, category === cat.name && { backgroundColor: colors.primary, borderColor: colors.primary }]} onPress={() => setCategory(cat.name)} activeOpacity={0.7}>
                  <Ionicons name={cat.icon} size={scale(16)} color={category === cat.name ? colors.onPrimary : colors.primary} />
                  <Text style={[styles.categoryText, category === cat.name && { color: colors.onPrimary }]}>{t(CATEGORY_TRANSLATION_KEYS[cat.id])}</Text>
                </TouchableOpacity>
              ))}
            </ScrollView>

            <Text style={styles.label}>{t('expiry_date')}</Text>
            <TouchableOpacity style={styles.inputContainer} onPress={() => setShowDatePicker(true)} activeOpacity={0.7}>
              <Text style={[styles.input, { color: expDate ? colors.text : colors.textLight }]}>{expDate ? formatDate(expDate, locale) : t('product_expiry_select')}</Text>
              <Ionicons name="calendar" size={scale(20)} color={colors.primary} />
            </TouchableOpacity>

            <Text style={styles.label}>{t('quantity')}</Text>
            <View style={styles.inputContainer}>
              <TextInput style={styles.input} accessibilityLabel={t('quantity')} keyboardType="decimal-pad" value={quantity} onChangeText={setQuantity} />
              <Ionicons name="flask-outline" size={scale(20)} color={colors.textLight} />
              <Text style={styles.unitText}>{t('product_quantity_unit')}</Text>
            </View>

            <Text style={styles.label}>{t('note')}</Text>
            <View style={[styles.inputContainer, { height: scale(80), alignItems: 'flex-start', paddingTop: scale(12) }]}>
              <TextInput style={styles.input} accessibilityLabel={t('note')} multiline value={note} onChangeText={setNote} />
            </View>

            <TouchableOpacity style={styles.calendarBtn} onPress={handleAddToCalendar} activeOpacity={0.7}>
              <Ionicons name="calendar-outline" size={scale(20)} color={colors.primary} />
              <Text style={styles.calendarBtnText} numberOfLines={1}>{t('product_calendar_reminder')}</Text>
              <Ionicons name="chevron-forward" size={scale(18)} color={colors.textLight} />
            </TouchableOpacity>

            <TouchableOpacity style={styles.saveButton} onPress={handleUpdate} activeOpacity={0.8}>
              <Ionicons name="checkmark-circle-outline" size={scale(24)} color={colors.onPrimary} />
              <Text style={styles.saveButtonText}>{t('update')}</Text>
            </TouchableOpacity>

            <TouchableOpacity style={[styles.deleteButton, { backgroundColor: colors.danger + '15', borderColor: colors.danger + '30' }]} onPress={handleDelete} activeOpacity={0.8}>
              <Ionicons name="trash-outline" size={scale(20)} color={colors.danger} />
              <Text style={styles.deleteButtonText}>{t('delete')}</Text>
            </TouchableOpacity>
            <View style={{ height: scale(40) }} />
          </View>
        </ScrollView>
      </TouchableWithoutFeedback>

      <DatePickerModal visible={showDatePicker} onClose={() => setShowDatePicker(false)} onSelect={(date) => setExpDate(date)} initialDate={expDate} />
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
    photoSection: { alignItems: 'center', marginBottom: scale(24) },
    photoWrapper: { position: 'relative' },
    photo: { borderRadius: scale(24), ...SHADOWS.medium },
    removePhotoBtn: { position: 'absolute', top: scale(-8), right: scale(-8), width: scale(32), height: scale(32), borderRadius: scale(16), justifyContent: 'center', alignItems: 'center', ...SHADOWS.soft },
    photoPlaceholder: { borderRadius: scale(24), backgroundColor: colors.surface, justifyContent: 'center', alignItems: 'center', borderWidth: 2, borderColor: colors.primary, borderStyle: 'dashed' },
    photoPlaceholderText: { color: colors.primary, fontWeight: '800', marginTop: scale(8), fontSize: moderateScale(TYPOGRAPHY.caption, 0.3) },
    photoButtons: { flexDirection: 'row', marginTop: scale(14) },
    photoBtn: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: scale(14), paddingVertical: scale(8), borderRadius: scale(16), backgroundColor: colors.surface, marginHorizontal: scale(6), ...SHADOWS.soft },
    photoBtnText: { color: colors.primary, fontSize: moderateScale(TYPOGRAPHY.small, 0.3), fontWeight: '800', marginLeft: scale(6) },
    label: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), fontWeight: '800', color: colors.text, marginBottom: scale(8) },
    inputContainer: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, borderRadius: scale(16), paddingHorizontal: scale(16), height: scale(54), marginBottom: scale(16), borderWidth: 1, borderColor: colors.border },
    input: { flex: 1, color: colors.text, fontSize: moderateScale(TYPOGRAPHY.body, 0.3) },
    barcodeRow: { flexDirection: 'row', alignItems: 'center', marginBottom: scale(16) },
    barcodeInputWrapper: { flex: 1, flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, borderRadius: scale(16), paddingHorizontal: scale(16), height: scale(54), borderWidth: 1, borderColor: colors.border },
    barcodeInput: { flex: 1, color: colors.text, fontSize: moderateScale(TYPOGRAPHY.body, 0.3) },
    unitText: { color: colors.textLight, marginLeft: scale(8), fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), fontWeight: '600' },
    categoryScroll: { marginBottom: scale(16) },
    categoryChip: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, paddingHorizontal: scale(14), paddingVertical: scale(8), borderRadius: scale(20), marginRight: scale(10), borderWidth: 1.5, borderColor: colors.primary + '40' },
    categoryText: { color: colors.primary, marginLeft: scale(6), fontSize: moderateScale(TYPOGRAPHY.small, 0.3), fontWeight: '800' },
    calendarBtn: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, padding: scale(16), borderRadius: scale(16), marginBottom: scale(16), borderWidth: 1, borderColor: colors.border },
    calendarBtnText: { flex: 1, fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.text, marginLeft: scale(10), marginRight: scale(8), fontWeight: '600' },
    saveButton: { flexDirection: 'row', backgroundColor: colors.primary, height: scale(60), borderRadius: scale(30), justifyContent: 'center', alignItems: 'center', marginBottom: scale(12), marginTop: scale(8), ...SHADOWS.medium },
    saveButtonText: { color: colors.onPrimary, fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', marginLeft: scale(8) },
    deleteButton: { flexDirection: 'row', height: scale(54), borderRadius: scale(27), justifyContent: 'center', alignItems: 'center', marginBottom: scale(40), borderWidth: 1.5 },
    deleteButtonText: { color: colors.danger, fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', marginLeft: scale(8) },
  });
};
