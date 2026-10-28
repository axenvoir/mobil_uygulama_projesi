-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

-keep class dagger.hilt.** { *; }
-keep,allowobfuscation @interface dagger.hilt.*.internal.*

# Room entities (reflection on column names)
-keep class com.axenvoir.sktplus.data.local.** { *; }
-keep class com.axenvoir.sktplus.domain.model.** { *; }

# Hilt generated components
-keep class com.axenvoir.sktplus.**Hilt* { *; }
-keep class com.axenvoir.sktplus.**_Factory { *; }
-keep class com.axenvoir.sktplus.**_MembersInjector { *; }

# Kotlin coroutines
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    *;
}

# WorkManager
-keep class androidx.work.** { *; }
-keep class com.axenvoir.sktplus.notification.** { *; }

# Compose
-dontwarn androidx.compose.**


# CrashReporter
-keep class com.axenvoir.sktplus.CrashReporter { *; }
-keep class com.axenvoir.sktplus.CrashDisplayActivity { *; }
