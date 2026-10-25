import React, {useMemo, useState} from 'react';
import {Pressable, ScrollView, StyleSheet, Text, View} from 'react-native';
import {useNavigation, useRoute} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RouteProp} from '@react-navigation/native';
import {useApp} from '../context/AppContext';
import {useT} from '../i18n';
import {COLORS, SPACING, TYPOGRAPHY} from '../theme';
import {CATEGORY_IDS} from '../utils/categories';
import {isValidISODate, daysFromToday} from '../utils/date';
import {validateProduct} from '../utils/validators';
import {Button, Input} from '../components';
import {Screen, ScreenHeader, categoryLabelOf} from './shared';
import type {RootStackParamList} from '../navigation/types';

type FormNav = NativeStackNavigationProp<RootStackParamList, 'ProductForm'>;
type FormRoute = RouteProp<RootStackParamList, 'ProductForm'>;

const QUICK_DAYS = [1, 3, 7, 14, 30];

export const ProductFormScreen: React.FC = () => {
  const navigation = useNavigation<FormNav>();
  const route = useRoute<FormRoute>();
  const {products, locale, addProduct, updateProduct} = useApp();
  const t = useT(locale);
  const editing = useMemo(
    () => products.find(p => p.id === route.params?.id),
    [products, route.params?.id],
  );

  const [name, setName] = useState(editing?.name ?? '');
  const [category, setCategory] = useState(editing?.category ?? 'dairy');
  const [expDate, setExpDate] = useState(editing?.expDate ?? daysFromToday(7));
  const [quantity, setQuantity] = useState(editing?.quantity ?? '');
  const [note, setNote] = useState(editing?.note ?? '');
  const [errors, setErrors] = useState<string[]>([]);

  const dateValid = isValidISODate(expDate);

  const handleSave = () => {
    const validationErrors = validateProduct({name, expDate, category});
    setErrors(validationErrors);
    if (validationErrors.length > 0) {
      return;
    }
    const payload = {name: name.trim(), category, expDate, quantity: quantity.trim(), note: note.trim() || undefined};
    if (editing) {
      updateProduct(editing.id, payload);
    } else {
      addProduct(payload);
    }
    navigation.goBack();
  };

  return (
    <Screen>
      <ScreenHeader title={editing ? t('editProduct') : t('addProduct')} />
      <ScrollView contentContainerStyle={styles.content}>
        <Input
          label={t('productName')}
          value={name}
          onChangeText={setName}
          placeholder={t('productNamePlaceholder')}
          error={errors.includes('name') ? t('productName') : null}
        />

        <Text style={styles.label}>{t('category')}</Text>
        <View style={styles.chipRow}>
          {CATEGORY_IDS.map(cat => (
            <Pressable
              key={cat}
              onPress={() => setCategory(cat)}
              style={[styles.chip, category === cat && styles.chipActive]}>
              <Text style={[styles.chipText, category === cat && styles.chipTextActive]}>
                {categoryLabelOf(cat, t)}
              </Text>
            </Pressable>
          ))}
        </View>

        <Text style={styles.label}>{t('expiryDate')}</Text>
        <View style={styles.quickRow}>
          {QUICK_DAYS.map(d => (
            <Pressable
              key={d}
              onPress={() => setExpDate(daysFromToday(d))}
              style={[styles.chip, expDate === daysFromToday(d) && styles.chipActive]}>
              <Text style={[styles.chipText, expDate === daysFromToday(d) && styles.chipTextActive]}>
                +{d}
              </Text>
            </Pressable>
          ))}
        </View>
        <Input
          value={expDate}
          onChangeText={setExpDate}
          placeholder="YYYY-MM-DD"
          error={!dateValid ? 'YYYY-MM-DD' : null}
        />

        <Input
          label={t('quantity')}
          value={quantity}
          onChangeText={setQuantity}
          placeholder={t('quantityPlaceholder')}
        />

        <Input
          label={t('note')}
          value={note}
          onChangeText={setNote}
          placeholder={t('notePlaceholder')}
          multiline
        />

        <View style={styles.actions}>
          <Button label={t('save')} onPress={handleSave} />
          <Button label={t('cancel')} onPress={() => navigation.goBack()} variant="secondary" />
        </View>
      </ScrollView>
    </Screen>
  );
};

const styles = StyleSheet.create({
  content: {padding: SPACING.md},
  label: {
    fontSize: TYPOGRAPHY.caption,
    fontWeight: '600',
    color: COLORS.text,
    marginBottom: SPACING.xs,
  },
  chipRow: {flexDirection: 'row', flexWrap: 'wrap', gap: SPACING.xs, marginBottom: SPACING.md},
  quickRow: {flexDirection: 'row', flexWrap: 'wrap', gap: SPACING.xs, marginBottom: SPACING.sm},
  chip: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 999,
    backgroundColor: COLORS.surface,
    borderWidth: 1,
    borderColor: COLORS.border,
  },
  chipActive: {backgroundColor: COLORS.primary, borderColor: COLORS.primary},
  chipText: {fontSize: TYPOGRAPHY.caption, color: COLORS.text, fontWeight: '600'},
  chipTextActive: {color: COLORS.white},
  actions: {marginTop: SPACING.sm, gap: SPACING.sm},
});
