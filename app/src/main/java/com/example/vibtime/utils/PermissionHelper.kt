package com.example.vibtime.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.vibtime.R

/**
 * PermissionHelper - 權限處理工具類別
 * 負責處理應用程式所需的各種權限
 */
object PermissionHelper {
    
    // 需要的權限列表
    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.VIBRATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.FOREGROUND_SERVICE
    )
    
    // Android 13+ 需要的權限
    val REQUIRED_PERMISSIONS_33_PLUS = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS
    )
    
    /**
     * 檢查權限是否已授予
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 檢查所有必要權限是否已授予
     */
    fun hasAllRequiredPermissions(context: Context): Boolean {
        val allPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            REQUIRED_PERMISSIONS + REQUIRED_PERMISSIONS_33_PLUS
        } else {
            REQUIRED_PERMISSIONS
        }
        
        return allPermissions.all { hasPermission(context, it) }
    }
    
    /**
     * 檢查是否需要顯示權限說明
     */
    fun shouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * 請求權限
     */
    @Suppress("DEPRECATION")
    fun requestPermissions(
        fragment: Fragment,
        permissions: Array<String>,
        requestCode: Int
    ) {
        fragment.requestPermissions(permissions, requestCode)
    }
    
    /**
     * 使用 ActivityResultLauncher 請求權限（推薦方式）
     */
    fun createPermissionLauncher(
        fragment: Fragment,
        onPermissionResult: (Map<String, Boolean>) -> Unit
    ): ActivityResultLauncher<Array<String>> {
        return fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            onPermissionResult(permissions)
        }
    }
    
    /**
     * 顯示權限說明對話框
     */
    fun showPermissionRationaleDialog(
        context: Context,
        permission: String,
        onPositiveClick: () -> Unit
    ) {
        val message = when (permission) {
            Manifest.permission.VIBRATE -> context.getString(R.string.permission_vibrate_desc)
            Manifest.permission.WAKE_LOCK -> context.getString(R.string.permission_wake_lock_desc)
            Manifest.permission.FOREGROUND_SERVICE -> context.getString(R.string.permission_foreground_service_desc)
            Manifest.permission.POST_NOTIFICATIONS -> context.getString(R.string.permission_notification_desc)
            else -> context.getString(R.string.permission_general_desc)
        }
        
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.permission_explanation_title))
            .setMessage(message)
            .setPositiveButton(context.getString(R.string.permission_grant)) { _, _ ->
                onPositiveClick()
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .show()
    }
    
    /**
     * 顯示權限被拒絕的對話框
     */
    fun showPermissionDeniedDialog(
        context: Context,
        onPositiveClick: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.permission_denied_title))
            .setMessage(context.getString(R.string.permission_denied_message))
            .setPositiveButton(context.getString(R.string.permission_go_to_settings)) { _, _ ->
                onPositiveClick()
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .show()
    }
    
    /**
     * 開啟應用程式設定頁面
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
    
    /**
     * 檢查並請求震動權限
     */
    @Suppress("UNUSED_PARAMETER")
    fun checkAndRequestVibrationPermission(
        fragment: Fragment,
        launcher: ActivityResultLauncher<Array<String>>,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (hasPermission(fragment.requireContext(), Manifest.permission.VIBRATE)) {
            onGranted()
        } else {
            launcher.launch(arrayOf(Manifest.permission.VIBRATE))
        }
    }
    
    /**
     * 檢查並請求通知權限（Android 13+）
     */
    @Suppress("UNUSED_PARAMETER")
    fun checkAndRequestNotificationPermission(
        fragment: Fragment,
        launcher: ActivityResultLauncher<Array<String>>,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Android 13 以下不需要通知權限
            onGranted()
            return
        }
        
        if (hasPermission(fragment.requireContext(), Manifest.permission.POST_NOTIFICATIONS)) {
            onGranted()
        } else {
            launcher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }
    }
    
    /**
     * 檢查並請求前景服務權限
     */
    @Suppress("UNUSED_PARAMETER")
    fun checkAndRequestForegroundServicePermission(
        fragment: Fragment,
        launcher: ActivityResultLauncher<Array<String>>,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            // Android 9 以下不需要前景服務權限
            onGranted()
            return
        }
        
        if (hasPermission(fragment.requireContext(), Manifest.permission.FOREGROUND_SERVICE)) {
            onGranted()
        } else {
            launcher.launch(arrayOf(Manifest.permission.FOREGROUND_SERVICE))
        }
    }
    
    /**
     * 處理權限請求結果
     */
    @Suppress("UNUSED_PARAMETER")
    fun handlePermissionResult(
        context: Context,
        permissions: Map<String, Boolean>,
        onAllGranted: () -> Unit,
        onSomeDenied: (List<String>) -> Unit
    ) {
        val deniedPermissions = permissions.filter { !it.value }.keys.toList()
        
        if (deniedPermissions.isEmpty()) {
            onAllGranted()
        } else {
            onSomeDenied(deniedPermissions)
        }
    }
}
