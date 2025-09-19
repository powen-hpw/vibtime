package com.example.vibtime.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.vibtime.utils.WatchModeManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * 改進的安全機制管理器
 * 提供視覺化倒數、簡化通知、用戶控制等功能
 */
object SafetyManager {
    private const val TAG = "SafetyManager"
    
    // 安全設定常數
    const val MAX_SERVICE_RUNTIME = 3 * 60 * 60 * 1000L // 3小時
    const val MIN_VIBRATION_INTERVAL = 10 * 60 * 1000L // 10分鐘
    const val AUTO_STOP_WARNING_TIME = 15 * 60 * 1000L // 15分鐘前警告
    
    // 測試模式常數
    const val TEST_MAX_SERVICE_RUNTIME = 5 * 60 * 1000L // 5分鐘
    const val TEST_MIN_VIBRATION_INTERVAL = 30 * 1000L // 30秒
    
    // 回調接口
    interface SafetyCallback {
        fun onServiceTimeUpdate(remainingTime: Long, progress: Float)
        fun onNextVibrationUpdate(remainingTime: Long, progress: Float)
        fun onServiceExpired()
        fun onFrequencyLimit(remainingTime: Long)
        fun onAutoStopWarning()
        fun onAutoStop()
        fun onWatchModeCooldownUpdate(remainingTime: Long, progress: Float)
        fun onWatchModeStatusChanged(status: WatchModeManager.WatchModeStatus)
    }
    
    private var callback: SafetyCallback? = null
    private var updateHandler: Handler? = null
    private var isTestMode = false
    private var context: Context? = null
    private var watchModeManager: WatchModeManager? = null
    
    /**
     * 初始化安全管理器
     */
    fun initialize(context: Context, callback: SafetyCallback, testMode: Boolean = false) {
        this.context = context
        this.callback = callback
        this.isTestMode = testMode
        this.updateHandler = Handler(Looper.getMainLooper())
        
        // 初始化 Watch Mode 管理器
        this.watchModeManager = WatchModeManager(context)
        this.watchModeManager?.initialize(object : WatchModeManager.WatchModeCallback {
            override fun onWatchModeStarted(duration: Long) {
                callback.onWatchModeStatusChanged(watchModeManager?.getStatus() ?: return)
            }
            
            override fun onWatchModeStopped(reason: String) {
                callback.onWatchModeStatusChanged(watchModeManager?.getStatus() ?: return)
            }
            
            override fun onTimeVibrationTriggered() {
                // 記錄震動時間
                recordVibration(context)
            }
            
            override fun onCooldownStarted(remainingTime: Long) {
                val progress = 1f - (remainingTime.toFloat() / WatchModeManager.COOLDOWN_PERIOD)
                callback.onWatchModeCooldownUpdate(remainingTime, progress)
            }
            
            override fun onCooldownEnded() {
                callback.onWatchModeCooldownUpdate(0L, 1f)
            }
            
            override fun onError(message: String) {
                Log.e(TAG, "Watch Mode error: $message")
            }
        })
        
        Log.d(TAG, "SafetyManager initialized, testMode=$testMode")
    }
    
    /**
     * 開始安全監控
     */
    fun startMonitoring() {
        updateHandler?.post(object : Runnable {
            override fun run() {
                updateSafetyStatus()
                updateHandler?.postDelayed(this, 1000) // 每秒更新一次
            }
        })
        
        Log.d(TAG, "Safety monitoring started")
    }
    
    /**
     * 停止安全監控
     */
    fun stopMonitoring() {
        updateHandler?.removeCallbacksAndMessages(null)
        Log.d(TAG, "Safety monitoring stopped")
    }
    
    /**
     * 更新安全狀態
     */
    private fun updateSafetyStatus() {
        val ctx = context ?: return
        val sharedPreferences = ctx.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        
        val currentTime = System.currentTimeMillis()
        val serviceStartTime = sharedPreferences.getLong("service_start_time", 0L)
        val lastVibrationTime = sharedPreferences.getLong("last_vibration_time", 0L)
        
        val maxRuntime = if (isTestMode) TEST_MAX_SERVICE_RUNTIME else MAX_SERVICE_RUNTIME
        val minInterval = if (isTestMode) TEST_MIN_VIBRATION_INTERVAL else MIN_VIBRATION_INTERVAL
        
        // 檢查服務時間
        val serviceRunTime = currentTime - serviceStartTime
        val remainingServiceTime = maxOf(0, maxRuntime - serviceRunTime)
        val serviceProgress = if (maxRuntime > 0) (serviceRunTime.toFloat() / maxRuntime) else 0f
        
        // 檢查震動頻率
        val timeSinceLastVibration = currentTime - lastVibrationTime
        val nextVibrationTime = maxOf(0, minInterval - timeSinceLastVibration)
        val vibrationProgress = if (minInterval > 0) (timeSinceLastVibration.toFloat() / minInterval) else 1f
        
        // 回調更新
        callback?.onServiceTimeUpdate(remainingServiceTime, serviceProgress)
        callback?.onNextVibrationUpdate(nextVibrationTime, vibrationProgress)
        
        // 檢查特殊情況
        if (remainingServiceTime <= 0) {
            callback?.onServiceExpired()
        } else if (remainingServiceTime <= AUTO_STOP_WARNING_TIME && remainingServiceTime > 0) {
            callback?.onAutoStopWarning()
        }
        
        if (nextVibrationTime > 0) {
            callback?.onFrequencyLimit(nextVibrationTime)
        }
    }
    
    /**
     * 記錄震動時間
     */
    fun recordVibration(context: Context) {
        val sharedPreferences = context.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        val currentTime = System.currentTimeMillis()
        
        sharedPreferences.edit()
            .putLong("last_vibration_time", currentTime)
            .apply()
        
        Log.d(TAG, "Vibration recorded at ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTime))}")
    }
    
    /**
     * 記錄服務啟動時間
     */
    fun recordServiceStart(context: Context) {
        val sharedPreferences = context.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        val currentTime = System.currentTimeMillis()
        
        sharedPreferences.edit()
            .putLong("service_start_time", currentTime)
            .apply()
        
        Log.d(TAG, "Service start recorded at ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTime))}")
    }
    
    /**
     * 緊急停止服務
     */
    fun emergencyStop(context: Context) {
        val sharedPreferences = context.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        
        sharedPreferences.edit()
            .remove("service_start_time")
            .remove("last_vibration_time")
            .apply()
        
        stopMonitoring()
        callback?.onAutoStop()
        
        Log.d(TAG, "Emergency stop executed")
    }
    
    /**
     * 格式化時間顯示
     */
    fun formatTime(milliseconds: Long): String {
        val minutes = milliseconds / (60 * 1000)
        val seconds = (milliseconds % (60 * 1000)) / 1000
        return String.format("%d:%02d", minutes, seconds)
    }
    
    /**
     * 檢查是否可以觸發震動
     */
    fun canTriggerVibration(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        val lastVibrationTime = sharedPreferences.getLong("last_vibration_time", 0L)
        val currentTime = System.currentTimeMillis()
        
        val minInterval = if (isTestMode) TEST_MIN_VIBRATION_INTERVAL else MIN_VIBRATION_INTERVAL
        val timeSinceLastVibration = currentTime - lastVibrationTime
        
        return timeSinceLastVibration >= minInterval
    }
    
    /**
     * 檢查服務是否超時
     */
    fun isServiceExpired(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        val serviceStartTime = sharedPreferences.getLong("service_start_time", 0L)
        val currentTime = System.currentTimeMillis()
        
        val maxRuntime = if (isTestMode) TEST_MAX_SERVICE_RUNTIME else MAX_SERVICE_RUNTIME
        val serviceRunTime = currentTime - serviceStartTime
        
        return serviceRunTime >= maxRuntime
    }
    
    // Watch Mode 相關方法
    
    /**
     * 啟動 Watch Mode
     */
    fun startWatchMode(duration: Long = WatchModeManager.WATCH_MODE_DURATION_10MIN): Boolean {
        return watchModeManager?.startWatchMode(duration) ?: false
    }
    
    /**
     * 停止 Watch Mode
     */
    fun stopWatchMode() {
        watchModeManager?.stopWatchMode()
    }
    
    /**
     * 獲取 Watch Mode 狀態
     */
    fun getWatchModeStatus(): WatchModeManager.WatchModeStatus? {
        return watchModeManager?.getStatus()
    }
    
    /**
     * 檢查是否可以啟動 Watch Mode
     */
    fun canStartWatchMode(): Boolean {
        return watchModeManager?.canStartWatchMode() ?: false
    }
    
    /**
     * 檢查是否在 Watch Mode 冷卻期
     */
    fun isInWatchModeCooldown(): Boolean {
        return watchModeManager?.getStatus()?.isInCooldown ?: false
    }
    
    /**
     * 獲取 Watch Mode 冷卻期剩餘時間
     */
    fun getWatchModeCooldownRemaining(): Long {
        return watchModeManager?.getStatus()?.remainingCooldown ?: 0L
    }
}
