package com.example.vibtime.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.vibtime.R
import com.example.vibtime.utils.WatchModeManager
import kotlin.math.sqrt

/**
 * 敲擊偵測服務
 * 使用加速度計偵測敲擊手機的動作
 * 整合 WatchModeManager 實現合規的限時監聽
 */
class TapDetectionService(
    private val context: Context,
    private val onTapDetected: () -> Unit,
    private val onTapCountChanged: ((count: Int, maxCount: Int) -> Unit)? = null,
    private val onSensorDataReceived: ((acceleration: Float) -> Unit)? = null,
    private val onWatchModeStatusChanged: ((status: WatchModeManager.WatchModeStatus) -> Unit)? = null
) : SensorEventListener, WatchModeManager.WatchModeCallback {
    
    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = 
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val vibrator: Vibrator = 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    
    // 敲擊偵測參數
    private var tapSensitivity = TapSensitivity.MEDIUM
    private var requiredTapCount = 2
    private var isEnabled = false
    
    // 敲擊偵測邏輯變數
    private var tapCount = 0
    private var lastTapTime = 0L
    private val tapTimeWindow = 2000L // 2秒內的敲擊才算連續
    private val tapCooldown = 300L // 敲擊後的冷卻時間（更快響應）
    private var lastProcessTime = 0L
    
    // Watch Mode 管理器
    private lateinit var watchModeManager: WatchModeManager
    private var useWatchMode = false // 是否使用 Watch Mode
    
    companion object {
        private const val TAG = "TapDetectionService"
    }
    
    /**
     * 敲擊靈敏度設定
     */
    enum class TapSensitivity(val threshold: Float) {
        LOW(4.0f),
        MEDIUM(2.5f), 
        HIGH(1.5f);
        
        fun getDescription(context: Context): String {
            return when (this) {
                LOW -> context.getString(R.string.sensitivity_low_desc)
                MEDIUM -> context.getString(R.string.sensitivity_medium_desc)
                HIGH -> context.getString(R.string.sensitivity_high_desc)
            }
        }
    }
    
    /**
     * 啟動敲擊偵測
     * @param useWatchMode 是否使用 Watch Mode (合規模式)
     * @param watchModeDuration Watch Mode 持續時間
     */
    fun startDetection(useWatchMode: Boolean = false, watchModeDuration: Long = WatchModeManager.WATCH_MODE_DURATION_10MIN) {
        this.useWatchMode = useWatchMode
        
        if (useWatchMode) {
            // 使用 Watch Mode (合規模式)
            startWatchModeDetection(watchModeDuration)
        } else {
            // 使用傳統模式 (僅用於測試)
            startTraditionalDetection()
        }
    }
    
    /**
     * 啟動 Watch Mode 偵測 (合規模式)
     */
    private fun startWatchModeDetection(duration: Long) {
        if (!::watchModeManager.isInitialized) {
            watchModeManager = WatchModeManager(context)
            watchModeManager.initialize(this)
        }
        
        val success = watchModeManager.startWatchMode(duration)
        if (success) {
            isEnabled = true
            Log.d(TAG, "Watch Mode detection started for ${duration / 60000} minutes")
        } else {
            Log.e(TAG, "Failed to start Watch Mode detection")
        }
    }
    
    /**
     * 啟動傳統偵測 (僅用於測試)
     */
    private fun startTraditionalDetection() {
        if (accelerometer == null) {
            Log.e(TAG, context.getString(R.string.tap_detection_accelerometer_unavailable))
            return
        }
        
        isEnabled = true
        sensorManager.registerListener(
            this, 
            accelerometer, 
            SensorManager.SENSOR_DELAY_UI
        )
        Log.d(TAG, context.getString(R.string.tap_detection_started, tapSensitivity.getDescription(context), requiredTapCount))
    }
    
    /**
     * 停止敲擊偵測
     */
    fun stopDetection() {
        isEnabled = false
        
        if (useWatchMode && ::watchModeManager.isInitialized) {
            watchModeManager.stopWatchMode()
        } else {
            sensorManager.unregisterListener(this)
        }
        
        resetTapCount()
        Log.d(TAG, context.getString(R.string.tap_detection_stopped))
    }
    
    /**
     * 設定敲擊靈敏度
     */
    fun setSensitivity(sensitivity: TapSensitivity) {
        tapSensitivity = sensitivity
        Log.d(TAG, context.getString(R.string.tap_detection_sensitivity_set, sensitivity.getDescription(context)))
    }
    
    /**
     * 設定所需敲擊次數
     */
    fun setRequiredTapCount(count: Int) {
        requiredTapCount = count.coerceIn(1, 5)
        Log.d(TAG, context.getString(R.string.tap_detection_required_taps_set, requiredTapCount))
    }
    
    /**
     * 感應器精度改變（不需處理）
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 不需要處理
    }
    
    /**
     * 感應器數值改變
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (!isEnabled || event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) {
            return
        }
        
        val currentTime = System.currentTimeMillis()
        
        // 避免過度頻繁的處理
        if (currentTime - lastProcessTime < 100) {
            return
        }
        lastProcessTime = currentTime
        
        // 計算加速度向量的大小
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
        
        // 回報感應器數據
        onSensorDataReceived?.invoke(acceleration)
        
        // 檢查是否超過敲擊閾值
        if (acceleration > tapSensitivity.threshold) {
            processTap(currentTime)
        }
        
        // 檢查敲擊時間窗口，重置計數
        if (currentTime - lastTapTime > tapTimeWindow) {
            resetTapCount()
        }
    }
    
    /**
     * 處理敲擊事件
     */
    private fun processTap(currentTime: Long) {
        // 冷卻時間內忽略
        if (currentTime - lastTapTime < tapCooldown) {
            return
        }
        
        tapCount++
        lastTapTime = currentTime
        
        Log.d(TAG, context.getString(R.string.tap_detection_tap_detected, tapCount, requiredTapCount))
        
        // 通知敲擊計數變化
        onTapCountChanged?.invoke(tapCount, requiredTapCount)
        
        // 給予觸覺回饋
        provideTapFeedback()
        
        // 檢查是否達到所需敲擊次數
        if (tapCount >= requiredTapCount) {
            Log.d(TAG, context.getString(R.string.tap_detection_tap_completed))
            onTapDetected()
            resetTapCount()
        }
    }
    
    /**
     * 提供敲擊觸覺回饋
     */
    private fun provideTapFeedback() {
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }
    
    /**
     * 重置敲擊計數
     */
    private fun resetTapCount() {
        if (tapCount > 0) {
            Log.d(TAG, context.getString(R.string.tap_detection_reset_count))
            tapCount = 0
            onTapCountChanged?.invoke(tapCount, requiredTapCount)
        }
    }
    
    /**
     * 檢查感應器是否可用
     */
    fun isAccelerometerAvailable(): Boolean {
        return accelerometer != null
    }
    
    /**
     * 取得目前設定資訊
     */
    fun getConfigInfo(): String {
        return context.getString(R.string.tap_detection_status_format, tapSensitivity.getDescription(context), requiredTapCount, if (isEnabled) context.getString(R.string.tap_detection_status_enabled) else context.getString(R.string.tap_detection_status_disabled))
    }
    
    // WatchModeManager.WatchModeCallback 實現
    
    override fun onWatchModeStarted(duration: Long) {
        Log.d(TAG, "Watch Mode started for ${duration / 60000} minutes")
        onWatchModeStatusChanged?.invoke(watchModeManager.getStatus())
    }
    
    override fun onWatchModeStopped(reason: String) {
        Log.d(TAG, "Watch Mode stopped: $reason")
        isEnabled = false
        onWatchModeStatusChanged?.invoke(watchModeManager.getStatus())
    }
    
    override fun onTimeVibrationTriggered() {
        Log.d(TAG, "Time vibration triggered via Watch Mode")
        onTapDetected()
    }
    
    override fun onCooldownStarted(remainingTime: Long) {
        Log.d(TAG, "Cooldown started: ${remainingTime / 1000}s remaining")
    }
    
    override fun onCooldownEnded() {
        Log.d(TAG, "Cooldown ended")
    }
    
    override fun onError(message: String) {
        Log.e(TAG, "Watch Mode error: $message")
    }
    
    /**
     * 獲取 Watch Mode 狀態
     */
    fun getWatchModeStatus(): WatchModeManager.WatchModeStatus? {
        return if (::watchModeManager.isInitialized) {
            watchModeManager.getStatus()
        } else null
    }
    
    /**
     * 檢查是否可以啟動 Watch Mode
     */
    fun canStartWatchMode(): Boolean {
        return if (::watchModeManager.isInitialized) {
            watchModeManager.canStartWatchMode()
        } else false
    }
}
