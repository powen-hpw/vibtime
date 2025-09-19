# 🌍 Vibtime 多語言架構指南

## 📋 概述

Vibtime 現在使用可擴充的多語言架構，支持動態添加新語言，使用資料庫存儲而不是硬編碼字串。

## 🏗️ 架構設計

### 1. 資料庫架構

```
VibtimeDatabase
├── LanguageEntity (語言表)
│   ├── code (語言代碼)
│   ├── name (語言名稱)
│   ├── nativeName (本地語言名稱)
│   ├── isActive (是否啟用)
│   ├── isDefault (是否為預設)
│   └── sortOrder (排序順序)
│
└── LocalizedStringEntity (本地化字串表)
    ├── key (字串鍵值)
    ├── languageCode (語言代碼)
    ├── value (本地化字串值)
    ├── category (分類)
    ├── isHtml (是否包含HTML)
    └── lastUpdated (最後更新時間)
```

### 2. 支持的語言

| 語言代碼 | 語言名稱 | 本地名稱 | 狀態 |
|---------|---------|---------|------|
| system | Follow System | Follow System | ✅ 啟用 |
| en | English | English | ✅ 啟用 |
| zh-TW | 繁體中文 | 繁體中文 | ✅ 啟用 |
| zh-CN | 简体中文 | 简体中文 | ✅ 啟用 |
| ja | 日本語 | 日本語 | ✅ 啟用 |
| es | Español | Español | ✅ 啟用 |

## 🔧 使用方法

### 1. 基本使用

```kotlin
// 在 Activity 或 Fragment 中
class MyActivity : AppCompatActivity() {
    private lateinit var localizationRepository: LocalizationRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化本地化系統
        val database = VibtimeDatabase.getDatabase(this)
        localizationRepository = LocalizationRepository(database)
        
        // 初始化資料庫
        lifecycleScope.launch {
            localizationRepository.initialize()
        }
    }
    
    // 獲取本地化字串
    private suspend fun getLocalizedString(key: String): String? {
        return localizationRepository.getString(key)
    }
}
```

### 2. 觀察語言變化

```kotlin
// 觀察當前語言
localizationRepository.currentLanguage.observe(this) { languageCode ->
    // 語言改變時的處理
    updateUI(languageCode)
}

// 觀察本地化字串
localizationRepository.localizedStrings.observe(this) { strings ->
    // 字串更新時的處理
    updateTextViews(strings)
}
```

### 3. 切換語言

```kotlin
// 設置新語言
localizationRepository.setCurrentLanguage("es") // 切換到西班牙語

// 應用語言到整個應用
val newContext = LocaleManager.applyLanguageToApp(context, "es")
```

## 📁 文件結構

```
app/src/main/java/com/example/vibtime/
├── data/
│   ├── database/
│   │   ├── VibtimeDatabase.kt
│   │   ├── entities/
│   │   │   ├── LanguageEntity.kt
│   │   │   └── LocalizedStringEntity.kt
│   │   └── dao/
│   │       ├── LanguageDao.kt
│   │       └── LocalizedStringDao.kt
│   └── repository/
│       └── LocalizationRepository.kt
├── utils/
│   ├── LocaleManager.kt (舊版本，向後兼容)
│   └── EnhancedLocaleManager.kt (新版本，資料庫驅動)
└── res/
    ├── values/strings.xml (預設英文)
    ├── values-zh-rTW/strings.xml (繁體中文)
    ├── values-zh-rCN/strings.xml (簡體中文)
    ├── values-ja/strings.xml (日文)
    └── values-es/strings.xml (西班牙文)
```

## 🚀 添加新語言

### 1. 添加語言資源文件

```bash
# 創建新的語言目錄
mkdir app/src/main/res/values-fr  # 法語
mkdir app/src/main/res/values-de  # 德語
mkdir app/src/main/res/values-ko  # 韓語
```

### 2. 創建 strings.xml

```xml
<!-- app/src/main/res/values-fr/strings.xml -->
<resources>
    <string name="app_name">Vibtime</string>
    <string name="nav_home">Accueil</string>
    <string name="start_service">Démarrer le Service</string>
    <!-- 更多字串... -->
</resources>
```

### 3. 添加語言到資料庫

```kotlin
// 在應用初始化時添加新語言
val newLanguage = LanguageEntity(
    code = "fr",
    name = "Français",
    nativeName = "Français",
    isActive = true,
    isDefault = false,
    sortOrder = 6
)

localizationRepository.addLanguage(newLanguage)
```

### 4. 添加本地化字串

```kotlin
val frenchStrings = listOf(
    LocalizedStringEntity("app_name", "fr", "Vibtime", "general"),
    LocalizedStringEntity("nav_home", "fr", "Accueil", "navigation"),
    LocalizedStringEntity("start_service", "fr", "Démarrer le Service", "service"),
    // 更多字串...
)

localizationRepository.addLocalizedStrings(frenchStrings)
```

## 🔍 字串分類

字串按功能分類，便於管理：

| 分類 | 描述 | 範例 |
|------|------|------|
| general | 一般應用信息 | app_name, app_version |
| navigation | 導航相關 | nav_home, nav_settings |
| service | 服務相關 | start_service, stop_service |
| error | 錯誤訊息 | permission_denied_message |
| accessibility | 無障礙支持 | start_service_description |
| toast | Toast 訊息 | toast_service_started |
| ui | 用戶界面 | button_text, label_text |

## 🛠️ 開發工具

### 1. 搜尋字串

```kotlin
// 搜尋包含特定關鍵字的字串
val searchResults = localizationRepository.searchStrings("service", "en")
```

### 2. 按分類獲取字串

```kotlin
// 獲取特定分類的所有字串
val serviceStrings = localizationRepository.getStringsByCategory("service", "en")
```

### 3. 檢查語言支持

```kotlin
// 檢查語言是否支持
val isSupported = localizationRepository.isLanguageSupported("fr")
```

## 📊 性能優化

### 1. 延遲載入

```kotlin
// 只在需要時載入特定語言的字符串
localizationRepository.loadLocalizedStrings("es")
```

### 2. 快取機制

```kotlin
// 使用 LiveData 自動快取和更新
val strings = localizationRepository.localizedStrings
```

### 3. 背景處理

```kotlin
// 所有資料庫操作都在背景線程執行
lifecycleScope.launch(Dispatchers.IO) {
    localizationRepository.initialize()
}
```

## 🔒 安全考慮

### 1. 輸入驗證

```kotlin
// 驗證語言代碼
fun isValidLanguageCode(code: String): Boolean {
    return code.matches(Regex("[a-z]{2}(-[A-Z]{2})?"))
}
```

### 2. SQL 注入防護

```kotlin
// 使用 Room 的參數化查詢
@Query("SELECT * FROM localized_strings WHERE key = :key AND language_code = :languageCode")
suspend fun getString(key: String, languageCode: String): LocalizedStringEntity?
```

## 🧪 測試

### 1. 單元測試

```kotlin
@Test
fun testGetLocalizedString() {
    runBlocking {
        val repository = LocalizationRepository(testDatabase)
        repository.initialize()
        
        val result = repository.getString("app_name", "en")
        assertEquals("Vibtime", result)
    }
}
```

### 2. UI 測試

```kotlin
@Test
fun testLanguageSwitch() {
    onView(withId(R.id.languageSpinner))
        .perform(click())
    
    onView(withText("Español"))
        .perform(click())
    
    onView(withId(R.id.appName))
        .check(matches(withText("Vibtime")))
}
```

## 📈 未來擴展

### 1. 動態語言下載

```kotlin
// 從服務器下載新語言包
suspend fun downloadLanguagePack(languageCode: String) {
    val languagePack = apiService.getLanguagePack(languageCode)
    localizationRepository.addLocalizedStrings(languagePack.strings)
}
```

### 2. 用戶自定義翻譯

```kotlin
// 允許用戶自定義翻譯
suspend fun updateUserTranslation(key: String, value: String) {
    val userString = LocalizedStringEntity(
        key = key,
        languageCode = "user_custom",
        value = value,
        category = "user_custom"
    )
    localizationRepository.addLocalizedString(userString)
}
```

### 3. 自動翻譯

```kotlin
// 使用 AI 自動翻譯缺失的字串
suspend fun autoTranslateMissingStrings() {
    val missingStrings = getMissingStrings()
    val translations = translateService.translate(missingStrings)
    localizationRepository.addLocalizedStrings(translations)
}
```

## 🎯 最佳實踐

1. **始終使用字串資源** - 避免硬編碼字串
2. **按分類組織字串** - 便於管理和維護
3. **使用描述性鍵值** - 如 `start_service` 而不是 `btn1`
4. **支持 HTML 標籤** - 用於格式化文本
5. **定期備份翻譯** - 防止數據丟失
6. **測試所有語言** - 確保 UI 適配正確
7. **監控性能** - 避免過度載入數據

## 📞 支持

如有問題或建議，請聯繫開發團隊或查看項目文檔。

---

**版本**: 1.3.0  
**最後更新**: 2024-12-19  
**維護者**: Vibtime Development Team
