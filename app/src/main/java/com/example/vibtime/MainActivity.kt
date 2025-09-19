package com.example.vibtime

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.vibtime.ads.AdManager
import com.example.vibtime.databinding.ActivityMainBinding
import com.example.vibtime.utils.LocaleManager
import com.example.vibtime.utils.ExactAlarmManager
import com.example.vibtime.data.database.VibtimeDatabase
import com.example.vibtime.data.repository.LocalizationRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * 主 Activity
 * 負責管理 6 個頁面的 Navigation
 */
class MainActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: android.content.Context) {
        val saved = LocaleManager.getCurrentLanguage(newBase)
        android.util.Log.d("MainActivity", "attachBaseContext savedLanguage=$saved")
        val wrapped = LocaleManager.applyLanguageToApp(newBase, saved)
        super.attachBaseContext(wrapped)
    }
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var localizationRepository: LocalizationRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // 在 attachBaseContext 中已經設定語言，這裡不需要重複設定
        android.util.Log.d("MainActivity", "onCreate - language already set in attachBaseContext")
        
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
        setupAds()
        checkAndroid14Compliance()
        initializeLocalization()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // 設定 Bottom Navigation 與 NavController 連結
        binding.bottomNavigation.setupWithNavController(navController)
    }
    
    /**
     * 設定廣告 - 業界最佳實踐實作
     */
    private fun setupAds() {
        // 初始化 AdMob
        AdManager.initialize(this) {
            // AdMob 初始化完成後載入 Banner 廣告
            loadBannerAd()
        }
        
        // 設定評價按鈕點擊事件
        binding.btnRateApp.setOnClickListener {
            openAppStoreForRating()
        }
    }
    
    /**
     * 載入 Banner 廣告
     */
    private fun loadBannerAd() {
        AdManager.loadBannerAd(
            context = this,
            adView = binding.bannerAdView,
            adContainer = binding.adContainer,
            fallbackContent = binding.adFallbackContent,
            onAdLoaded = {
                // 廣告載入成功
                android.util.Log.d("MainActivity", "Banner ad loaded successfully")
            },
            onAdFailed = { error ->
                // 廣告載入失敗，使用業界最佳實踐處理
                android.util.Log.w("MainActivity", "Banner ad failed: $error")
                AdManager.handleAdFailure(
                    strategy = AdManager.FallbackStrategy.SHOW_RATING_PROMPT,
                    adContainer = binding.adContainer,
                    fallbackContent = binding.adFallbackContent,
                    context = this
                )
            }
        )
    }
    
    /**
     * 開啟應用商店進行評價
     */
    private fun openAppStoreForRating() {
        try {
            // 嘗試開啟 Google Play Store
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("market://details?id=${packageName}")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NO_HISTORY or
                        android.content.Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // 如果沒有安裝 Google Play Store，開啟瀏覽器
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")
                }
                startActivity(intent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    this,
                    getString(R.string.cannot_open_app_store),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    /**
     * 當應用程式進入背景時觸發插頁廣告
     */
    override fun onPause() {
        super.onPause()
        // 根據 cursor rules 的邏輯顯示插頁廣告
        AdManager.showInterstitialAdIfEligible(this)
    }
    
    /**
     * 處理配置變更
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 應用已保存的語言設定
        val savedLanguage = LocaleManager.getCurrentLanguage(this)
        LocaleManager.applyLanguageToApp(this, savedLanguage)
    }
    
    /**
     * 初始化本地化系統
     */
    private fun initializeLocalization() {
        val database = VibtimeDatabase.getDatabase(this)
        localizationRepository = LocalizationRepository(database)
        
        // 初始化資料庫
        lifecycleScope.launch(Dispatchers.IO) {
            localizationRepository.initialize()
            
            // 載入當前語言的本地化字串
            val currentLanguage = LocaleManager.getCurrentLanguage(this@MainActivity)
            localizationRepository.setCurrentLanguage(currentLanguage)
        }
    }
    
    /**
     * 檢查 Android 14 合規性
     */
    private fun checkAndroid14Compliance() {
        // 檢查精確鬧鐘權限
        ExactAlarmManager.checkAndRequestPermission(
            activity = this,
            onPermissionGranted = {
                android.util.Log.d("MainActivity", "Exact alarm permission granted")
            },
            onPermissionDenied = {
                android.util.Log.w("MainActivity", "Exact alarm permission denied")
            }
        )
    }
    
    /**
     * 清理資源
     */
    override fun onDestroy() {
        super.onDestroy()
        AdManager.cleanup()
    }
}
