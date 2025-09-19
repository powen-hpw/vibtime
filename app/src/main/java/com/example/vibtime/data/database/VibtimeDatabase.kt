package com.example.vibtime.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.vibtime.data.database.dao.LanguageDao
import com.example.vibtime.data.database.dao.LocalizedStringDao
import com.example.vibtime.data.database.entities.LanguageEntity
import com.example.vibtime.data.database.entities.LocalizedStringEntity

/**
 * Vibtime 資料庫
 * 包含多語言支持的所有資料表
 */
@Database(
    entities = [
        LanguageEntity::class,
        LocalizedStringEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VibtimeDatabase : RoomDatabase() {
    
    abstract fun languageDao(): LanguageDao
    abstract fun localizedStringDao(): LocalizedStringDao
    
    companion object {
        @Volatile
        private var INSTANCE: VibtimeDatabase? = null
        
        fun getDatabase(context: Context): VibtimeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VibtimeDatabase::class.java,
                    "vibtime_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

