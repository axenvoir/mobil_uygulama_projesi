import * as DocumentPicker from 'expo-document-picker';
import { File, Paths } from 'expo-file-system';
import * as Sharing from 'expo-sharing';
import { Platform } from 'react-native';
import { isValidDateString } from './validators';

const BACKUP_APP = 'Expira';
const BACKUP_VERSION = 1;

const isRecord = (value) => value !== null && typeof value === 'object' && !Array.isArray(value);

const isValidSettings = (settings) => (
  settings === undefined
  || (
    isRecord(settings)
    && ['dailyReminder', 'instantNotifications', 'hapticsEnabled']
      .every(key => settings[key] === undefined || typeof settings[key] === 'boolean')
    && (settings.notificationMessage === undefined || typeof settings.notificationMessage === 'string')
    && (
      settings.reminderTime === undefined
      || (/^\d{2}:\d{2}$/.test(settings.reminderTime)
        && Number(settings.reminderTime.slice(0, 2)) < 24
        && Number(settings.reminderTime.slice(3, 5)) < 60)
    )
  )
);

const isValidProfile = (profile) => (
  profile === undefined
  || (
    isRecord(profile)
    && (profile.name === undefined || typeof profile.name === 'string')
    && (profile.avatarColor === undefined || typeof profile.avatarColor === 'string')
    && (profile.language === undefined || ['tr', 'en'].includes(profile.language))
    && (profile.biometricsEnabled === undefined || typeof profile.biometricsEnabled === 'boolean')
  )
);

const withoutLocalImage = (product) => {
  const copy = { ...product };
  delete copy.imageUri;
  return copy;
};

const isValidBackup = (backup) => (
  isRecord(backup)
  && backup.app === BACKUP_APP
  && backup.version === BACKUP_VERSION
  && Array.isArray(backup.products)
  && Array.isArray(backup.notes)
  && backup.products.every(product => (
    isRecord(product)
    && (typeof product.id === 'string' || typeof product.id === 'number')
    && typeof product.name === 'string'
    && product.name.trim().length > 0
    && isValidDateString(product.expDate)
  ))
  && backup.notes.every(note => (
    isRecord(note)
    && (typeof note.id === 'string' || typeof note.id === 'number')
    && typeof note.title === 'string'
    && typeof note.desc === 'string'
  ))
  && isValidSettings(backup.settings)
  && isValidProfile(backup.userProfile)
  && (backup.themeMode === undefined || ['system', 'light', 'dark'].includes(backup.themeMode))
);

export const createBackup = async ({ products, notes, settings, userProfile, themeMode }) => {
  try {
    if (Platform.OS === 'web') {
      return { success: false, error: 'Yedek dosyası paylaşımı web sürümünde desteklenmiyor.' };
    }
    if (!(await Sharing.isAvailableAsync())) {
      return { success: false, error: 'Bu cihazda dosya paylaşımı kullanılamıyor.' };
    }

    const backup = {
      app: BACKUP_APP,
      version: BACKUP_VERSION,
      exportedAt: new Date().toISOString(),
      products: products.map(withoutLocalImage),
      notes,
      settings,
      userProfile,
      themeMode,
    };

    const file = new File(Paths.cache, `expira-backup-${Date.now()}-${Math.random().toString(36).slice(2, 7)}.json`);
    file.create();
    file.write(JSON.stringify(backup, null, 2));
    await Sharing.shareAsync(file.uri, {
      mimeType: 'application/json',
      dialogTitle: 'Expira yedeğini paylaş',
    });
    return { success: true };
  } catch (error) {
    return { success: false, error: error.message || 'Yedek dosyası oluşturulamadı.' };
  }
};

export const restoreBackup = async () => {
  try {
    const result = await DocumentPicker.getDocumentAsync({
      type: ['application/json', 'text/json'],
      copyToCacheDirectory: true,
    });
    if (result.canceled) return { canceled: true };

    const asset = result.assets[0];
    const contents = asset.file?.text
      ? await asset.file.text()
      : new File(asset.uri).textSync();
    const backup = JSON.parse(contents);
    if (!isValidBackup(backup)) {
      return { success: false, error: 'Dosya geçerli bir Expira yedeği değil veya biçimi desteklenmiyor.' };
    }

    return {
      success: true,
      data: {
        ...backup,
        products: backup.products.map(product => ({ ...withoutLocalImage(product), id: String(product.id) })),
        notes: backup.notes.map(note => ({ ...note, id: String(note.id) })),
      },
      stats: { products: backup.products.length, notes: backup.notes.length },
    };
  } catch (error) {
    return { success: false, error: error.message || 'Yedek dosyası okunamadı.' };
  }
};
