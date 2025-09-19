package com.example.vibtime

import android.app.Application
import android.content.Context
import com.example.vibtime.utils.LocaleManager

class VibtimeApplication : Application() {
    
    override fun attachBaseContext(base: Context) {
        // 獲取已保存的語言設定
        val savedLanguage = LocaleManager.getCurrentLanguage(base)
        android.util.Log.d("VibtimeApp", "attachBaseContext savedLanguage=$savedLanguage")
        // 應用語言設定到 Context
        val context = LocaleManager.applyLanguageToApp(base, savedLanguage)
        // 使用新的 Context 作為 base context
        super.attachBaseContext(context)
    }
    
    override fun onCreate() {
        super.onCreate()
        // 在 attachBaseContext 中已經設定語言，這裡不需要重複設定
        // 避免重複呼叫造成的潛在問題
        android.util.Log.d("VibtimeApp", "onCreate - language already set in attachBaseContext")
        
        // 初始化其他全局設定
        initializeAppSettings()
    }
    
    /**
     * 初始化應用程式設定
     */
    private fun initializeAppSettings() {
        // 這裡可以初始化其他全局設定，例如 Crash Reporting、Analytics 等
    }
}