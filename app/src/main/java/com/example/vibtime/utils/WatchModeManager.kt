package com.example.vibtime.utils

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.vibtime.R
import kotlin.math.sqrt

/**
 * Watch Mode 管理器
 * 實現合規的限時感測器監聽機制，符合 Android 12+ 背景限制
 */
class WatchModeManager(private val context: Context) : SensorEventListener {
    
    companion object {
        private const val TAG = "WatchModeManager"
        
        // 時間常數
        const val WATCH_MODE_DURATION_10MIN = 10 * 60 * 1000L
        const val WATCH_MODE_DURATION_2HOUR = 2 * 60 * 60 * 1000L
        const val COOLDOWN_PERIOD = 15 * 60 * 1000L // 15分鐘冷卻期
        
        // 感測器設定
        private const val SENSOR_DELAY = SensorManager.SENSOR_DELAY_NORMAL
        private const val PROCESS_INTERVAL = 100L // 100ms處理間隔
    }
    
    // 回調接口
    interface WatchModeCallback {
        fun onWatchModeStarted(duration: Long)
        fun onWatchModeStopped(reason: String)
        fun onTimeVibrationTriggered()
        fun onCooldownStarted(remainingTime: Long)
        fun onCooldownEnded()
        fun onError(message: String)
    }
    
    private var callback: WatchModeCallback? = null
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val handler = Handler(Looper.getMainLooper())
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
    
    // 狀態變數
    private var isWatchModeActive = false
    private var watchModeStartTime = 0L
    private var watchModeDuration = WATCH_MODE_DURATION_10MIN
    private var lastVibrationTime = 0L
    private var lastProcessTime = 0L
    
    // 感測器相關
    private var accelerometer: Sensor? = null
    private var isSensorRegistered = false
    
    /**
     * 初始化 Watch Mode 管理器
     */
    fun initialize(callback: WatchModeCallback) {
        this.callback = callback
        
        // 獲取wake-up加速度計
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER, true)
        
        if (accelerometer == null) {
            // 如果沒有wake-up感測器，嘗試普通感測器
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            Log.w(TAG, "Wake-up accelerometer not available, using regular accelerometer")
        }
        
        // 載入上次的震動時間
        lastVibrationTime = sharedPreferences.getLong("last_vibration_time", 0L)
        
        Log.d(TAG, "WatchModeManager initialized")
    }
    
    /**
     * 啟動 Watch Mode
     * @param duration 監聽時長 (10分鐘或2小時)
     * @return 是否成功啟動
     */
    fun startWatchMode(duration: Long = WATCH_MODE_DURATION_10MIN): Boolean {
        if (isWatchModeActive) {
            Log.w(TAG, "Watch Mode is already active")
            return false
        }
        
        // 檢查冷卻期
        if (isInCooldownPeriod()) {
            val remainingCooldown = getRemainingCooldownTime()
            Log.w(TAG, "Still in cooldown period: ${remainingCooldown / 1000}s remaining")
            callback?.onError(context.getString(R.string.watch_mode_cooldown_active, remainingCooldown / 60000))
            return false
        }
        
        // 檢查感測器可用性
        if (accelerometer == null) {
            Log.e(TAG, "Accelerometer not available")
            callback?.onError(context.getString(R.string.watch_mode_sensor_unavailable))
            return false
        }
        
        // 註冊感測器監聽器
        val success = sensorManager.registerListener(
            this,
            accelerometer,
            SENSOR_DELAY
        )
        
        if (!success) {
            Log.e(TAG, "Failed to register sensor listener")
            callback?.onError(context.getString(R.string.watch_mode_sensor_failed))
            return false
        }
        
        // 設定狀態
        isWatchModeActive = true
        isSensorRegistered = true
        watchModeStartTime = System.currentTimeMillis()
        watchModeDuration = duration
        
        // 記錄啟動時間
        sharedPreferences.edit()
            .putLong("watch_mode_start_time", watchModeStartTime)
            .putLong("watch_mode_duration", duration)
            .apply()
        
        // 設定自動停止
        handler.postDelayed({
            stopWatchMode("timeout")
        }, duration)
        
        Log.d(TAG, "Watch Mode started for ${duration / 60000} minutes")
        callback?.onWatchModeStarted(duration)
        
        return true
    }
    
    /**
     * 停止 Watch Mode
     * @param reason 停止原因
     */
    fun stopWatchMode(reason: String = "manual") {
        if (!isWatchModeActive) {
            return
        }
        
        // 取消感測器監聽
        if (isSensorRegistered) {
            sensorManager.unregisterListener(this)
            isSensorRegistered = false
        }
        
        // 取消自動停止計時器
        handler.removeCallbacksAndMessages(null)
        
        // 更新狀態
        isWatchModeActive = false
        
        // 清除記錄
        sharedPreferences.edit()
            .remove("watch_mode_start_time")
            .remove("watch_mode_duration")
            .apply()
        
        Log.d(TAG, "Watch Mode stopped: $reason")
        callback?.onWatchModeStopped(reason)
    }
    
    /**
     * 檢查是否在冷卻期內
     */
    private fun isInCooldownPeriod(): Boolean {
        val timeSinceLastVibration = System.currentTimeMillis() - lastVibrationTime
        return timeSinceLastVibration < COOLDOWN_PERIOD
    }
    
    /**
     * 獲取剩餘冷卻時間
     */
    private fun getRemainingCooldownTime(): Long {
        val timeSinceLastVibration = System.currentTimeMillis() - lastVibrationTime
        return maxOf(0, COOLDOWN_PERIOD - timeSinceLastVibration)
    }
    
    /**
     * 處理時間震動觸發
     */
    private fun onTimeVibrationTriggered() {
        lastVibrationTime = System.currentTimeMillis()
        
        // 記錄震動時間
        sharedPreferences.edit()
            .putLong("last_vibration_time", lastVibrationTime)
            .apply()
        
        // 通知回調
        callback?.onTimeVibrationTriggered()
        
        // 進入冷卻期，自動停止Watch Mode
        stopWatchMode("vibration_triggered")
        
        // 開始冷卻期倒數
        startCooldownCountdown()
        
        Log.d(TAG, "Time vibration triggered, entering cooldown period")
    }
    
    /**
     * 開始冷卻期倒數
     */
    private fun startCooldownCountdown() {
        val cooldownRunnable = object : Runnable {
            override fun run() {
                val remainingTime = getRemainingCooldownTime()
                
                if (remainingTime > 0) {
                    callback?.onCooldownStarted(remainingTime)
                    handler.postDelayed(this, 1000) // 每秒更新
                } else {
                    callback?.onCooldownEnded()
                    Log.d(TAG, "Cooldown period ended")
                }
            }
        }
        
        handler.post(cooldownRunnable)
    }
    
    /**
     * 獲取當前狀態
     */
    fun getStatus(): WatchModeStatus {
        return WatchModeStatus(
            isActive = isWatchModeActive,
            startTime = watchModeStartTime,
            duration = watchModeDuration,
            remainingTime = if (isWatchModeActive) {
                maxOf(0, watchModeDuration - (System.currentTimeMillis() - watchModeStartTime))
            } else 0L,
            isInCooldown = isInCooldownPeriod(),
            remainingCooldown = getRemainingCooldownTime()
        )
    }
    
    /**
     * 檢查是否可以啟動 Watch Mode
     */
    fun canStartWatchMode(): Boolean {
        return !isWatchModeActive && !isInCooldownPeriod() && accelerometer != null
    }
    
    // SensorEventListener 實現
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (!isWatchModeActive || event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) {
            return
        }
        
        val currentTime = System.currentTimeMillis()
        
        // 避免過度頻繁的處理
        if (currentTime - lastProcessTime < PROCESS_INTERVAL) {
            return
        }
        lastProcessTime = currentTime
        
        // 計算加速度向量的大小
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
        
        // 檢查是否超過敲擊閾值 (使用中等敏感度)
        if (acceleration > 2.5f) {
            onTimeVibrationTriggered()
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 不需要處理
    }
    
    /**
     * 清理資源
     */
    fun cleanup() {
        stopWatchMode("cleanup")
        callback = null
    }
    
    /**
     * Watch Mode 狀態數據類
     */
    data class WatchModeStatus(
        val isActive: Boolean,
        val startTime: Long,
        val duration: Long,
        val remainingTime: Long,
        val isInCooldown: Boolean,
        val remainingCooldown: Long
    )
}
