package com.example.vibtime.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.example.vibtime.R
import java.util.*

/**
 * 語言切換管理器
 */
object LocaleManager {
    
    private const val LANGUAGE_KEY = "selected_language"
    private const val TAG = "LocaleManager"
    
    /**
     * 設定語言
     */
    fun setLocale(context: Context, languageCode: String): Context {
        // 使用 applyLanguageToApp 的邏輯確保一致性
        val effectiveCode = if (languageCode == "system") getSystemLanguage() else languageCode
        
        // 如果語言不被支援，使用預設語言而不是直接返回原始 context
        val finalCode = if (!isSupportedLanguage(effectiveCode)) {
            android.util.Log.w(TAG, "Unsupported language code: $effectiveCode, using zh-TW as default")
            "zh-TW"
        } else {
            effectiveCode
        }
        
        val locale = when (finalCode) {
            "zh-TW" -> Locale("zh", "TW")
            "zh-CN" -> Locale("zh", "CN")
            "en" -> Locale("en", "US")
            "ja" -> Locale("ja", "JP")
            else -> {
                android.util.Log.w(TAG, "Unknown language: $finalCode, using zh-TW")
                Locale("zh", "TW")
            }
        }
        
        android.util.Log.d(TAG, "setLocale -> setting locale to: ${locale.language}-${locale.country}")
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        // 保存語言設定
        saveLanguage(context, languageCode)
        
        val newContext = context.createConfigurationContext(config)
        android.util.Log.d(TAG, "setLocale -> created new context with locale: ${newContext.resources.configuration.locales[0]}")
        return newContext
    }
    
    /**
     * 獲取當前語言
     */
    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        val stored = prefs.getString(LANGUAGE_KEY, "system") ?: "system"
        android.util.Log.d(TAG, "getCurrentLanguage -> $stored")
        return stored
    }
    
    /**
     * 保存語言設定
     */
    fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        android.util.Log.d(TAG, "saveLanguage -> $languageCode")
        prefs.edit().putString(LANGUAGE_KEY, languageCode).apply()
    }
    
    /**
     * 獲取系統語言
     */
    private fun getSystemLanguage(): String {
        val systemLocale = Locale.getDefault()
        return when {
            systemLocale.language == "zh" && systemLocale.country == "TW" -> "zh-TW"
            systemLocale.language == "zh" && systemLocale.country == "CN" -> "zh-CN"
            systemLocale.language == "zh" && (systemLocale.country.isEmpty() || systemLocale.country == "HK") -> "zh-TW" // 香港和未指定國家的中文預設使用繁體中文
            systemLocale.language == "en" -> "en"
            systemLocale.language == "ja" -> "ja"
            else -> {
                // 記錄系統語言以便調試
                android.util.Log.d(TAG, "System language: ${systemLocale.language}-${systemLocale.country}")
                
                // 如果系統語言不是我們支援的語言，則根據語言代碼選擇最接近的語言
                when (systemLocale.language) {
                    "zh" -> "zh-TW" // 任何其他中文變體使用繁體中文
                    "en" -> "en"    // 任何英文變體使用英文
                    "ja" -> "ja"    // 日文
                    else -> "en"    // 其他語言預設使用英文
                }
            }
        }
    }
    
    /**
     * 獲取支援的語言列表
     */
    fun getSupportedLanguages(context: Context): List<LanguageOption> {
        return listOf(
            LanguageOption("system", context.getString(R.string.language_system), "🌐"),
            LanguageOption("zh-TW", context.getString(R.string.language_zh_tw), "🇹🇼"),
            LanguageOption("en", context.getString(R.string.language_en), "🇺🇸"),
            LanguageOption("zh-CN", context.getString(R.string.language_zh_cn), "🇨🇳"),
            LanguageOption("ja", context.getString(R.string.language_ja), "🇯🇵")
        )
    }
    
    /**
     * 獲取語言顯示名稱
     */
    fun getLanguageDisplayName(context: Context, languageCode: String): String {
        return when (languageCode) {
            "system" -> context.getString(R.string.language_system)
            "zh-TW" -> context.getString(R.string.language_zh_tw)
            "en" -> context.getString(R.string.language_en)
            "zh-CN" -> context.getString(R.string.language_zh_cn)
            "ja" -> context.getString(R.string.language_ja)
            else -> context.getString(R.string.language_zh_tw)
        }
    }
    
    /**
     * 檢查是否為支援的語言（不包含 "system"，僅檢查實際語言代碼）
     */
    fun isSupportedLanguage(languageCode: String): Boolean {
        return languageCode == "zh-TW" || languageCode == "zh-CN" || languageCode == "en" || languageCode == "ja"
    }
    
    /**
     * 檢查是否為有效的語言選項（包含 "system"）
     */
    fun isValidLanguageOption(languageCode: String): Boolean {
        return languageCode == "system" || isSupportedLanguage(languageCode)
    }
    
    /**
     * 應用語言到整個應用程式
     */
    fun applyLanguageToApp(context: Context, languageCode: String): Context {
        val effectiveCode = if (languageCode == "system") getSystemLanguage() else languageCode
        android.util.Log.d(TAG, "applyLanguageToApp -> requested=$languageCode, effective=$effectiveCode")

        // 如果語言不被支援，使用預設語言而不是直接返回原始 context
        val finalCode = if (!isSupportedLanguage(effectiveCode)) {
            android.util.Log.w(TAG, "Unsupported language: $effectiveCode, using zh-TW as default")
            "zh-TW"
        } else {
            effectiveCode
        }

        val locale = when (finalCode) {
            "zh-TW" -> Locale("zh", "TW")
            "zh-CN" -> Locale("zh", "CN")
            "en" -> Locale("en", "US")
            "ja" -> Locale("ja", "JP")
            else -> {
                android.util.Log.w(TAG, "Unknown language: $finalCode, using zh-TW")
                Locale("zh", "TW")
            }
        }

        android.util.Log.d(TAG, "Setting locale to: ${locale.language}-${locale.country}")
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)

        // 不在此自動保存，避免覆蓋「跟隨系統」或使用者尚未確認的設定

        val newContext = context.createConfigurationContext(config)
        android.util.Log.d(TAG, "Created new context with locale: ${newContext.resources.configuration.locales[0]}")
        return newContext
    }
    
    data class LanguageOption(
        val code: String,
        val name: String,
        val flag: String
    )
}
