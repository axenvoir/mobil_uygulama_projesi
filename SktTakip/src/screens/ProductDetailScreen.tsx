import React, {useMemo, useState} from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {useNavigation, useRoute} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RouteProp} from '@react-navigation/native';
import {useApp} from '../context/AppContext';
import {useT} from '../i18n';
import {COLORS, SPACING, TYPOGRAPHY} from '../theme';
import {freshnessOf} from '../utils/categories';
import {formatDate} from '../utils/date';
import {Button, Card, CardRow, ConfirmModal} from '../components';
import {FreshnessBadge, Screen, ScreenHeader, categoryLabelOf, expiryText} from './shared';
import type {RootStackParamList} from '../navigation/types';

type DetailNav = NativeStackNavigationProp<RootStackParamList, 'ProductDetail'>;
type DetailRoute = RouteProp<RootStackParamList, 'ProductDetail'>;

export const ProductDetailScreen: React.FC = () => {
  const navigation = useNavigation<DetailNav>();
  const route = useRoute<DetailRoute>();
  const {products, locale, deleteProduct} = useApp();
  const t = useT(locale);
  const [confirming, setConfirming] = useState(false);

  const product = useMemo(
    () => products.find(p => p.id === route.params.id),
    [products, route.params.id],
  );

  if (!product) {
    return (
      <Screen>
        <ScreenHeader title={t('productDetail')} />
        <View style={styles.missing}>
          <Button label={t('close')} onPress={() => navigation.goBack()} variant="secondary" />
        </View>
      </Screen>
    );
  }

  const level = freshnessOf(product);

  return (
    <Screen>
      <ScreenHeader title={product.name} subtitle={t('productDetail')} />
      <View style={styles.content}>
        <View style={styles.badgeWrap}>
          <FreshnessBadge level={level} label={t(level)} />
        </View>
        <Card>
          <CardRow label={t('productDetail')} value={product.name} />
          <CardRow label={t('category')} value={categoryLabelOf(product.category, t)} />
          <CardRow label={t('expiryDate')} value={formatDate(product.expDate, locale)} />
          <CardRow label={t('quantity')} value={product.quantity || '—'} />
          {product.note ? <CardRow label={t('note')} value={product.note} /> : null}
        </Card>
        <Text style={styles.expiry}>{expiryText(product, t)}</Text>
        <View style={styles.actions}>
          <Button
            label={t('edit')}
            onPress={() => navigation.navigate('ProductForm', {id: product.id})}
          />
          <Button label={t('delete')} variant="danger" onPress={() => setConfirming(true)} />
        </View>
      </View>
      <ConfirmModal
        visible={confirming}
        title={t('deleteProductTitle')}
        message={t('deleteProductMessage')}
        confirmLabel={t('delete')}
        cancelLabel={t('cancel')}
        destructive
        onConfirm={() => {
          setConfirming(false);
          deleteProduct(product.id);
          navigation.goBack();
        }}
        onCancel={() => setConfirming(false)}
      />
    </Screen>
  );
};

const styles = StyleSheet.create({
  content: {padding: SPACING.md, gap: SPACING.md},
  badgeWrap: {alignItems: 'flex-start'},
  expiry: {fontSize: TYPOGRAPHY.body, color: COLORS.textLight, textAlign: 'center'},
  actions: {marginTop: SPACING.sm},
  missing: {padding: SPACING.md},
});
