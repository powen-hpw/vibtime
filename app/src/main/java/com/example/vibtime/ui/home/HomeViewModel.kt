package com.example.vibtime.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.vibtime.data.repository.SettingsRepository
import com.example.vibtime.service.VibrationService
import com.example.vibtime.service.TapDetectionService
import kotlinx.coroutines.launch

/**
 * HomeViewModel - 主頁面的 ViewModel
 * 負責管理主頁面的狀態和業務邏輯
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settingsRepository = SettingsRepository(application)
    
    // LiveData for UI state
    private val _isVibrationServiceRunning = MutableLiveData<Boolean>()
    val isVibrationServiceRunning: LiveData<Boolean> = _isVibrationServiceRunning
    
    private val _isHourlyVibrationEnabled = MutableLiveData<Boolean>()
    val isHourlyVibrationEnabled: LiveData<Boolean> = _isHourlyVibrationEnabled
    
    private val _isHalfHourVibrationEnabled = MutableLiveData<Boolean>()
    val isHalfHourVibrationEnabled: LiveData<Boolean> = _isHalfHourVibrationEnabled
    
    private val _countdownMinutes = MutableLiveData<Int>()
    val countdownMinutes: LiveData<Int> = _countdownMinutes
    
    private val _vibrationMode = MutableLiveData<String>()
    val vibrationMode: LiveData<String> = _vibrationMode
    
    private val _tapSensitivity = MutableLiveData<TapDetectionService.TapSensitivity>()
    val tapSensitivity: LiveData<TapDetectionService.TapSensitivity> = _tapSensitivity
    
    init {
        loadSettings()
    }
    
    /**
     * 載入設定
     */
    private fun loadSettings() {
        viewModelScope.launch {
            _isVibrationServiceRunning.value = settingsRepository.isVibrationServiceRunning()
            _isHourlyVibrationEnabled.value = settingsRepository.isHourlyVibrationEnabled()
            _isHalfHourVibrationEnabled.value = settingsRepository.isHalfHourVibrationEnabled()
            _countdownMinutes.value = settingsRepository.getCountdownMinutes()
            _vibrationMode.value = settingsRepository.getVibrationMode()
            _tapSensitivity.value = settingsRepository.getTapSensitivity()
        }
    }
    
    /**
     * 切換整點震動
     */
    fun toggleHourlyVibration() {
        val newValue = !(_isHourlyVibrationEnabled.value ?: false)
        _isHourlyVibrationEnabled.value = newValue
        viewModelScope.launch {
            settingsRepository.setHourlyVibrationEnabled(newValue)
        }
    }
    
    /**
     * 切換半點震動
     */
    fun toggleHalfHourVibration() {
        val newValue = !(_isHalfHourVibrationEnabled.value ?: false)
        _isHalfHourVibrationEnabled.value = newValue
        viewModelScope.launch {
            settingsRepository.setHalfHourVibrationEnabled(newValue)
        }
    }
    
    /**
     * 設定倒數計時分鐘數
     */
    fun setCountdownMinutes(minutes: Int) {
        _countdownMinutes.value = minutes
        viewModelScope.launch {
            settingsRepository.setCountdownMinutes(minutes)
        }
    }
    
    /**
     * 設定震動模式
     */
    fun setVibrationMode(mode: String) {
        _vibrationMode.value = mode
        viewModelScope.launch {
            settingsRepository.setVibrationMode(mode)
        }
    }
    
    /**
     * 設定敲擊靈敏度
     */
    fun setTapSensitivity(sensitivity: TapDetectionService.TapSensitivity) {
        _tapSensitivity.value = sensitivity
        viewModelScope.launch {
            settingsRepository.setTapSensitivity(sensitivity)
        }
    }
    
    /**
     * 開始震動服務
     */
    fun startVibrationService() {
        val sensitivity = _tapSensitivity.value ?: TapDetectionService.TapSensitivity.MEDIUM
        VibrationService.startService(getApplication(), sensitivity)
        _isVibrationServiceRunning.value = true
        viewModelScope.launch {
            settingsRepository.setVibrationServiceRunning(true)
        }
    }
    
    /**
     * 停止震動服務
     */
    fun stopVibrationService() {
        VibrationService.stopService(getApplication())
        _isVibrationServiceRunning.value = false
        viewModelScope.launch {
            settingsRepository.setVibrationServiceRunning(false)
        }
    }
    
    /**
     * 切換震動服務狀態
     */
    fun toggleVibrationService() {
        if (_isVibrationServiceRunning.value == true) {
            stopVibrationService()
        } else {
            startVibrationService()
        }
    }
    
    /**
     * 檢查是否有任何震動模式啟用
     */
    fun hasAnyVibrationEnabled(): Boolean {
        return (_isHourlyVibrationEnabled.value ?: false) ||
               (_isHalfHourVibrationEnabled.value ?: false) ||
               (_countdownMinutes.value ?: 0) > 0
    }
    
    /**
     * 重置所有設定
     */
    fun resetSettings() {
        viewModelScope.launch {
            settingsRepository.resetAllSettings()
            loadSettings()
        }
    }
}
