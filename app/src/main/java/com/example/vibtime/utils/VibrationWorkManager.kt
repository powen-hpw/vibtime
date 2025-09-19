package com.example.vibtime.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.vibtime.service.TapDetectionService
import java.util.concurrent.TimeUnit

/**
 * VibrationWorkManager - WorkManager 工具類別
 * 負責處理震動相關的背景任務和服務重啟
 */
object VibrationWorkManager {
    
    private const val TAG = "VibrationWorkManager"
    
    // Work 任務的標籤
    const val VIBRATION_WORK_TAG = "vibration_work"
    const val RESTART_SERVICE_WORK_TAG = "restart_service_work"
    const val SAFETY_CHECK_WORK_TAG = "safety_check_work"
    
    /**
     * 調度震動任務
     */
    fun scheduleVibrationWork(context: Context, delayMinutes: Long = 5) {
        val vibrationWork = OneTimeWorkRequestBuilder<VibrationWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .addTag(VIBRATION_WORK_TAG)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            "vibration_work_${System.currentTimeMillis()}",
            ExistingWorkPolicy.REPLACE,
            vibrationWork
        )
        
        Log.d(TAG, "Scheduled vibration work in $delayMinutes minutes")
    }
    
    /**
     * 調度服務重啟任務
     */
    fun scheduleServiceRestart(context: Context, delayMinutes: Long = 5) {
        val restartWork = OneTimeWorkRequestBuilder<RestartServiceWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .addTag(RESTART_SERVICE_WORK_TAG)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 2, TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            "restart_service_work_${System.currentTimeMillis()}",
            ExistingWorkPolicy.REPLACE,
            restartWork
        )
        
        Log.d(TAG, "Scheduled service restart work in $delayMinutes minutes")
    }
    
    /**
     * 調度安全檢查任務
     */
    fun scheduleSafetyCheck(context: Context, intervalMinutes: Long = 15) {
        val safetyWork = PeriodicWorkRequestBuilder<SafetyCheckWorker>(
            intervalMinutes, TimeUnit.MINUTES
        )
            .addTag(SAFETY_CHECK_WORK_TAG)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "safety_check_work",
            ExistingPeriodicWorkPolicy.REPLACE,
            safetyWork
        )
        
        Log.d(TAG, "Scheduled safety check work every $intervalMinutes minutes")
    }
    
    /**
     * 取消所有震動相關的任務
     */
    fun cancelAllVibrationWork(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(VIBRATION_WORK_TAG)
        WorkManager.getInstance(context).cancelAllWorkByTag(RESTART_SERVICE_WORK_TAG)
        WorkManager.getInstance(context).cancelAllWorkByTag(SAFETY_CHECK_WORK_TAG)
        
        Log.d(TAG, "Cancelled all vibration work")
    }
    
    /**
     * 檢查是否有正在運行的任務
     */
    @Suppress("UNUSED_PARAMETER")
    fun hasRunningWork(context: Context): Boolean {
        // 簡化實現，避免依賴問題
        return false
    }
}

/**
 * 震動 Worker
 * 處理震動相關的背景任務
 */
class VibrationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        Log.d("VibrationWorker", "Executing vibration work")
        
        return try {
            // 這裡可以添加震動邏輯
            // 例如：檢查是否需要震動提醒
            Log.d("VibrationWorker", "Vibration work completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("VibrationWorker", "Vibration work failed: ${e.message}")
            Result.retry()
        }
    }
}

/**
 * 服務重啟 Worker
 * 處理服務重啟的背景任務
 */
class RestartServiceWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        Log.d("RestartServiceWorker", "Executing service restart work")
        
        return try {
            // 檢查服務是否需要重啟
            if (!ServiceManager.isVibrationServiceRunning(applicationContext)) {
                Log.d("RestartServiceWorker", "Service not running, attempting restart")
                
                // 嘗試重啟服務
                val success = ServiceManager.startVibrationServiceSafely(
                    applicationContext,
                    TapDetectionService.TapSensitivity.MEDIUM
                )
                
                if (success) {
                    Log.d("RestartServiceWorker", "Service restarted successfully")
                    Result.success()
                } else {
                    Log.w("RestartServiceWorker", "Failed to restart service")
                    Result.retry()
                }
            } else {
                Log.d("RestartServiceWorker", "Service already running")
                Result.success()
            }
        } catch (e: Exception) {
            Log.e("RestartServiceWorker", "Service restart work failed: ${e.message}")
            Result.retry()
        }
    }
}

/**
 * 安全檢查 Worker
 * 定期檢查服務的安全狀態
 */
class SafetyCheckWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        Log.d("SafetyCheckWorker", "Executing safety check work")
        
        return try {
            // 檢查服務是否超時
            if (SafetyManager.isServiceExpired(applicationContext)) {
                Log.d("SafetyCheckWorker", "Service expired, stopping service")
                ServiceManager.stopVibrationService(applicationContext)
                
                // 發送安全通知
                NotificationHelper.showTimeAlertNotification(
                    applicationContext,
                    0, 0,
                    "Service stopped for safety reasons"
                )
            }
            
            Log.d("SafetyCheckWorker", "Safety check completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("SafetyCheckWorker", "Safety check work failed: ${e.message}")
            Result.retry()
        }
    }
}
