package com.example.vibtime.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vibtime.R
import com.example.vibtime.databinding.FragmentHomeBinding
import com.example.vibtime.service.VibrationService
import com.example.vibtime.service.TimeVibrationHelper
import com.example.vibtime.utils.PremiumManager
import com.example.vibtime.utils.ServiceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.concurrent.Executors

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var serviceStatusText: TextView
    private lateinit var testVibrationButton: MaterialButton
    private lateinit var startServiceButton: MaterialButton
    private lateinit var stopServiceButton: MaterialButton

    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val statusUpdateRunnable = object : Runnable {
        override fun run() {
            updateServiceStatus()
            handler.postDelayed(this, 1000) // Update every second
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views using ViewBinding
        serviceStatusText = binding.serviceStatusText
        testVibrationButton = binding.testVibrationButton
        startServiceButton = binding.startServiceButton
        stopServiceButton = binding.stopServiceButton

        setupPermissionLauncher()
        setupClickListeners()
        PremiumManager.initialize(requireContext())
        updateServiceStatus()
        startStatusUpdates()
    }

    private fun setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // 處理權限請求結果
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                // 權限已授予，重新嘗試啟動服務
                startBackgroundService()
            }
        }
    }

    private fun setupClickListeners() {
        // 設置無障礙支持
        testVibrationButton.apply {
            setContentDescription(getString(R.string.test_vibration_description))
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES)
            setOnClickListener {
                testTimeVibration()
            }
        }

        startServiceButton.apply {
            setContentDescription(getString(R.string.start_service_description))
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES)
            setOnClickListener {
                startBackgroundService()
            }
        }

        stopServiceButton.apply {
            setContentDescription(getString(R.string.stop_service_description))
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES)
            setOnClickListener {
                stopBackgroundService()
            }
        }
    }

    private fun testTimeVibration() {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        TimeVibrationHelper.vibrateTime(requireContext(), hour, minute)
        recordVibrationUsage()
        PremiumManager.recordVibrationUsage()
        
        // 檢查是否應該顯示購買彈窗
        if (PremiumManager.shouldShowPurchasePopup()) {
            showPurchaseDialog()
        }
        
    }

    private fun startBackgroundService() {
        // 使用 ServiceManager 啟動服務，包含權限檢查
        ServiceManager.startServiceWithPermissionCheck(
            fragment = this,
            launcher = permissionLauncher,
            onServiceStarted = {
                updateServiceStatus()
                Toast.makeText(context, getString(R.string.toast_service_started), Toast.LENGTH_LONG).show()
            },
            onPermissionDenied = {
                Toast.makeText(context, getString(R.string.permission_denied_message), Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun stopBackgroundService() {
        ServiceManager.stopVibrationService(requireContext())
        updateServiceStatus()
        Toast.makeText(context, getString(R.string.toast_service_stopped), Toast.LENGTH_LONG).show()
    }

    private fun updateServiceStatus() {
        val isRunning = isVibrationServiceRunning()
        if (isRunning) {
            serviceStatusText.text = getString(R.string.service_running)
            startServiceButton.visibility = View.GONE
            stopServiceButton.visibility = View.VISIBLE
        } else {
            serviceStatusText.text = getString(R.string.service_stopped)
            startServiceButton.visibility = View.VISIBLE
            stopServiceButton.visibility = View.GONE
        }
    }

    private fun isVibrationServiceRunning(): Boolean {
        return ServiceManager.isVibrationServiceRunning(requireContext())
    }

    private fun recordVibrationUsage() {
        executor.execute {
            // Simple usage recording - can be expanded later
            val sharedPreferences = requireContext().getSharedPreferences("vibtime_prefs", android.content.Context.MODE_PRIVATE)
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val todayUsage = sharedPreferences.getInt("usage_$today", 0)
            sharedPreferences.edit().putInt("usage_$today", todayUsage + 1).apply()
        }
    }

    private fun startStatusUpdates() {
        handler.post(statusUpdateRunnable)
    }

    private fun stopStatusUpdates() {
        handler.removeCallbacks(statusUpdateRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopStatusUpdates()
        _binding = null
    }
    
    /**
     * 顯示購買彈窗
     */
    private fun showPurchaseDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.premium_purchase_title))
            .setMessage(getString(R.string.premium_purchase_message))
            .setPositiveButton(getString(R.string.premium_purchase_buy)) { _, _ ->
                // 導航到設定頁面
                findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
            }
            .setNegativeButton(getString(R.string.premium_purchase_later), null)
            .show()
    }
}
