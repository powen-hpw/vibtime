package com.example.vibtime.data.database.dao

import androidx.room.*
import com.example.vibtime.data.database.entities.LanguageEntity
import kotlinx.coroutines.flow.Flow

/**
 * 語言資料存取物件
 */
@Dao
interface LanguageDao {
    
    @Query("SELECT * FROM languages WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun getAllActiveLanguages(): Flow<List<LanguageEntity>>
    
    @Query("SELECT * FROM languages WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultLanguage(): LanguageEntity?
    
    @Query("SELECT * FROM languages WHERE code = :code")
    suspend fun getLanguageByCode(code: String): LanguageEntity?
    
    @Query("SELECT * FROM languages WHERE code = :code")
    fun getLanguageByCodeFlow(code: String): Flow<LanguageEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguage(language: LanguageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguages(languages: List<LanguageEntity>)
    
    @Update
    suspend fun updateLanguage(language: LanguageEntity)
    
    @Delete
    suspend fun deleteLanguage(language: LanguageEntity)
    
    @Query("UPDATE languages SET isDefault = 0")
    suspend fun clearDefaultLanguage()
    
    @Query("UPDATE languages SET isDefault = 1 WHERE code = :code")
    suspend fun setDefaultLanguage(code: String)
    
    @Query("SELECT COUNT(*) FROM languages WHERE isActive = 1")
    suspend fun getActiveLanguageCount(): Int
}
