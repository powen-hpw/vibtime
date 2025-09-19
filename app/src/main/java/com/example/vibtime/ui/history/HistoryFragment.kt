package com.example.vibtime.ui.history

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vibtime.R
import com.example.vibtime.databinding.FragmentHistoryBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

/**
 * History Fragment - 歷史記錄頁面
 * 顯示使用統計和震動歷史
 */
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    
    // UI Elements
    private lateinit var todayStatsCard: MaterialCardView
    private lateinit var todayUsageText: TextView
    private lateinit var todayTimeText: TextView
    
    private lateinit var weekStatsCard: MaterialCardView
    private lateinit var weekUsageText: TextView
    private lateinit var weekTotalText: TextView
    
    private lateinit var totalStatsCard: MaterialCardView
    private lateinit var totalUsageText: TextView
    private lateinit var firstUseText: TextView
    
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var clearHistoryCard: MaterialCardView
    
    private val historyList = mutableListOf<HistoryItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupSharedPreferences()
        setupRecyclerView()
        setupClickListeners()
        loadStatistics()
        loadHistory()
    }
    
    override fun onResume() {
        super.onResume()
        loadStatistics()
        loadHistory()
    }
    
    /**
     * 初始化視圖
     */
    private fun initializeViews(view: View) {
        todayStatsCard = binding.todayStatsCard
        todayUsageText = binding.todayUsageText
        todayTimeText = binding.todayTimeText
        
        weekStatsCard = binding.weekStatsCard
        weekUsageText = binding.weekUsageText
        weekTotalText = binding.weekTotalText
        
        totalStatsCard = binding.totalStatsCard
        totalUsageText = binding.totalUsageText
        firstUseText = binding.firstUseText
        
        historyRecyclerView = binding.historyRecyclerView
        clearHistoryCard = binding.clearHistoryCard
    }
    
    /**
     * 設定 SharedPreferences
     */
    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("vibtime_prefs", Context.MODE_PRIVATE)
    }
    
    /**
     * 設定 RecyclerView
     */
    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(historyList)
        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyRecyclerView.adapter = historyAdapter
    }
    
    /**
     * 設定點擊監聽器
     */
    private fun setupClickListeners() {
        clearHistoryCard.setOnClickListener {
            showClearHistoryDialog()
        }
    }
    
    /**
     * 載入統計資料
     */
    private fun loadStatistics() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val thisWeek = getWeekKey(Date())
        
        // 今日統計
        val todayUsage = sharedPreferences.getInt("usage_$today", 0)
        val todayVibrations = sharedPreferences.getInt("vibrations_$today", 0)
        val todayTime = sharedPreferences.getString("last_vibration_time_$today", "--")
        
        todayUsageText.text = getString(R.string.today_usage_fmt, todayUsage, todayVibrations)
        todayTimeText.text = getString(R.string.last_vibration_fmt, todayTime)
        
        // 本週統計
        val weekUsage = sharedPreferences.getInt("usage_$thisWeek", 0)
        val weekTotal = calculateWeekTotal()
        
        weekUsageText.text = getString(R.string.week_usage_fmt, weekUsage)
        weekTotalText.text = getString(R.string.week_avg_fmt, weekTotal / 7)
        
        // 總計統計
        val totalUsage = sharedPreferences.getInt("total_usage", 0)
        val totalVibrations = sharedPreferences.getInt("total_vibrations", 0)
        val firstUse = sharedPreferences.getString("first_use_date", getString(R.string.first_use_none))
        
        totalUsageText.text = getString(R.string.total_usage_fmt, totalUsage, totalVibrations)
        firstUseText.text = getString(R.string.first_use_fmt, firstUse)
        
        // 如果是第一次使用，記錄日期
        if (firstUse == getString(R.string.first_use_none) && totalUsage > 0) {
            sharedPreferences.edit()
                .putString("first_use_date", today)
                .apply()
            firstUseText.text = getString(R.string.first_use_fmt, today)
        }
    }
    
    /**
     * 載入歷史記錄
     */
    private fun loadHistory() {
        historyList.clear()
        
        // 從 SharedPreferences 讀取歷史記錄
        val allPrefs = sharedPreferences.all
        val historyEntries = mutableListOf<HistoryItem>()
        
        for ((key, value) in allPrefs) {
            when {
                key.startsWith("usage_") && key.contains("-") -> {
                    val date = key.removePrefix("usage_")
                    val usage = value as? Int ?: 0
                    if (usage > 0) {
                        historyEntries.add(
                            HistoryItem(
                                date = date,
                                type = "daily_usage",
                                description = getString(R.string.history_usage_fmt, usage),
                                time = "--"
                            )
                        )
                    }
                }
                key.startsWith("last_use_time_") -> {
                    val date = key.removePrefix("last_use_time_")
                    val time = value as? String ?: "--"
                    if (time != "--") {
                        historyEntries.add(
                            HistoryItem(
                                date = date,
                                type = "time_check",
                                description = getString(R.string.history_time_check),
                                time = time
                            )
                        )
                    }
                }
            }
        }
        
        // 按日期排序（最新在前）
        historyEntries.sortByDescending { it.date }
        
        // 限制顯示最近 30 條記錄
        historyList.addAll(historyEntries.take(30))
        
        // 如果沒有歷史記錄，加入提示
        if (historyList.isEmpty()) {
            historyList.add(
                HistoryItem(
                    date = getString(R.string.history_empty_title),
                    type = "empty",
                    description = getString(R.string.history_empty_desc),
                    time = "--"
                )
            )
        }
        
        historyAdapter.notifyDataSetChanged()
    }
    
    /**
     * 計算本週總計
     */
    private fun calculateWeekTotal(): Int {
        val calendar = Calendar.getInstance()
        var total = 0
        
        // 計算過去7天的使用量
        for (i in 0..6) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            total += sharedPreferences.getInt("usage_$date", 0)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        return total
    }
    
    /**
     * 獲取週數Key
     */
    private fun getWeekKey(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        return "${year}-W${String.format("%02d", week)}"
    }
    
    /**
     * 顯示清除歷史確認對話框
     */
    private fun showClearHistoryDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.clear_history_dialog_title))
            .setMessage(getString(R.string.clear_history_dialog_message))
            .setPositiveButton(getString(R.string.clear_history_confirm)) { _, _ ->
                clearHistory()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    /**
     * 清除歷史記錄
     */
    private fun clearHistory() {
        val editor = sharedPreferences.edit()
        val allPrefs = sharedPreferences.all
        
        // 刪除所有使用相關的記錄
        for (key in allPrefs.keys) {
            if (key.startsWith("usage_") || 
                key.startsWith("last_use_time_") || 
                key.startsWith("total_usage") ||
                key.startsWith("first_use_date")) {
                editor.remove(key)
            }
        }
        
        editor.apply()
        
        // 重新載入數據
        loadStatistics()
        loadHistory()
    }
    
    /**
     * 歷史記錄項目
     */
    data class HistoryItem(
        val date: String,
        val type: String,
        val description: String,
        val time: String
    )
    
    /**
     * 歷史記錄適配器
     */
    inner class HistoryAdapter(private val historyList: List<HistoryItem>) : 
        RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val dateText: TextView = view.findViewById(R.id.history_date_text)
            val descriptionText: TextView = view.findViewById(R.id.history_description_text)
            val timeText: TextView = view.findViewById(R.id.history_time_text)
            val typeIcon: TextView = view.findViewById(R.id.history_type_icon)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = historyList[position]
            
            holder.dateText.text = item.date
            holder.descriptionText.text = item.description
            holder.timeText.text = item.time
            
            // 設定圖示
            holder.typeIcon.text = when (item.type) {
                "daily_usage" -> "📊"
                "time_check" -> "⏰"
                "empty" -> "💡"
                else -> getString(R.string.icon_phone)
            }
        }
        
        override fun getItemCount() = historyList.size
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}