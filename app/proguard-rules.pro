-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep,allowobfuscation @interface dagger.hilt.*.internal.*
-keep class com.axenvoir.expira.**Hilt* { *; }
-keep class com.axenvoir.expira.**_Factory { *; }
-keep class com.axenvoir.expira.**_MembersInjector { *; }
-keep class dagger.hilt.internal.managers.* { *; }
-keep class dagger.hilt.internal.** { *; }

# Hilt ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep,allowobfuscation @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Room
-keep class com.axenvoir.expira.data.local.** { *; }
-keep class com.axenvoir.expira.domain.model.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class com.axenvoir.expira.data.local.**
-keep class kotlin.reflect.** { *; }
-keep class androidx.room.paging.** { *; }

# Kotlin coroutines
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    *;
}

# WorkManager
-keep class androidx.work.** { *; }
-keep class com.axenvoir.expira.notification.** { *; }

# Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# CrashReporter
-keep class com.axenvoir.expira.CrashReporter { *; }
-keep class com.axenvoir.expira.CrashDisplayActivity { *; }

# Gson / JSON (if any)
-keep class com.google.gson.** { *; }
