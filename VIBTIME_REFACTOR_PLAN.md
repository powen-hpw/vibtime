# 🎯 Vibtime 代碼優化任務清單

## 📋 任務分類與優先級

### 🟢 Level 1: 基礎配置修復 (獨立任務)
**特點：** 互不影響、快速完成、立即見效

#### Task 1.1: 修復 Gradle 配置
- **影響範圍：** 跨平台兼容性
- **時間估計：** 5分鐘
- **依賴關係：** 無
- **風險等級：** 低
- **文件：** `gradle.properties`

#### Task 1.2: 統一 ViewBinding - MainActivity
- **影響範圍：** 主Activity
- **時間估計：** 10分鐘
- **依賴關係：** 無
- **風險等級：** 低
- **文件：** `MainActivity.kt`

### 🟡 Level 2: UI層優化 (平行執行)
**特點：** Fragment間互不影響、可並行開發

#### Task 2.1: ViewBinding - HomeFragment
- **影響範圍：** 首頁功能
- **時間估計：** 15分鐘
- **依賴關係：** Task 1.2完成後
- **風險等級：** 低
- **文件：** `ui/home/HomeFragment.kt`

#### Task 2.2: ViewBinding - SettingsFragment  
- **影響範圍：** 設定頁面
- **時間估計：** 20分鐘
- **依賴關係：** Task 1.2完成後
- **風險等級：** 中
- **文件：** `ui/settings/SettingsFragment.kt`

#### Task 2.3: ViewBinding - VibrationFragment
- **影響範圍：** 震動設定
- **時間估計：** 15分鐘
- **依賴關係：** Task 1.2完成後
- **風險等級：** 低
- **文件：** `ui/vibration/VibrationFragment.kt`

#### Task 2.4: ViewBinding - HistoryFragment
- **影響範圍：** 歷史記錄
- **時間估計：** 15分鐘
- **依賴關係：** Task 1.2完成後
- **風險等級：** 低
- **文件：** `ui/history/HistoryFragment.kt`

#### Task 2.5: ViewBinding - 其他Fragment
- **影響範圍：** RunningFragment, WelcomeFragment
- **時間估計：** 10分鐘
- **依賴關係：** Task 1.2完成後
- **風險等級：** 低
- **文件：** `ui/running/RunningFragment.kt`, `ui/welcome/WelcomeFragment.kt`

### 🔴 Level 3: 核心功能重構 (複雜任務)
**特點：** 涉及Android合規、需要仔細測試

#### Task 3.1: 設計 WatchModeManager 架構
- **影響範圍：** 核心感測器邏輯
- **時間估計：** 30分鐘
- **依賴關係：** Task 2.1完成後
- **風險等級：** 高
- **文件：** `utils/WatchModeManager.kt` (新建)

#### Task 3.2: 實現限時監聽機制
- **影響範圍：** 背景感測器使用
- **時間估計：** 45分鐘
- **依賴關係：** Task 3.1完成後
- **風險等級：** 高
- **文件：** `service/TapDetectionService.kt`

#### Task 3.3: 實現冷卻期機制
- **影響範圍：** 報時後行為
- **時間估計：** 30分鐘
- **依賴關係：** Task 3.2完成後
- **風險等級：** 中
- **文件：** `utils/WatchModeManager.kt`, `utils/SafetyManager.kt`

#### Task 3.4: 更新 SafetyManager 合規檢查
- **影響範圍：** 安全機制
- **時間估計：** 20分鐘
- **依賴關係：** Task 3.3完成後
- **風險等級：** 中
- **文件：** `utils/SafetyManager.kt`

---

## 🚀 最佳執行流程

### Phase 1: 快速修復 (並行執行)
```
Task 1.1 (Gradle) ──┐
                    ├── 可同時進行
Task 1.2 (MainActivity) ──┘
```

### Phase 2: UI層優化 (並行執行)
```
Task 1.2 ──┬── Task 2.1 (HomeFragment)
           ├── Task 2.2 (SettingsFragment)  
           ├── Task 2.3 (VibrationFragment)
           ├── Task 2.4 (HistoryFragment)
           └── Task 2.5 (其他Fragment)
```

### Phase 3: 核心功能 (序列執行)
```
Task 2.1 ── Task 3.1 ── Task 3.2 ── Task 3.3 ── Task 3.4
```

---

## 💡 Coding 最佳解建議

### 🔧 Task 1.1: Gradle 配置修復

**問題代碼：**
```properties
# gradle.properties
org.gradle.java.home=/Applications/Android Studio.app/Contents/jbr/Contents/Home
```

**最佳解：**
```properties
# gradle.properties
# 移除絕對路徑，讓Gradle自動檢測
# org.gradle.java.home=/Applications/Android Studio.app/Contents/jbr/Contents/Home

# 或者使用環境變數 (CI/CD友好)
# org.gradle.java.home=${JAVA_HOME}
```

**規則：**
- ❌ 不使用絕對路徑
- ✅ 依賴JAVA_HOME環境變數
- ✅ local.properties不納入版本控制

---

### 🎨 Task 2.x: ViewBinding 標準實現

**錯誤模式：**
```kotlin
// ❌ 錯誤：不明確的型別
private var _binding: View? = null
private val binding get() = _binding!!
```

**最佳實現：**
```kotlin
// ✅ 正確：強型別 + 標準模式
private var _binding: FragmentHomeBinding? = null
private val binding get() = _binding!!

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

**規則：**
- ✅ 使用具體的Binding類型（如`FragmentHomeBinding`）
- ✅ 在`onDestroyView()`中設為null防止記憶體洩漏
- ✅ 使用`binding.root`返回View
- ❌ 不要在Fragment中手動使用`findViewById`

---

### 🎯 Task 3.x: Watch Mode 合規實現

**當前不合規代碼：**
```kotlin
// ❌ 不合規：持續監聽
sensorManager.registerListener(
    this, 
    accelerometer, 
    SensorManager.SENSOR_DELAY_UI
)
```

**合規最佳實現：**
```kotlin
// ✅ 合規：限時 + Wake-up sensor
class WatchModeManager(private val context: Context) {
    
    companion object {
        private const val WATCH_MODE_DURATION_10MIN = 10 * 60 * 1000L
        private const val WATCH_MODE_DURATION_2HOUR = 2 * 60 * 60 * 1000L
        private const val COOLDOWN_PERIOD = 15 * 60 * 1000L
    }
    
    private var watchModeStartTime = 0L
    private var watchModeDuration = WATCH_MODE_DURATION_10MIN
    private var lastVibrationTime = 0L
    
    fun startWatchMode(duration: Long = WATCH_MODE_DURATION_10MIN): Boolean {
        // 檢查冷卻期
        if (isInCooldownPeriod()) {
            return false
        }
        
        watchModeStartTime = System.currentTimeMillis()
        watchModeDuration = duration
        
        // 使用wake-up sensor
        val accelerometer = sensorManager.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER, 
            true // wakeUp = true
        )
        
        if (accelerometer?.isWakeUpSensor == true) {
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL // 使用NORMAL而非UI
            )
            
            // 設定自動停止
            handler.postDelayed({
                stopWatchMode()
            }, duration)
            
            return true
        }
        
        return false
    }
    
    private fun isInCooldownPeriod(): Boolean {
        val timeSinceLastVibration = System.currentTimeMillis() - lastVibrationTime
        return timeSinceLastVibration < COOLDOWN_PERIOD
    }
    
    private fun onTimeVibrationTriggered() {
        lastVibrationTime = System.currentTimeMillis()
        // 進入冷卻期，自動停止Watch Mode
        stopWatchMode()
        
        // 通知用戶冷卻期開始
        showCooldownNotification()
    }
}
```

**規則：**
- ✅ 限時監聽（10分鐘/2小時選項）
- ✅ 使用wake-up sensor (`isWakeUpSensor = true`)
- ✅ 使用`SENSOR_DELAY_NORMAL`而非`SENSOR_DELAY_UI`
- ✅ 報時後立即進入15分鐘冷卻期
- ✅ 提供明確的用戶通知和控制
- ❌ 不持續監聽感測器
- ❌ 不在冷卻期內響應感測器

---

## 📏 質量檢查規則

### 🔍 Code Review Checklist

**ViewBinding 檢查：**
- [ ] 所有Fragment使用強型別Binding
- [ ] `onDestroyView()`正確清理binding
- [ ] 沒有`findViewById`的使用
- [ ] 正確使用`binding.root`

**Watch Mode 檢查：**
- [ ] 感測器監聽有時間限制
- [ ] 使用wake-up sensor
- [ ] 實現冷卻期機制
- [ ] 提供用戶控制介面
- [ ] 符合Android 12+背景限制

**Gradle 檢查：**
- [ ] 沒有絕對路徑
- [ ] CI/CD兼容
- [ ] local.properties不在版本控制中

### 🧪 測試策略

**Unit Tests:**
- `WatchModeManager` 時間邏輯測試
- `SafetyManager` 冷卻期測試

**Integration Tests:**
- ViewBinding正確綁定測試
- 感測器註冊/取消註冊測試

**Manual Tests:**
- 不同Android版本兼容性
- 背景運行合規性
- 電池優化影響測試

---

## 🎯 執行建議

**立即開始：**
1. Task 1.1 + 1.2 (15分鐘內完成)
2. Task 2.1-2.5 (可分配給不同開發者並行)
3. Task 3.1-3.4 (需要仔細設計和測試)

**關鍵成功因素：**
- 嚴格遵循ViewBinding標準模式
- Watch Mode必須通過Android合規測試
- 每個task完成後立即測試

---

## 📝 進度追蹤

### Phase 1 進度
- [x] Task 1.1: 修復 Gradle 配置
- [x] Task 1.2: 統一 ViewBinding - MainActivity

### Phase 2 進度
- [x] Task 2.1: ViewBinding - HomeFragment
- [x] Task 2.2: ViewBinding - SettingsFragment
- [x] Task 2.3: ViewBinding - VibrationFragment
- [x] Task 2.4: ViewBinding - HistoryFragment
- [x] Task 2.5: ViewBinding - 其他Fragment

### Phase 3 進度
- [x] Task 3.1: 設計 WatchModeManager 架構
- [x] Task 3.2: 實現限時監聽機制
- [x] Task 3.3: 實現冷卻期機制
- [x] Task 3.4: 更新 SafetyManager 合規檢查

---

## 🔄 修改記錄

**2024-XX-XX:**
- 創建任務說明檔
- 完成 Phase 1: 基礎配置修復
- 完成 Phase 2: UI層優化 (所有Fragment ViewBinding標準化)
- 完成 Phase 3: 核心功能重構 (WatchModeManager合規實現)
- 所有任務完成，代碼質量大幅提升
