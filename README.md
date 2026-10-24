# Mobil Uygulama Projesi

This repository contains **Expira**, an Expo/React Native mobile app for tracking product expiry dates, notes, reminders, and inventory statistics. All application code lives in [`SktTakip/`](./SktTakip/).

See the app [README](./SktTakip/README.md) for features, setup, validation and release instructions.

## Quick start

```sh
cd SktTakip
npm install
npm start
```

## Documentation

| File | Contents |
|------|----------|
| [`SktTakip/README.md`](./SktTakip/README.md) | Features, setup, validation, project structure |
| [`SktTakip/ROADMAP.md`](./SktTakip/ROADMAP.md) | Version plan from v1.1.3 to v2.0.0 |
| [`SktTakip/DESIGN_SYSTEM.md`](./SktTakip/DESIGN_SYSTEM.md) | v2.0 design direction (target spec) |
| [`SktTakip/PLAY_STORE_RELEASE.md`](./SktTakip/PLAY_STORE_RELEASE.md) | Google Play release checklist |
| [`SktTakip/PLAY_STORE_LISTING_TR.md`](./SktTakip/PLAY_STORE_LISTING_TR.md) | Turkish store listing draft |
| [`SktTakip/PRIVACY_POLICY.md`](./SktTakip/PRIVACY_POLICY.md) | Privacy policy draft (pre-publish) |
| [`SktTakip/AGENTS.md`](./SktTakip/AGENTS.md) | Agent/development conventions |

## CI

- `Tests` (`.github/workflows/tests.yml`): lint + Jest (65 tests) on PRs touching app code and on `main`
- `Build Android AAB` (`.github/workflows/android-build.yml`): on-demand signed `.aab` build — auto-increments `android.versionCode` (and commits the bump), runs `expo prebuild`, applies release signing, and builds with R8 minification + resource shrinking
