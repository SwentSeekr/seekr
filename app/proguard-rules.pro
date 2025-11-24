# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Firebase model classes
-keep class com.swentseekr.seekr.model.** { *; }
-keepclassmembers class com.swentseekr.seekr.model.** { *; }

# Keep the Profile data class
-keep class com.swentseekr.seekr.ui.profile.Profile { *; }

# Keep Kotlinx serialization runtime
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Keep generated serializers
-keepclassmembers class **$$serializer { *; }

# Keep any class annotated with @Serializable and its members
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable *;
}