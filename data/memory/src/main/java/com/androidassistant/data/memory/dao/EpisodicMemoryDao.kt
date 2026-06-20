package com.androidassistant.data.memory.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidassistant.data.memory.entity.EpisodicMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodicMemoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: EpisodicMemoryEntity)

    @Query("SELECT * FROM episodic_memories ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 20): List<EpisodicMemoryEntity>

    @Query("SELECT * FROM episodic_memories WHERE session_id = :sessionId ORDER BY timestamp ASC")
    fun getSessionEpisodes(sessionId: String): Flow<List<EpisodicMemoryEntity>>

    @Query("SELECT * FROM episodic_memories WHERE timestamp > :since ORDER BY importance DESC")
    suspend fun getSince(since: Long): List<EpisodicMemoryEntity>

    @Query("UPDATE episodic_memories SET importance = :importance WHERE id = :id")
    suspend fun updateImportance(id: String, importance: Int)

    @Query("DELETE FROM episodic_memories WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long): Int

    @Query("DELETE FROM episodic_memories WHERE id = :id")
    suspend fun delete(id: String)
}
