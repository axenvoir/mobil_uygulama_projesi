import React, {useState} from 'react';
import {Alert, Pressable, ScrollView, StyleSheet, Switch, Text, View} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import {useApp} from '../context/AppContext';
import {useT} from '../i18n';
import {COLORS, SPACING, TYPOGRAPHY} from '../theme';
import {Button, ConfirmModal} from '../components';
import {Screen, ScreenHeader} from './shared';
import {requestPermission} from '../utils/notifications';
import type {RootStackParamList} from '../navigation/types';

export const ProfileScreen: React.FC = () => {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const {settings, notes, products, locale, updateSettings, clearAllData} = useApp();
  const t = useT(locale);
  const [confirming, setConfirming] = useState(false);

  const languages: Array<{key: 'system' | 'tr' | 'en'; label: string}> = [
    {key: 'system', label: t('languageSystem')},
    {key: 'tr', label: t('turkish')},
    {key: 'en', label: t('english')},
  ];

  const handleReminderToggle = async (value: boolean) => {
    if (value) {
      await requestPermission();
    }
    updateSettings({dailyReminder: value});
  };

  const handleTimePicker = () => {
    const hours = Array.from({length: 24}, (_, i) => i);
    Alert.alert(t('reminderTime'), undefined, [
      ...hours.slice(0, 12).map(h => ({
        text: `${String(h).padStart(2, '0')}:00`,
        onPress: () => updateSettings({reminderHour: h, reminderMinute: 0}),
      })),
      {
        text: `${t('done')} ›`,
        onPress: () =>
          Alert.alert(t('reminderTime'), undefined, [
            ...hours.slice(12).map(h => ({
              text: `${String(h).padStart(2, '0')}:00`,
              onPress: () => updateSettings({reminderHour: h, reminderMinute: 0}),
            })),
            {text: t('cancel'), style: 'cancel' as const},
          ]),
      },
      {text: t('cancel'), style: 'cancel' as const},
    ]);
  };

  return (
    <Screen>
      <ScreenHeader title={t('tabProfile')} />
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.section}>{t('settings')}</Text>

        <View style={styles.card}>
          <Text style={styles.cardLabel}>{t('language')}</Text>
          <View style={styles.row}>
            {languages.map(lang => (
              <Pressable
                key={lang.key}
                onPress={() => updateSettings({language: lang.key})}
                style={[styles.chip, settings.language === lang.key && styles.chipActive]}>
                <Text style={[styles.chipText, settings.language === lang.key && styles.chipTextActive]}>
                  {lang.label}
                </Text>
              </Pressable>
            ))}
          </View>
        </View>

        <View style={styles.card}>
          <View style={styles.switchRow}>
            <Text style={styles.cardLabel}>{t('dailyReminder')}</Text>
            <Switch
              value={settings.dailyReminder}
              onValueChange={handleReminderToggle}
              trackColor={{true: COLORS.primary, false: COLORS.border}}
              thumbColor={COLORS.white}
            />
          </View>
          {settings.dailyReminder && (
            <Pressable style={styles.switchRow} onPress={handleTimePicker}>
              <Text style={styles.cardLabel}>{t('reminderTime')}</Text>
              <Text style={styles.timeValue}>
                {String(settings.reminderHour).padStart(2, '0')}:
                {String(settings.reminderMinute).padStart(2, '0')}
              </Text>
            </Pressable>
          )}
        </View>

        <View style={styles.card}>
          <View style={styles.switchRow}>
            <Text style={styles.cardLabel}>{t('haptics')}</Text>
            <Switch
              value={settings.hapticsEnabled}
              onValueChange={v => updateSettings({hapticsEnabled: v})}
              trackColor={{true: COLORS.primary, false: COLORS.border}}
              thumbColor={COLORS.white}
            />
          </View>
        </View>

        <Pressable style={styles.card} onPress={() => navigation.navigate('Backup')}>
          <View style={styles.switchRow}>
            <Text style={styles.cardLabel}>{t('backup')}</Text>
            <Text style={styles.timeValue}>›</Text>
          </View>
        </Pressable>

        <Text style={styles.section}>{t('about')}</Text>
        <View style={styles.card}>
          <View style={styles.switchRow}>
            <Text style={styles.cardLabel}>{t('version')}</Text>
            <Text style={styles.timeValue}>2.0.0</Text>
          </View>
          <View style={styles.switchRow}>
            <Text style={styles.cardLabel}>{t('tabProducts')}</Text>
            <Text style={styles.timeValue}>{products.length}</Text>
          </View>
          <View style={styles.switchRow}>
            <Text style={styles.cardLabel}>{t('tabNotes')}</Text>
            <Text style={styles.timeValue}>{notes.length}</Text>
          </View>
        </View>

        <Button label={t('clearData')} variant="danger" onPress={() => setConfirming(true)} style={{marginTop: SPACING.lg}} />
      </ScrollView>
      <ConfirmModal
        visible={confirming}
        title={t('clearData')}
        message={t('clearDataConfirm')}
        confirmLabel={t('delete')}
        cancelLabel={t('cancel')}
        destructive
        onConfirm={async () => {
          setConfirming(false);
          await clearAllData();
        }}
        onCancel={() => setConfirming(false)}
      />
    </Screen>
  );
};

const styles = StyleSheet.create({
  content: {padding: SPACING.md, paddingBottom: 120},
  section: {
    fontSize: TYPOGRAPHY.caption,
    fontWeight: '700',
    color: COLORS.textLight,
    textTransform: 'uppercase',
    marginBottom: SPACING.sm,
    marginTop: SPACING.sm,
  },
  card: {
    backgroundColor: COLORS.surface,
    borderRadius: 16,
    padding: SPACING.md,
    marginBottom: SPACING.sm,
  },
  cardLabel: {fontSize: TYPOGRAPHY.body, color: COLORS.text},
  row: {flexDirection: 'row', flexWrap: 'wrap', gap: SPACING.xs, marginTop: SPACING.sm},
  switchRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: SPACING.xs,
  },
  chip: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 999,
    backgroundColor: COLORS.background,
    borderWidth: 1,
    borderColor: COLORS.border,
  },
  chipActive: {backgroundColor: COLORS.primary, borderColor: COLORS.primary},
  chipText: {fontSize: TYPOGRAPHY.caption, color: COLORS.text, fontWeight: '600'},
  chipTextActive: {color: COLORS.white},
  timeValue: {fontSize: TYPOGRAPHY.body, color: COLORS.textLight},
});
