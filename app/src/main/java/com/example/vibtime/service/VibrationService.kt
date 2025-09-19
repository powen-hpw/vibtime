package com.example.vibtime.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.vibtime.MainActivity
import com.example.vibtime.R
import com.example.vibtime.utils.SafetyManager
import com.example.vibtime.utils.NotificationHelper
import com.example.vibtime.utils.WatchModeManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Vibration Foreground Service
 * 背景運行的震動服務，支援螢幕關閉時的敲擊偵測
 */
class VibrationService : Service() {
    
    private lateinit var tapDetectionService: TapDetectionService
    private lateinit var screenReceiver: ScreenReceiver
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceRunning = false
    
    companion object {
        private const val TAG = "VibrationService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "VibrationServiceChannel"
        
        // Service Control Actions
        const val ACTION_START_SERVICE = "START_SERVICE"
        const val ACTION_STOP_SERVICE = "STOP_SERVICE"
        const val ACTION_UPDATE_SENSITIVITY = "UPDATE_SENSITIVITY"
        
        // Intent Extras
        const val EXTRA_SENSITIVITY = "sensitivity"
        
        /**
         * 啟動服務的靜態方法
         */
        fun startService(context: Context, sensitivity: TapDetectionService.TapSensitivity = TapDetectionService.TapSensitivity.MEDIUM) {
            val intent = Intent(context, VibrationService::class.java).apply {
                action = ACTION_START_SERVICE
                putExtra(EXTRA_SENSITIVITY, sensitivity.name)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        /**
         * 停止服務的靜態方法
         */
        fun stopService(context: Context) {
            val intent = Intent(context, VibrationService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
        
        /**
         * 檢查服務是否正在運行
         */
        fun isServiceRunning(context: Context): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Android 9+ 使用新的 API
                try {
                    @Suppress("DEPRECATION")
                    val runningServices = manager.getRunningServices(Integer.MAX_VALUE)
                    runningServices.any { it.service.className == VibrationService::class.java.name }
                } catch (e: SecurityException) {
                    // 如果沒有權限，使用其他方法檢查
                    false
                }
            } else {
                @Suppress("DEPRECATION")
                val runningServices = manager.getRunningServices(Integer.MAX_VALUE)
                runningServices.any { it.service.className == VibrationService::class.java.name }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VibrationService created")
        
        createNotificationChannel()
        setupWakeLock()
        setupScreenReceiver()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                val sensitivityName = intent.getStringExtra(EXTRA_SENSITIVITY) ?: TapDetectionService.TapSensitivity.MEDIUM.name
                val sensitivity = TapDetectionService.TapSensitivity.valueOf(sensitivityName)
                startForegroundDetection(sensitivity)
            }
            ACTION_STOP_SERVICE -> {
                stopForegroundDetection()
            }
            ACTION_UPDATE_SENSITIVITY -> {
                val sensitivityName = intent.getStringExtra(EXTRA_SENSITIVITY) ?: TapDetectionService.TapSensitivity.MEDIUM.name
                val sensitivity = TapDetectionService.TapSensitivity.valueOf(sensitivityName)
                updateSensitivity(sensitivity)
            }
        }
        
        // 重啟時恢復服務
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null // 不需要綁定
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "VibrationService destroyed")
        
        stopForegroundDetection()
        cleanupResources()
    }
    
    /**
     * 開始前景偵測
     */
    private fun startForegroundDetection(sensitivity: TapDetectionService.TapSensitivity) {
        if (isServiceRunning) {
            Log.d(TAG, "Service already running")
            return
        }
        
        Log.d(TAG, "Starting foreground detection with sensitivity: ${sensitivity.getDescription(this)}")
        
        // 建立通知並啟動前景服務
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        // 初始化敲擊偵測服務 (使用合規的 Watch Mode)
        tapDetectionService = TapDetectionService(
            context = this,
            onTapDetected = {
                handleTapDetected()
            },
            onTapCountChanged = { count, maxCount ->
                updateNotification(getString(R.string.notification_tap_count, count, maxCount))
            },
            onWatchModeStatusChanged = { status ->
                updateWatchModeNotification(status)
            }
        )
        
        tapDetectionService.setSensitivity(sensitivity)
        // 使用 Watch Mode (合規模式)
        tapDetectionService.startDetection(useWatchMode = true, watchModeDuration = WatchModeManager.WATCH_MODE_DURATION_10MIN)
        
        // 獲取 WakeLock 保持敲擊偵測運行
        wakeLock?.acquire(10*60*1000L /*10 minutes*/)
        
        isServiceRunning = true
        updateNotification(getString(R.string.notification_service_running))
    }
    
    /**
     * 停止前景偵測
     */
    private fun stopForegroundDetection() {
        if (!isServiceRunning) {
            return
        }
        
        Log.d(TAG, "Stopping foreground detection")
        
        if (::tapDetectionService.isInitialized) {
            tapDetectionService.stopDetection()
        }
        
        wakeLock?.release()
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        
        isServiceRunning = false
    }
    
    /**
     * 更新敏感度
     */
    private fun updateSensitivity(sensitivity: TapDetectionService.TapSensitivity) {
        if (::tapDetectionService.isInitialized) {
            tapDetectionService.setSensitivity(sensitivity)
            updateNotification(getString(R.string.notification_sensitivity_updated, sensitivity.getDescription(this)))
        }
    }
    
    /**
     * 處理敲擊偵測成功
     */
    private fun handleTapDetected() {
        Log.d(TAG, "Tap detected in background service")
        
        // 觸發時間震動
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        // 使用與 HomeFragment 相同的震動邏輯
        TimeVibrationHelper.vibrateTime(this, hour, minute)
        
        // 記錄使用統計
        recordVibrationUsage()
        
        // 更新通知顯示成功
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        updateNotification(getString(R.string.notification_tap_success, timeStr))
        
        // 3秒後恢復正常狀態
        android.os.Handler(mainLooper).postDelayed({
            updateNotification(getString(R.string.notification_service_running))
        }, 3000)
    }
    
    /**
     * 記錄震動使用統計
     */
    private fun recordVibrationUsage() {
        val sharedPreferences = getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        
        // 增加今日震動次數
        val todayVibrations = sharedPreferences.getInt("vibrations_$today", 0)
        val totalVibrations = sharedPreferences.getInt("total_vibrations", 0)
        
        sharedPreferences.edit()
            .putInt("vibrations_$today", todayVibrations + 1)
            .putInt("total_vibrations", totalVibrations + 1)
            .putString("last_vibration_time_$today", currentTime)
            .putString("last_vibration_time", "$today $currentTime")
            .apply()
        
        Log.d(TAG, "Recorded vibration usage: $todayVibrations -> ${todayVibrations + 1}")
    }
    
    /**
     * 設定 WakeLock
     */
    private fun setupWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Vibtime::TapDetectionWakeLock"
        )
    }
    
    /**
     * 設定螢幕狀態監聽器
     */
    private fun setupScreenReceiver() {
        screenReceiver = ScreenReceiver { isScreenOn ->
            Log.d(TAG, "Screen state changed: ${if (isScreenOn) "ON" else "OFF"}")
            
            if (isServiceRunning) {
                if (isScreenOn) {
                    updateNotification(getString(R.string.notification_screen_on))
                } else {
                    updateNotification(getString(R.string.notification_screen_off))
                }
            }
        }
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        
        registerReceiver(screenReceiver, filter)
    }
    
    /**
     * 建立通知頻道
     */
    private fun createNotificationChannel() {
        // 使用 NotificationHelper 創建通知通道
        NotificationHelper.createNotificationChannels(this)
    }
    
    /**
     * 建立通知
     */
    private fun createNotification(): Notification {
        // 使用 NotificationHelper 創建通知
        return NotificationHelper.createVibrationServiceNotification(this)
    }
    
    /**
     * 更新通知內容
     */
    private fun updateNotification(message: String) {
        if (!isServiceRunning) return
        
        // 使用 NotificationHelper 更新通知，帶有權限檢查
        NotificationHelper.updateVibrationServiceNotification(
            this,
            getString(R.string.notification_title),
            message
        )
    }
    
    /**
     * 更新 Watch Mode 通知
     */
    private fun updateWatchModeNotification(status: WatchModeManager.WatchModeStatus) {
        if (!isServiceRunning) return
        
        val message = when {
            status.isActive -> {
                val remainingMinutes = status.remainingTime / 60000
                getString(R.string.notification_watch_mode_active, remainingMinutes)
            }
            status.isInCooldown -> {
                val remainingMinutes = status.remainingCooldown / 60000
                getString(R.string.notification_watch_mode_cooldown, remainingMinutes)
            }
            else -> getString(R.string.notification_service_running)
        }
        
        updateNotification(message)
    }
    
    /**
     * 清理資源
     */
    private fun cleanupResources() {
        try {
            if (::screenReceiver.isInitialized) {
                unregisterReceiver(screenReceiver)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering screen receiver", e)
        }
        
        wakeLock?.release()
        wakeLock = null
    }
}
