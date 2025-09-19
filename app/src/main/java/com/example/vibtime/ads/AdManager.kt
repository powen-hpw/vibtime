package com.example.vibtime.ads

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * AdManager - 廣告管理器
 * 統一管理所有廣告邏輯，實作業界最佳實踐
 */
object AdManager {
    
    private const val TAG = "AdManager"
    
    // 廣告狀態
    private var isAdMobInitialized = false
    private var interstitialAd: InterstitialAd? = null
    private var usageCount = 0
    
    /**
     * 初始化 AdMob
     */
    fun initialize(context: Context, onInitialized: () -> Unit = {}) {
        if (isAdMobInitialized) {
            onInitialized()
            return
        }
        
        MobileAds.initialize(context) { initializationStatus ->
            isAdMobInitialized = true
            Log.d(TAG, "AdMob initialized: ${initializationStatus.adapterStatusMap}")
            onInitialized()
        }
    }
    
    /**
     * 載入 Banner 廣告 - 業界最佳實踐實作
     */
    fun loadBannerAd(
        context: Context,
        adView: AdView,
        adContainer: FrameLayout,
        fallbackContent: View,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null
    ) {
        if (!isAdMobInitialized) {
            Log.w(TAG, "AdMob not initialized, showing fallback content")
            showFallbackContent(adContainer, fallbackContent)
            onAdFailed?.invoke("AdMob not initialized")
            return
        }
        
        val adRequest = AdRequest.Builder().build()
        
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Banner ad loaded successfully")
                showBannerAd(adContainer, adView, fallbackContent)
                onAdLoaded?.invoke()
            }
            
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.w(TAG, "Banner ad failed to load: ${loadAdError.message}")
                showFallbackContent(adContainer, fallbackContent)
                onAdFailed?.invoke(loadAdError.message)
            }
            
            override fun onAdClicked() {
                Log.d(TAG, "Banner ad clicked")
            }
            
            override fun onAdClosed() {
                Log.d(TAG, "Banner ad closed")
            }
            
            override fun onAdOpened() {
                Log.d(TAG, "Banner ad opened")
            }
        }
        
        // 載入廣告
        adView.loadAd(adRequest)
    }
    
    /**
     * 顯示 Banner 廣告
     */
    private fun showBannerAd(adContainer: FrameLayout, adView: AdView, fallbackContent: View) {
        adView.visibility = View.VISIBLE
        fallbackContent.visibility = View.GONE
        adContainer.visibility = View.VISIBLE
    }
    
    /**
     * 顯示備用內容 - 業界最佳實踐
     */
    private fun showFallbackContent(adContainer: FrameLayout, fallbackContent: View) {
        fallbackContent.visibility = View.VISIBLE
        adContainer.visibility = View.VISIBLE
        
        // 或者完全隱藏廣告區域 (另一種選擇)
        // adContainer.visibility = View.GONE
    }
    
    /**
     * 隱藏廣告區域 - 當不需要顯示任何廣告相關內容時
     */
    fun hideAdContainer(adContainer: FrameLayout) {
        adContainer.visibility = View.GONE
    }
    
    /**
     * 載入插頁廣告
     */
    fun loadInterstitialAd(context: Context, adUnitId: String) {
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d(TAG, "Interstitial ad loaded")
                interstitialAd = ad
            }
            
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                interstitialAd = null
            }
        })
    }
    
    /**
     * 顯示插頁廣告 - 根據 cursor rules 的使用次數邏輯
     */
    fun showInterstitialAdIfEligible(context: Context) {
        usageCount++
        
        if (shouldShowInterstitialAd()) {
            interstitialAd?.let { ad ->
                ad.show(context as android.app.Activity)
                Log.d(TAG, "Interstitial ad shown")
                // 重置使用次數
                usageCount = 0
                // 重新載入下一個廣告
                loadInterstitialAd(context, "your_interstitial_ad_unit_id")
            } ?: run {
                Log.w(TAG, "Interstitial ad not ready")
            }
        }
    }
    
    /**
     * 根據 cursor rules 判斷是否應該顯示插頁廣告
     * 使用次數達到 3-5 次時，30% 機率顯示
     */
    private fun shouldShowInterstitialAd(): Boolean {
        return usageCount in 3..5 && Math.random() < 0.3
    }
    
    /**
     * 處理廣告載入失敗的不同策略
     */
    enum class FallbackStrategy {
        SHOW_RATING_PROMPT,     // 顯示評價提示 (推薦)
        HIDE_COMPLETELY,        // 完全隱藏
        SHOW_BRAND_MESSAGE,     // 顯示品牌訊息
        SHOW_PLACEHOLDER        // 顯示佔位符
    }
    
    /**
     * 根據策略處理廣告載入失敗
     */
    fun handleAdFailure(
        strategy: FallbackStrategy,
        adContainer: FrameLayout,
        fallbackContent: View,
        context: Context
    ) {
        when (strategy) {
            FallbackStrategy.SHOW_RATING_PROMPT -> {
                showFallbackContent(adContainer, fallbackContent)
            }
            FallbackStrategy.HIDE_COMPLETELY -> {
                hideAdContainer(adContainer)
            }
            FallbackStrategy.SHOW_BRAND_MESSAGE -> {
                // 可以顯示應用品牌訊息
                showFallbackContent(adContainer, fallbackContent)
            }
            FallbackStrategy.SHOW_PLACEHOLDER -> {
                // 顯示佔位符內容
                showFallbackContent(adContainer, fallbackContent)
            }
        }
    }
    
    /**
     * 清理廣告資源
     */
    fun cleanup() {
        interstitialAd = null
    }
    
    /**
     * 檢查廣告可用性
     */
    fun isAdAvailable(): Boolean {
        return isAdMobInitialized && interstitialAd != null
    }
    
    /**
     * 獲取當前使用次數 (用於測試)
     */
    fun getCurrentUsageCount(): Int = usageCount
    
    /**
     * 重置使用次數 (用於測試)
     */
    fun resetUsageCount() {
        usageCount = 0
    }
}
