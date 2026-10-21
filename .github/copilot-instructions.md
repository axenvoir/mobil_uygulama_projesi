# Copilot instructions

This repository is an Expo/React Native app for tracking product expiry dates, notes, reminders, and inventory stats. The app code lives in `SktTakip/`; run project commands from that directory unless the task is repo-level documentation or release work.

## Build, lint, and validation

From `SktTakip/`:

```sh
npm install
npm start
npm run android
npm run ios
npm run web
npm run lint
npx expo export --platform android
```

`npm run lint` runs `expo lint`. There is no configured Jest/Vitest/Mocha test runner in this repository, and no test script for a single file or full suite. If tests are added later, prefer the project’s chosen runner directly instead of inventing a new workflow. Production Android bundles use:

```sh
npx eas-cli@latest build --platform android --profile production
```

For any Expo/EAS/React Native library or API change, check the Expo major version in `SktTakip/package.json` and consult the matching docs before editing code.

## High-level architecture

- `SktTakip/App.js` is the app entry point. It wraps the app in `GestureHandlerRootView`, `SafeAreaProvider`, `ErrorBoundary`, `ThemeProvider`, and `AppProvider`, then sets up the stack navigator and bottom tab navigator. This project uses React Navigation, not Expo Router.
- `SktTakip/src/context/AppContext.js` is the central state container for products, notes, settings, profile, lock state, toasts, and app-level actions. It loads and persists values with AsyncStorage through a queued `multiSet`, and it surfaces load/parse errors instead of silently replacing stored data with empty arrays.
- `SktTakip/src/context/ThemeContext.js` manages theme mode persistence (`light`, `dark`, `system`) and exposes `useTheme`-compatible colors from `src/theme.js`. Prefer theme tokens and the shared color palette over hard-coded colors.
- Screen/UI boundaries are organized by responsibility: `src/screens/` for route-level screens, `src/components/` for shared UI and modals, `src/hooks/` for reusable hooks, and `src/utils/` for helpers such as backup logic, localization, notifications, validators, and formatters.
- The app is local-first: products, notes, preferences, profile, and local device reminders are stored on-device. Backup validation and import/export live in `src/utils/backupUtils.js`; product image files are device-local and are not included in backups.
- Localization is initialized in `src/utils/i18n.js` and dictionaries live in `src/translations/en.json` and `src/translations/tr.json`. Keep language keys aligned in both files and preserve support for `tr` and `en`.
- Native capabilities such as notifications, calendar access, biometrics, and image picking are accessed through Expo modules and configured via `app.json` and its plugins. There are no checked-in native iOS/Android projects; do not hand-edit generated native directories.

## Key conventions

- Use the existing app context and hooks rather than introducing a second data layer or duplicate persistence logic.
- Keep UI and business logic aligned with the current structure: screens call context actions, utilities handle device integrations, and shared UI remains in components.
- Prefer semantic theme colors from `src/theme.js` and the `useTheme` hook instead of raw color literals when modifying screens.
- Preserve the Android app ID `com.expira.app.tr` for release work. Check `SktTakip/PLAY_STORE_RELEASE.md` before changes to release configuration or publishing metadata.
- Add new Expo dependencies with `npx expo install <package>` so the versions match the installed SDK.
- Keep platform-specific behavior behind the existing utility patterns and account for missing capabilities on Expo Go or web when editing app features.
- The app is bilingual and user-facing copy often follows local-language conventions. Keep text and event labels consistent with the project’s current Turkish/English patterns and translation dictionaries.
- Before changing Expo, EAS, or React Native APIs, confirm the current Expo major version and consult the matching official docs; these APIs change between SDK releases.

## Repo-specific notes

- The root `README.md` is intentionally minimal; `SktTakip/README.md` is the source of setup and release details.
- `SktTakip/AGENTS.md` is a project-specific agent guide, but the implementation in this repo remains the React Navigation-based app described here.
