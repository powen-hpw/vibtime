# 🎨 UI 修改最佳實踐指南

## 📋 概述

本指南提供 Vibtime App 的 UI 修改最佳實踐，包括分層修改、響應式設計、深色模式支持等。

## 🏗️ 分層修改架構

### 1. 資源分層結構

```
app/src/main/res/
├── values/           # 預設資源
│   ├── colors.xml    # 顏色定義
│   ├── strings.xml   # 字串資源
│   ├── dimens.xml    # 尺寸定義
│   └── themes.xml    # 主題定義
├── values-night/     # 深色模式資源
│   ├── colors.xml    # 深色模式顏色
│   └── themes.xml    # 深色模式主題
├── values-zh-rTW/    # 繁體中文資源
├── values-zh-rCN/    # 簡體中文資源
├── values-ja/        # 日文資源
├── values-es/        # 西班牙文資源
├── drawable/         # 圖標和背景
├── layout/           # 佈局文件
└── mipmap/           # 應用圖標
```

### 2. 顏色層修改

**定義顏色：**
```xml
<!-- values/colors.xml -->
<resources>
    <!-- Primary Colors -->
    <color name="primary_color">#2196F3</color>
    <color name="primary_dark">#1976D2</color>
    <color name="primary_light">#BBDEFB</color>
    
    <!-- Accent Colors -->
    <color name="accent_color">#FF4081</color>
    <color name="accent_dark">#F50057</color>
    
    <!-- Background Colors -->
    <color name="background_light">#FFFFFF</color>
    <color name="surface_light">#F5F5F5</color>
    
    <!-- Text Colors -->
    <color name="text_primary_light">#212121</color>
    <color name="text_secondary_light">#757575</color>
    
    <!-- Status Colors -->
    <color name="success_color">#4CAF50</color>
    <color name="warning_color">#FF9800</color>
    <color name="error_color">#F44336</color>
</resources>
```

**深色模式顏色：**
```xml
<!-- values-night/colors.xml -->
<resources>
    <!-- Primary Colors (保持不變) -->
    <color name="primary_color">#2196F3</color>
    <color name="primary_dark">#1976D2</color>
    <color name="primary_light">#BBDEFB</color>
    
    <!-- Background Colors -->
    <color name="background_light">#121212</color>
    <color name="surface_light">#1E1E1E</color>
    
    <!-- Text Colors -->
    <color name="text_primary_light">#FFFFFF</color>
    <color name="text_secondary_light">#B3FFFFFF</color>
</resources>
```

### 3. 主題層修改

**淺色主題：**
```xml
<!-- values/themes.xml -->
<style name="Theme.Vibtime" parent="Theme.Material3.DayNight">
    <!-- Primary Colors -->
    <item name="colorPrimary">@color/primary_color</item>
    <item name="colorPrimaryVariant">@color/primary_dark</item>
    <item name="colorOnPrimary">@color/white</item>
    
    <!-- Secondary Colors -->
    <item name="colorSecondary">@color/accent_color</item>
    <item name="colorSecondaryVariant">@color/accent_dark</item>
    <item name="colorOnSecondary">@color/white</item>
    
    <!-- Background Colors -->
    <item name="android:colorBackground">@color/background_light</item>
    <item name="colorSurface">@color/surface_light</item>
    <item name="colorOnBackground">@color/text_primary_light</item>
    <item name="colorOnSurface">@color/text_primary_light</item>
    
    <!-- Status Bar -->
    <item name="android:statusBarColor">@color/primary_dark</item>
    <item name="android:windowLightStatusBar">false</item>
</style>
```

**深色主題：**
```xml
<!-- values-night/themes.xml -->
<style name="Theme.Vibtime" parent="Theme.Material3.DayNight">
    <!-- Primary Colors (保持不變) -->
    <item name="colorPrimary">@color/primary_color</item>
    <item name="colorPrimaryVariant">@color/primary_light</item>
    <item name="colorOnPrimary">@color/black</item>
    
    <!-- Background Colors -->
    <item name="android:colorBackground">@color/background_light</item>
    <item name="colorSurface">@color/surface_light</item>
    <item name="colorOnBackground">@color/text_primary_light</item>
    <item name="colorOnSurface">@color/text_primary_light</item>
    
    <!-- Status Bar -->
    <item name="android:statusBarColor">@color/black</item>
    <item name="android:windowLightStatusBar">false</item>
</style>
```

### 4. 字串層修改

**多語言字串：**
```xml
<!-- values/strings.xml -->
<string name="app_name">Vibtime</string>
<string name="welcome_title">Welcome to Vibtime</string>
<string name="btn_start_service">Start Service</string>

<!-- values-zh-rTW/strings.xml -->
<string name="app_name">Vibtime</string>
<string name="welcome_title">歡迎使用 Vibtime</string>
<string name="btn_start_service">啟動服務</string>

<!-- values-zh-rCN/strings.xml -->
<string name="app_name">Vibtime</string>
<string name="welcome_title">欢迎使用 Vibtime</string>
<string name="btn_start_service">启动服务</string>
```

### 5. 尺寸層修改

**定義尺寸：**
```xml
<!-- values/dimens.xml -->
<resources>
    <!-- Spacing -->
    <dimen name="margin_small">8dp</dimen>
    <dimen name="margin_medium">16dp</dimen>
    <dimen name="margin_large">24dp</dimen>
    <dimen name="margin_xlarge">32dp</dimen>
    
    <!-- Text Sizes -->
    <dimen name="text_size_small">12sp</dimen>
    <dimen name="text_size_medium">14sp</dimen>
    <dimen name="text_size_large">16sp</dimen>
    <dimen name="text_size_xlarge">18sp</dimen>
    <dimen name="text_size_title">20sp</dimen>
    
    <!-- Component Sizes -->
    <dimen name="button_height">48dp</dimen>
    <dimen name="button_height_small">36dp</dimen>
    <dimen name="icon_size">24dp</dimen>
    <dimen name="icon_size_large">32dp</dimen>
    
    <!-- Corner Radius -->
    <dimen name="corner_radius_small">4dp</dimen>
    <dimen name="corner_radius_medium">8dp</dimen>
    <dimen name="corner_radius_large">12dp</dimen>
</resources>
```

## 📱 響應式設計

### 1. ConstraintLayout 最佳實踐

**基本約束：**
```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- 按鈕居中 -->
    <Button
        android:id="@+id/btn_center"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/btn_start_service"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent" />
    
    <!-- 按鈕水平排列 -->
    <Button
        android:id="@+id/btn_left"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginEnd="@dimen/margin_small"
        android:text="@string/btn_cancel"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toStartOf="@id/btn_right"
        app:layout_constraintTop_toTopOf="@id/btn_right" />
    
    <Button
        android:id="@+id/btn_right"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="@dimen/margin_small"
        android:text="@string/btn_ok"
        app:layout_constraintStart_toEndOf="@id/btn_left"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toBottomOf="@id/btn_center"
        app:layout_constraintTop_margin="@dimen/margin_medium" />
        
</androidx.constraintlayout.widget.ConstraintLayout>
```

### 2. 支援不同螢幕尺寸

**使用 dp 和 sp 單位：**
```xml
<!-- 正確：使用 dp 和 sp -->
<Button
    android:layout_width="match_parent"
    android:layout_height="@dimen/button_height"
    android:layout_margin="@dimen/margin_medium"
    android:textSize="@dimen/text_size_medium"
    android:text="@string/btn_start_service" />

<!-- 錯誤：使用 px -->
<Button
    android:layout_width="match_parent"
    android:layout_height="48px"
    android:layout_margin="16px"
    android:textSize="14px" />
```

**使用 match_parent 和 wrap_content：**
```xml
<!-- 正確：適當使用 match_parent 和 wrap_content -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">
    
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/welcome_title" />
        
    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/btn_start_service" />
        
</LinearLayout>
```

### 3. 支援不同方向

**橫向佈局：**
```xml
<!-- layout-land/fragment_home.xml -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="horizontal">
    
    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:orientation="vertical">
        <!-- 左側內容 -->
    </LinearLayout>
    
    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:orientation="vertical">
        <!-- 右側內容 -->
    </LinearLayout>
    
</LinearLayout>
```

## 🌙 深色模式支持

### 1. 自動深色模式切換

**在 Application 中啟用：**
```kotlin
// Application.kt
class VibtimeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 啟用深色模式支持
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
    }
}
```

### 2. 深色模式資源

**顏色資源：**
```xml
<!-- values/colors.xml -->
<color name="background_light">#FFFFFF</color>
<color name="text_primary_light">#212121</color>

<!-- values-night/colors.xml -->
<color name="background_light">#121212</color>
<color name="text_primary_light">#FFFFFF</color>
```

**主題資源：**
```xml
<!-- values/themes.xml -->
<style name="Theme.Vibtime" parent="Theme.Material3.DayNight">
    <item name="android:colorBackground">@color/background_light</item>
    <item name="colorOnBackground">@color/text_primary_light</item>
</style>
```

### 3. 深色模式測試

**測試步驟：**
1. 在 Android Studio 中運行 App
2. 在設備設定中切換深色模式
3. 檢查 App 是否正確切換
4. 使用 Preview 面板測試不同主題

## 🎯 無障礙功能

### 1. 內容描述

**添加 contentDescription：**
```xml
<Button
    android:id="@+id/btn_start_service"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/btn_start_service"
    android:contentDescription="@string/btn_start_service_description" />
```

**字串資源：**
```xml
<string name="btn_start_service">Start Service</string>
<string name="btn_start_service_description">Start vibration service to detect taps and provide time vibration</string>
```

### 2. 觸控目標大小

**確保觸控目標足夠大：**
```xml
<Button
    android:layout_width="match_parent"
    android:layout_height="@dimen/button_height"
    android:minHeight="@dimen/button_height"
    android:text="@string/btn_start_service" />
```

### 3. 字體縮放支持

**使用 sp 單位：**
```xml
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textSize="@dimen/text_size_medium"
    android:text="@string/welcome_title" />
```

## 🚀 性能優化

### 1. 減少佈局層級

**使用 ConstraintLayout：**
```xml
<!-- 好的做法：使用 ConstraintLayout -->
<androidx.constraintlayout.widget.ConstraintLayout>
    <Button
        android:id="@+id/btn1"
        app:layout_constraintTop_toTopOf="parent" />
    <Button
        android:id="@+id/btn2"
        app:layout_constraintTop_toBottomOf="@id/btn1" />
</androidx.constraintlayout.widget.ConstraintLayout>

<!-- 避免：過度嵌套 -->
<LinearLayout>
    <LinearLayout>
        <LinearLayout>
            <Button />
        </LinearLayout>
    </LinearLayout>
</LinearLayout>
```

### 2. 重用佈局

**使用 include 標籤：**
```xml
<!-- common_button.xml -->
<Button
    android:layout_width="match_parent"
    android:layout_height="@dimen/button_height"
    android:background="@drawable/button_primary"
    android:textColor="@color/white" />

<!-- 在其他佈局中重用 -->
<include layout="@layout/common_button" />
```

### 3. 延遲載入

**使用 ViewStub：**
```xml
<ViewStub
    android:id="@+id/stub_advanced_settings"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout="@layout/advanced_settings" />
```

## 📝 最佳實踐總結

### ✅ 應該做的

1. **使用資源文件**：將所有硬編碼的值移到資源文件中
2. **支援多語言**：為所有字串提供多語言版本
3. **響應式設計**：使用 ConstraintLayout 和適當的單位
4. **深色模式**：提供完整的深色模式支持
5. **無障礙功能**：添加內容描述和適當的觸控目標
6. **性能優化**：減少佈局層級，重用組件

### ❌ 不應該做的

1. **硬編碼值**：不要在佈局中硬編碼顏色、尺寸、字串
2. **忽略深色模式**：不要只提供淺色模式
3. **過度嵌套**：避免不必要的佈局嵌套
4. **忽略無障礙**：不要忘記添加內容描述
5. **使用 px 單位**：不要使用 px 單位設定尺寸
6. **忽略多語言**：不要只提供英文版本

---

*最後更新：2024年12月*
