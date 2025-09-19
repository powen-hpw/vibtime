package com.example.vibtime.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.example.vibtime.service.VibrationService
import com.example.vibtime.service.TapDetectionService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.vibtime.R

/**
 * ServiceManager - 服務管理工具類別
 * 負責管理前景服務的啟動、停止和權限檢查
 */
object ServiceManager {
    
    private const val TAG = "ServiceManager"
    
    /**
     * 檢查是否可以啟動前景服務
     */
    fun canStartForegroundService(context: Context): Boolean {
        // 檢查基本權限
        val hasVibratePermission = PermissionHelper.hasPermission(context, android.Manifest.permission.VIBRATE)
        val hasWakeLockPermission = PermissionHelper.hasPermission(context, android.Manifest.permission.WAKE_LOCK)
        val hasForegroundServicePermission = PermissionHelper.hasPermission(context, android.Manifest.permission.FOREGROUND_SERVICE)
        
        // 檢查通知權限（Android 13+）
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionHelper.hasPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
        
        return hasVibratePermission && hasWakeLockPermission && hasForegroundServicePermission && hasNotificationPermission
    }
    
    /**
     * 顯示服務啟動說明對話框
     */
    fun showServiceStartDialog(
        context: Context,
        onPositiveClick: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.service_start_dialog_title))
            .setMessage(context.getString(R.string.service_start_dialog_message))
            .setPositiveButton(context.getString(R.string.service_start_confirm)) { _, _ ->
                onPositiveClick()
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .show()
    }
    
    /**
     * 顯示權限缺失對話框
     */
    fun showMissingPermissionsDialog(
        context: Context,
        missingPermissions: List<String>,
        onPositiveClick: () -> Unit
    ) {
        val permissionNames = missingPermissions.joinToString("\n• ") { 
            when (it) {
                android.Manifest.permission.VIBRATE -> context.getString(R.string.permission_vibrate_desc)
                android.Manifest.permission.WAKE_LOCK -> context.getString(R.string.permission_wake_lock_desc)
                android.Manifest.permission.FOREGROUND_SERVICE -> context.getString(R.string.permission_foreground_service_desc)
                android.Manifest.permission.POST_NOTIFICATIONS -> context.getString(R.string.permission_notification_desc)
                else -> it
            }
        }
        
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.service_permissions_missing_title))
            .setMessage(context.getString(R.string.service_permissions_missing_message, permissionNames))
            .setPositiveButton(context.getString(R.string.permission_go_to_settings)) { _, _ ->
                onPositiveClick()
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .show()
    }
    
    /**
     * 安全地啟動震動服務
     */
    fun startVibrationServiceSafely(
        context: Context,
        sensitivity: TapDetectionService.TapSensitivity = TapDetectionService.TapSensitivity.MEDIUM
    ): Boolean {
        if (!canStartForegroundService(context)) {
            Log.w(TAG, "Cannot start service: missing permissions")
            return false
        }
        
        try {
            VibrationService.startService(context, sensitivity)
            Log.d(TAG, "Vibration service started successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start vibration service: ${e.message}")
            return false
        }
    }
    
    /**
     * 停止震動服務
     */
    fun stopVibrationService(context: Context) {
        try {
            VibrationService.stopService(context)
            Log.d(TAG, "Vibration service stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop vibration service: ${e.message}")
        }
    }
    
    /**
     * 檢查服務是否正在運行
     */
    fun isVibrationServiceRunning(context: Context): Boolean {
        return VibrationService.isServiceRunning(context)
    }
    
    /**
     * 獲取缺失的權限列表
     */
    fun getMissingPermissions(context: Context): List<String> {
        val missingPermissions = mutableListOf<String>()
        
        if (!PermissionHelper.hasPermission(context, android.Manifest.permission.VIBRATE)) {
            missingPermissions.add(android.Manifest.permission.VIBRATE)
        }
        
        if (!PermissionHelper.hasPermission(context, android.Manifest.permission.WAKE_LOCK)) {
            missingPermissions.add(android.Manifest.permission.WAKE_LOCK)
        }
        
        if (!PermissionHelper.hasPermission(context, android.Manifest.permission.FOREGROUND_SERVICE)) {
            missingPermissions.add(android.Manifest.permission.FOREGROUND_SERVICE)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionHelper.hasPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)) {
                missingPermissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        return missingPermissions
    }
    
    /**
     * 完整的服務啟動流程（包含權限檢查和用戶引導）
     */
    @Suppress("UNUSED_PARAMETER")
    fun startServiceWithPermissionCheck(
        fragment: Fragment,
        launcher: ActivityResultLauncher<Array<String>>,
        sensitivity: TapDetectionService.TapSensitivity = TapDetectionService.TapSensitivity.MEDIUM,
        onServiceStarted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        val context = fragment.requireContext()
        
        if (canStartForegroundService(context)) {
            // 權限齊全，直接啟動服務
            if (startVibrationServiceSafely(context, sensitivity)) {
                onServiceStarted()
            } else {
                onPermissionDenied()
            }
        } else {
            // 檢查缺失的權限
            val missingPermissions = getMissingPermissions(context)
            
            if (missingPermissions.isNotEmpty()) {
                // 顯示權限說明並請求權限
                showMissingPermissionsDialog(context, missingPermissions) {
                    // 用戶點擊前往設定
                    PermissionHelper.openAppSettings(context)
                }
            } else {
                // 顯示服務啟動說明
                showServiceStartDialog(context) {
                    if (startVibrationServiceSafely(context, sensitivity)) {
                        onServiceStarted()
                    } else {
                        onPermissionDenied()
                    }
                }
            }
        }
    }
}
