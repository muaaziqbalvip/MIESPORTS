# Firebase / Firestore model classes need their fields kept for reflection-based (de)serialization.
-keepclassmembers class com.miesport.app.data.model.** {
    *;
}
-keep class com.miesport.app.data.model.** { *; }

# Firebase
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Kotlin coroutines
-dontwarn kotlinx.coroutines.**

# YouTube player library
-keep class com.pierfrancescosoffritti.androidyoutubeplayer.** { *; }
