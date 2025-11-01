# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# FFmpeg rules
-keep class com.arthenica.mobileffmpeg.** { *; }
-dontwarn com.arthenica.mobileffmpeg.**

# AdMob rules
-keep public class com.google.android.gms.ads.** {
   public *;
}

-keep class com.google.ads.** { *; }

# Material components
-keep class com.google.android.material.** { *; }
-keep public class androidx.appcompat.widget.** { *; }

# Keep - Applications. Keep all application classes, as they may be accessed dynamically.
-keep public class * extends android.app.Application

# Keep - native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep - Enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep - Parcelable implementations
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep - View bindings
-keep class * extends androidx.viewbinding.ViewBinding {
    public static * bind(android.view.View);
    public static * inflate(android.view.LayoutInflater);
}

# Remove debug logs in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
