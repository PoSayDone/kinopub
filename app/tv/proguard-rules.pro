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

# Strip all android.util.Log calls from release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
    public static int wtf(...);
}

# Gson uses reflection to match JSON keys to field names.
# Obfuscation renames fields (e.g. "title" -> "a"), breaking deserialization.
# Disable obfuscation entirely: class names stay readable, Log stripping still works.
-dontobfuscate

# Keep model and network DTO classes so Gson reflection finds the right fields.
-keep class io.github.posaydone.kinopub.core.model.** { *; }
-keep class io.github.posaydone.kinopub.core.network.** { *; }

# Preserve generic type signatures needed by Gson TypeToken / TypeAdapters.
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit service interfaces
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**