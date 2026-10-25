# --- React Native core ---
-keep class com.facebook.react.** { *; }
-keep class com.facebook.soloader.** { *; }
-keep class com.facebook.hermes.** { *; }
-dontwarn com.facebook.react.**

# --- App ---
-keep class com.expira.app.tr.** { *; }

# --- Sentry ---
-keep class io.sentry.** { *; }
-keep class io.sentry.android.** { *; }
-keep class io.sentry.android.ndk.** { *; }
-keepattributes SourceFile,LineNumberTable
-dontwarn io.sentry.**

# --- Notifee ---
-keep class io.invertase.notifee.** { *; }
-dontwarn io.invertase.notifee.**

# --- React Navigation / Screens ---
-keep class com.swmansion.rnscreens.** { *; }
-keep class com.swmansion.gesturehandler.** { *; }
-dontwarn com.swmansion.**

# --- SafeArea / AsyncStorage / Localize ---
-keep class com.th3rdwave.safeareacontext.** { *; }
-keep class com.reactnativecommunity.asyncstorage.** { *; }
-keep class com.reactnativecommunity.localization.** { *; }

# --- JS bundle (Metro) ---
-keep class com.expire.** { *; }
-keepclassmembers class * { native <methods>; }

# Kotlin metadata for reflection-based libs
-keep class kotlin.Metadata.** { *; }
-dontwarn kotlin.**
