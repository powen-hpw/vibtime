package com.example.vibtime.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * 安全機制測試輔助類
 * 用於測試和驗證安全功能
 */
object SafetyTestHelper {
    private const val TAG = "SafetyTestHelper"
    
    // 測試用的時間常數（較短，便於測試）
    const val TEST_MAX_SERVICE_RUNTIME = 5 * 60 * 1000L // 5分鐘（測試用）
    const val TEST_MIN_VIBRATION_INTERVAL = 30 * 1000L // 30秒（測試用）
    
    /**
     * 測試頻率限制功能
     */
    fun testFrequencyLimit(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        val lastVibrationTime = sharedPreferences.getLong("last_vibration_time", 0L)
        val currentTime = System.currentTimeMillis()
        
        val timeSinceLastVibration = currentTime - lastVibrationTime
        val canTrigger = timeSinceLastVibration >= TEST_MIN_VIBRATION_INTERVAL
        
        Log.d(TAG, "Frequency limit test: timeSinceLast=$timeSinceLastVibration ms, canTrigger=$canTrigger")
        
        return canTrigger
    }
    
    /**
     * 測試服務時間限制
     */
    fun testServiceTimeLimit(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        val serviceStartTime = sharedPreferences.getLong("service_start_time", 0L)
        val currentTime = System.currentTimeMillis()
        
        val serviceRunTime = currentTime - serviceStartTime
        val isExpired = serviceRunTime >= TEST_MAX_SERVICE_RUNTIME
        
        Log.d(TAG, "Service time test: runTime=$serviceRunTime ms, isExpired=$isExpired")
        
        return !isExpired
    }
    
    /**
     * 模擬震動觸發（用於測試）
     */
    fun simulateVibration(context: Context) {
        val sharedPreferences = context.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        val currentTime = System.currentTimeMillis()
        
        sharedPreferences.edit()
            .putLong("last_vibration_time", currentTime)
            .apply()
        
        Log.d(TAG, "Simulated vibration at ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTime))}")
    }
    
    /**
     * 模擬服務啟動（用於測試）
     */
    fun simulateServiceStart(context: Context) {
        val sharedPreferences = context.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        val currentTime = System.currentTimeMillis()
        
        sharedPreferences.edit()
            .putLong("service_start_time", currentTime)
            .apply()
        
        Log.d(TAG, "Simulated service start at ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTime))}")
    }
    
    /**
     * 獲取測試狀態資訊
     */
    fun getTestStatus(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        val lastVibrationTime = sharedPreferences.getLong("last_vibration_time", 0L)
        val serviceStartTime = sharedPreferences.getLong("service_start_time", 0L)
        val currentTime = System.currentTimeMillis()
        
        val timeSinceLastVibration = currentTime - lastVibrationTime
        val serviceRunTime = currentTime - serviceStartTime
        
        val nextVibrationTime = maxOf(0, TEST_MIN_VIBRATION_INTERVAL - timeSinceLastVibration)
        val remainingServiceTime = maxOf(0, TEST_MAX_SERVICE_RUNTIME - serviceRunTime)
        
        return """
            Safety Test Status:
            - Last vibration: ${if (lastVibrationTime > 0) SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastVibrationTime)) else "Never"}
            - Service start: ${if (serviceStartTime > 0) SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(serviceStartTime)) else "Never"}
            - Time since last vibration: ${timeSinceLastVibration / 1000}s
            - Service run time: ${serviceRunTime / 1000}s
            - Next vibration available in: ${nextVibrationTime / 1000}s
            - Service remaining time: ${remainingServiceTime / 1000}s
            - Can trigger vibration: ${testFrequencyLimit(context)}
            - Service not expired: ${testServiceTimeLimit(context)}
        """.trimIndent()
    }
}
