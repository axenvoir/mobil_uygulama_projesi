# Expira

Expira is an Expo/React Native app for tracking product expiry dates, notes, reminders, and inventory statistics. The app uses React Navigation and stores app data locally with AsyncStorage.

## Run locally

```sh
npm install
npm start
```

For platform-specific development:

```sh
npm run android
npm run ios
npm run web
```

## Validation

```sh
npm run lint
npx expo export --platform android
```

## Google Play release

Production EAS builds are configured to target Android 16 (API 36) and produce a signed Android App Bundle (`.aab`):

```sh
npx eas-cli@latest build --platform android --profile production
```

Before uploading the bundle, complete the Play Console store listing, privacy policy URL, Data safety form, content rating, and any testing requirements shown for your developer account. See [PLAY_STORE_RELEASE.md](./PLAY_STORE_RELEASE.md) for the release checklist. Confirm the Android application ID `com.expira.app.tr` is available in Play Console; keep it unchanged after the first Play release.

## Data and backups

Product data, notes, and preferences are stored on the device. From **Yedekleme**, users can export a JSON backup through the system share sheet, select a backup JSON file to restore, or reset local app data. Restore replaces current app data. Product image files are device-local and are not included in backups.
