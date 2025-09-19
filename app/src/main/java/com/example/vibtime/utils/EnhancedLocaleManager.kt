package com.example.vibtime.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.vibtime.data.database.VibtimeDatabase
import com.example.vibtime.data.database.entities.LanguageEntity
import com.example.vibtime.data.database.entities.LocalizedStringEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * 可擴充的增強語言管理器
 * 使用資料庫存儲多語言數據，支持動態添加新語言
 */
object EnhancedLocaleManager {
    private const val PREF_NAME = "vibtime_language"
    private const val PREF_LANGUAGE = "language"
    
    private var database: VibtimeDatabase? = null
    private val _currentLanguage = MutableLiveData<String>()
    val currentLanguage: LiveData<String> = _currentLanguage
    
    private val _supportedLanguages = MutableLiveData<List<LanguageEntity>>()
    val supportedLanguages: LiveData<List<LanguageEntity>> = _supportedLanguages
    
    private val _localizedStrings = MutableLiveData<Map<String, String>>()
    val localizedStrings: LiveData<Map<String, String>> = _localizedStrings
    
    /**
     * 初始化語言管理器
     */
    fun initialize(context: Context) {
        database = VibtimeDatabase.getDatabase(context)
        initializeDefaultLanguages()
        loadCurrentLanguage(context)
    }
    
    /**
     * 初始化預設語言數據
     */
    private fun initializeDefaultLanguages() {
        val db = database ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            val languageDao = db.languageDao()
            val localizedStringDao = db.localizedStringDao()
            
            // 檢查是否已經初始化
            if (languageDao.getActiveLanguageCount() == 0) {
                // 插入預設語言
                val defaultLanguages = listOf(
                    LanguageEntity("system", "Follow System", "Follow System", true, true, 0),
                    LanguageEntity("en", "English", "English", true, false, 1),
                    LanguageEntity("zh-TW", "繁體中文", "繁體中文", true, false, 2),
                    LanguageEntity("zh-CN", "简体中文", "简体中文", true, false, 3),
                    LanguageEntity("ja", "日本語", "日本語", true, false, 4),
                    LanguageEntity("es", "Español", "Español", true, false, 5)
                )
                
                languageDao.insertLanguages(defaultLanguages)
                
                // 插入預設字串
                insertDefaultStrings(localizedStringDao)
            }
            
            // 載入支持的語言列表
            val languages = languageDao.getAllActiveLanguages().first()
            withContext(Dispatchers.Main) {
                _supportedLanguages.value = languages
            }
        }
    }
    
    /**
     * 插入預設字串
     */
    private suspend fun insertDefaultStrings(dao: LocalizedStringDao) {
        val defaultStrings = listOf(
            // 基本應用信息
            LocalizedStringEntity("app_name", "en", "Vibtime", "general"),
            LocalizedStringEntity("app_name", "zh-TW", "Vibtime", "general"),
            LocalizedStringEntity("app_name", "zh-CN", "Vibtime", "general"),
            LocalizedStringEntity("app_name", "ja", "Vibtime", "general"),
            LocalizedStringEntity("app_name", "es", "Vibtime", "general"),
            
            // 導航
            LocalizedStringEntity("nav_home", "en", "Home", "navigation"),
            LocalizedStringEntity("nav_home", "zh-TW", "首頁", "navigation"),
            LocalizedStringEntity("nav_home", "zh-CN", "首页", "navigation"),
            LocalizedStringEntity("nav_home", "ja", "ホーム", "navigation"),
            LocalizedStringEntity("nav_home", "es", "Inicio", "navigation"),
            
            LocalizedStringEntity("nav_settings", "en", "Settings", "navigation"),
            LocalizedStringEntity("nav_settings", "zh-TW", "設定", "navigation"),
            LocalizedStringEntity("nav_settings", "zh-CN", "设置", "navigation"),
            LocalizedStringEntity("nav_settings", "ja", "設定", "navigation"),
            LocalizedStringEntity("nav_settings", "es", "Configuración", "navigation"),
            
            // 服務相關
            LocalizedStringEntity("start_service", "en", "Start Service", "service"),
            LocalizedStringEntity("start_service", "zh-TW", "啟動服務", "service"),
            LocalizedStringEntity("start_service", "zh-CN", "启动服务", "service"),
            LocalizedStringEntity("start_service", "ja", "サービス開始", "service"),
            LocalizedStringEntity("start_service", "es", "Iniciar Servicio", "service"),
            
            LocalizedStringEntity("stop_service", "en", "Stop Service", "service"),
            LocalizedStringEntity("stop_service", "zh-TW", "停止服務", "service"),
            LocalizedStringEntity("stop_service", "zh-CN", "停止服务", "service"),
            LocalizedStringEntity("stop_service", "ja", "サービス停止", "service"),
            LocalizedStringEntity("stop_service", "es", "Detener Servicio", "service"),
            
            LocalizedStringEntity("service_running", "en", "Service Running", "service"),
            LocalizedStringEntity("service_running", "zh-TW", "服務運行中", "service"),
            LocalizedStringEntity("service_running", "zh-CN", "服务运行中", "service"),
            LocalizedStringEntity("service_running", "ja", "サービス実行中", "service"),
            LocalizedStringEntity("service_running", "es", "Servicio Ejecutándose", "service"),
            
            LocalizedStringEntity("service_stopped", "en", "Service Stopped", "service"),
            LocalizedStringEntity("service_stopped", "zh-TW", "服務已停止", "service"),
            LocalizedStringEntity("service_stopped", "zh-CN", "服务已停止", "service"),
            LocalizedStringEntity("service_stopped", "ja", "サービス停止済み", "service"),
            LocalizedStringEntity("service_stopped", "es", "Servicio Detenido", "service"),
            
            // 錯誤訊息
            LocalizedStringEntity("permission_denied_message", "en", "Unable to start service: insufficient permissions", "error"),
            LocalizedStringEntity("permission_denied_message", "zh-TW", "無法啟動服務：權限不足", "error"),
            LocalizedStringEntity("permission_denied_message", "zh-CN", "无法启动服务：权限不足", "error"),
            LocalizedStringEntity("permission_denied_message", "ja", "サービスを開始できません：権限が不足しています", "error"),
            LocalizedStringEntity("permission_denied_message", "es", "No se puede iniciar el servicio: permisos insuficientes", "error"),
            
            // 無障礙支持
            LocalizedStringEntity("start_service_description", "en", "Start vibration service to detect taps and provide time vibration", "accessibility"),
            LocalizedStringEntity("start_service_description", "zh-TW", "啟動震動服務以偵測敲擊並提供時間震動", "accessibility"),
            LocalizedStringEntity("start_service_description", "zh-CN", "启动震动服务以侦测敲击并提供时间震动", "accessibility"),
            LocalizedStringEntity("start_service_description", "ja", "タップを検出して時間振動を提供する振動サービスを開始", "accessibility"),
            LocalizedStringEntity("start_service_description", "es", "Iniciar servicio de vibración para detectar toques y proporcionar vibración de tiempo", "accessibility"),
            
            LocalizedStringEntity("stop_service_description", "en", "Stop vibration service", "accessibility"),
            LocalizedStringEntity("stop_service_description", "zh-TW", "停止震動服務", "accessibility"),
            LocalizedStringEntity("stop_service_description", "zh-CN", "停止震动服务", "accessibility"),
            LocalizedStringEntity("stop_service_description", "ja", "振動サービスを停止", "accessibility"),
            LocalizedStringEntity("stop_service_description", "es", "Detener servicio de vibración", "accessibility"),
            
            LocalizedStringEntity("test_vibration_description", "en", "Test vibration pattern for current time", "accessibility"),
            LocalizedStringEntity("test_vibration_description", "zh-TW", "測試當前時間的震動模式", "accessibility"),
            LocalizedStringEntity("test_vibration_description", "zh-CN", "测试当前时间的震动模式", "accessibility"),
            LocalizedStringEntity("test_vibration_description", "ja", "現在時刻の振動パターンをテスト", "accessibility"),
            LocalizedStringEntity("test_vibration_description", "es", "Probar patrón de vibración para la hora actual", "accessibility"),
            
            // Toast 訊息
            LocalizedStringEntity("toast_service_started", "en", "Service started", "toast"),
            LocalizedStringEntity("toast_service_started", "zh-TW", "服務已啟動", "toast"),
            LocalizedStringEntity("toast_service_started", "zh-CN", "服务已启动", "toast"),
            LocalizedStringEntity("toast_service_started", "ja", "サービス開始", "toast"),
            LocalizedStringEntity("toast_service_started", "es", "Servicio iniciado", "toast"),
            
            LocalizedStringEntity("toast_service_stopped", "en", "Service stopped", "toast"),
            LocalizedStringEntity("toast_service_stopped", "zh-TW", "服務已停止", "toast"),
            LocalizedStringEntity("toast_service_stopped", "zh-CN", "服务已停止", "toast"),
            LocalizedStringEntity("toast_service_stopped", "ja", "サービス停止", "toast"),
            LocalizedStringEntity("toast_service_stopped", "es", "Servicio detenido", "toast")
        )
        
        dao.insertStrings(defaultStrings)
    }
    
    /**
     * 獲取當前語言
     */
    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_LANGUAGE, "system") ?: "system"
    }
    
    /**
     * 載入當前語言
     */
    private fun loadCurrentLanguage(context: Context) {
        val current = getCurrentLanguage(context)
        _currentLanguage.value = current
    }
    
    /**
     * 設置語言
     */
    fun setLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_LANGUAGE, language).apply()
        _currentLanguage.value = language
        loadLocalizedStrings(language)
    }
    
    /**
     * 載入本地化字串
     */
    private fun loadLocalizedStrings(languageCode: String) {
        val db = database ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            val strings = db.localizedStringDao().getStringsByLanguage(languageCode).first()
            val stringMap = strings.associate { it.key to it.value }
            withContext(Dispatchers.Main) {
                _localizedStrings.value = stringMap
            }
        }
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
     * 獲取本地化字串
     */
    suspend fun getString(key: String, languageCode: String? = null): String? {
        val db = database ?: return null
        val currentLang = languageCode ?: _currentLanguage.value ?: "en"
        
        return withContext(Dispatchers.IO) {
            db.localizedStringDao().getStringValue(key, currentLang)
        }
    }
    
    /**
     * 獲取本地化字串 (同步版本，用於緊急情況)
     */
    fun getStringSync(key: String, languageCode: String? = null): String? {
        val currentLang = languageCode ?: _currentLanguage.value ?: "en"
        return _localizedStrings.value?.get(key)
    }
    
    /**
     * 添加新語言
     */
    suspend fun addLanguage(language: LanguageEntity) {
        val db = database ?: return
        
        withContext(Dispatchers.IO) {
            db.languageDao().insertLanguage(language)
        }
    }
    
    /**
     * 添加本地化字串
     */
    suspend fun addLocalizedString(localizedString: LocalizedStringEntity) {
        val db = database ?: return
        
        withContext(Dispatchers.IO) {
            db.localizedStringDao().insertString(localizedString)
        }
    }
    
    /**
     * 批量添加本地化字串
     */
    suspend fun addLocalizedStrings(localizedStrings: List<LocalizedStringEntity>) {
        val db = database ?: return
        
        withContext(Dispatchers.IO) {
            db.localizedStringDao().insertStrings(localizedStrings)
        }
    }
    
    /**
     * 檢查語言是否支持
     */
    suspend fun isLanguageSupported(languageCode: String): Boolean {
        val db = database ?: return false
        
        return withContext(Dispatchers.IO) {
            db.languageDao().getLanguageByCode(languageCode) != null
        }
    }
    
    /**
     * 獲取語言顯示名稱
     */
    suspend fun getLanguageDisplayName(languageCode: String): String? {
        val db = database ?: return null
        
        return withContext(Dispatchers.IO) {
            db.languageDao().getLanguageByCode(languageCode)?.name
        }
    }
    
    /**
     * 獲取支援的語言列表 (兼容舊版本)
     */
    fun getSupportedLanguages(context: Context): List<LanguageOption> {
        val languages = _supportedLanguages.value ?: emptyList()
        return languages.map { language ->
            LanguageOption(
                language.code,
                language.name,
                getFlagForLanguage(language.code)
            )
        }
    }
    
    /**
     * 獲取語言對應的國旗 emoji
     */
    private fun getFlagForLanguage(languageCode: String): String {
        return when (languageCode) {
            "system" -> "🌐"
            "en" -> "🇺🇸"
            "zh-TW" -> "🇹🇼"
            "zh-CN" -> "🇨🇳"
            "ja" -> "🇯🇵"
            "es" -> "🇪🇸"
            else -> "🌐"
        }
    }
    
    data class LanguageOption(
        val code: String,
        val name: String,
        val flag: String
    )
}
