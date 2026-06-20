package com.androidassistant.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.androidassistant.data.local.dao.ConversationDao
import com.androidassistant.data.local.dao.SessionDao
import com.androidassistant.data.local.dao.ToolExecutionDao
import com.androidassistant.data.local.entity.ConversationEntity
import com.androidassistant.data.local.entity.SessionEntity
import com.androidassistant.data.local.entity.ToolExecutionEntity

@Database(
    entities = [
        SessionEntity::class,
        ConversationEntity::class,
        ToolExecutionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun conversationDao(): ConversationDao
    abstract fun toolExecutionDao(): ToolExecutionDao

    companion object {
        private const val DB_NAME = "assistant.db"

        fun create(context: android.content.Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DB_NAME
            ).fallbackToDestructiveMigration()
                .build()
        }
    }
}
