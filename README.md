# Expira

Expira is an Android app for tracking product expiry dates and keeping personal notes. The app is available in Turkish and English.

## What it does

- Stores products, categories, expiry dates, quantities, and storage locations on the device.
- Shows products by expiry status and supports search and filtering.
- Keeps searchable, pinnable notes.
- Schedules best-effort daily reminders for products expiring within the next three days. Android may delay background work, and notifications require permission on Android 13 and later.
- Exports products and notes as UTF-8 CSV through Android's document picker. This is a one-way export; the app does not currently import CSV or restore a backup from it.
- Supports light and dark themes.

## Technology

- Kotlin, Jetpack Compose, and Material 3
- Android Gradle Plugin 9.3.1, Gradle 9.5.0, and Kotlin 2.4.21
- MVVM with repositories and Kotlin Coroutines/Flow
- Room for on-device storage, Hilt for dependency injection, and WorkManager for reminders
- minSdk 26; compileSdk 37; targetSdk 36

## Build

Use Android Studio with Java 17, or build a debug APK with the included Gradle wrapper:

```sh
./gradlew :app:assembleDebug
```

The GitHub Actions verification workflow runs unit tests, Android lint, and a debug build for pull requests and updates to `main`. Dependabot checks Gradle and GitHub Actions dependencies weekly and opens update pull requests.

## Release signing

For local signing, copy `keystore.properties.example` to `keystore.properties`, fill in the private values, and keep the keystore and properties file out of Git. GitHub Actions can create signed APK and AAB artifacts on pushes to `main`, version tags, and manual runs when the signing secrets are configured.

## Compatibility and data

The public application ID (`com.expira.app.tr`), internal Kotlin namespace (`com.axenvoir.expira`), Room database filename (`expira.db`), and signing key are intentionally preserved. Changing these can prevent existing users from installing an update or accessing their saved data. The app currently keeps product and note data locally; CSV export is the available way to copy that data outside the app.
#inceleme başlatıldı.
