package com.androidassistant.data.memory

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.androidassistant.data.memory.dao.EpisodicMemoryDao
import com.androidassistant.data.memory.dao.SemanticMemoryDao
import com.androidassistant.data.memory.entity.EpisodicMemoryEntity
import com.androidassistant.data.memory.entity.SemanticMemoryEntity
import com.github.ashemooo.sqlitevec.SqliteVec
import org.sqlite.database.sqlite.SQLiteDatabase

@Database(
    entities = [
        SemanticMemoryEntity::class,
        EpisodicMemoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MemoryDatabase : RoomDatabase() {

    abstract fun semanticMemoryDao(): SemanticMemoryDao
    abstract fun episodicMemoryDao(): EpisodicMemoryDao

    companion object {
        private const val DB_NAME = "memory.db"

        @Volatile
        private var INSTANCE: MemoryDatabase? = null

        fun getInstance(context: Context): MemoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): MemoryDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MemoryDatabase::class.java,
                DB_NAME
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        initializeVecExtension(db)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        initializeVecExtension(db)
                    }
                })
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
        }

        private fun initializeVecExtension(db: SupportSQLiteDatabase) {
            try {
                val sqliteDb = db as? org.sqlite.database.sqlite.SQLiteDatabase
                sqliteDb?.let { SqliteVec.load(it) }
            } catch (e: Exception) {
                // sqlite-vec not available, will use fallback
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create virtual table for vector search
                try {
                    database.execSQL("""
                        CREATE VIRTUAL TABLE IF NOT EXISTS vec_memories USING vec0(
                            embedding float[768] distance_metric=cosine
                        )
                    """.trimIndent())
                } catch (e: Exception) {
                    // sqlite-vec not available
                }
            }
        }
    }
}