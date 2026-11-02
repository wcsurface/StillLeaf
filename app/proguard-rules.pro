# Keep Kotlin stdlib & sequences to prevent IllegalAccessError on launch
-keep class kotlin.sequences.** { *; }
-keep class kotlin.internal.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class * extends kotlin.sequences.Sequence { *; }

# Keep Compose & AndroidX lifecycle
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }
