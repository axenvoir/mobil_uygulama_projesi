# Expira

Expira is an Android app for tracking product expiry dates and keeping personal notes. The app is available in Turkish and English.

## What it does

- Stores products, categories, expiry dates, quantities, and storage locations on the device.
- Shows products by expiry status and supports search and filtering.
- Keeps searchable, pinnable notes.
- Schedules best-effort daily reminders for products expiring within the next three days. Android may delay background work, and notifications require permission on Android 13 and later.
- Exports products and notes as UTF-8 CSV through Android's document picker. This is a one-way export; the app does not currently import CSV or restore a backup from it.
- Supports light, dark, and optional Android dynamic-color themes.
- Offers optional user-initiated rewarded ads for an in-app point and a cosmetic supporter badge; existing Play ad-free entitlements remain honored.

## Technology

- Kotlin, Jetpack Compose, and Material 3
- Android Gradle Plugin 9.4.1, Gradle 9.8.2, and Kotlin 2.4.21
- MVVM with repositories and Kotlin Coroutines/Flow
- Room for on-device storage, Hilt for dependency injection, and WorkManager for reminders
- minSdk 26; compileSdk 37; targetSdk 37

## Build

Use Android Studio with Java 17, or build a debug APK with the included Gradle wrapper:

```sh
./gradlew :app:assembleDebug
```

Run unit tests, Android lint, and a debug build locally with:

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

To run the Room migration and Compose smoke tests on an attached Android device or emulator, use `./scripts/run_automated_tests.sh --connected`. GitHub Actions runs unit tests, lint, a debug APK build, and the instrumentation suite on an API 35 emulator for pull requests targeting `main`, pushes to `main`, `v*` tags, and manual runs. Signed release artifacts are generated only when release-signing and production AdMob secrets are configured. Dependabot checks Gradle and GitHub Actions dependencies weekly and opens update pull requests.

## Release signing

For local signing, copy `keystore.properties.example` to `keystore.properties`, fill in the private values, and keep the keystore and properties file out of Git. GitHub Actions can create signed APK and AAB artifacts on pushes to `main`, version tags, and manual runs when the signing secrets are configured.

## Compatibility and data

The public application ID (`com.expira.app.tr`), internal Kotlin namespace (`com.axenvoir.expira`), Room database filename (`skt_plus.db`), and signing key are intentionally preserved. Changing these can prevent existing users from installing an update or accessing their saved data. Product, category, and note data remain on-device; Android cloud backup is explicitly limited to this data and selected preferences and is skipped when the device cannot provide client-side encryption. Device-to-device transfer is also enabled for the same allowlist. CSV export remains a separate one-way copy.
