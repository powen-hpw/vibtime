package com.example.vibtime.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 語言實體
 * 定義支持的語言列表
 */
@Entity(tableName = "languages")
data class LanguageEntity(
    @PrimaryKey
    val code: String, // 語言代碼，如 "en", "zh-TW", "zh-CN", "ja", "es"
    val name: String, // 語言名稱，如 "English", "繁體中文", "简体中文", "日本語", "Español"
    val nativeName: String, // 本地語言名稱，如 "English", "繁體中文", "简体中文", "日本語", "Español"
    val isActive: Boolean = true, // 是否啟用
    val isDefault: Boolean = false, // 是否為預設語言
    val sortOrder: Int = 0 // 排序順序
)
