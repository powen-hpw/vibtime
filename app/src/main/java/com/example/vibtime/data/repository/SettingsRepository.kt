package com.example.vibtime.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.vibtime.service.TapDetectionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SettingsRepository - 設定資料存取層
 * 負責管理應用程式的所有設定
 */
class SettingsRepository(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "vibtime_prefs", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        // SharedPreferences 鍵值
        private const val KEY_VIBRATION_SERVICE_RUNNING = "vibration_service_running"
        private const val KEY_HOURLY_VIBRATION_ENABLED = "hourly_vibration_enabled"
        private const val KEY_HALF_HOUR_VIBRATION_ENABLED = "half_hour_vibration_enabled"
        private const val KEY_COUNTDOWN_MINUTES = "countdown_minutes"
        private const val KEY_VIBRATION_MODE = "vibration_mode"
        private const val KEY_TAP_SENSITIVITY = "tap_sensitivity"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_VIBRATION_DURATION = "vibration_duration"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        
        // 預設值
        private const val DEFAULT_COUNTDOWN_MINUTES = 0
        private const val DEFAULT_VIBRATION_MODE = "time"
        private const val DEFAULT_LANGUAGE = "zh-TW"
        private const val DEFAULT_VIBRATION_DURATION = 500L
    }
    
    // 震動服務狀態
    suspend fun isVibrationServiceRunning(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean(KEY_VIBRATION_SERVICE_RUNNING, false)
    }
    
    suspend fun setVibrationServiceRunning(running: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putBoolean(KEY_VIBRATION_SERVICE_RUNNING, running).apply()
    }
    
    // 整點震動設定
    suspend fun isHourlyVibrationEnabled(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean(KEY_HOURLY_VIBRATION_ENABLED, false)
    }
    
    suspend fun setHourlyVibrationEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putBoolean(KEY_HOURLY_VIBRATION_ENABLED, enabled).apply()
    }
    
    // 半點震動設定
    suspend fun isHalfHourVibrationEnabled(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean(KEY_HALF_HOUR_VIBRATION_ENABLED, false)
    }
    
    suspend fun setHalfHourVibrationEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putBoolean(KEY_HALF_HOUR_VIBRATION_ENABLED, enabled).apply()
    }
    
    // 倒數計時設定
    suspend fun getCountdownMinutes(): Int = withContext(Dispatchers.IO) {
        sharedPreferences.getInt(KEY_COUNTDOWN_MINUTES, DEFAULT_COUNTDOWN_MINUTES)
    }
    
    suspend fun setCountdownMinutes(minutes: Int) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putInt(KEY_COUNTDOWN_MINUTES, minutes).apply()
    }
    
    // 震動模式設定
    suspend fun getVibrationMode(): String = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_VIBRATION_MODE, DEFAULT_VIBRATION_MODE) ?: DEFAULT_VIBRATION_MODE
    }
    
    suspend fun setVibrationMode(mode: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putString(KEY_VIBRATION_MODE, mode).apply()
    }
    
    // 敲擊靈敏度設定
    suspend fun getTapSensitivity(): TapDetectionService.TapSensitivity = withContext(Dispatchers.IO) {
        val sensitivityName = sharedPreferences.getString(KEY_TAP_SENSITIVITY, TapDetectionService.TapSensitivity.MEDIUM.name)
        try {
            TapDetectionService.TapSensitivity.valueOf(sensitivityName ?: TapDetectionService.TapSensitivity.MEDIUM.name)
        } catch (e: IllegalArgumentException) {
            TapDetectionService.TapSensitivity.MEDIUM
        }
    }
    
    suspend fun setTapSensitivity(sensitivity: TapDetectionService.TapSensitivity) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putString(KEY_TAP_SENSITIVITY, sensitivity.name).apply()
    }
    
    // 語言設定
    suspend fun getLanguage(): String = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }
    
    suspend fun setLanguage(language: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putString(KEY_LANGUAGE, language).apply()
    }
    
    // 深色模式設定
    suspend fun isDarkModeEnabled(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean(KEY_DARK_MODE, false)
    }
    
    suspend fun setDarkModeEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }
    
    // 通知設定
    suspend fun isNotificationsEnabled(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }
    
    // 震動時長設定
    suspend fun getVibrationDuration(): Long = withContext(Dispatchers.IO) {
        sharedPreferences.getLong(KEY_VIBRATION_DURATION, DEFAULT_VIBRATION_DURATION)
    }
    
    suspend fun setVibrationDuration(duration: Long) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putLong(KEY_VIBRATION_DURATION, duration).apply()
    }
    
    // 首次啟動設定
    suspend fun isFirstLaunch(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    suspend fun setFirstLaunch(isFirst: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, isFirst).apply()
    }
    
    // 重置所有設定
    suspend fun resetAllSettings() = withContext(Dispatchers.IO) {
        sharedPreferences.edit().clear().apply()
    }
    
    // 獲取所有設定摘要
    suspend fun getSettingsSummary(): Map<String, Any> = withContext(Dispatchers.IO) {
        mapOf(
            "vibrationServiceRunning" to isVibrationServiceRunning(),
            "hourlyVibrationEnabled" to isHourlyVibrationEnabled(),
            "halfHourVibrationEnabled" to isHalfHourVibrationEnabled(),
            "countdownMinutes" to getCountdownMinutes(),
            "vibrationMode" to getVibrationMode(),
            "tapSensitivity" to getTapSensitivity().name,
            "language" to getLanguage(),
            "darkModeEnabled" to isDarkModeEnabled(),
            "notificationsEnabled" to isNotificationsEnabled(),
            "vibrationDuration" to getVibrationDuration(),
            "firstLaunch" to isFirstLaunch()
        )
    }
}
