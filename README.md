# Expira (Mobil Uygulama Projesi)

This repository contains **Expira**, a privacy-first mobile & web application for tracking product expiry dates, pantry inventory, smart reminders, notes, and statistics.

- **Mobile App (Android / Expo):** Located in [`SktTakip/`](./SktTakip/) (Targets Android 16 / API 36, signed AAB release workflow with R8 minification).
- **Web App (React / Vite):** Located in the root directory for fast web previews and desktop access.

## Quick start

### Mobile App (Expo / Android)
```sh
cd SktTakip
npm install
npm start
```

### Web App
```sh
npm install
npm run dev
```

## Documentation & Release Materials

| File | Contents |
|------|----------|
| [`SktTakip/README.md`](./SktTakip/README.md) | Mobile app architecture, setup, validation, project structure |
| [`SktTakip/PLAY_STORE_RELEASE.md`](./SktTakip/PLAY_STORE_RELEASE.md) | Google Play release checklist & signing instructions |
| [`SktTakip/PLAY_STORE_LISTING_EN.md`](./SktTakip/PLAY_STORE_LISTING_EN.md) | Global English Google Play store listing (Title, Short/Full description, ASO keywords) |
| [`SktTakip/PLAY_STORE_LISTING_TR.md`](./SktTakip/PLAY_STORE_LISTING_TR.md) | Turkish Google Play store listing |
| [`SktTakip/PRIVACY_POLICY_EN.md`](./SktTakip/PRIVACY_POLICY_EN.md) | Privacy policy (English) |
| [`SktTakip/PRIVACY_POLICY.md`](./SktTakip/PRIVACY_POLICY.md) | Privacy policy (Turkish) |
| [`SktTakip/ROADMAP.md`](./SktTakip/ROADMAP.md) | Version roadmap |
| [`SktTakip/DESIGN_SYSTEM.md`](./SktTakip/DESIGN_SYSTEM.md) | Design tokens & component specifications |

## CI & Automated Builds

- `Tests` (`.github/workflows/tests.yml`): Lint + Jest test suite (65 tests) on pull requests and push to `main`.
- `Build Android AAB` (`.github/workflows/android-build.yml`): Auto-increments `android.versionCode`, runs `expo prebuild`, applies release signing, and builds signed `.aab` with R8 minification + resource shrinking.
