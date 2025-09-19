package com.example.vibtime.utils

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.activity.result.ActivityResultLauncher
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.vibtime.R

/**
 * NotificationPermissionManager - 通知權限管理類別
 * 專門處理 Android 13+ 的通知權限請求和管理
 */
object NotificationPermissionManager {
    
    private const val TAG = "NotificationPermissionManager"
    
    /**
     * 檢查是否需要請求通知權限
     */
    fun needsNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }
    
    /**
     * 檢查通知權限是否已授予
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (needsNotificationPermission()) {
            PermissionHelper.hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
    }
    
    /**
     * 顯示通知權限說明對話框
     */
    fun showNotificationPermissionRationale(
        context: Context,
        onPositiveClick: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.permission_notification_title))
            .setMessage(context.getString(R.string.permission_notification_rationale))
            .setPositiveButton(context.getString(R.string.permission_grant)) { _, _ ->
                onPositiveClick()
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .show()
    }
    
    /**
     * 顯示通知權限被拒絕的對話框
     */
    fun showNotificationPermissionDeniedDialog(
        context: Context,
        onPositiveClick: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.permission_notification_denied_title))
            .setMessage(context.getString(R.string.permission_notification_denied_message))
            .setPositiveButton(context.getString(R.string.permission_go_to_settings)) { _, _ ->
                onPositiveClick()
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .show()
    }
    
    /**
     * 請求通知權限
     */
    @Suppress("UNUSED_PARAMETER")
    fun requestNotificationPermission(
        fragment: Fragment,
        launcher: ActivityResultLauncher<Array<String>>,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (!needsNotificationPermission()) {
            // Android 13 以下不需要通知權限
            onGranted()
            return
        }
        
        if (hasNotificationPermission(fragment.requireContext())) {
            onGranted()
        } else {
            // 檢查是否需要顯示說明
            if (PermissionHelper.shouldShowRequestPermissionRationale(
                fragment.requireActivity(),
                Manifest.permission.POST_NOTIFICATIONS
            )) {
                // 顯示權限說明
                showNotificationPermissionRationale(fragment.requireContext()) {
                    launcher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }
            } else {
                // 直接請求權限
                launcher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            }
        }
    }
    
    /**
     * 處理通知權限請求結果
     */
    @Suppress("UNUSED_PARAMETER")
    fun handleNotificationPermissionResult(
        context: Context,
        permissions: Map<String, Boolean>,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        val notificationPermissionGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        
        if (notificationPermissionGranted) {
            android.util.Log.d(TAG, "Notification permission granted")
            onGranted()
        } else {
            android.util.Log.w(TAG, "Notification permission denied")
            onDenied()
        }
    }
    
    /**
     * 完整的通知權限檢查和請求流程
     */
    fun checkAndRequestNotificationPermission(
        fragment: Fragment,
        launcher: ActivityResultLauncher<Array<String>>,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (!needsNotificationPermission()) {
            onPermissionGranted()
            return
        }
        
        if (hasNotificationPermission(fragment.requireContext())) {
            onPermissionGranted()
        } else {
            requestNotificationPermission(
                fragment,
                launcher,
                onPermissionGranted,
                onPermissionDenied
            )
        }
    }
    
    /**
     * 檢查並顯示權限被拒絕的處理
     */
    fun checkAndShowPermissionDeniedDialog(
        context: Context,
        onGoToSettings: () -> Unit
    ) {
        if (needsNotificationPermission() && !hasNotificationPermission(context)) {
            showNotificationPermissionDeniedDialog(context, onGoToSettings)
        }
    }
}
