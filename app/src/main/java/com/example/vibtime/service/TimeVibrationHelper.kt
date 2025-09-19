package com.example.vibtime.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlin.math.floor

/**
 * 時間震動助手
 * 統一管理時間震動邏輯
 */
object TimeVibrationHelper {
    
    private const val TAG = "TimeVibrationHelper"
    
    /**
     * 根據時間產生震動模式
     * @param context 上下文
     * @param hour 小時 (0-23)
     * @param minute 分鐘 (0-59)
     */
    fun vibrateTime(context: Context, hour: Int, minute: Int) {
        val vibrator = 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        
        if (!vibrator.hasVibrator()) {
            Log.w(TAG, "Device does not have vibrator")
            return
        }
        
        // 簡化的時間震動模式
        val hourVibrations = hour % 12 // 12小時制
        val minuteVibrations = floor(minute / 5.0).toInt() // 每5分鐘一次短震動
        
        Log.d(TAG, "Vibrating time: $hour:$minute -> ${hourVibrations} long + ${minuteVibrations} short")
        
        vibrateTimePattern(vibrator, hourVibrations, minuteVibrations)
    }
    
    /**
     * 執行震動時間模式
     */
    private fun vibrateTimePattern(vibrator: Vibrator, hourCount: Int, minuteCount: Int) {
        val pattern = mutableListOf<Long>()
        
        // 開始前的停頓
        pattern.add(100)
        
        // 小時震動 (長震動 600ms，間隔 300ms)
        repeat(hourCount) {
            pattern.add(600) // 震動
            pattern.add(300) // 停頓
        }
        
        // 小時和分鐘之間的長停頓
        if (hourCount > 0 && minuteCount > 0) {
            pattern.add(1000)
        }
        
        // 分鐘震動 (短震動 200ms，間隔 200ms)
        repeat(minuteCount) {
            pattern.add(200) // 震動
            pattern.add(200) // 停頓
        }
        
        // 如果沒有任何震動，至少震動一次表示零點
        if (hourCount == 0 && minuteCount == 0) {
            pattern.add(300)
        }
        
        // 轉換為 LongArray 並執行震動
        val vibrationPattern = pattern.toLongArray()
        
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(vibrationPattern, -1)
            }
            
            Log.d(TAG, "Vibration pattern executed: ${vibrationPattern.contentToString()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error executing vibration pattern", e)
        }
    }
    
    /**
     * 測試震動
     */
    fun testVibration(context: Context, duration: Long = 500) {
        val vibrator = 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        
        if (!vibrator.hasVibrator()) {
            Log.w(TAG, "Device does not have vibrator")
            return
        }
        
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error testing vibration", e)
        }
    }
}
