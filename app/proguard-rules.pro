-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

-keep class dagger.hilt.** { *; }
-keep,allowobfuscation @interface dagger.hilt.*.internal.*

# Room entities (reflection on column names)
-keep class com.axenvoir.sktplus.data.local.** { *; }

# Hilt generated components
-keep class com.axenvoir.sktplus.**Hilt* { *; }
-keep,allowobfuscation @interface dagger.hilt.*.internal.*

# Kotlin coroutines
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Compose
-dontwarn androidx.compose.**
