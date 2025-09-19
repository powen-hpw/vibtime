package com.example.vibtime.data.database.dao

import androidx.room.*
import com.example.vibtime.data.database.entities.LocalizedStringEntity
import kotlinx.coroutines.flow.Flow

/**
 * 本地化字串資料存取物件
 */
@Dao
interface LocalizedStringDao {
    
    @Query("SELECT * FROM localized_strings WHERE languageCode = :languageCode ORDER BY key ASC")
    fun getStringsByLanguage(languageCode: String): Flow<List<LocalizedStringEntity>>
    
    @Query("SELECT * FROM localized_strings WHERE key = :key AND languageCode = :languageCode")
    suspend fun getString(key: String, languageCode: String): LocalizedStringEntity?
    
    @Query("SELECT * FROM localized_strings WHERE key = :key AND languageCode = :languageCode")
    fun getStringFlow(key: String, languageCode: String): Flow<LocalizedStringEntity?>
    
    @Query("SELECT value FROM localized_strings WHERE key = :key AND languageCode = :languageCode")
    suspend fun getStringValue(key: String, languageCode: String): String?
    
    @Query("SELECT value FROM localized_strings WHERE key = :key AND languageCode = :languageCode")
    fun getStringValueFlow(key: String, languageCode: String): Flow<String?>
    
    @Query("SELECT * FROM localized_strings WHERE category = :category AND languageCode = :languageCode ORDER BY key ASC")
    fun getStringsByCategory(category: String, languageCode: String): Flow<List<LocalizedStringEntity>>
    
    @Query("SELECT DISTINCT key FROM localized_strings WHERE languageCode = :languageCode ORDER BY key ASC")
    fun getAllKeys(languageCode: String): Flow<List<String>>
    
    @Query("SELECT DISTINCT category FROM localized_strings WHERE languageCode = :languageCode ORDER BY category ASC")
    fun getAllCategories(languageCode: String): Flow<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertString(localizedString: LocalizedStringEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrings(localizedStrings: List<LocalizedStringEntity>)
    
    @Update
    suspend fun updateString(localizedString: LocalizedStringEntity)
    
    @Delete
    suspend fun deleteString(localizedString: LocalizedStringEntity)
    
    @Query("DELETE FROM localized_strings WHERE languageCode = :languageCode")
    suspend fun deleteStringsByLanguage(languageCode: String)
    
    @Query("DELETE FROM localized_strings WHERE key = :key")
    suspend fun deleteStringsByKey(key: String)
    
    @Query("SELECT COUNT(*) FROM localized_strings WHERE languageCode = :languageCode")
    suspend fun getStringCount(languageCode: String): Int
    
    @Query("SELECT COUNT(*) FROM localized_strings WHERE key = :key")
    suspend fun getKeyCount(key: String): Int
    
    // 搜尋功能
    @Query("SELECT * FROM localized_strings WHERE (key LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%') AND languageCode = :languageCode ORDER BY key ASC")
    fun searchStrings(query: String, languageCode: String): Flow<List<LocalizedStringEntity>>
}
