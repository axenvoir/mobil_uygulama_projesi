# Expira

Privacy-first expiry date & pantry tracker for Android. Bare **React Native 0.76 + TypeScript** — no Expo.

Mobile app lives in [`SktTakip/`](./SktTakip/). Setup, architecture and commands are documented in [`SktTakip/README.md`](./SktTakip/README.md).

## Quick start

```sh
cd SktTakip
npm install --legacy-peer-deps
npm start            # Metro dev server
npm run android      # build & run on device/emulator
```

## CI (GitHub Actions)

| Workflow | What it does |
|---|---|
| `Tests` | ESLint + `tsc --noEmit` + Jest (23 tests) on every push/PR touching app code |
| `Build Android AAB` | Signed `.aab` + `mapping.txt` artifact; triggered by changes to `SktTakip/android/**`, `SktTakip/src/**`, package files |

## Features

- Product expiry tracking with freshness levels (fresh / approaching / expired)
- Category filters, search, statistics (freshness & category distribution)
- Notes, daily reminder (Notifee), JSON backup/restore
- TR/EN localization, haptic feedback
- Crash reporting via Sentry (activated by `SENTRY_DSN` secret; disabled by default when unset)

## Android release

Signed AAB built in CI. Signing uses the `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` repository secrets. R8 minification is enabled; upload the generated `mapping.txt` to Play Console (deobfuscation) for each release.
