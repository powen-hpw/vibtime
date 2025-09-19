# 📱 Vibtime - 震動報時應用

[![Android](https://img.shields.io/badge/Android-14+-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.3.0-orange.svg)](https://github.com/yourusername/vibtime/releases)

> 專為視障用戶設計的智能震動報時應用，讓您無需看螢幕就能知道時間

## 🌟 特色功能

### 🎯 核心功能
- **震動報時** - 輕敲手機兩次即可獲取當前時間
- **背景服務** - 螢幕關閉時仍可正常工作
- **智能感應** - 可調整的敲擊敏感度
- **安全機制** - 3小時自動停止，防止過度耗電

### 🌍 多語言支持
- 🇹🇼 繁體中文
- 🇨🇳 简体中文  
- 🇺🇸 English
- 🇯🇵 日本語
- 🇪🇸 Español

### ♿ 無障礙設計
- **專為視障用戶設計** - 完全無需視覺操作
- **語音提示** - 完整的無障礙支持
- **大按鈕設計** - 易於觸控操作
- **高對比度** - 支持深色模式

## 📱 截圖

| 主頁面 | 設定頁面 | 運行狀態 |
|--------|----------|----------|
| ![主頁面](screenshots/home.png) | ![設定頁面](screenshots/settings.png) | ![運行狀態](screenshots/running.png) |

## 🚀 快速開始

### 系統要求
- **Android 7.0+** (API 24+)
- **推薦 Android 10+** (API 29+)
- **最佳體驗 Android 14** (API 34)

### 安裝方法

#### 方法一：下載 APK
1. 前往 [Releases](https://github.com/yourusername/vibtime/releases) 頁面
2. 下載最新的 `app-release.apk`
3. 在手機上允許"未知來源"安裝
4. 點擊 APK 文件進行安裝

#### 方法二：從源碼構建
```bash
# 克隆項目
git clone https://github.com/yourusername/vibtime.git
cd vibtime

# 構建 APK
./gradlew assembleRelease

# APK 位置
app/build/outputs/apk/release/app-release.apk
```

## 📖 使用指南

### 基本操作
1. **啟動應用** - 點擊 Vibtime 圖標
2. **啟動服務** - 點擊"啟動服務"按鈕
3. **獲取時間** - 輕敲手機兩次
4. **停止服務** - 點擊"停止服務"按鈕

### 震動模式說明
- **長震動** = 小時 (12小時制)
- **短震動** = 分鐘 ÷ 5
- **範例**: 3:12 → 3次長震動 + 2次短震動

### 進階設定
- **敏感度調整** - 低/中/高三個等級
- **自動停止** - 3小時後自動停止服務
- **頻率限制** - 10分鐘內只能使用一次
- **測試模式** - 5分鐘限制，適合測試

## 🏗️ 技術架構

### 技術棧
- **語言**: Kotlin
- **架構**: MVVM + Repository Pattern
- **資料庫**: Room Database
- **UI**: Material Design 3
- **依賴注入**: 手動依賴注入
- **多語言**: 資料庫驅動的本地化系統

### 項目結構
```
app/src/main/java/com/example/vibtime/
├── data/                    # 資料層
│   ├── database/           # Room 資料庫
│   └── repository/         # 資料存儲庫
├── ui/                     # 用戶界面
│   ├── home/              # 主頁面
│   ├── settings/          # 設定頁面
│   ├── running/           # 運行狀態
│   └── ...
├── service/               # 背景服務
├── utils/                 # 工具類
└── ads/                   # 廣告管理
```

### 核心組件
- **VibrationService** - 前景服務，處理震動邏輯
- **TapDetectionService** - 敲擊檢測服務
- **SafetyManager** - 安全機制管理
- **LocaleManager** - 多語言管理
- **NotificationHelper** - 通知管理

## 🔧 開發指南

### 環境設置
```bash
# 安裝 Android Studio
# 設置 JAVA_HOME
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# 克隆並構建
git clone https://github.com/yourusername/vibtime.git
cd vibtime
./gradlew assembleDebug
```

### 代碼規範
- 遵循 [Vibtime 編碼規範](PROJECT_CODING_RULES.md)
- 使用繁體中文註釋
- 遵循 Android 14 合規性要求
- 優先考慮無障礙設計

### 測試
```bash
# 運行單元測試
./gradlew test

# 運行 UI 測試
./gradlew connectedAndroidTest

# 生成測試報告
./gradlew jacocoTestReport
```

## 📋 功能清單

### ✅ 已實現
- [x] 基本震動報時功能
- [x] 背景服務支持
- [x] 多語言支持 (5種語言)
- [x] 無障礙設計
- [x] 安全機制 (時間限制、冷卻期)
- [x] Android 14 合規性
- [x] 深色模式支持
- [x] 使用統計記錄

### 🚧 開發中
- [ ] 二進制時間報告
- [ ] 摩斯密碼時間報告
- [ ] 雲端同步設定
- [ ] 更多語言支持

### 💡 計劃中
- [ ] Apple Watch 支持
- [ ] 智能手錶支持
- [ ] 語音控制
- [ ] 手勢識別

## 🤝 貢獻指南

我們歡迎所有形式的貢獻！

### 如何貢獻
1. Fork 這個項目
2. 創建您的功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟一個 Pull Request

### 貢獻類型
- 🐛 Bug 修復
- ✨ 新功能
- 📚 文檔改進
- 🌍 翻譯
- 🎨 UI/UX 改進
- ⚡ 性能優化

## 📄 許可證

本項目採用 MIT 許可證 - 查看 [LICENSE](LICENSE) 文件了解詳情。

## 🙏 致謝

- **視障社群** - 提供寶貴的用戶反饋
- **Android 開發社群** - 技術支持和最佳實踐
- **開源貢獻者** - 各種開源庫的支持

## 📞 聯繫我們

- **項目維護者**: [您的名字](https://github.com/yourusername)
- **問題回報**: [Issues](https://github.com/yourusername/vibtime/issues)
- **功能建議**: [Discussions](https://github.com/yourusername/vibtime/discussions)
- **電子郵件**: your.email@example.com

## 📊 項目統計

![GitHub stars](https://img.shields.io/github/stars/yourusername/vibtime?style=social)
![GitHub forks](https://img.shields.io/github/forks/yourusername/vibtime?style=social)
![GitHub issues](https://img.shields.io/github/issues/yourusername/vibtime)
![GitHub pull requests](https://img.shields.io/github/issues-pr/yourusername/vibtime)

---

<div align="center">

**⭐ 如果這個項目對您有幫助，請給我們一個 Star！**

[下載 APK](https://github.com/yourusername/vibtime/releases) • [查看文檔](docs/) • [報告問題](https://github.com/yourusername/vibtime/issues) • [貢獻代碼](CONTRIBUTING.md)

</div>