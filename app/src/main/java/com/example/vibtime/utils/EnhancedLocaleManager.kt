package com.example.vibtime.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.*

/**
 * 簡化的增強語言管理器
 * 使用 Android 原生字串資源系統，支援動態語言切換
 */
object EnhancedLocaleManager {
    private const val PREF_NAME = "vibtime_language"
    private const val PREF_LANGUAGE = "language"
    
    private val _currentLanguage = MutableLiveData<String>()
    val currentLanguage: LiveData<String> = _currentLanguage
    
    private val _supportedLanguages = MutableLiveData<List<LanguageOption>>()
    val supportedLanguages: LiveData<List<LanguageOption>> = _supportedLanguages
    
    /**
     * 語言選項資料類
     */
    data class LanguageOption(
        val code: String,
        val name: String,
        val nativeName: String,
        val flag: String
    )
    
    /**
     * 初始化語言管理器
     */
    fun initialize(context: Context) {
        initializeSupportedLanguages()
        loadCurrentLanguage(context)
    }
    
    /**
     * 初始化支援的語言列表
     */
    private fun initializeSupportedLanguages() {
        val languages = listOf(
            LanguageOption("system", "Follow System", "跟隨系統", "🌐"),
            LanguageOption("en", "English", "English", "🇺🇸"),
            LanguageOption("zh-TW", "繁體中文", "繁體中文", "🇹🇼"),
            LanguageOption("zh-CN", "简体中文", "简体中文", "🇨🇳"),
            LanguageOption("ja", "日本語", "日本語", "🇯🇵"),
            LanguageOption("es", "Español", "Español", "🇪🇸")
        )
        _supportedLanguages.value = languages
    }
    
    /**
     * 載入當前語言設定
     */
    private fun loadCurrentLanguage(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString(PREF_LANGUAGE, "system") ?: "system"
        _currentLanguage.value = savedLanguage
    }
    
    /**
     * 獲取當前語言
     */
    fun getCurrentLanguage(context: Context): String {
        return _currentLanguage.value ?: "system"
    }
    
    /**
     * 設置語言
     */
    fun setLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_LANGUAGE, language).apply()
        _currentLanguage.value = language
    }
    
    /**
     * 應用語言到應用程式
     */
    fun applyLanguageToApp(context: Context, language: String): Context {
        val locale = when (language) {
            "system" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.resources.configuration.locales[0]
                } else {
                    @Suppress("DEPRECATION")
                    context.resources.configuration.locale
                }
            }
            "zh-TW" -> Locale.TRADITIONAL_CHINESE
            "zh-CN" -> Locale.SIMPLIFIED_CHINESE
            "en" -> Locale.ENGLISH
            "ja" -> Locale.JAPANESE
            "es" -> Locale("es")
            else -> Locale.ENGLISH
        }
        
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
    
    /**
     * 獲取本地化字串（使用 Android 原生資源系統）
     */
    fun getString(context: Context, key: String): String {
        return try {
            val resourceId = context.resources.getIdentifier(key, "string", context.packageName)
            if (resourceId != 0) {
                context.getString(resourceId)
            } else {
                key // 如果找不到資源，返回 key 本身
            }
        } catch (e: Exception) {
            key // 發生錯誤時返回 key 本身
        }
    }
    
    /**
     * 獲取本地化字串（帶格式化參數）
     */
    fun getString(context: Context, key: String, vararg args: Any): String {
        return try {
            val resourceId = context.resources.getIdentifier(key, "string", context.packageName)
            if (resourceId != 0) {
                context.getString(resourceId, *args)
            } else {
                key // 如果找不到資源，返回 key 本身
            }
        } catch (e: Exception) {
            key // 發生錯誤時返回 key 本身
        }
    }
    
    /**
     * 檢查語言是否被支援
     */
    fun isSupportedLanguage(languageCode: String): Boolean {
        val supportedCodes = listOf("system", "en", "zh-TW", "zh-CN", "ja", "es")
        return supportedCodes.contains(languageCode)
    }
    
    /**
     * 獲取系統語言
     */
    fun getSystemLanguage(): String {
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Locale.getDefault()
        } else {
            @Suppress("DEPRECATION")
            Locale.getDefault()
        }
        
        return when {
            systemLocale.language == "zh" -> {
                when (systemLocale.country) {
                    "TW", "HK", "MO" -> "zh-TW"
                    "CN", "SG" -> "zh-CN"
                    else -> "zh-TW" // 預設為繁體中文
                }
            }
            systemLocale.language == "en" -> "en"
            systemLocale.language == "ja" -> "ja"
            systemLocale.language == "es" -> "es"
            else -> "en" // 預設為英文
        }
    }
    
    /**
     * 獲取語言顯示名稱
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "system" -> "Follow System"
            "en" -> "English"
            "zh-TW" -> "繁體中文"
            "zh-CN" -> "简体中文"
            "ja" -> "日本語"
            "es" -> "Español"
            else -> "English"
        }
    }
    
    /**
     * 獲取語言本地名稱
     */
    fun getLanguageNativeName(languageCode: String): String {
        return when (languageCode) {
            "system" -> "跟隨系統"
            "en" -> "English"
            "zh-TW" -> "繁體中文"
            "zh-CN" -> "简体中文"
            "ja" -> "日本語"
            "es" -> "Español"
            else -> "English"
        }
    }
}