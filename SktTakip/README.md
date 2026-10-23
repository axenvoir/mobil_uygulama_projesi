# Expira

Expira is an Expo/React Native app for tracking product expiry dates, notes, reminders, and inventory statistics. The app uses React Navigation and stores app data locally with AsyncStorage — there is no account system and no server-side data collection.

## Features

- Product tracking with name, code, quantity, category, note and optional photo
- Expiry overview: today, this week, and overdue items on the home screen
- Daily local reminders with a customizable time and message
- Optional calendar integration for per-product reminders
- Notes with pinning and colors; category management and statistics
- Inventory sharing (text/SMS/clipboard) and PDF export
- JSON backup export/restore with integrity checksum verification
- Light/dark/system theme; biometric app lock
- Bilingual UI (Turkish/English); by default the app follows the device language
- Accessibility support: font scaling, screen-reader labels on interactive elements

## Requirements

- Node.js 20+ and npm
- Expo CLI (`npx expo`) — no Android Studio/Xcode needed for Expo Go development

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
npm run lint   # ESLint (expo config)
npm test       # Jest unit tests (validators, formatters, translation parity)
npx expo export --platform android   # bundle export smoke check
```

The `Tests` GitHub Actions workflow (`.github/workflows/tests.yml` at the repo root) runs lint and tests on PRs touching `SktTakip/src` or `SktTakip/__tests__`, and on pushes to `main`.

## Project structure

```
src/
  components/   # shared UI (Button, Input, Card, Toast, modals, ...)
  context/      # AppContext (data + actions), ThemeContext
  hooks/        # useTheme, useTranslation, useResponsive
  screens/      # one file per screen
  translations/ # tr.json, en.json (keys kept in parity by tests)
  theme.js      # light/dark palettes, typography, shadows
  utils/        # validators, formatters, notifications, backup, ...
__tests__/      # Jest suites (pure logic; no React rendering yet)
```

See `DESIGN_SYSTEM.md` for the v2.0 design direction and `AGENTS.md` for agent/development conventions. Note: the app currently uses React Navigation (not Expo Router) and the JSC engine with the old architecture disabled; `src/theme.js` is the source of truth for the shipped design.

## Google Play release

Production EAS builds are configured to target Android 16 (API 36) and produce a signed Android App Bundle (`.aab`):

```sh
npx eas-cli@latest build --platform android --profile production
```

Before uploading the bundle, complete the Play Console store listing, privacy policy URL, Data safety form, content rating, and any testing requirements shown for your developer account. See [PLAY_STORE_RELEASE.md](./PLAY_STORE_RELEASE.md) for the release checklist. Confirm the Android application ID `com.expira.app.tr` is available in Play Console; keep it unchanged after the first Play release.

## Data and backups

Product data, notes, and preferences are stored on the device. From the backup screen, users can export a JSON backup through the system share sheet, select a backup JSON file to restore, or reset local app data. Restore replaces current app data; backups carry a checksum and modified or corrupted files are rejected. Product image files are device-local and are not included in backups.

## Language

The default language setting is **System (device language)**: the app opens in Turkish if the device is Turkish, English otherwise, and re-resolves when returning to the foreground. Users can pin Turkish or English from the profile screen at any time.
