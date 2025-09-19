package com.example.vibtime.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Premium Manager
 * 管理進階功能購買狀態和邏輯
 */
object PremiumManager {
    
    private const val TAG = "PremiumManager"
    private const val PREFS_NAME = "premium_prefs"
    
    // Premium Features
    const val FEATURE_BINARY = "binary_time"
    const val FEATURE_MORSE = "morse_time"
    
    // Purchase Tracking
    private const val KEY_BINARY_OWNED = "binary_owned"
    private const val KEY_MORSE_OWNED = "morse_owned"
    private const val KEY_VIBRATION_COUNT = "vibration_count"
    private const val KEY_LAST_POPUP_COUNT = "last_popup_count"
    private const val KEY_POPUP_SHOWN_TODAY = "popup_shown_today"
    
    private lateinit var prefs: SharedPreferences
    
    /**
     * 初始化 Premium Manager
     */
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        Log.d(TAG, "PremiumManager initialized")
    }
    
    /**
     * 檢查是否擁有二進制報時功能
     */
    fun isBinaryOwned(): Boolean {
        return prefs.getBoolean(KEY_BINARY_OWNED, false)
    }
    
    /**
     * 檢查是否擁有摩斯電碼報時功能
     */
    fun isMorseOwned(): Boolean {
        return prefs.getBoolean(KEY_MORSE_OWNED, false)
    }
    
    /**
     * 檢查是否擁有任何進階功能
     */
    fun hasAnyPremiumFeature(): Boolean {
        return isBinaryOwned() || isMorseOwned()
    }
    
    /**
     * 設置二進制功能為已擁有
     */
    fun setBinaryOwned() {
        prefs.edit().putBoolean(KEY_BINARY_OWNED, true).apply()
        Log.d(TAG, "Binary feature purchased")
    }
    
    /**
     * 設置摩斯電碼功能為已擁有
     */
    fun setMorseOwned() {
        prefs.edit().putBoolean(KEY_MORSE_OWNED, true).apply()
        Log.d(TAG, "Morse feature purchased")
    }
    
    /**
     * 記錄震動使用次數
     */
    fun recordVibrationUsage() {
        val currentCount = prefs.getInt(KEY_VIBRATION_COUNT, 0)
        prefs.edit().putInt(KEY_VIBRATION_COUNT, currentCount + 1).apply()
        Log.d(TAG, "Vibration count: ${currentCount + 1}")
    }
    
    /**
     * 檢查是否應該顯示購買彈窗
     */
    fun shouldShowPurchasePopup(): Boolean {
        // 如果已經擁有任何進階功能，不顯示彈窗
        if (hasAnyPremiumFeature()) {
            return false
        }
        
        val vibrationCount = prefs.getInt(KEY_VIBRATION_COUNT, 0)
        val lastPopupCount = prefs.getInt(KEY_LAST_POPUP_COUNT, 0)
        
        // 智能彈出策略：第3次、第7次、第15次、第31次...
        val popupTriggers = listOf(3, 7, 15, 31, 63, 127)
        
        for (trigger in popupTriggers) {
            if (vibrationCount >= trigger && lastPopupCount < trigger) {
                prefs.edit().putInt(KEY_LAST_POPUP_COUNT, trigger).apply()
                Log.d(TAG, "Should show popup at count: $trigger")
                return true
            }
        }
        
        return false
    }
    
    /**
     * 獲取當前震動使用次數
     */
    fun getVibrationCount(): Int {
        return prefs.getInt(KEY_VIBRATION_COUNT, 0)
    }
    
    /**
     * 模擬購買流程（實際應用中需要整合真實的支付系統）
     */
    fun simulatePurchase(feature: String): Boolean {
        return when (feature) {
            FEATURE_BINARY -> {
                setBinaryOwned()
                true
            }
            FEATURE_MORSE -> {
                setMorseOwned()
                true
            }
            else -> false
        }
    }
    
    /**
     * 重置所有購買狀態（用於測試）
     */
    fun resetAllPurchases() {
        prefs.edit()
            .putBoolean(KEY_BINARY_OWNED, false)
            .putBoolean(KEY_MORSE_OWNED, false)
            .putInt(KEY_VIBRATION_COUNT, 0)
            .putInt(KEY_LAST_POPUP_COUNT, 0)
            .apply()
        Log.d(TAG, "All purchases reset")
    }
}
