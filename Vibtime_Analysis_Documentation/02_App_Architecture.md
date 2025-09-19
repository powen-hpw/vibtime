# Vibtime App 架構分析

## 🏗️ 整體架構概述

Vibtime 採用現代 Android 開發架構，結合 MVVM 模式、Repository 模式和依賴注入，確保代碼的可維護性、可測試性和可擴展性。

## 📁 專案結構

```
app/src/main/java/com/example/vibtime/
├── 📱 MainActivity.kt                    # 主 Activity，管理 Navigation
├── 🚀 VibtimeApplication.kt             # Application 類，全局初始化
├── 📊 data/                             # 資料層
│   ├── database/                        # Room 資料庫 (已移除，改用原生字串資源)
│   └── repository/                      # 資料存儲庫
│       └── SettingsRepository.kt        # 設定資料存儲庫
├── 🎨 ui/                               # 用戶界面層
│   ├── home/                           # 主頁面
│   │   ├── HomeFragment.kt             # 主頁面 Fragment
│   │   └── HomeViewModel.kt            # 主頁面 ViewModel
│   ├── settings/                       # 設定頁面
│   │   └── SettingsFragment.kt         # 設定頁面 Fragment
│   ├── running/                        # 運行狀態頁面
│   │   └── RunningFragment.kt          # 運行狀態 Fragment
│   ├── history/                        # 歷史記錄頁面
│   │   └── HistoryFragment.kt          # 歷史記錄 Fragment
│   ├── vibration/                      # 震動設定頁面
│   │   └── VibrationFragment.kt        # 震動設定 Fragment
│   └── welcome/                        # 歡迎頁面
│       └── WelcomeFragment.kt          # 歡迎頁面 Fragment
├── ⚙️ service/                          # 服務層
│   ├── VibrationService.kt             # 前景震動服務
│   ├── TapDetectionService.kt          # 敲擊偵測服務
│   ├── TimeVibrationHelper.kt          # 時間震動輔助類
│   └── ScreenReceiver.kt               # 螢幕狀態接收器
├── 🛠️ utils/                           # 工具類
│   ├── LocaleManager.kt                # 語言管理
│   ├── NotificationHelper.kt           # 通知管理
│   ├── PermissionHelper.kt             # 權限管理
│   ├── SafetyManager.kt                # 安全管理
│   ├── ServiceManager.kt               # 服務管理
│   ├── PremiumManager.kt               # 進階功能管理
│   ├── AdManager.kt                    # 廣告管理
│   ├── BatteryOptimizationHelper.kt    # 電池優化輔助
│   ├── ExactAlarmManager.kt            # 精確鬧鐘管理
│   ├── WatchModeManager.kt             # Watch Mode 管理
│   └── ... (其他工具類)
└── 📢 ads/                             # 廣告管理
    └── AdManager.kt                    # 廣告管理器
```

## 🏛️ 架構模式

### 1. MVVM (Model-View-ViewModel) 模式

#### View Layer (視圖層)
- **Fragment**：負責 UI 顯示和用戶交互
- **Activity**：管理 Fragment 和 Navigation
- **ViewBinding**：類型安全的視圖綁定

#### ViewModel Layer (視圖模型層)
- **HomeViewModel**：管理主頁面的業務邏輯
- **LiveData**：響應式數據更新
- **Coroutines**：異步操作處理

#### Model Layer (模型層)
- **Repository**：數據訪問抽象層
- **SharedPreferences**：本地數據存儲
- **Service**：背景服務和業務邏輯

### 2. Repository 模式

```kotlin
// SettingsRepository.kt
class SettingsRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
    
    fun saveLanguage(language: String) {
        prefs.edit().putString("selected_language", language).apply()
    }
    
    fun getLanguage(): String {
        return prefs.getString("selected_language", "system") ?: "system"
    }
}
```

### 3. 依賴注入 (手動實現)

```kotlin
// 在 Fragment 中手動注入依賴
class HomeFragment : Fragment() {
    private lateinit var serviceManager: ServiceManager
    private lateinit var premiumManager: PremiumManager
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 手動初始化依賴
        serviceManager = ServiceManager(requireContext())
        premiumManager = PremiumManager(requireContext())
    }
}
```

## 🔧 核心組件架構

### 1. 服務架構

#### VibrationService (前景服務)
```kotlin
class VibrationService : Service() {
    companion object {
        fun startService(context: Context, sensitivity: TapSensitivity)
        fun stopService(context: Context)
        fun isServiceRunning(context: Context): Boolean
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 處理服務啟動、停止、更新敏感度等操作
        return START_STICKY
    }
}
```

#### TapDetectionService (敲擊偵測)
```kotlin
class TapDetectionService(
    private val context: Context,
    private val onTapDetected: () -> Unit,
    private val onTapCountChanged: (Int, Int) -> Unit,
    private val onWatchModeStatusChanged: (WatchModeStatus) -> Unit
) {
    enum class TapSensitivity { LOW, MEDIUM, HIGH }
    
    fun startDetection(useWatchMode: Boolean, watchModeDuration: Long)
    fun stopDetection()
    fun setSensitivity(sensitivity: TapSensitivity)
}
```

### 2. 工具類架構

#### LocaleManager (語言管理)
```kotlin
object LocaleManager {
    fun setLocale(context: Context, languageCode: String): Context
    fun getCurrentLanguage(context: Context): String
    fun getSupportedLanguages(context: Context): List<LanguageOption>
    fun applyLanguageToApp(context: Context, languageCode: String): Context
}
```

#### SafetyManager (安全管理)
```kotlin
object SafetyManager {
    const val MAX_SERVICE_RUNTIME = 3 * 60 * 60 * 1000L // 3小時
    const val MIN_VIBRATION_INTERVAL = 10 * 60 * 1000L // 10分鐘
    
    interface SafetyCallback {
        fun onServiceTimeUpdate(remainingTime: Long, progress: Float)
        fun onNextVibrationUpdate(remainingTime: Long, progress: Float)
        fun onServiceExpired()
        fun onFrequencyLimit(remainingTime: Long)
    }
}
```

### 3. 權限管理架構

#### PermissionHelper (權限管理)
```kotlin
object PermissionHelper {
    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.VIBRATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.FOREGROUND_SERVICE
    )
    
    val REQUIRED_PERMISSIONS_33_PLUS = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS
    )
    
    fun hasAllRequiredPermissions(context: Context): Boolean
    fun createPermissionLauncher(fragment: Fragment, onResult: (Map<String, Boolean>) -> Unit)
}
```

## 📱 UI 架構

### 1. Navigation 架構

```kotlin
// MainActivity.kt
private fun setupNavigation() {
    val navHostFragment = supportFragmentManager
        .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    val navController = navHostFragment.navController
    
    binding.bottomNavigation.setupWithNavController(navController)
}
```

### 2. Fragment 架構

#### HomeFragment (主頁面)
```kotlin
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPermissionLauncher()
        setupClickListeners()
        updateServiceStatus()
    }
}
```

### 3. ViewBinding 使用

```kotlin
// 在 Fragment 中使用 ViewBinding
override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View {
    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    return binding.root
}

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}
```

## 🔄 數據流架構

### 1. 設定數據流

```
User Input → Fragment → Repository → SharedPreferences
                ↓
            ViewModel ← Repository ← SharedPreferences
                ↓
            Fragment (UI Update)
```

### 2. 服務數據流

```
User Action → Fragment → ServiceManager → VibrationService
                ↓
            Service → TapDetectionService → TimeVibrationHelper
                ↓
            Vibration Output → User Feedback
```

### 3. 語言切換數據流

```
Language Selection → LocaleManager → Context Update
                        ↓
                    Application → All Activities/Fragments
                        ↓
                    UI Refresh with New Language
```

## 🛡️ 安全架構

### 1. 權限檢查流程

```kotlin
// 權限檢查和請求流程
fun startBackgroundService() {
    ServiceManager.startServiceWithPermissionCheck(
        fragment = this,
        launcher = permissionLauncher,
        onServiceStarted = { /* 服務啟動成功 */ },
        onPermissionDenied = { /* 權限被拒絕 */ }
    )
}
```

### 2. 安全機制架構

```kotlin
// SafetyManager 安全監控
fun startMonitoring() {
    updateHandler?.post(object : Runnable {
        override fun run() {
            updateSafetyStatus()
            updateHandler?.postDelayed(this, 1000)
        }
    })
}
```

## 📊 多語言架構

### 1. 語言資源結構

```
app/src/main/res/
├── values/strings.xml           # 預設語言 (英文)
├── values-zh-rTW/strings.xml    # 繁體中文
├── values-zh-rCN/strings.xml    # 簡體中文
├── values-ja/strings.xml        # 日文
└── values-es/strings.xml        # 西班牙文
```

### 2. 動態語言切換

```kotlin
// LocaleManager 語言切換
fun applyLanguageToApp(context: Context, languageCode: String): Context {
    val locale = when (languageCode) {
        "zh-TW" -> Locale("zh", "TW")
        "zh-CN" -> Locale("zh", "CN")
        "en" -> Locale("en", "US")
        "ja" -> Locale("ja", "JP")
        "es" -> Locale("es", "ES")
        else -> Locale("zh", "TW")
    }
    
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    return context.createConfigurationContext(config)
}
```

## 🎯 架構優勢

### 1. 可維護性
- **模組化設計**：每個組件職責明確
- **分層架構**：清晰的層次結構
- **代碼組織**：按功能分組的包結構

### 2. 可測試性
- **Repository 模式**：便於模擬數據源
- **依賴注入**：便於注入測試依賴
- **單一職責**：每個類職責單一，便於單元測試

### 3. 可擴展性
- **插件化設計**：新功能可獨立開發
- **配置驅動**：通過配置文件擴展功能
- **多語言支持**：易於添加新語言

### 4. 性能優化
- **異步處理**：使用 Coroutines 處理異步操作
- **資源管理**：及時釋放資源，避免記憶體洩漏
- **背景服務**：合理使用前景服務

## 🔧 技術棧

### 核心技術
- **語言**：Kotlin 1.9.0
- **架構**：MVVM + Repository Pattern
- **UI**：Material Design 3
- **導航**：Navigation Component
- **異步**：Coroutines + Flow
- **依賴注入**：手動依賴注入

### 第三方庫
- **Google Mobile Ads**：AdMob 廣告
- **Material Components**：Material Design 組件
- **WorkManager**：背景任務管理

### Android 版本支持
- **最低版本**：Android 7.0 (API 24)
- **目標版本**：Android 14 (API 34)
- **推薦版本**：Android 10+ (API 29+)

## 📈 架構演進

### 當前架構 (v1.3.0)
- ✅ MVVM 模式實現
- ✅ Repository 模式
- ✅ 手動依賴注入
- ✅ 多語言支持
- ✅ 無障礙設計

### 未來改進方向
- 🔄 引入 Hilt 依賴注入
- 🔄 實現 Room 資料庫
- 🔄 添加單元測試
- 🔄 實現 CI/CD 流程
- 🔄 添加性能監控

---

*文件創建時間：2024-12-19*  
*版本：1.0*  
*分析基於：Vibtime v1.3.0 完整程式碼*
