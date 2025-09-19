package com.example.vibtime.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlin.math.floor

/**
 * 進階時間震動助手
 * 實現二進制和摩斯電碼報時功能
 */
object AdvancedTimeVibrationHelper {
    
    private const val TAG = "AdvancedTimeVibration"
    
    // 震動參數
    private const val SHORT_VIBRATION = 200L  // 短震動 (點/0)
    private const val LONG_VIBRATION = 600L   // 長震動 (劃/1)
    private const val SHORT_PAUSE = 200L      // 短停頓
    private const val LONG_PAUSE = 1000L      // 長停頓
    
    /**
     * 二進制報時
     * 使用短震動(0)和長震動(1)表示二進制數字
     */
    fun vibrateBinaryTime(context: Context, hour: Int, minute: Int) {
        val vibrator = 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }.let { 
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    (it as VibratorManager).defaultVibrator
                } else {
                    it as Vibrator
                }
            }
        
        if (!vibrator.hasVibrator()) {
            Log.w(TAG, "Device does not have vibrator")
            return
        }
        
        Log.d(TAG, "Binary time: $hour:$minute")
        
        val pattern = mutableListOf<Long>()
        
        // 開始前的停頓
        pattern.add(500L)
        
        // 小時的二進制表示
        val hourBinary = hour.toString(2)
        Log.d(TAG, "Hour $hour in binary: $hourBinary")
        
        for (bit in hourBinary) {
            if (bit == '1') {
                pattern.add(LONG_VIBRATION)
            } else {
                pattern.add(SHORT_VIBRATION)
            }
            pattern.add(SHORT_PAUSE)
        }
        
        // 小時和分鐘之間的長停頓
        pattern.add(LONG_PAUSE)
        
        // 分鐘的二進制表示
        val minuteBinary = minute.toString(2)
        Log.d(TAG, "Minute $minute in binary: $minuteBinary")
        
        for (bit in minuteBinary) {
            if (bit == '1') {
                pattern.add(LONG_VIBRATION)
            } else {
                pattern.add(SHORT_VIBRATION)
            }
            pattern.add(SHORT_PAUSE)
        }
        
        executeVibrationPattern(vibrator, pattern)
    }
    
    /**
     * 摩斯電碼報時
     * 使用標準摩斯電碼表示數字
     */
    fun vibrateMorseTime(context: Context, hour: Int, minute: Int) {
        val vibrator = 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }.let { 
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    (it as VibratorManager).defaultVibrator
                } else {
                    it as Vibrator
                }
            }
        
        if (!vibrator.hasVibrator()) {
            Log.w(TAG, "Device does not have vibrator")
            return
        }
        
        Log.d(TAG, "Morse time: $hour:$minute")
        
        val pattern = mutableListOf<Long>()
        
        // 開始前的停頓
        pattern.add(500L)
        
        // 小時的摩斯電碼
        val hourStr = String.format("%02d", hour)
        Log.d(TAG, "Hour in Morse: $hourStr")
        
        for (digit in hourStr) {
            val morseCode = getMorseCode(digit)
            Log.d(TAG, "Digit $digit: $morseCode")
            
            for (symbol in morseCode) {
                if (symbol == '.') {
                    pattern.add(SHORT_VIBRATION)
                } else if (symbol == '-') {
                    pattern.add(LONG_VIBRATION)
                }
                pattern.add(SHORT_PAUSE)
            }
            // 數字間的停頓
            pattern.add(500L)
        }
        
        // 小時和分鐘之間的長停頓
        pattern.add(LONG_PAUSE)
        
        // 分鐘的摩斯電碼
        val minuteStr = String.format("%02d", minute)
        Log.d(TAG, "Minute in Morse: $minuteStr")
        
        for (digit in minuteStr) {
            val morseCode = getMorseCode(digit)
            Log.d(TAG, "Digit $digit: $morseCode")
            
            for (symbol in morseCode) {
                if (symbol == '.') {
                    pattern.add(SHORT_VIBRATION)
                } else if (symbol == '-') {
                    pattern.add(LONG_VIBRATION)
                }
                pattern.add(SHORT_PAUSE)
            }
            // 數字間的停頓
            pattern.add(500L)
        }
        
        executeVibrationPattern(vibrator, pattern)
    }
    
    /**
     * 獲取數字的摩斯電碼
     */
    private fun getMorseCode(digit: Char): String {
        return when (digit) {
            '0' -> "-----"
            '1' -> ".----"
            '2' -> "..---"
            '3' -> "...--"
            '4' -> "....-"
            '5' -> "....."
            '6' -> "-...."
            '7' -> "--..."
            '8' -> "---.."
            '9' -> "----."
            else -> "....."
        }
    }
    
    /**
     * 執行震動模式
     */
    private fun executeVibrationPattern(vibrator: Vibrator, pattern: List<Long>) {
        val vibrationPattern = pattern.toLongArray()
        
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(vibrationPattern, -1)
            }
            
            Log.d(TAG, "Advanced vibration pattern executed: ${vibrationPattern.contentToString()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error executing advanced vibration pattern", e)
        }
    }
}
