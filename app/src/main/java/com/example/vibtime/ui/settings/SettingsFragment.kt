package com.example.vibtime.ui.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.materialswitch.MaterialSwitch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.vibtime.R
import com.example.vibtime.databinding.FragmentSettingsBinding
import com.example.vibtime.utils.LocaleManager
import com.example.vibtime.utils.QCValidationHelper
import com.example.vibtime.utils.PremiumManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Settings Fragment - 設定頁面
 * 用戶個人化設定和 App 管理
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    
    // UI Elements
    private lateinit var languageCard: MaterialCardView
    private lateinit var languageText: TextView
    private lateinit var darkModeSwitch: MaterialSwitch
    private lateinit var vibrationDurationCard: MaterialCardView
    private lateinit var vibrationDurationText: TextView
    private lateinit var notificationsSwitch: MaterialSwitch
    private lateinit var vibrationInstructionsCard: MaterialCardView
    private lateinit var batteryOptimizationCard: MaterialCardView
    private lateinit var aboutCard: MaterialCardView
    private lateinit var resetCard: MaterialCardView
    
    // Safety Settings UI Elements
    private lateinit var autoStopSwitch: MaterialSwitch
    private lateinit var frequencyLimitSwitch: MaterialSwitch
    private lateinit var testModeSwitch: MaterialSwitch
    
    // Premium Features UI Elements
    private lateinit var binaryTimeCard: MaterialCardView
    private lateinit var binaryPriceText: TextView
    private lateinit var binaryStatusText: TextView
    private lateinit var morseTimeCard: MaterialCardView
    private lateinit var morsePriceText: TextView
    private lateinit var morseStatusText: TextView
    
    // 設定值
    private var currentLanguage = "zh-TW"
    private var currentVibrationDuration = 500L
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 執行 QC 檢查
        val qcIssues = QCValidationHelper.performFullQCCheck(this)
        if (qcIssues.isNotEmpty()) {
            val report = QCValidationHelper.generateQCReport(qcIssues)
            android.util.Log.d("QC_REPORT", report)
        }
        
        initializeViews(view)
        setupSharedPreferences()
        PremiumManager.initialize(requireContext())
        loadSettings()
        setupClickListeners()
    }
    
    /**
     * 初始化視圖
     */
    private fun initializeViews(view: View) {
        languageCard = binding.languageCard
        languageText = binding.languageText
        darkModeSwitch = binding.darkModeSwitch
        vibrationDurationCard = binding.vibrationDurationCard
        vibrationDurationText = binding.vibrationDurationText
        notificationsSwitch = binding.notificationsSwitch
        vibrationInstructionsCard = binding.vibrationInstructionsCard
        batteryOptimizationCard = binding.batteryOptimizationCard
        aboutCard = binding.aboutCard
        resetCard = binding.resetCard
        
        // Safety Settings
        autoStopSwitch = binding.autoStopSwitch
        frequencyLimitSwitch = binding.frequencyLimitSwitch
        testModeSwitch = binding.testModeSwitch
        
        // Premium Features
        binaryTimeCard = binding.binaryTimeCard
        binaryPriceText = binding.binaryPriceText
        binaryStatusText = binding.binaryStatusText
        morseTimeCard = binding.morseTimeCard
        morsePriceText = binding.morsePriceText
        morseStatusText = binding.morseStatusText
    }
    
    /**
     * 設定 SharedPreferences
     */
    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
    }
    
    /**
     * 載入設定
     */
    private fun loadSettings() {
        // 語言設定 - 使用 LocaleManager
        currentLanguage = LocaleManager.getCurrentLanguage(requireContext())
        updateLanguageText()
        
        // 深色模式
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        darkModeSwitch.isChecked = isDarkMode
        
        // 震動時長
        currentVibrationDuration = sharedPreferences.getLong("vibration_duration", 500L)
        updateVibrationDurationText()
        
        // 通知設定
        val notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
        notificationsSwitch.isChecked = notificationsEnabled
        
        // 安全設定
        val autoStopEnabled = sharedPreferences.getBoolean("auto_stop_enabled", true)
        autoStopSwitch.isChecked = autoStopEnabled
        
        val frequencyLimitEnabled = sharedPreferences.getBoolean("frequency_limit_enabled", true)
        frequencyLimitSwitch.isChecked = frequencyLimitEnabled
        
        val testModeEnabled = sharedPreferences.getBoolean("test_mode_enabled", false)
        testModeSwitch.isChecked = testModeEnabled
        
        // 載入進階功能狀態
        updatePremiumFeaturesStatus()
    }
    
    /**
     * 設定點擊監聽器
     */
    private fun setupClickListeners() {
        // 語言選擇
        languageCard.setOnClickListener {
            showLanguageDialog()
        }
        
        // 深色模式
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleDarkMode(isChecked)
        }
        
        // 震動時長
        vibrationDurationCard.setOnClickListener {
            showVibrationDurationDialog()
        }
        
        // 通知設定
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleNotifications(isChecked)
        }
        
        // 電池優化
        batteryOptimizationCard.setOnClickListener {
            showBatteryOptimizationInfo()
        }
        
        // 關於
        aboutCard.setOnClickListener {
            showAboutDialog()
        }
        
        // 重置設定
        resetCard.setOnClickListener {
            showResetDialog()
        }
        
        // 安全設定
        autoStopSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleAutoStop(isChecked)
        }
        
        frequencyLimitSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleFrequencyLimit(isChecked)
        }
        
        testModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleTestMode(isChecked)
        }
        
        // 進階功能購買
        binaryTimeCard.setOnClickListener {
            handlePremiumPurchase(PremiumManager.FEATURE_BINARY)
        }
        
        morseTimeCard.setOnClickListener {
            handlePremiumPurchase(PremiumManager.FEATURE_MORSE)
        }
    }
    
    /**
     * 顯示語言選擇對話框
     */
    private fun showLanguageDialog() {
        val languages = arrayOf(
            getString(R.string.language_system),
            getString(R.string.language_zh_tw),
            getString(R.string.language_zh_cn),
            getString(R.string.language_en),
            getString(R.string.language_ja)
        )
        val languageCodes = arrayOf("system", "zh-TW", "zh-CN", "en", "ja")
        
        val currentIndex = languageCodes.indexOf(currentLanguage)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.language_dialog_title)
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val newLanguage = languageCodes[which]
                if (newLanguage != currentLanguage) {
                    // 驗證語言選項是否有效
                    if (!LocaleManager.isValidLanguageOption(newLanguage)) {
                        android.util.Log.e("SettingsFragment", "Invalid language option: $newLanguage")
                        return@setSingleChoiceItems
                    }
                    
                    // 使用者選擇語言：保存使用者選擇（可為 system）
                    LocaleManager.saveLanguage(requireContext(), newLanguage)
                    currentLanguage = newLanguage
                    android.util.Log.d("SettingsFragment", "Language selected: $newLanguage")
                    
                    // 立即在當前 Context 套用，供對話框之後的 UI 更新使用
                    LocaleManager.setLocale(requireContext(), newLanguage)
                    
                    // 更新文本
                    updateLanguageText()
                    
                    // 顯示重啟提示 (完全重啟以確保所有頁面都使用新語言)
                    showRestartDialog()
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    /**
     * 顯示重啟對話框
     */
    private fun showRestartDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.language_changed_title)
            .setMessage(R.string.language_changed_message)
            .setPositiveButton(R.string.restart_now) { _, _ ->
                restartApp()
            }
            .setNegativeButton(R.string.restart_later, null)
            .show()
    }
    
    /**
     * 重新啟動應用程式
     */
    private fun restartApp() {
        try {
            val intent = requireActivity().packageManager.getLaunchIntentForPackage(requireActivity().packageName)
            if (intent != null) {
                // 完全重啟應用程式，清除所有活動
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or 
                               Intent.FLAG_ACTIVITY_CLEAR_TASK or
                               Intent.FLAG_ACTIVITY_CLEAR_TOP)
                
                // 添加額外標記以確保重新創建活動
                intent.putExtra("restart_trigger", System.currentTimeMillis())
                
                // 啟動新的實例
                startActivity(intent)
                
                // 結束當前活動
                Runtime.getRuntime().exit(0)
            } else {
                // 如果無法獲取啟動 Intent，顯示錯誤訊息
                showToast(getString(R.string.restart_error))
            }
        } catch (e: Exception) {
            showToast(getString(R.string.restart_error))
        }
    }
    
    /**
     * 切換深色模式
     */
    private fun toggleDarkMode(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean("dark_mode", enabled)
            .apply()
        
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        
        showToast(if (enabled) getString(R.string.toast_dark_mode_enabled) else getString(R.string.toast_dark_mode_disabled))
    }
    
    /**
     * 顯示震動時長選擇對話框
     */
    private fun showVibrationDurationDialog() {
        val durations = arrayOf(getString(R.string.vibration_duration_short), getString(R.string.vibration_duration_medium), getString(R.string.vibration_duration_long), getString(R.string.vibration_duration_extra_long))
        val durationValues = arrayOf(300L, 500L, 800L, 1200L)
        
        val currentIndex = durationValues.indexOf(currentVibrationDuration)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.vibration_duration_dialog_title))
            .setSingleChoiceItems(durations, currentIndex) { dialog, which ->
                currentVibrationDuration = durationValues[which]
                sharedPreferences.edit()
                    .putLong("vibration_duration", currentVibrationDuration)
                    .apply()
                updateVibrationDurationText()
                showToast(getString(R.string.toast_vibration_duration_updated))
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }
    
    /**
     * 切換通知設定
     */
    private fun toggleNotifications(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean("notifications_enabled", enabled)
            .apply()
        
        showToast(if (enabled) getString(R.string.toast_notifications_enabled) else getString(R.string.toast_notifications_disabled))
    }
    
    /**
     * 顯示電池優化資訊
     */
    private fun showBatteryOptimizationInfo() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_battery_optimization_title))
            .setMessage("""
                為確保背景服務穩定運行，建議：
                
                📱 設定步驟：
                1. 進入手機設定
                2. 電池 → 電池優化
                3. 找到 Vibtime
                4. 選擇「不優化」
                
                🏃‍♂️ 也可以設定：
                • 自啟動管理：允許
                • 後台活動：允許
                • 省電模式：白名單
                
                這樣就能確保隨時敲擊都有回應！
            """.trimIndent())
            .setPositiveButton(getString(R.string.dialog_understand)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * 顯示關於對話框
     */
    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_about_title))
            .setMessage("""
                🎯 Vibtime 震動報時 v1.1
                
                💡 核心功能：
                • 敲擊手機2次獲得震動報時
                • 支援螢幕關閉時使用
                • 可調節敲擊敏感度
                • 專業背景服務管理
                • 多語言支援
                • 智能廣告管理
                
                🎨 設計理念：
                在安靜環境或黑暗中，不用看螢幕就能知道時間
                
                🔧 開發資訊：
                • 原生 Android 開發
                • Material Design 3
                • 遵循 Android 最佳實踐
                
                📧 意見回饋：
                如有建議或問題，歡迎聯繫開發者
            """.trimIndent())
            .setPositiveButton(getString(R.string.dialog_ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * 顯示重置確認對話框
     */
    private fun showResetDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.reset_dialog_title))
            .setMessage(getString(R.string.reset_dialog_message))
            .setPositiveButton(getString(R.string.reset_confirm)) { _, _ ->
                resetAllSettings()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }
    
    /**
     * 重置所有設定
     */
    private fun resetAllSettings() {
        sharedPreferences.edit().clear().apply()
        
        // 重新載入預設設定
        loadSettings()
        
        showToast(getString(R.string.toast_settings_reset))
    }
    
    /**
     * 更新語言顯示文字
     */
    private fun updateLanguageText() {
        languageText.text = LocaleManager.getLanguageDisplayName(requireContext(), currentLanguage)
    }
    
    /**
     * 更新震動時長顯示文字
     */
    private fun updateVibrationDurationText() {
        val durationMap = mapOf(
            300L to getString(R.string.vibration_duration_short),
            500L to getString(R.string.vibration_duration_medium),
            800L to getString(R.string.vibration_duration_long),
            1200L to getString(R.string.vibration_duration_extra_long)
        )
        vibrationDurationText.text = durationMap[currentVibrationDuration] ?: getString(R.string.vibration_duration_medium)
    }
    
    /**
     * 顯示提示訊息
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * 切換自動停止設定
     */
    private fun toggleAutoStop(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("auto_stop_enabled", isEnabled).apply()
        showToast(if (isEnabled) getString(R.string.toast_auto_stop_enabled) else getString(R.string.toast_auto_stop_disabled))
    }
    
    /**
     * 切換頻率限制設定
     */
    private fun toggleFrequencyLimit(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("frequency_limit_enabled", isEnabled).apply()
        showToast(if (isEnabled) getString(R.string.toast_frequency_limit_enabled) else getString(R.string.toast_frequency_limit_disabled))
    }
    
    /**
     * 切換測試模式設定
     */
    private fun toggleTestMode(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("test_mode_enabled", isEnabled).apply()
        showToast(if (isEnabled) getString(R.string.toast_test_mode_enabled) else getString(R.string.toast_test_mode_disabled))
    }
    
    /**
     * 更新進階功能狀態顯示
     */
    private fun updatePremiumFeaturesStatus() {
        // 二進制功能
        if (PremiumManager.isBinaryOwned()) {
            binaryPriceText.visibility = View.GONE
            binaryStatusText.visibility = View.VISIBLE
            binaryTimeCard.isEnabled = false
        } else {
            binaryPriceText.visibility = View.VISIBLE
            binaryStatusText.visibility = View.GONE
            binaryTimeCard.isEnabled = true
        }
        
        // 摩斯電碼功能
        if (PremiumManager.isMorseOwned()) {
            morsePriceText.visibility = View.GONE
            morseStatusText.visibility = View.VISIBLE
            morseTimeCard.isEnabled = false
        } else {
            morsePriceText.visibility = View.VISIBLE
            morseStatusText.visibility = View.GONE
            morseTimeCard.isEnabled = true
        }
    }
    
    /**
     * 處理進階功能購買
     */
    private fun handlePremiumPurchase(feature: String) {
        val title = when (feature) {
            PremiumManager.FEATURE_BINARY -> getString(R.string.premium_binary_title)
            PremiumManager.FEATURE_MORSE -> getString(R.string.premium_morse_title)
            else -> getString(R.string.premium_feature_default)
        }
        
        val price = when (feature) {
            PremiumManager.FEATURE_BINARY -> getString(R.string.premium_binary_price)
            PremiumManager.FEATURE_MORSE -> getString(R.string.premium_morse_price)
            else -> "$0.99"
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.premium_purchase_title))
            .setMessage("$title\n\n${getString(R.string.premium_purchase_price_label)}: $price\n\n${getString(R.string.premium_purchase_message)}")
            .setPositiveButton(getString(R.string.premium_purchase_buy)) { _, _ ->
                processPurchase(feature)
            }
            .setNegativeButton(getString(R.string.premium_purchase_later), null)
            .show()
    }
    
    /**
     * 處理購買流程
     */
    private fun processPurchase(feature: String) {
        // 這裡應該整合真實的支付系統
        // 目前使用模擬購買
        val success = PremiumManager.simulatePurchase(feature)
        
        if (success) {
            showToast(getString(R.string.premium_purchase_success))
            updatePremiumFeaturesStatus()
        } else {
            showToast(getString(R.string.premium_purchase_failed))
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}