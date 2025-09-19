package com.example.vibtime.ui.vibration

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.slider.Slider
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.vibtime.R
import com.example.vibtime.databinding.FragmentVibrationBinding
import com.example.vibtime.service.TimeVibrationHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Vibration Fragment - 震動設定頁面
 * 自訂震動模式和測試功能
 */
class VibrationFragment : Fragment() {

    private var _binding: FragmentVibrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    
    // 震動模式設定
    private lateinit var basicVibrationCard: MaterialCardView
    private lateinit var basicDurationSeekBar: Slider
    private lateinit var basicDurationText: TextView
    private lateinit var testBasicButton: MaterialButton
    
    // 時間震動設定
    private lateinit var timeVibrationCard: MaterialCardView
    private lateinit var hourVibrationSeekBar: Slider
    private lateinit var hourVibrationText: TextView
    private lateinit var minuteVibrationSeekBar: Slider
    private lateinit var minuteVibrationText: TextView
    private lateinit var testTimeButton: MaterialButton
    
    // 自訂時間測試
    private lateinit var customTimeCard: MaterialCardView
    private lateinit var hourSeekBar: Slider
    private lateinit var minuteSeekBar: Slider
    private lateinit var hourText: TextView
    private lateinit var minuteText: TextView
    private lateinit var testCustomTimeButton: MaterialButton
    
    // 震動模式預覽
    private lateinit var previewCard: MaterialCardView
    private lateinit var previewText: TextView
    
    // 當前設定值
    private var basicDuration = 500L
    private var hourVibrationDuration = 600L
    private var minuteVibrationDuration = 200L
    private var testHour = 14
    private var testMinute = 30

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVibrationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupSharedPreferences()
        loadSettings()
        setupSeekBars()
        setupClickListeners()
        updatePreview()
    }
    
    /**
     * 初始化視圖
     */
    private fun initializeViews(view: View) {
        // 基本震動設定
        basicVibrationCard = binding.basicVibrationCard
        basicDurationSeekBar = binding.basicDurationSeekbar
        basicDurationText = binding.basicDurationText
        testBasicButton = binding.btnTestBasic
        
        // 時間震動設定
        timeVibrationCard = binding.timeVibrationCard
        hourVibrationSeekBar = binding.hourVibrationSeekbar
        hourVibrationText = binding.hourVibrationText
        minuteVibrationSeekBar = binding.minuteVibrationSeekbar
        minuteVibrationText = binding.minuteVibrationText
        testTimeButton = binding.btnTestTime
        
        // 自訂時間測試
        customTimeCard = binding.customTimeCard
        hourSeekBar = binding.hourSeekbar
        minuteSeekBar = binding.minuteSeekbar
        hourText = binding.hourText
        minuteText = binding.minuteText
        testCustomTimeButton = binding.btnTestCustomTime
        
        // 預覽
        previewCard = binding.previewCard
        previewText = binding.previewText
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
        basicDuration = sharedPreferences.getLong("basic_vibration_duration", 500L)
        hourVibrationDuration = sharedPreferences.getLong("hour_vibration_duration", 600L)
        minuteVibrationDuration = sharedPreferences.getLong("minute_vibration_duration", 200L)
        testHour = sharedPreferences.getInt("test_hour", 14)
        testMinute = sharedPreferences.getInt("test_minute", 30)
    }
    
    /**
     * 設定滑桿
     */
    private fun setupSeekBars() {
        // 基本震動時長 (100ms - 2000ms)
        basicDurationSeekBar.valueFrom = 100f
        basicDurationSeekBar.valueTo = 2000f
        basicDurationSeekBar.value = basicDuration.toFloat()
        basicDurationText.text = getString(R.string.duration_format, basicDuration)
        
        // 小時震動時長 (300ms - 1000ms)
        hourVibrationSeekBar.valueFrom = 300f
        hourVibrationSeekBar.valueTo = 1000f
        hourVibrationSeekBar.value = hourVibrationDuration.toFloat()
        hourVibrationText.text = getString(R.string.duration_format, hourVibrationDuration)
        
        // 分鐘震動時長 (100ms - 500ms)
        minuteVibrationSeekBar.valueFrom = 100f
        minuteVibrationSeekBar.valueTo = 500f
        minuteVibrationSeekBar.value = minuteVibrationDuration.toFloat()
        minuteVibrationText.text = getString(R.string.duration_format, minuteVibrationDuration)
        
        // 測試時間
        hourSeekBar.valueFrom = 0f
        hourSeekBar.valueTo = 23f
        hourSeekBar.value = testHour.toFloat()
        hourText.text = getString(R.string.hour_format, testHour)
        
        minuteSeekBar.valueFrom = 0f
        minuteSeekBar.valueTo = 59f
        minuteSeekBar.value = testMinute.toFloat()
        minuteText.text = getString(R.string.minute_format, testMinute)
        
        // 滑桿監聽器
        basicDurationSeekBar.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                basicDuration = value.toLong()
                basicDurationText.text = getString(R.string.duration_format, basicDuration)
                saveSettings()
                updatePreview()
            }
        }
        
        hourVibrationSeekBar.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                hourVibrationDuration = value.toLong()
                hourVibrationText.text = getString(R.string.duration_format, hourVibrationDuration)
                saveSettings()
                updatePreview()
            }
        }
        
        minuteVibrationSeekBar.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                minuteVibrationDuration = value.toLong()
                minuteVibrationText.text = getString(R.string.duration_format, minuteVibrationDuration)
                saveSettings()
                updatePreview()
            }
        }
        
        hourSeekBar.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                testHour = value.toInt()
                hourText.text = getString(R.string.hour_format, testHour)
                saveSettings()
                updatePreview()
            }
        }
        
        minuteSeekBar.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                testMinute = value.toInt()
                minuteText.text = getString(R.string.minute_format, testMinute)
                saveSettings()
                updatePreview()
            }
        }
    }
    
    /**
     * 設定點擊監聽器
     */
    private fun setupClickListeners() {
        testBasicButton.setOnClickListener {
            testBasicVibration()
        }
        
        testTimeButton.setOnClickListener {
            testCurrentTime()
        }
        
        testCustomTimeButton.setOnClickListener {
            testCustomTime()
        }
    }
    
    /**
     * 測試基本震動
     */
    private fun testBasicVibration() {
        TimeVibrationHelper.testVibration(requireContext(), basicDuration)
        showToast(getString(R.string.toast_basic_vibration_test, basicDuration))
    }
    
    /**
     * 測試當前時間
     */
    private fun testCurrentTime() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        TimeVibrationHelper.vibrateTime(requireContext(), hour, minute)
        
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        showToast(getString(R.string.toast_current_time_test, timeStr))
    }
    
    /**
     * 測試自訂時間
     */
    private fun testCustomTime() {
        TimeVibrationHelper.vibrateTime(requireContext(), testHour, testMinute)
        
        val timeStr = getString(R.string.time_format_hour_minute, testHour, testMinute)
        showToast(getString(R.string.toast_custom_time_test, timeStr))
    }
    
    /**
     * 儲存設定
     */
    private fun saveSettings() {
        sharedPreferences.edit()
            .putLong("basic_vibration_duration", basicDuration)
            .putLong("hour_vibration_duration", hourVibrationDuration)
            .putLong("minute_vibration_duration", minuteVibrationDuration)
            .putInt("test_hour", testHour)
            .putInt("test_minute", testMinute)
            .apply()
    }
    
    /**
     * 更新預覽
     */
    private fun updatePreview() {
        val hourCount = testHour % 12
        val minuteCount = testMinute / 5
        
        val preview = StringBuilder()
        preview.append(getString(R.string.preview_time_vibration, getString(R.string.time_format_hour_minute, testHour, testMinute)) + "\n\n")
        
        if (hourCount > 0) {
            preview.append(getString(R.string.preview_hour_vibration, hourCount, hourVibrationDuration) + "\n")
        }
        
        if (minuteCount > 0) {
            preview.append(getString(R.string.preview_minute_vibration, minuteCount, minuteVibrationDuration) + "\n")
        }
        
        if (hourCount == 0 && minuteCount == 0) {
            preview.append(getString(R.string.preview_zero_vibration) + "\n")
        }
        
        preview.append("\n" + getString(R.string.preview_mode_title) + "\n")
        preview.append(getString(R.string.preview_long_vibration) + "\n")
        preview.append(getString(R.string.preview_short_vibration) + "\n")
        preview.append(getString(R.string.preview_zero_time))
        
        previewText.text = preview.toString()
    }
    
    /**
     * 顯示提示訊息
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}