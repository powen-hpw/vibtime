package com.example.vibtime.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * 本地化字串實體
 * 存儲所有多語言字串
 */
@Entity(
    tableName = "localized_strings",
    primaryKeys = ["key", "languageCode"],
    foreignKeys = [
        ForeignKey(
            entity = LanguageEntity::class,
            parentColumns = ["code"],
            childColumns = ["languageCode"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["key"]),
        Index(value = ["languageCode"]),
        Index(value = ["key", "languageCode"], unique = true)
    ]
)
data class LocalizedStringEntity(
    val key: String, // 字串鍵值，如 "app_name", "start_service"
    val languageCode: String, // 語言代碼
    val value: String, // 本地化字串值
    val category: String = "general", // 分類，如 "general", "ui", "error", "permission"
    val isHtml: Boolean = false, // 是否包含 HTML 標籤
    val lastUpdated: Long = System.currentTimeMillis() // 最後更新時間
)
