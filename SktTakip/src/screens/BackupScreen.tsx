import React, {useState} from 'react';
import {Alert, Share, StyleSheet, Text, View} from 'react-native';
import {useApp} from '../context/AppContext';
import {useT} from '../i18n';
import {COLORS, SPACING, TYPOGRAPHY} from '../theme';
import {Button, Card, Input} from '../components';
import {Screen, ScreenHeader} from './shared';
import {exportData, importData} from '../utils/storage';
import type {AppData} from '../types';

export const BackupScreen: React.FC = () => {
  const {products, notes, settings, locale, replaceAllData} = useApp();
  const t = useT(locale);
  const [importText, setImportText] = useState('');

  const handleExport = async () => {
    try {
      const json = await exportData({version: 1, products, notes, settings});
      await Share.share({message: json, title: 'expira-backup.json'});
    } catch {
      // user cancelled share sheet or share unavailable
    }
  };

  const handleImport = async () => {
    if (!importText.trim()) {
      return;
    }
    try {
      const data: AppData = await importData(importText.trim());
      Alert.alert(t('appName'), t('dataRestored'));
      replaceAllData(data);
      setImportText('');
    } catch {
      Alert.alert(t('error'), t('errorMessage'));
    }
  };

  return (
    <Screen>
      <ScreenHeader title={t('backupTitle')} />
      <View style={styles.content}>
        <Card>
          <Text style={styles.cardTitle}>{t('backupExportTitle')}</Text>
          <Text style={styles.cardBody}>{t('backupExportHint')}</Text>
          <Button label={t('backupExport')} onPress={handleExport} style={{marginTop: SPACING.md}} />
        </Card>
        <Card style={{marginTop: SPACING.md}}>
          <Text style={styles.cardTitle}>{t('backupImportTitle')}</Text>
          <Text style={styles.cardBody}>{t('backupImportHint')}</Text>
          <View style={{marginTop: SPACING.md}}>
            <Input
              value={importText}
              onChangeText={setImportText}
              placeholder="{ ... }"
              multiline
            />
            <Button label={t('backupImport')} onPress={handleImport} variant="secondary" />
          </View>
        </Card>
      </View>
    </Screen>
  );
};

const styles = StyleSheet.create({
  content: {padding: SPACING.md},
  cardTitle: {fontSize: TYPOGRAPHY.title, fontWeight: '700', color: COLORS.text},
  cardBody: {fontSize: TYPOGRAPHY.body, color: COLORS.textLight, marginTop: SPACING.xs},
});
