# Expira

Expira is an Android app for tracking product expiry dates and keeping personal notes. The app is available in Turkish and English.

## What it does

- Stores products, categories, expiry dates, quantities, and storage locations on the device.
- Shows products by expiry status and supports search and filtering.
- Keeps searchable, pinnable notes.
- Schedules best-effort daily reminders for products expiring within the next three days. Android may delay background work, and notifications require permission on Android 13 and later.
- Exports products and notes as UTF-8 CSV through Android's document picker. This is a one-way export; the app does not currently import CSV or restore a backup from it.
- Supports light/dark themes, an original botanical free palette, cosmetic palettes unlocked with points, and optional Android dynamic colors.
- Offers optional user-initiated rewarded ads for non-cash points. Points can permanently unlock cosmetic palettes; five lifetime points unlock a permanent supporter badge. Existing Play ad-free entitlements remain honored.

## Technology

- Kotlin, Jetpack Compose, and Material 3 (Compose BOM 2026.09.00; Material 3 1.4.0, stable versions checked October 30, 2026)
- Android Gradle Plugin 9.4.1, Gradle 9.8.2, and Kotlin 2.2.21
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

To run the Room migration and Compose smoke tests on an attached Android device or emulator, use `./scripts/run_automated_tests.sh --connected`. GitHub Actions runs unit tests, lint, a debug APK build, and the instrumentation suite on an API 35 emulator for pull requests targeting `main` and pushes to `main`. Signed release artifacts are produced only through a manually dispatched release workflow on the matching `v<versionName>` tag, after the exact tag is confirmed and the protected `play-production` environment has the required signing and production AdMob secrets. Dependabot checks Gradle and GitHub Actions dependencies weekly and opens update pull requests.

## Release signing

For local signing, copy `keystore.properties.example` to `keystore.properties`, fill in the private values, and keep the keystore and properties file out of Git. Ordinary pushes to `main` run verification but do not create signed release artifacts. GitHub Actions creates signed APK and AAB artifacts only through the gated manual release flow described above.

## Compatibility and data

The public application ID (`com.expira.app.tr`), internal Kotlin namespace (`com.axenvoir.expira`), Room database filename (`skt_plus.db`), and signing key are intentionally preserved. Changing these can prevent existing users from installing an update or accessing their saved data. The main database stores products, categories, notes, the local support-points wallet, and unlocked cosmetic palettes. The Android 12+ cloud-backup rules opt out when the OS reports that client-side encryption is unavailable; the legacy full-backup rules for Android 11 and below do not provide the same encryption-capability guarantee. Device-to-device transfer uses the same allowlist. The old `support_reward_preferences.xml` file is temporarily included so a pre-Room points balance can be imported once; the pending-reward retry-ID file is excluded. CSV export remains a separate one-way copy.
