package com.androidassistant.data.memory.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidassistant.data.memory.entity.SemanticMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SemanticMemoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: SemanticMemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memories: List<SemanticMemoryEntity>)

    @Query("SELECT * FROM semantic_memories ORDER BY importance DESC, last_accessed DESC LIMIT :limit")
    suspend fun getMostImportant(limit: Int = 50): List<SemanticMemoryEntity>

    @Query("SELECT * FROM semantic_memories WHERE category = :category ORDER BY importance DESC LIMIT :limit")
    suspend fun getByCategory(category: String, limit: Int = 20): List<SemanticMemoryEntity>

    @Query("SELECT * FROM semantic_memories WHERE id = :id")
    suspend fun getById(id: String): SemanticMemoryEntity?

    @Query("SELECT * FROM semantic_memories WHERE content LIKE '%' || :query || '%'")
    suspend fun searchByText(query: String): List<SemanticMemoryEntity>

    @Query("UPDATE semantic_memories SET access_count = access_count + 1, last_accessed = :now WHERE id = :id")
    suspend fun markAccessed(id: String, now: Long)

    @Query("DELETE FROM semantic_memories WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM semantic_memories WHERE importance < :threshold AND last_accessed < :before")
    suspend fun deleteDecayed(threshold: Int, before: Long): Int

    @Query("SELECT COUNT(*) FROM semantic_memories")
    suspend fun count(): Int
}
