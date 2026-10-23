import * as DocumentPicker from 'expo-document-picker';
import { File, Paths } from 'expo-file-system';
import * as Sharing from 'expo-sharing';
import { Platform } from 'react-native';
import { isValidDateString } from './validators';
import i18n from './i18n';


const simpleHash = (str) => {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash; 
  }
  return hash.toString(16);
};


const BACKUP_APP = 'Expira';
const BACKUP_VERSION = 1;

const isRecord = (value) => value !== null && typeof value === 'object' && !Array.isArray(value);
const isValidId = (id) => (
  (typeof id === 'string' && id.trim().length > 0)
  || (typeof id === 'number' && Number.isFinite(id))
);
const hasUniqueIds = (items) => {
  const ids = items.map(item => String(item.id));
  return new Set(ids).size === ids.length;
};
const hasOnlyStringValues = (record, keys) => (
  keys.every(key => record[key] === undefined || typeof record[key] === 'string')
);

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
    && (profile.avatarColor === undefined || /^#[\da-f]{6}$/i.test(profile.avatarColor))
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
  && (backup.checksum === undefined || typeof backup.checksum === 'string')
  && backup.products.every(product => (
    isRecord(product)
    && isValidId(product.id)
    && typeof product.name === 'string'
    && product.name.trim().length > 0
    && isValidDateString(product.expDate)
    && hasOnlyStringValues(product, ['productCode', 'category', 'quantity', 'note'])
    && (product.imageUri === undefined || product.imageUri === null || typeof product.imageUri === 'string')
  ))
  && backup.notes.every(note => (
    isRecord(note)
    && isValidId(note.id)
    && typeof note.title === 'string'
    && typeof note.desc === 'string'
    && (note.pinned === undefined || typeof note.pinned === 'boolean')
    && (note.color === undefined || /^#[\da-f]{6}$/i.test(note.color))
    && (note.date === undefined || typeof note.date === 'string')
  ))
  && hasUniqueIds(backup.products)
  && hasUniqueIds(backup.notes)
  && isValidSettings(backup.settings)
  && isValidProfile(backup.userProfile)
  && (backup.themeMode === undefined || ['system', 'light', 'dark'].includes(backup.themeMode))
);

export const createBackup = async ({ products, notes, settings, userProfile, themeMode }) => {
  try {
    if (Platform.OS === 'web') {
      return { success: false, error: 'Sharing backup files is not supported on the web version.' };
    }
    if (!(await Sharing.isAvailableAsync())) {
      return { success: false, error: 'File sharing is not available on this device.' };
    }

    const backupData = {
      products: products.map(withoutLocalImage),
      notes,
      settings,
      userProfile,
      themeMode,
    };
    
    const backup = {
      app: BACKUP_APP,
      version: BACKUP_VERSION,
      exportedAt: new Date().toISOString(),
      checksum: simpleHash(JSON.stringify(backupData)),
      ...backupData,
    };

    const file = new File(Paths.cache, `expira-backup-${Date.now()}-${Math.random().toString(36).slice(2, 7)}.json`);
    file.create();
    file.write(JSON.stringify(backup, null, 2));
    await Sharing.shareAsync(file.uri, {
      mimeType: 'application/json',
      dialogTitle: 'Share Expira Backup',
    });
    return { success: true };
  } catch (error) {
    return { success: false, error: error.message || 'Backup file could not be created.' };
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
      return { success: false, error: i18n.t('backup_invalid') };
    }

    if (typeof backup.checksum === 'string') {
      const payload = {
        products: backup.products,
        notes: backup.notes,
        settings: backup.settings,
        userProfile: backup.userProfile,
        themeMode: backup.themeMode,
      };
      if (backup.checksum !== simpleHash(JSON.stringify(payload))) {
        return { success: false, error: i18n.t('backup_corrupted') };
      }
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
    return { success: false, error: error.message || 'Backup file could not be read.' };
  }
};
