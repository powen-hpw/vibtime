# Vibtime ProGuard Rules
# 保留應用程式核心功能

# 保留所有 Fragment 類別
-keep class com.example.vibtime.ui.** { *; }

# 保留所有 Service 類別
-keep class com.example.vibtime.service.** { *; }

# 保留所有 Activity 類別
-keep class com.example.vibtime.MainActivity { *; }
-keep class com.example.vibtime.VibtimeApplication { *; }

# 保留 Navigation 相關類別
-keep class com.example.vibtime.navigation.** { *; }

# 保留資料庫相關類別
-keep class com.example.vibtime.data.** { *; }

# 保留 Material Design 組件
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# 保留 AndroidX 組件
-keep class androidx.** { *; }
-dontwarn androidx.**

# 保留 Navigation 組件
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# 保留 ViewBinding 生成的類別
-keep class com.example.vibtime.databinding.** { *; }

# 保留字串資源
-keep class com.example.vibtime.R$string { *; }

# 保留震動相關功能
-keep class android.os.Vibrator { *; }
-keep class android.os.VibrationEffect { *; }

# 保留通知相關功能
-keep class android.app.NotificationManager { *; }
-keep class android.app.NotificationChannel { *; }
-keep class androidx.core.app.NotificationCompat { *; }

# 保留 SharedPreferences
-keep class android.content.SharedPreferences { *; }

# 保留 PowerManager 相關功能
-keep class android.os.PowerManager { *; }

# 保留 BroadcastReceiver 相關功能
-keep class android.content.BroadcastReceiver { *; }

# 保留 Intent 相關功能
-keep class android.content.Intent { *; }

# 保留 Context 相關功能
-keep class android.content.Context { *; }

# 保留 Fragment 相關功能
-keep class androidx.fragment.app.Fragment { *; }

# 保留 Activity 相關功能
-keep class androidx.appcompat.app.AppCompatActivity { *; }

# 保留 Service 相關功能
-keep class android.app.Service { *; }

# 保留 Application 相關功能
-keep class android.app.Application { *; }

# 保留 View 相關功能
-keep class android.view.View { *; }
-keep class android.view.ViewGroup { *; }

# 保留 Layout 相關功能
-keep class android.widget.** { *; }

# 保留 Log 功能 (可選，發布時可以移除)
-keep class android.util.Log { *; }

# 保留堆疊追蹤資訊
-keepattributes SourceFile,LineNumberTable

# 保留註解資訊
-keepattributes *Annotation*

# 保留泛型資訊
-keepattributes Signature

# 保留內部類別
-keepattributes InnerClasses

# 移除未使用的程式碼
-dontwarn android.support.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# 優化設定
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# 保留行號資訊用於除錯
-keepattributes SourceFile,LineNumberTable

# 隱藏原始檔案名稱
-renamesourcefileattribute SourceFile