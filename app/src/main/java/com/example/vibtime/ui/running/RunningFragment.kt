package com.example.vibtime.ui.running

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.vibtime.R
import com.example.vibtime.databinding.FragmentRunningBinding
import com.example.vibtime.service.TapDetectionService
import com.example.vibtime.service.VibrationService
import com.example.vibtime.utils.SafetyManager
import com.example.vibtime.utils.ServiceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.SimpleDateFormat
import java.util.*

/**
 * Running Fragment - 服務控制中心
 * 專業的背景服務管理界面，包含改進的安全機制
 */
class RunningFragment : Fragment(), SafetyManager.SafetyCallback {

    private var _binding: FragmentRunningBinding? = null
    private val binding get() = _binding!!

    private lateinit var serviceStatusCard: MaterialCardView
    private lateinit var serviceStatusText: TextView
    private lateinit var serviceUptimeText: TextView
    private lateinit var todayUsageText: TextView
    private lateinit var totalUsageText: TextView
    
    // 安全機制 UI 元素
    private lateinit var safetyTimerContainer: LinearLayout
    private lateinit var safetyTimerProgress: LinearProgressIndicator
    private lateinit var safetyTimerText: TextView
    private lateinit var nextVibrationContainer: LinearLayout
    private lateinit var nextVibrationProgress: LinearProgressIndicator
    private lateinit var nextVibrationText: TextView
    private lateinit var emergencyStopButton: MaterialButton
    private lateinit var testModeButton: MaterialButton
    
    private lateinit var toggleServiceButton: MaterialButton
    private lateinit var sensitivityLowButton: MaterialButton
    private lateinit var sensitivityMediumButton: MaterialButton
    private lateinit var sensitivityHighButton: MaterialButton
    
    private lateinit var sharedPreferences: SharedPreferences
    private val updateHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    
    private var currentSensitivity = TapDetectionService.TapSensitivity.MEDIUM
    private var isTestMode = false
    private var isServiceRunning = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRunningBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupSharedPreferences()
        setupClickListeners()
        initializeSafetyManager()
        startStatusUpdates()
    }
    
    override fun onResume() {
        super.onResume()
        updateServiceStatus()
        updateUsageStatistics()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        stopStatusUpdates()
        SafetyManager.stopMonitoring()
        _binding = null
    }
    
    /**
     * 初始化視圖
     */
    private fun initializeViews(view: View) {
        serviceStatusCard = binding.serviceStatusCard
        serviceStatusText = binding.serviceStatusText
        serviceUptimeText = binding.serviceUptimeText
        todayUsageText = binding.todayUsageText
        totalUsageText = binding.totalUsageText
        
        // 安全機制 UI
        safetyTimerContainer = binding.safetyTimerContainer
        safetyTimerProgress = binding.safetyTimerProgress
        safetyTimerText = binding.safetyTimerText
        nextVibrationContainer = binding.nextVibrationContainer
        nextVibrationProgress = binding.nextVibrationProgress
        nextVibrationText = binding.nextVibrationText
        emergencyStopButton = binding.btnEmergencyStop
        testModeButton = binding.btnTestMode
        
        toggleServiceButton = binding.btnToggleService
        sensitivityLowButton = binding.btnSensitivityLow
        sensitivityMediumButton = binding.btnSensitivityMedium
        sensitivityHighButton = binding.btnSensitivityHigh
    }
    
    /**
     * 設定 SharedPreferences
     */
    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
        
        // 載入保存的敏感度設定
        val savedSensitivity = sharedPreferences.getString("tap_sensitivity", TapDetectionService.TapSensitivity.MEDIUM.name)
        currentSensitivity = TapDetectionService.TapSensitivity.valueOf(savedSensitivity ?: TapDetectionService.TapSensitivity.MEDIUM.name)
        updateSensitivityButtons()
    }
    
    /**
     * 初始化安全管理器
     */
    private fun initializeSafetyManager() {
        SafetyManager.initialize(requireContext(), this, isTestMode)
    }
    
    /**
     * 設定點擊監聽器
     */
    private fun setupClickListeners() {
        toggleServiceButton.setOnClickListener {
            toggleService()
        }
        
        testModeButton.setOnClickListener {
            toggleTestMode()
        }
        
        emergencyStopButton.setOnClickListener {
            emergencyStop()
        }
        
        sensitivityLowButton.setOnClickListener {
            setSensitivity(TapDetectionService.TapSensitivity.LOW)
        }
        
        sensitivityMediumButton.setOnClickListener {
            setSensitivity(TapDetectionService.TapSensitivity.MEDIUM)
        }
        
        sensitivityHighButton.setOnClickListener {
            setSensitivity(TapDetectionService.TapSensitivity.HIGH)
        }
    }
    
    /**
     * 切換測試模式
     */
    private fun toggleTestMode() {
        isTestMode = !isTestMode
        testModeButton.text = if (isTestMode) getString(R.string.safety_normal_mode) else getString(R.string.safety_test_mode)
        
        // 重新初始化 SafetyManager
        SafetyManager.stopMonitoring()
        SafetyManager.initialize(requireContext(), this, isTestMode)
        
        if (isServiceRunning) {
            SafetyManager.startMonitoring()
        }
        
        Toast.makeText(requireContext(), 
            if (isTestMode) getString(R.string.test_mode_enabled) else getString(R.string.normal_mode_enabled), 
            Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 緊急停止
     */
    private fun emergencyStop() {
        SafetyManager.emergencyStop(requireContext())
        ServiceManager.stopVibrationService(requireContext())
        updateServiceStatus()
        Toast.makeText(requireContext(), getString(R.string.service_emergency_stopped), Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 切換服務狀態
     */
    private fun toggleService() {
        if (isServiceRunning) {
            ServiceManager.stopVibrationService(requireContext())
            SafetyManager.stopMonitoring()
            updateServiceStatus()
        } else {
            // 使用 ServiceManager 啟動服務，包含權限檢查
            ServiceManager.startVibrationServiceSafely(requireContext(), currentSensitivity)
            if (ServiceManager.isVibrationServiceRunning(requireContext())) {
                SafetyManager.recordServiceStart(requireContext())
                SafetyManager.startMonitoring()
                updateServiceStatus()
            } else {
                Toast.makeText(requireContext(), getString(R.string.permission_denied_message), Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * 設定敏感度
     */
    private fun setSensitivity(sensitivity: TapDetectionService.TapSensitivity) {
        currentSensitivity = sensitivity
        sharedPreferences.edit().putString("tap_sensitivity", sensitivity.name).apply()
        updateSensitivityButtons()
        
        if (isServiceRunning) {
            // 更新運行中的服務敏感度
            val intent = android.content.Intent(requireContext(), VibrationService::class.java).apply {
                action = VibrationService.ACTION_UPDATE_SENSITIVITY
                putExtra(VibrationService.EXTRA_SENSITIVITY, sensitivity.name)
            }
            requireContext().startService(intent)
        }
    }
    
    /**
     * 更新敏感度按鈕狀態
     */
    private fun updateSensitivityButtons() {
        sensitivityLowButton.apply {
            backgroundTintList = if (currentSensitivity == TapDetectionService.TapSensitivity.LOW) {
                android.content.res.ColorStateList.valueOf(resources.getColor(R.color.vibtime_primary, null))
            } else {
                android.content.res.ColorStateList.valueOf(resources.getColor(R.color.surface_variant, null))
            }
            setTextColor(if (currentSensitivity == TapDetectionService.TapSensitivity.LOW) {
                resources.getColor(R.color.white, null)
            } else {
                resources.getColor(R.color.text_primary, null)
            })
        }
        
        sensitivityMediumButton.apply {
            backgroundTintList = if (currentSensitivity == TapDetectionService.TapSensitivity.MEDIUM) {
                android.content.res.ColorStateList.valueOf(resources.getColor(R.color.vibtime_primary, null))
            } else {
                android.content.res.ColorStateList.valueOf(resources.getColor(R.color.surface_variant, null))
            }
            setTextColor(if (currentSensitivity == TapDetectionService.TapSensitivity.MEDIUM) {
                resources.getColor(R.color.white, null)
            } else {
                resources.getColor(R.color.text_primary, null)
            })
        }
        
        sensitivityHighButton.apply {
            backgroundTintList = if (currentSensitivity == TapDetectionService.TapSensitivity.HIGH) {
                android.content.res.ColorStateList.valueOf(resources.getColor(R.color.vibtime_primary, null))
            } else {
                android.content.res.ColorStateList.valueOf(resources.getColor(R.color.surface_variant, null))
            }
            setTextColor(if (currentSensitivity == TapDetectionService.TapSensitivity.HIGH) {
                resources.getColor(R.color.white, null)
            } else {
                resources.getColor(R.color.text_primary, null)
            })
        }
    }
    
    /**
     * 更新服務狀態
     */
    private fun updateServiceStatus() {
        isServiceRunning = isServiceRunning()
        
        if (isServiceRunning) {
            serviceStatusText.text = getString(R.string.service_running)
            toggleServiceButton.text = getString(R.string.stop_service)
            toggleServiceButton.backgroundTintList = android.content.res.ColorStateList.valueOf(resources.getColor(R.color.error, null))
            
            // 顯示安全機制 UI
            safetyTimerContainer.visibility = View.VISIBLE
            emergencyStopButton.visibility = View.VISIBLE
        } else {
            serviceStatusText.text = getString(R.string.service_stopped)
            toggleServiceButton.text = getString(R.string.start_service)
            toggleServiceButton.backgroundTintList = android.content.res.ColorStateList.valueOf(resources.getColor(R.color.success, null))
            
            // 隱藏安全機制 UI
            safetyTimerContainer.visibility = View.GONE
            nextVibrationContainer.visibility = View.GONE
            emergencyStopButton.visibility = View.GONE
        }
    }
    
    /**
     * 檢查服務是否運行
     */
    private fun isServiceRunning(): Boolean {
        return ServiceManager.isVibrationServiceRunning(requireContext())
    }
    
    /**
     * 更新使用統計
     */
    private fun updateUsageStatistics() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayVibrations = sharedPreferences.getInt("vibrations_$today", 0)
        val totalVibrations = sharedPreferences.getInt("total_vibrations", 0)
        
        todayUsageText.text = getString(R.string.today_usage_fmt_label, todayVibrations)
        totalUsageText.text = getString(R.string.total_usage_fmt_label, totalVibrations)
    }
    
    /**
     * 開始狀態更新
     */
    private fun startStatusUpdates() {
        updateRunnable = object : Runnable {
            override fun run() {
                updateServiceStatus()
                updateUptime()
                updateHandler.postDelayed(this, 1000)
            }
        }
        updateHandler.post(updateRunnable!!)
    }
    
    /**
     * 停止狀態更新
     */
    private fun stopStatusUpdates() {
        updateRunnable?.let { updateHandler.removeCallbacks(it) }
    }
    
    /**
     * 更新運行時間
     */
    private fun updateUptime() {
        if (!isServiceRunning) {
            serviceUptimeText.text = getString(R.string.uptime_placeholder)
            return
        }
        
        val serviceStartTime = sharedPreferences.getLong("service_start_time", 0L)
        if (serviceStartTime == 0L) {
            serviceUptimeText.text = getString(R.string.uptime_placeholder)
            return
        }
        
        val currentTime = System.currentTimeMillis()
        val uptime = currentTime - serviceStartTime
        val hours = uptime / (60 * 60 * 1000)
        val minutes = (uptime % (60 * 60 * 1000)) / (60 * 1000)
        val seconds = (uptime % (60 * 1000)) / 1000
        
        serviceUptimeText.text = getString(R.string.uptime_fmt, getString(R.string.time_format_hh_mm_ss, hours, minutes, seconds))
    }
    
    // SafetyManager.SafetyCallback 實現
    
    override fun onServiceTimeUpdate(remainingTime: Long, progress: Float) {
        requireActivity().runOnUiThread {
            safetyTimerProgress.progress = (progress * 100).toInt()
            safetyTimerText.text = SafetyManager.formatTime(remainingTime)
        }
    }
    
    override fun onNextVibrationUpdate(remainingTime: Long, progress: Float) {
        requireActivity().runOnUiThread {
            if (remainingTime > 0) {
                nextVibrationContainer.visibility = View.VISIBLE
                nextVibrationProgress.progress = (progress * 100).toInt()
                nextVibrationText.text = SafetyManager.formatTime(remainingTime)
            } else {
                nextVibrationContainer.visibility = View.GONE
            }
        }
    }
    
    override fun onServiceExpired() {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), getString(R.string.safety_service_expired), Toast.LENGTH_LONG).show()
            VibrationService.stopService(requireContext())
            updateServiceStatus()
        }
    }
    
    override fun onFrequencyLimit(remainingTime: Long) {
        // 頻率限制時不顯示通知，只在 UI 中顯示
    }
    
    override fun onAutoStopWarning() {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), 
                getString(R.string.safety_auto_stop_soon, 15), 
                Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onAutoStop() {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), getString(R.string.safety_auto_stopped), Toast.LENGTH_LONG).show()
            updateServiceStatus()
        }
    }
    
    override fun onWatchModeCooldownUpdate(remainingTime: Long, progress: Float) {
        requireActivity().runOnUiThread {
            // 可以在 UI 中顯示 Watch Mode 冷卻期狀態
            // 這裡暫時不實現具體的 UI 更新
        }
    }
    
    override fun onWatchModeStatusChanged(status: com.example.vibtime.utils.WatchModeManager.WatchModeStatus) {
        requireActivity().runOnUiThread {
            // 可以在 UI 中顯示 Watch Mode 狀態變化
            // 這裡暫時不實現具體的 UI 更新
        }
    }
}
