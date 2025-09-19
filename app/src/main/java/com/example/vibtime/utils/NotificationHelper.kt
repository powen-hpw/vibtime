package com.example.vibtime.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.vibtime.MainActivity
import com.example.vibtime.R

/**
 * NotificationHelper - 通知處理工具類別
 * 負責管理應用程式的各種通知
 */
object NotificationHelper {
    
    // 通知通道 ID（ID 需固定，可本地化名稱與描述）
    const val CHANNEL_ID_VIBRATION_SERVICE = "VibrationServiceChannel"
    const val CHANNEL_ID_GENERAL = "GeneralChannel"
    const val CHANNEL_ID_ALERTS = "AlertsChannel"
    
    // 通知 ID
    const val NOTIFICATION_ID_VIBRATION_SERVICE = 1001
    const val NOTIFICATION_ID_TIME_ALERT = 1002
    const val NOTIFICATION_ID_PERMISSION_REMINDER = 1003
    
    private const val TAG = "NotificationHelper"
    
    /**
     * 建立所有通知通道
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            android.util.Log.d(TAG, "createNotificationChannels()")
            
            // 震動服務通知通道
            val nameVibration = context.getString(R.string.notif_channel_vibration)
            val descVibration = context.getString(R.string.notif_desc_vibration)
            val vibrationServiceChannel = NotificationChannel(
                CHANNEL_ID_VIBRATION_SERVICE,
                nameVibration,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = descVibration
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            
            // 一般通知通道
            val nameGeneral = context.getString(R.string.notif_channel_general)
            val descGeneral = context.getString(R.string.notif_desc_general)
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                nameGeneral,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = descGeneral
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
            }
            
            // 提醒通知通道
            val nameAlerts = context.getString(R.string.notif_channel_alerts)
            val descAlerts = context.getString(R.string.notif_desc_alerts)
            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                nameAlerts,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = descAlerts
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannels(
                listOf(vibrationServiceChannel, generalChannel, alertsChannel)
            )
        }
    }
    
    /**
     * 建立震動服務通知
     */
    fun createVibrationServiceNotification(context: Context): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, CHANNEL_ID_VIBRATION_SERVICE)
            .setContentTitle(context.getString(R.string.notif_vibration_running))
            .setContentText(context.getString(R.string.notif_tap_hint))
            .setSmallIcon(R.drawable.ic_vibration)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    /**
     * 顯示震動服務通知
     */
    fun showVibrationServiceNotification(context: Context) {
        if (!hasNotificationPermission(context)) {
            android.util.Log.w(TAG, "Notification permission denied, cannot show vibration service notification")
            return
        }
        
        try {
            val notification = createVibrationServiceNotification(context)
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_VIBRATION_SERVICE,
                notification
            )
            android.util.Log.d(TAG, "Vibration service notification shown successfully")
        } catch (e: SecurityException) {
            android.util.Log.e(TAG, "SecurityException when showing vibration service notification: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error showing vibration service notification: ${e.message}")
        }
    }
    
    /**
     * 隱藏震動服務通知
     */
    fun hideVibrationServiceNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_VIBRATION_SERVICE)
    }
    
    /**
     * 顯示時間提醒通知
     */
    fun showTimeAlertNotification(
        context: Context,
        hour: Int,
        minute: Int,
        message: String
    ) {
        if (!hasNotificationPermission(context)) {
            android.util.Log.w(TAG, "Notification permission denied, cannot show time alert notification")
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                .setContentTitle(context.getString(R.string.notif_time_alert))
                .setContentText(context.getString(R.string.current_time_format, context.getString(R.string.time_format_hour_minute, hour, minute)))
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.drawable.ic_vibration)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build()
            
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_TIME_ALERT,
                notification
            )
            android.util.Log.d(TAG, "Time alert notification shown successfully")
        } catch (e: SecurityException) {
            android.util.Log.e(TAG, "SecurityException when showing time alert notification: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error showing time alert notification: ${e.message}")
        }
    }
    
    /**
     * 顯示權限提醒通知
     */
    fun showPermissionReminderNotification(context: Context) {
        if (!hasNotificationPermission(context)) {
            android.util.Log.w(TAG, "Notification permission denied, cannot show permission reminder notification")
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
                .setContentTitle(context.getString(R.string.notif_permission_title))
                .setContentText(context.getString(R.string.notif_permission_text))
                .setSmallIcon(R.drawable.ic_vibration)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build()
            
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_PERMISSION_REMINDER,
                notification
            )
            android.util.Log.d(TAG, "Permission reminder notification shown successfully")
        } catch (e: SecurityException) {
            android.util.Log.e(TAG, "SecurityException when showing permission reminder notification: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error showing permission reminder notification: ${e.message}")
        }
    }
    
    /**
     * 隱藏所有通知
     */
    fun hideAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
    
    /**
     * 檢查通知權限
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 需要檢查 POST_NOTIFICATIONS 權限
            PermissionHelper.hasPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Android 13 以下不需要通知權限
            true
        }
    }
    
    /**
     * 更新震動服務通知內容
     */
    fun updateVibrationServiceNotification(
        context: Context,
        title: String,
        content: String
    ) {
        if (!hasNotificationPermission(context)) {
            android.util.Log.w(TAG, "Notification permission denied, cannot update vibration service notification")
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID_VIBRATION_SERVICE)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_vibration)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()
            
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_VIBRATION_SERVICE,
                notification
            )
            android.util.Log.d(TAG, "Vibration service notification updated successfully")
        } catch (e: SecurityException) {
            android.util.Log.e(TAG, "SecurityException when updating vibration service notification: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error updating vibration service notification: ${e.message}")
        }
    }
    
    /**
     * 安全地顯示通知（帶有降級方案）
     */
    fun showNotificationSafely(
        context: Context,
        notificationId: Int,
        notification: android.app.Notification,
        fallbackAction: (() -> Unit)? = null
    ) {
        if (!hasNotificationPermission(context)) {
            android.util.Log.w(TAG, "Notification permission denied, using fallback action")
            fallbackAction?.invoke()
            return
        }
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            android.util.Log.d(TAG, "Notification shown successfully with ID: $notificationId")
        } catch (e: SecurityException) {
            android.util.Log.e(TAG, "SecurityException when showing notification: ${e.message}")
            fallbackAction?.invoke()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error showing notification: ${e.message}")
            fallbackAction?.invoke()
        }
    }
    
    /**
     * 檢查通知權限並提供降級方案
     */
    fun checkNotificationPermissionWithFallback(
        context: Context,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (hasNotificationPermission(context)) {
            onPermissionGranted()
        } else {
            android.util.Log.w(TAG, "Notification permission denied, using fallback")
            onPermissionDenied()
        }
    }
}
