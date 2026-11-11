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
# --- Sesion 14: reglas minimas para la version de entrega ---
# Serializacion (kotlinx.serialization): conservar serializadores generados y metadatos.
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisibleAnnotations, EnclosingMethod
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class pe.edu.upeu.pharmamobil.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class pe.edu.upeu.pharmamobil.**$$serializer { *; }
-keepclassmembers class pe.edu.upeu.pharmamobil.** { *** Companion; }
-keep @kotlinx.serialization.Serializable class pe.edu.upeu.pharmamobil.** { *; }
# Inyeccion (Koin): clases resueltas por reflexion desde el contenedor.
-keep class pe.edu.upeu.pharmamobil.** { *; }
-keep class org.koin.** { *; }
-dontwarn org.koin.**
# Ktor y OkHttp
-dontwarn io.ktor.**
-dontwarn org.slf4j.**
-dontwarn okhttp3.**
-dontwarn okio.**
