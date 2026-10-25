# Expira (Mobile)

Privacy-first expiry date & pantry tracker. **Bare React Native + TypeScript** — no Expo.

## Stack

- React Native 0.76 (Hermes, old arch), TypeScript
- Navigation: `@react-navigation/native` + `bottom-tabs` + `native-stack`
- Storage: `@react-native-async-storage/async-storage` (single-key schema `@expira/data`, legacy keys migrated)
- Notifications: `@notifee/react-native` (daily reminder, Android channel)
- Biometrics: `react-native-biometrics` (optional lock)
- Locale: `react-native-localize` (TR/EN, typed i18n with full key parity — enforced by test)

## Commands

```sh
npm install --legacy-peer-deps
npm start            # Metro
npm run android      # build & run debug
npm test             # Jest unit tests
npm run lint         # ESLint
npm run typecheck    # tsc --noEmit
```

## Architecture

```
src/
  App.tsx             # Root: ErrorBoundary > SafeArea > AppProvider > Navigator
  navigation/         # Typed stack + bottom tabs
  context/AppContext  # Single source of truth; persists on change; schedules reminders
  screens/            # Home, Products, ProductForm, ProductDetail, Notes, Statistics, Profile
  components/         # Button, Input, Card, ConfirmModal, Toast, EmptyState, ErrorBoundary
  utils/              # date, categories, validators, storage, notifications, biometrics, haptics
  i18n/               # tr.ts, en.ts (key parity tested), typed translate()
  theme/              # Design tokens (colors, shadows, typography, spacing)
__tests__/            # Unit tests (utils, i18n parity, theme contract)
android/              # Bare RN shell (API 36 target, R8 optional, signed release via env vars)
```

## Conventions

- All user-facing strings go through `t()` — never hardcoded.
- Data mutation only via `AppContext` actions; storage writes are centralized in the provider effect.
- Malformed persisted data is normalized on load (`validators.normalize*`); invalid entries are dropped, not crashed on.
- Tests live in `__tests__` and must pass alongside `lint` and `typecheck` before any merge.

## Release (Android)

Signing uses env vars `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` with `android/app/release.keystore`. CI builds a signed AAB via `.github/workflows/android-build.yml`.
