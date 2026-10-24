This is an Expo/React Native mobile application. Prioritize mobile-first patterns, performance, and cross-platform compatibility.

## Expo has changed — do not trust your training data

Expo ships breaking changes every SDK release. APIs you remember are likely renamed, moved, or removed. Before writing any code that touches an Expo, EAS, or React Native API:

1. Read the major version of the `expo` package in `package.json`.
2. Fetch the matching versioned docs: `https://docs.expo.dev/versions/v<major>.0.0/`
3. For anything else, fetch https://docs.expo.dev/llms.txt — an index of all Expo docs with corrections to common LLM misconceptions. Follow its links to the specific page you need; never answer from memory.

## Commands

```bash
npx expo install <package>  # ALWAYS use instead of npm/yarn/pnpm/bun add — resolves SDK-compatible versions
npx expo start              # start the dev server
npx expo lint               # lint
npm test                    # Jest unit tests (validators, formatters, translations, theme contracts)
npx expo-doctor             # diagnose dependency and config issues
npx expo install --fix      # fix incompatible package versions
```

Run lint and tests before declaring any task done. Test suites live in `__tests__/`; when you fix a bug, add a regression test (see `__tests__/themeShadows.test.js` for an example that pins a UI contract).

## Navigation & Routing

- The app uses **React Navigation** (not Expo Router). Navigators (bottom tabs + stack) are defined in `App.js`; screens live in `src/screens/`, shared UI in `src/components/`.
- Import navigation props/hooks from `@react-navigation/native`, `@react-navigation/bottom-tabs`, and `@react-navigation/stack`.
- Docs: https://reactnavigation.org/docs/getting-started

## Building the AAB

The app is built and signed by the `Build Android AAB` GitHub Actions workflow (`.github/workflows/android-build.yml`, manual trigger) — no local Xcode, Android Studio, or EAS account required. The workflow runs `expo prebuild`, applies the release signing config from repository secrets, and produces an `.aab` artifact. It also auto-increments `android.versionCode` in `app.json` and commits the bump back to the branch before each build.

## Rules

- If `ios/` and `android/` directories do not exist, they are generated (Continuous Native Generation). Never create or edit them by hand — configure native behavior in `app.json` and config plugins.
- After adding a library with native code, produce a new build via the AAB workflow (or `npx expo run:android` locally with the Android toolchain installed). There is no Expo Go support and no OTA channel; every change ships as a new build through Google Play.
- Prefer recommended Expo modules over third-party libraries, and check your available skills before adding dependencies. Docs: https://docs.expo.dev/versions/latest/index.md
