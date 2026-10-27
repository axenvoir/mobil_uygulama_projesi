# Expira

Expira helps you track product expiry dates and keep notes from one place.

- Kotlin, Jetpack Compose, Material 3 with light and dark themes
- MVVM + Repository, Room, Hilt, Coroutines/Flow
- Best-effort daily WorkManager reminders for products expiring within 3 days
- Localized in Turkish and English
- minSdk 26, targetSdk 36

## Build

The Gradle wrapper is included. Open the project in Android Studio with Java 17, or run:

    ./gradlew :app:assembleDebug

To run the checks used by GitHub Actions:

    ./gradlew :app:testDebugUnitTest :app:lintDebug

## Release signing

Create a local `keystore.properties` from `keystore.properties.example` and keep the keystore file private. GitHub Actions uses repository secrets to build signed release APK and AAB artifacts on pushes to `main`, version tags, and manual runs. Pull requests run tests, lint, and a debug build without release secrets.

The application ID, database file name, and signing identity are kept stable so existing installations and their data can be updated safely.
