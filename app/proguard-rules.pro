# 核心 Kotlin 與反射規則
-keepattributes Signature, InnerClasses, EnclosingMethod
-keep class kotlin.Metadata { *; }

# Kotlinx Serialization 規則
-keep class **$$serializer { *; }
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Retrofit & OkHttp 規則
-dontwarn com.squareup.okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-keep class com.squareup.okhttp3.internal.platform.* { *; }
-keep class retrofit2.DefaultCallAdapterFactory { *; }

# 協程與 Suspend Function 支援
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation