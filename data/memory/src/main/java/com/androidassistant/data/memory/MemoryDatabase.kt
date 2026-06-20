package com.androidassistant.data.memory

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.androidassistant.data.memory.dao.EpisodicMemoryDao
import com.androidassistant.data.memory.dao.SemanticMemoryDao
import com.androidassistant.data.memory.entity.EpisodicMemoryEntity
import com.androidassistant.data.memory.entity.SemanticMemoryEntity

@Database(
    entities = [
        SemanticMemoryEntity::class,
        EpisodicMemoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MemoryDatabase : RoomDatabase() {

    abstract fun semanticMemoryDao(): SemanticMemoryDao
    abstract fun episodicMemoryDao(): EpisodicMemoryDao

    companion object {
        private const val DB_NAME = "memory.db"

        fun create(context: android.content.Context): MemoryDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MemoryDatabase::class.java,
                DB_NAME
            ).fallbackToDestructiveMigration()
                .build()
        }
    }
}
