# Codebase Analysis Report

## Scope

Static review of the Android app source and release configuration. The app has 57 Kotlin source files and 3 unit-test files.

## Architecture and strengths

- Single-activity Jetpack Compose UI with MVVM, repositories, Room, Hilt, Coroutines/Flow, and WorkManager.
- Product and note data are stored locally; Android backup is disabled.
- Date and expiry-status logic is separated into domain utilities and has boundary-focused unit tests.
- CSV export escapes delimiters and prefixes formula-like values to reduce spreadsheet formula injection risk.
- The app has Turkish and English resources, light/dark themes, product search/filtering, pinned notes, and responsive navigation.

## Release and maintenance notes

- Release builds enable code minification and resource shrinking.
- Google Play Billing is on version 8.3.0. Subscription purchases pass an available offer token, and entitlement refresh queries both subscriptions and one-time products.
- The original Room database name, `skt_plus.db`, is retained for existing local data.
- CI currently builds a debug APK but does not run unit tests or Android lint.
- Default category labels are localized at display time; notification permission is requested only after a user action in notification settings.

## Remaining limitations

- Automated coverage is limited to domain/model tests; billing, database migrations, repositories, WorkManager, and UI flows still need focused tests. The CI workflow should also be updated to run its existing unit tests and Android lint task.
- CSV export is one-way; the app does not yet import or restore from an exported file.
- A release build and Play Billing purchase flow should be verified in an Android SDK/Play test environment before publishing.