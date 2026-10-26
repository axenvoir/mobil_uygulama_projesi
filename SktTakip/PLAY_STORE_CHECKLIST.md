# Play Store Release Checklist

## Data safety form (Play Console) — recommended answers

| Question | Answer |
|---|---|
| Does your app collect or share user data? | **Yes** — Crash reporting (Sentry), error diagnosis only |
| Data types collected | **App activity** → Crash logs, diagnostics; **Device or other IDs** → random install ID |
| Is data collected shared or transmitted? | Yes, to Sentry (crash reporting service) |
| Encrypted in transit? | Yes (HTTPS) |
| Can users request data deletion? | Data is anonymous crash reports; uninstalling the app stops all collection. No account exists. |

Everything else (products, notes, settings): **stored locally on device only, never transmitted.**

## Store listing checklist

- [x] App name: Expira
- [x] Package: `com.expira.app.tr`
- [x] versionName `2.0.0`, versionCode auto-incremented by CI (54+)
- [x] Target SDK 36, min SDK 24 (Android 7.0+) — meets 2025/2026 Play requirements
- [x] Signed AAB built in CI with mapping.txt artifact (upload mapping to Play Console for deobfuscation)
- [x] Launcher icons: all densities + adaptive + monochrome (original Expira branding)
- [x] Privacy policy (EN + TR) — must be hosted at a public URL for the Play listing; copy the content of `PRIVACY_POLICY.md` to any public page (GitHub Pages, gist, or your site) and paste the URL into Play Console → App content → Privacy policy
- [x] Notifications/reminder functionality described in listing (uses POST_NOTIFICATIONS)
- [ ] Feature graphic 1024×500 — source asset: `assets/play-store/play-store-feature-graphic.png`
- [ ] App screenshots (min 2) — take from a device after install
- [ ] Content rating questionnaire — answer: no user-generated content sharing, no ads

## Release procedure

1. Push to `main` (or let CI run) → `Build Android AAB` workflow produces signed AAB + mapping
2. Download artifact `app-release-aab` from the run
3. Play Console → Production (or Internal testing first) → Create release → upload AAB
4. **Upload `mapping.txt` to the same version** (Play Console → App bundle explorer → deobfuscation files) — required to read crash traces from Play
5. Fill release notes, submit for review

## Reminder

Every new AAB has a new mapping. Repeat step 4 for each release, or crash traces will stay obfuscated.
