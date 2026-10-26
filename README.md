# SKT Plus

Android app for tracking product expiry dates (SKT) and taking notes.

- Kotlin, Jetpack Compose, Material 3 (dynamic color + dark theme)
- MVVM + Repository, Room, Hilt, Coroutines/Flow
- WorkManager daily reminders (3-day expiry threshold)
- Localized: Turkish (default) + English
- minSdk 26, targetSdk 35

## Build

Open in Android Studio (Java 17) or run:

    ./gradlew :app:assembleDebug

## Notes

The Gradle wrapper JAR is not committed; run `gradle wrapper` once with a local Gradle 8.9+ install, or open the project in Android Studio to generate it.
