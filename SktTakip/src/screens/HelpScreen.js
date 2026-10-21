import React, { useState, useMemo, useContext } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Linking } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { SHADOWS, TYPOGRAPHY } from '../theme';
import { AppContext } from '../context/AppContext';

const FAQ = [
  { q: 'Ürün nasıl eklerim?', a: 'Alt menüdeki "+" butonuna bas, ürün bilgilerini doldur ve "Kaydet"e bas. Ürün adı zorunludur, diğer alanlar isteğe bağlıdır.' },
  { q: 'Son kullanma tarihi ne anlama geliyor?', a: 'Ürünün güvenle tüketilebileceği son tarihtir. Bu tarih geçtikten sonra ürünün bozulma riski vardır.' },
  { q: 'Bildirimleri nasıl ayarlarım?', a: 'Bildirimler sekmesinden günlük hatırlatmayı açabilir, saatini değiştirebilirsin.' },
  { q: 'Verilerim nerede saklanıyor?', a: 'Tüm verilerin yalnızca senin telefonunda, güvenli bir şekilde saklanır. Hiçbir sunucuya gönderilmez.' },
  { q: 'Ürünü nasıl silerim?', a: 'Ürünlerim sekmesinde ürünün yanındaki çöp kutusu ikonuna bas ya da ürünü sola kaydır ve "Sil"e bas.' },
  { q: 'İstatistikler ne işe yarar?', a: 'Hangi kategoride kaç ürünün olduğunu, taze/yaklaşan/geçmiş dağılımını gösterir. Böylece israfı önlemek için önlem alabilirsin.' },
  { q: 'Yedekleme nasıl yapılır?', a: 'Profilim → Yedekleme ve Geri Yükleme bölümünden JSON dosyası olarak yedek alabilirsin. Bu yedeği WhatsApp/e-posta ile saklayabilirsin.' },
  { q: 'Tema nasıl değiştirilir?', a: 'Profilim → Tema satırına dokunarak Sistem/Açık/Karanlık arasında geçiş yapabilirsin.' },
  { q: 'Notlar ne için kullanılır?', a: 'Alışveriş listesi, buzdolabı kontrolü, bakliyat stoğu gibi hatırlatmalar için kullanabilirsin. Notları sabitleyebilirsin.' },
];

export default function HelpScreen({ navigation }) {
  const { colors } = useTheme();
  const { showToast } = useContext(AppContext);
  const responsive = useResponsive();
  const { scale } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);
  const [expandedIndex, setExpandedIndex] = useState(null);

  const toggleExpand = (index) => setExpandedIndex(expandedIndex === index ? null : index);

  const openEmail = () => {
    Linking.openURL('mailto:axenvoir@yahoo.com?subject=Expira Destek').catch(() => {
      showToast('E-posta uygulaması bulunamadı.', 'error');
    });
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.innerWrap}>
        <View style={styles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()} activeOpacity={0.7}>
            <Ionicons name="arrow-back" size={scale(24)} color={colors.text} />
          </TouchableOpacity>
          <Text style={styles.headerTitle}>Yardım ve Destek</Text>
          <View style={{ width: scale(24) }} />
        </View>

        <View style={styles.heroCard}>
          <View style={styles.heroIconBox}>
            <Ionicons name="help-buoy-outline" size={scale(32)} color={colors.onPrimary} />
          </View>
          <Text style={styles.heroTitle}>Sana nasıl yardımcı olabiliriz?</Text>
          <Text style={styles.heroSub}>Aşağıdaki sık sorulan sorulara göz at veya bize ulaş.</Text>
        </View>

        <Text style={styles.sectionTitle}>Sık Sorulan Sorular ({FAQ.length})</Text>

        {FAQ.map((item, index) => (
          <TouchableOpacity key={index} style={styles.faqCard} onPress={() => toggleExpand(index)} activeOpacity={0.7}>
            <View style={styles.faqHeader}>
              <View style={[styles.faqNumber, { backgroundColor: colors.primary + '15' }]}>
                <Text style={[styles.faqNumberText, { color: colors.primary }]}>{index + 1}</Text>
              </View>
              <Text style={styles.faqQuestion} numberOfLines={2}>{item.q}</Text>
              <Ionicons name={expandedIndex === index ? 'chevron-up' : 'chevron-down'} size={scale(20)} color={colors.primary} />
            </View>
            {expandedIndex === index && (
              <View style={[styles.faqAnswer, { borderTopColor: colors.border }]}>
                <Text style={styles.faqAnswerText}>{item.a}</Text>
              </View>
            )}
          </TouchableOpacity>
        ))}

        <Text style={styles.sectionTitle}>Bize Ulaş</Text>

        <TouchableOpacity style={styles.contactCard} onPress={openEmail} activeOpacity={0.7}>
          <View style={[styles.contactIcon, { backgroundColor: colors.primary + '15' }]}>
            <Ionicons name="mail-outline" size={scale(22)} color={colors.primary} />
          </View>
          <View style={{ flex: 1, marginLeft: scale(14), marginRight: scale(8) }}>
            <Text style={styles.contactTitle}>E-posta ile Destek</Text>
            <Text style={styles.contactDesc} numberOfLines={1}>axenvoir@yahoo.com</Text>
          </View>
          <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} />
        </TouchableOpacity>

        <TouchableOpacity style={styles.contactCard} onPress={() => showToast('Uygulama mağazada yayınlandığında puanlama aktif olacak!', 'info')} activeOpacity={0.7}>
          <View style={[styles.contactIcon, { backgroundColor: colors.warning + '15' }]}>
            <Ionicons name="star-outline" size={scale(22)} color={colors.warning} />
          </View>
          <View style={{ flex: 1, marginLeft: scale(14), marginRight: scale(8) }}>
            <Text style={styles.contactTitle}>Uygulamayı Puanla</Text>
            <Text style={styles.contactDesc}>Görüşlerin bizim için değerli</Text>
          </View>
          <Ionicons name="chevron-forward" size={scale(20)} color={colors.textLight} />
        </TouchableOpacity>

        <View style={styles.versionBox}>
          <Text style={styles.versionText}>Expira v1.0.0</Text>
          <Text style={styles.versionSub}>© 2026 Expira. Tüm hakları saklıdır.</Text>
        </View>

        <View style={{ height: scale(60) }} />
      </View>
    </ScrollView>
  );
}

const createStyles = (colors, responsive) => {
  const { horizontalPadding, contentMaxWidth, scale, moderateScale } = responsive;
  return StyleSheet.create({
    container: { flex: 1, backgroundColor: colors.background },
    content: { paddingBottom: scale(60), alignItems: 'center' },
    innerWrap: { width: '100%', maxWidth: contentMaxWidth, paddingHorizontal: horizontalPadding, paddingTop: scale(20) },
    header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: scale(24) },
    headerTitle: { fontSize: moderateScale(TYPOGRAPHY.h2, 0.3), fontWeight: '900', color: colors.text },
    heroCard: { backgroundColor: colors.primaryDark, borderRadius: scale(24), padding: scale(24), alignItems: 'center', marginBottom: scale(24), ...SHADOWS.medium },
    heroIconBox: { width: scale(64), height: scale(64), borderRadius: scale(32), backgroundColor: colors.primary, justifyContent: 'center', alignItems: 'center', marginBottom: scale(16) },
    heroTitle: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.white, marginBottom: scale(8), textAlign: 'center' },
    heroSub: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.white, textAlign: 'center', lineHeight: scale(20), fontWeight: '500' },
    sectionTitle: { fontSize: moderateScale(TYPOGRAPHY.h3, 0.3), fontWeight: '800', color: colors.text, marginBottom: scale(14), marginTop: scale(8) },
    faqCard: { backgroundColor: colors.surface, borderRadius: scale(16), padding: scale(16), marginBottom: scale(12), ...SHADOWS.soft },
    faqHeader: { flexDirection: 'row', alignItems: 'center' },
    faqNumber: { width: scale(32), height: scale(32), borderRadius: scale(16), justifyContent: 'center', alignItems: 'center', marginRight: scale(12) },
    faqNumberText: { fontWeight: '800', fontSize: moderateScale(TYPOGRAPHY.caption, 0.3) },
    faqQuestion: { flex: 1, fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text, paddingRight: scale(8) },
    faqAnswer: { marginTop: scale(12), paddingTop: scale(12), borderTopWidth: 1, paddingLeft: scale(44) },
    faqAnswerText: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), color: colors.textLight, lineHeight: scale(22), fontWeight: '500' },
    contactCard: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, padding: scale(16), borderRadius: scale(16), marginBottom: scale(12), ...SHADOWS.soft },
    contactIcon: { width: scale(48), height: scale(48), borderRadius: scale(24), justifyContent: 'center', alignItems: 'center' },
    contactTitle: { fontSize: moderateScale(TYPOGRAPHY.body, 0.3), fontWeight: '800', color: colors.text },
    contactDesc: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: scale(2), fontWeight: '500' },
    versionBox: { alignItems: 'center', marginTop: scale(24), marginBottom: scale(20) },
    versionText: { fontSize: moderateScale(TYPOGRAPHY.caption, 0.3), fontWeight: '800', color: colors.textLight },
    versionSub: { fontSize: moderateScale(TYPOGRAPHY.small, 0.3), color: colors.textLight, marginTop: scale(4), fontWeight: '500' },
  });
};