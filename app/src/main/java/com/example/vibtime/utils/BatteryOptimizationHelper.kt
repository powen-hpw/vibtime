package com.example.vibtime.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.example.vibtime.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * BatteryOptimizationHelper - 電池最佳化工具類別
 * 負責處理電池最佳化相關功能
 */
object BatteryOptimizationHelper {
    
    /**
     * 檢查應用程式是否被電池最佳化
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
    
    /**
     * 請求忽略電池最佳化
     */
    fun requestIgnoreBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
    
    /**
     * 開啟電池最佳化設定頁面
     */
    fun openBatteryOptimizationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
    
    /**
     * 顯示電池最佳化說明對話框
     */
    fun showBatteryOptimizationDialog(
        context: Context,
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.battery_optimization_title))
            .setMessage(context.getString(R.string.battery_optimization_message))
            .setPositiveButton(context.getString(R.string.battery_optimization_go_settings)) { _, _ ->
                onPositiveClick()
            }
            .setNegativeButton(context.getString(R.string.battery_optimization_later)) { _, _ ->
                onNegativeClick()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * 顯示電池最佳化已啟用對話框
     */
    fun showBatteryOptimizationEnabledDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.battery_optimization_enabled_title))
            .setMessage(context.getString(R.string.battery_optimization_enabled_message))
            .setPositiveButton(context.getString(R.string.dialog_ok), null)
            .show()
    }
    
    /**
     * 檢查並請求電池最佳化權限
     */
    fun checkAndRequestBatteryOptimization(
        context: Context,
        onOptimized: () -> Unit,
        onNotOptimized: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Android 6.0 以下不需要電池最佳化設定
            onOptimized()
            return
        }
        
        if (isIgnoringBatteryOptimizations(context)) {
            onOptimized()
        } else {
            onNotOptimized()
        }
    }
    
    /**
     * 檢查是否支援電池最佳化
     */
    fun isBatteryOptimizationSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }
    
    /**
     * 獲取電池最佳化狀態描述
     */
    fun getBatteryOptimizationStatusText(context: Context): String {
        return if (isIgnoringBatteryOptimizations(context)) {
            context.getString(R.string.battery_optimization_status_enabled)
        } else {
            context.getString(R.string.battery_optimization_status_disabled)
        }
    }
    
    /**
     * 檢查是否為低電量模式
     */
    fun isPowerSaveMode(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            powerManager.isPowerSaveMode
        } else {
            false
        }
    }
    
    /**
     * 檢查是否為省電模式
     */
    fun isBatterySaverMode(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            powerManager.isPowerSaveMode
        } else {
            false
        }
    }
    
    /**
     * 顯示省電模式警告
     */
    fun showPowerSaveModeWarning(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.power_save_warning_title))
            .setMessage(context.getString(R.string.power_save_warning_message))
            .setPositiveButton(context.getString(R.string.battery_optimization_go_settings)) { _, _ ->
                openBatteryOptimizationSettings(context)
            }
            .setNegativeButton(context.getString(R.string.battery_optimization_later), null)
            .show()
    }
    
    /**
     * 檢查並處理電池相關問題
     */
    fun checkBatteryIssues(context: Context): BatteryIssue? {
        return when {
            !isIgnoringBatteryOptimizations(context) -> BatteryIssue.NOT_OPTIMIZED
            isPowerSaveMode(context) -> BatteryIssue.POWER_SAVE_MODE
            else -> null
        }
    }
    
    /**
     * 電池問題類型
     */
    enum class BatteryIssue {
        NOT_OPTIMIZED,
        POWER_SAVE_MODE
    }
    
    /**
     * 獲取電池問題的解決方案
     */
    fun getBatteryIssueSolution(context: Context, issue: BatteryIssue): String {
        return when (issue) {
            BatteryIssue.NOT_OPTIMIZED -> context.getString(R.string.battery_issue_not_optimized)
            BatteryIssue.POWER_SAVE_MODE -> context.getString(R.string.battery_issue_power_save)
        }
    }
}
