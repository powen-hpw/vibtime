package com.example.vibtime.utils

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * 精確鬧鐘管理器
 * 處理 Android 14+ 精確鬧鐘權限變更
 * 
 * @since 1.3.0
 */
object ExactAlarmManager {
    private const val TAG = "ExactAlarmManager"
    
    /**
     * 檢查精確鬧鐘權限
     * @param context 應用上下文
     * @return 是否有精確鬧鐘權限
     */
    fun checkExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            // Android 13 及以下版本預設有權限
            true
        }
    }
    
    /**
     * 請求精確鬧鐘權限
     * @param activity 活動實例
     */
    fun requestExactAlarmPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(activity, AlarmManager::class.java)
            
            if (alarmManager?.canScheduleExactAlarms() == false) {
                Log.d(TAG, "Requesting exact alarm permission")
                
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                
                try {
                    activity.startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open exact alarm settings", e)
                    // 如果無法開啟特定設定頁面，開啟一般設定
                    openAppSettings(activity)
                }
            } else {
                Log.d(TAG, "Exact alarm permission already granted")
            }
        } else {
            Log.d(TAG, "Exact alarm permission not required for Android ${Build.VERSION.SDK_INT}")
        }
    }
    
    /**
     * 開啟應用設定頁面
     * @param activity 活動實例
     */
    private fun openAppSettings(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open app settings", e)
        }
    }
    
    /**
     * 檢查並處理精確鬧鐘權限
     * @param activity 活動實例
     * @param onPermissionGranted 權限授予時的回調
     * @param onPermissionDenied 權限拒絕時的回調
     */
    fun checkAndRequestPermission(
        activity: Activity,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (checkExactAlarmPermission(activity)) {
            onPermissionGranted()
        } else {
            Log.d(TAG, "Exact alarm permission not granted, requesting...")
            requestExactAlarmPermission(activity)
            onPermissionDenied()
        }
    }
    
    /**
     * 獲取精確鬧鐘權限狀態描述
     * @param context 應用上下文
     * @return 權限狀態描述
     */
    fun getPermissionStatusDescription(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkExactAlarmPermission(context)) {
                "精確鬧鐘權限已授予"
            } else {
                "精確鬧鐘權限未授予，需要手動開啟"
            }
        } else {
            "精確鬧鐘權限不需要 (Android ${Build.VERSION.SDK_INT})"
        }
    }
}