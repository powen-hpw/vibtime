package com.example.vibtime.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.vibtime.data.database.VibtimeDatabase
import com.example.vibtime.data.database.entities.LanguageEntity
import com.example.vibtime.data.database.entities.LocalizedStringEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 本地化資料庫存儲庫
 * 管理多語言數據的存取
 */
class LocalizationRepository(private val database: VibtimeDatabase) {
    
    private val languageDao = database.languageDao()
    private val localizedStringDao = database.localizedStringDao()
    
    // LiveData 用於觀察語言變化
    private val _currentLanguage = MutableLiveData<String>()
    val currentLanguage: LiveData<String> = _currentLanguage
    
    private val _supportedLanguages = MutableLiveData<List<LanguageEntity>>()
    val supportedLanguages: LiveData<List<LanguageEntity>> = _supportedLanguages
    
    private val _localizedStrings = MutableLiveData<Map<String, String>>()
    val localizedStrings: LiveData<Map<String, String>> = _localizedStrings
    
    /**
     * 初始化資料庫
     */
    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            // 檢查是否已經初始化
            if (languageDao.getActiveLanguageCount() == 0) {
                insertDefaultData()
            }
            
            // 載入支持的語言
            val languages = languageDao.getAllActiveLanguages().first()
            _supportedLanguages.postValue(languages)
        }
    }
    
    /**
     * 插入預設數據
     */
    private suspend fun insertDefaultData() {
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
        insertDefaultStrings()
    }
    
    /**
     * 插入預設字串
     */
    private suspend fun insertDefaultStrings() {
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
        
        localizedStringDao.insertStrings(defaultStrings)
    }
    
    /**
     * 獲取本地化字串
     */
    suspend fun getString(key: String, languageCode: String? = null): String? {
        return withContext(Dispatchers.IO) {
            val currentLang = languageCode ?: _currentLanguage.value ?: "en"
            localizedStringDao.getStringValue(key, currentLang)
        }
    }
    
    /**
     * 載入指定語言的本地化字串
     */
    suspend fun loadLocalizedStrings(languageCode: String) {
        withContext(Dispatchers.IO) {
            val strings = localizedStringDao.getStringsByLanguage(languageCode).first()
            val stringMap = strings.associate { it.key to it.value }
            _localizedStrings.postValue(stringMap)
        }
    }
    
    /**
     * 設置當前語言
     */
    fun setCurrentLanguage(languageCode: String) {
        _currentLanguage.value = languageCode
        CoroutineScope(Dispatchers.IO).launch {
            loadLocalizedStrings(languageCode)
        }
    }
    
    /**
     * 添加新語言
     */
    suspend fun addLanguage(language: LanguageEntity) {
        withContext(Dispatchers.IO) {
            languageDao.insertLanguage(language)
        }
    }
    
    /**
     * 添加本地化字串
     */
    suspend fun addLocalizedString(localizedString: LocalizedStringEntity) {
        withContext(Dispatchers.IO) {
            localizedStringDao.insertString(localizedString)
        }
    }
    
    /**
     * 批量添加本地化字串
     */
    suspend fun addLocalizedStrings(localizedStrings: List<LocalizedStringEntity>) {
        withContext(Dispatchers.IO) {
            localizedStringDao.insertStrings(localizedStrings)
        }
    }
    
    /**
     * 檢查語言是否支持
     */
    suspend fun isLanguageSupported(languageCode: String): Boolean {
        return withContext(Dispatchers.IO) {
            languageDao.getLanguageByCode(languageCode) != null
        }
    }
    
    /**
     * 獲取語言顯示名稱
     */
    suspend fun getLanguageDisplayName(languageCode: String): String? {
        return withContext(Dispatchers.IO) {
            languageDao.getLanguageByCode(languageCode)?.name
        }
    }
    
    /**
     * 獲取所有支持的語言
     */
    suspend fun getAllSupportedLanguages(): List<LanguageEntity> {
        return withContext(Dispatchers.IO) {
            languageDao.getAllActiveLanguages().first()
        }
    }
    
    /**
     * 搜尋本地化字串
     */
    suspend fun searchStrings(query: String, languageCode: String): List<LocalizedStringEntity> {
        return withContext(Dispatchers.IO) {
            localizedStringDao.searchStrings(query, languageCode).first()
        }
    }
    
    /**
     * 獲取指定分類的字串
     */
    suspend fun getStringsByCategory(category: String, languageCode: String): List<LocalizedStringEntity> {
        return withContext(Dispatchers.IO) {
            localizedStringDao.getStringsByCategory(category, languageCode).first()
        }
    }
}
