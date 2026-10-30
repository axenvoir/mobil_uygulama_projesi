# Historical Codebase Analysis Report (2.1.0 baseline)

> Archived snapshot only. It is not the current release-readiness source. At the time this report was prepared, the candidate was 2.2.0/versionCode 102; consult `PLAY_RELEASE_READINESS_CHECKLIST.md` and `AUTOMATED_TEST_PLAN.md` for the current migration and test coverage.

## Scope

Static review of the Android app source and release configuration at the 2.1.0 release-candidate baseline; the findings below describe that historical snapshot, not later changes. File counts are intentionally omitted because they change frequently.

## Architecture and strengths

- Single-activity Jetpack Compose UI with MVVM, repositories, Room, Hilt, Coroutines/Flow, and WorkManager.
- Product and note data are stored locally. Android Auto Backup and device-to-device transfer use an explicit allowlist; cloud backup is disabled when client-side encryption capability is unavailable.
- Date and expiry-status logic is separated into domain utilities and has boundary-focused unit tests.
- CSV export escapes delimiters and prefixes formula-like values to reduce spreadsheet formula injection risk.
- The app has Turkish and English resources, light/dark themes, product search/filtering, pinned notes, and responsive navigation.

## Release and maintenance notes

- Release builds enable code minification and resource shrinking.
- Google Play Billing remains for checking and restoring legacy ad-free entitlements. New in-app purchase UI was removed in favor of optional, user-initiated rewarded ads; paid legacy users remain ad-free.
- The original Room database name, `skt_plus.db`, is retained for existing local data.
- GitHub Actions runs unit tests, strict Android lint, a debug APK build, and connected instrumentation tests on an API 35 emulator for the configured pull-request, main-branch, version-tag, and manual triggers. Signed release artifacts require signing and production AdMob secrets.
- Default category labels are localized at display time; notification permission is requested only after a user action in notification settings.

## Remaining limitations

- Automated coverage includes domain/model and search behavior, BillingViewModel restoration states, a Room 1→2 migration/data-preservation test, and basic Compose navigation/onboarding smoke tests. There is not yet a deterministic ReminderWorker, CSV export, backup/restore, notification delivery, live Billing, or live AdMob/UMP integration test.
- CSV export is one-way; the app does not yet import or restore from an exported file.
- Release signing, the closed-test upgrade path, real-device backup/restore and notification behavior, Play Console purchase-history checks, and AdMob/UMP serving/consent setup still need verification in the actual owner accounts and on test devices before global publication.
